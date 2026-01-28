# Example: Minigame Arena System

This example demonstrates how to create a minigame system with resettable arenas using AdvancedSlimePaper.

## Overview

- Arena templates that can be quickly cloned
- Automatic arena reset after each game
- Multiple concurrent games with separate worlds

## Configuration

### worlds.yml

```yaml
worlds:
  # Arena templates (read-only)
  arena_spleef_template:
    source: file
    loadOnStartup: true
    readOnly: true
    spawn: 0, 100, 0
    difficulty: peaceful
    allowMonsters: false
    pvp: true

  arena_bedwars_template:
    source: file
    loadOnStartup: true
    readOnly: true
    spawn: 0, 65, 0
    difficulty: easy
    allowMonsters: false
    pvp: true

  arena_skywars_template:
    source: file
    loadOnStartup: true
    readOnly: true
    spawn: 0, 100, 0
    difficulty: normal
    allowMonsters: false
    pvp: true
```

## Arena Manager

```java
package com.example.minigames;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.SlimePlugin;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaManager {

    private final Plugin plugin;
    private final AdvancedSlimePaperAPI api;
    private final SlimeLoader loader;

    // Pool of available arenas per game type
    private final Map<String, Queue<Arena>> arenaPool = new ConcurrentHashMap<>();

    // Currently active arenas
    private final Map<String, Arena> activeArenas = new ConcurrentHashMap<>();

    // Arena ID counter
    private final AtomicInteger arenaCounter = new AtomicInteger(0);

    public ArenaManager(Plugin plugin) {
        this.plugin = plugin;
        this.api = AdvancedSlimePaperAPI.instance();

        SlimePlugin swm = (SlimePlugin) Bukkit.getPluginManager()
            .getPlugin("SlimeWorldManager");
        this.loader = swm.getLoader("file");
    }

    /**
     * Pre-creates arenas for faster game starts
     */
    public void warmupArenas(String gameType, int count) {
        plugin.getLogger().info("Warming up " + count + " " + gameType + " arenas...");

        arenaPool.computeIfAbsent(gameType, k -> new ConcurrentLinkedQueue<>());

        for (int i = 0; i < count; i++) {
            createArenaAsync(gameType).thenAccept(arena -> {
                if (arena != null) {
                    arenaPool.get(gameType).offer(arena);
                    plugin.getLogger().info("Arena ready: " + arena.getWorldName());
                }
            });
        }
    }

    /**
     * Gets an available arena or creates a new one
     */
    public Arena acquireArena(String gameType) {
        Queue<Arena> pool = arenaPool.get(gameType);

        if (pool != null && !pool.isEmpty()) {
            Arena arena = pool.poll();
            if (arena != null) {
                activeArenas.put(arena.getWorldName(), arena);
                return arena;
            }
        }

        // No pooled arena available, create new one synchronously
        Arena arena = createArenaSync(gameType);
        if (arena != null) {
            activeArenas.put(arena.getWorldName(), arena);
        }
        return arena;
    }

    /**
     * Releases an arena back to the pool (resets it)
     */
    public void releaseArena(Arena arena) {
        activeArenas.remove(arena.getWorldName());

        String gameType = arena.getGameType();
        String worldName = arena.getWorldName();

        // Unload the world
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            // Teleport players out
            Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            world.getPlayers().forEach(p -> p.teleport(spawn));

            // Unload without saving (we'll create fresh from template)
            Bukkit.unloadWorld(world, false);
        }

        // Create a fresh arena from template
        createArenaAsync(gameType).thenAccept(newArena -> {
            if (newArena != null) {
                arenaPool.computeIfAbsent(gameType, k -> new ConcurrentLinkedQueue<>())
                    .offer(newArena);
            }
        });
    }

    /**
     * Creates an arena asynchronously
     */
    private java.util.concurrent.CompletableFuture<Arena> createArenaAsync(String gameType) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return createArena(gameType);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create arena: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Creates an arena synchronously
     */
    private Arena createArenaSync(String gameType) {
        try {
            return createArena(gameType);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create arena: " + e.getMessage());
            return null;
        }
    }

    private Arena createArena(String gameType) throws Exception {
        String templateName = "arena_" + gameType + "_template";
        String arenaName = "arena_" + gameType + "_" + arenaCounter.incrementAndGet();

        // Read template
        SlimePropertyMap props = new SlimePropertyMap();
        props.setValue(SlimeProperties.PVP, true);
        props.setValue(SlimeProperties.DIFFICULTY, "normal");

        SlimeWorld template = api.readWorld(loader, templateName, true, props);

        // Clone as temporary (no persistence needed)
        SlimeWorld arenaWorld = template.clone(arenaName);

        // Load on main thread
        java.util.concurrent.CompletableFuture<SlimeWorldInstance> future =
            new java.util.concurrent.CompletableFuture<>();

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                SlimeWorldInstance instance = api.loadWorld(arenaWorld, true);
                future.complete(instance);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        SlimeWorldInstance instance = future.join();
        return new Arena(arenaName, gameType, instance);
    }

    /**
     * Shuts down all arenas
     */
    public void shutdown() {
        // Unload all active arenas
        for (Arena arena : activeArenas.values()) {
            World world = Bukkit.getWorld(arena.getWorldName());
            if (world != null) {
                world.getPlayers().forEach(p ->
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));
                Bukkit.unloadWorld(world, false);
            }
        }

        // Unload pooled arenas
        for (Queue<Arena> pool : arenaPool.values()) {
            for (Arena arena : pool) {
                World world = Bukkit.getWorld(arena.getWorldName());
                if (world != null) {
                    Bukkit.unloadWorld(world, false);
                }
            }
        }
    }
}
```

## Arena Class

```java
package com.example.minigames;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private final String worldName;
    private final String gameType;
    private final SlimeWorldInstance worldInstance;
    private final List<Location> spawnPoints = new ArrayList<>();
    private ArenaState state = ArenaState.WAITING;

    public Arena(String worldName, String gameType, SlimeWorldInstance worldInstance) {
        this.worldName = worldName;
        this.gameType = gameType;
        this.worldInstance = worldInstance;

        // Load spawn points from world
        loadSpawnPoints();
    }

    private void loadSpawnPoints() {
        World world = getBukkitWorld();
        if (world == null) return;

        // Default spawn
        spawnPoints.add(world.getSpawnLocation());

        // You could also load from signs, armor stands, or config
    }

    public String getWorldName() {
        return worldName;
    }

    public String getGameType() {
        return gameType;
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(worldName);
    }

    public SlimeWorldInstance getWorldInstance() {
        return worldInstance;
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public Location getRandomSpawn() {
        if (spawnPoints.isEmpty()) {
            return getBukkitWorld().getSpawnLocation();
        }
        return spawnPoints.get((int) (Math.random() * spawnPoints.size()));
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public enum ArenaState {
        WAITING,
        STARTING,
        IN_GAME,
        ENDING
    }
}
```

## Game Manager

```java
package com.example.minigames;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class GameManager {

    private final Plugin plugin;
    private final ArenaManager arenaManager;
    private final Map<UUID, Game> playerGames = new HashMap<>();
    private final Map<Arena, Game> arenaGames = new HashMap<>();

    public GameManager(Plugin plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
    }

    /**
     * Creates a new game
     */
    public Game createGame(String gameType) {
        Arena arena = arenaManager.acquireArena(gameType);
        if (arena == null) {
            return null;
        }

        Game game = new Game(this, arena, gameType);
        arenaGames.put(arena, game);
        return game;
    }

    /**
     * Joins a player to an existing or new game
     */
    public boolean joinGame(Player player, String gameType) {
        // Check if already in a game
        if (playerGames.containsKey(player.getUniqueId())) {
            player.sendMessage("You're already in a game!");
            return false;
        }

        // Find a waiting game or create new
        Game game = findWaitingGame(gameType);
        if (game == null) {
            game = createGame(gameType);
        }

        if (game == null) {
            player.sendMessage("No arenas available!");
            return false;
        }

        game.addPlayer(player);
        playerGames.put(player.getUniqueId(), game);

        return true;
    }

    /**
     * Removes a player from their game
     */
    public void leaveGame(Player player) {
        Game game = playerGames.remove(player.getUniqueId());
        if (game != null) {
            game.removePlayer(player);
        }
    }

    /**
     * Ends a game and releases the arena
     */
    public void endGame(Game game) {
        Arena arena = game.getArena();

        // Remove all players from tracking
        for (Player player : game.getPlayers()) {
            playerGames.remove(player.getUniqueId());
        }

        arenaGames.remove(arena);

        // Release arena back to pool (will reset it)
        arenaManager.releaseArena(arena);
    }

    private Game findWaitingGame(String gameType) {
        for (Game game : arenaGames.values()) {
            if (game.getGameType().equals(gameType) &&
                game.getState() == Arena.ArenaState.WAITING &&
                !game.isFull()) {
                return game;
            }
        }
        return null;
    }

    public Game getPlayerGame(Player player) {
        return playerGames.get(player.getUniqueId());
    }
}
```

## Game Class

```java
package com.example.minigames;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private final GameManager gameManager;
    private final Arena arena;
    private final String gameType;
    private final List<Player> players = new ArrayList<>();
    private final int maxPlayers = 8;
    private final int minPlayers = 2;

    public Game(GameManager gameManager, Arena arena, String gameType) {
        this.gameManager = gameManager;
        this.arena = arena;
        this.gameType = gameType;
    }

    public void addPlayer(Player player) {
        players.add(player);

        // Teleport to arena
        player.teleport(arena.getRandomSpawn());
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);

        player.sendMessage("You joined " + gameType + "! (" + players.size() + "/" + maxPlayers + ")");

        // Broadcast to other players
        for (Player p : players) {
            if (p != player) {
                p.sendMessage(player.getName() + " joined! (" + players.size() + "/" + maxPlayers + ")");
            }
        }

        // Check if we can start
        if (players.size() >= minPlayers && getState() == Arena.ArenaState.WAITING) {
            startCountdown();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);

        // Teleport to lobby
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        player.setGameMode(GameMode.ADVENTURE);

        // Check if game should end
        if (players.size() < minPlayers && getState() == Arena.ArenaState.IN_GAME) {
            endGame();
        }
    }

    private void startCountdown() {
        arena.setState(Arena.ArenaState.STARTING);

        new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (countdown <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                for (Player player : players) {
                    player.sendMessage("Game starting in " + countdown + "...");
                }

                countdown--;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("MinigamesPlugin"), 0L, 20L);
    }

    private void startGame() {
        arena.setState(Arena.ArenaState.IN_GAME);

        for (Player player : players) {
            player.sendMessage("Game started!");
            // Give items, set up game-specific stuff
        }
    }

    public void endGame() {
        arena.setState(Arena.ArenaState.ENDING);

        for (Player player : players) {
            player.sendMessage("Game over!");
        }

        // Return arena to pool
        gameManager.endGame(this);
    }

    public Arena getArena() {
        return arena;
    }

    public String getGameType() {
        return gameType;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Arena.ArenaState getState() {
        return arena.getState();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
}
```

## Usage Commands

```java
package com.example.minigames;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinigameCommand implements CommandExecutor {

    private final GameManager gameManager;

    public MinigameCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage("Usage: /minigame join <type>");
                    player.sendMessage("Types: spleef, bedwars, skywars");
                    return true;
                }
                gameManager.joinGame(player, args[1].toLowerCase());
            }
            case "leave" -> gameManager.leaveGame(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("Minigame Commands:");
        player.sendMessage("/minigame join <type> - Join a game");
        player.sendMessage("/minigame leave - Leave current game");
    }
}
```

## Key Points

1. **Templates are read-only** - Base arenas never change
2. **Temporary clones** - `template.clone(name)` without loader = not saved
3. **Arena pooling** - Pre-create arenas for instant game starts
4. **No saving needed** - Arenas are disposable, just unload without saving
5. **Memory management** - Unload arenas when games end
6. **Fast reset** - Create fresh clone instead of rolling back changes
