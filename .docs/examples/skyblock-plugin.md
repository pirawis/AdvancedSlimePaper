# Example: Skyblock Island System

This example demonstrates how to create a per-player island system using AdvancedSlimePaper.

## Overview

- Each player gets their own island world cloned from a template
- Islands are stored in a database for persistence
- Islands can be loaded/unloaded on demand

## Dependencies

```yaml
# plugin.yml
name: SkyblockPlugin
version: 1.0.0
main: com.example.skyblock.SkyblockPlugin
depend: [SlimeWorldManager]
api-version: '1.20'
```

## Main Plugin Class

```java
package com.example.skyblock;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.SlimePlugin;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyblockPlugin extends JavaPlugin {

    private AdvancedSlimePaperAPI api;
    private SlimeLoader loader;
    private IslandManager islandManager;

    @Override
    public void onEnable() {
        // Get ASWM API
        this.api = AdvancedSlimePaperAPI.instance();

        // Get SlimePlugin for loader access
        SlimePlugin swm = (SlimePlugin) getServer().getPluginManager()
            .getPlugin("SlimeWorldManager");

        if (swm == null) {
            getLogger().severe("SlimeWorldManager not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Use MySQL for multi-server support (or "file" for single server)
        this.loader = swm.getLoader("mysql");

        // Initialize island manager
        this.islandManager = new IslandManager(this, api, loader);

        // Register commands
        getCommand("island").setExecutor(new IslandCommand(islandManager));

        // Register listeners
        getServer().getPluginManager().registerEvents(
            new PlayerListener(islandManager), this
        );

        getLogger().info("Skyblock plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (islandManager != null) {
            islandManager.saveAllIslands();
        }
    }
}
```

## Island Manager

```java
package com.example.skyblock;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager {

    private static final String TEMPLATE_WORLD = "island_template";
    private static final String ISLAND_PREFIX = "island_";

    private final Plugin plugin;
    private final AdvancedSlimePaperAPI api;
    private final SlimeLoader loader;
    private final Map<UUID, SlimeWorldInstance> loadedIslands = new ConcurrentHashMap<>();

    public IslandManager(Plugin plugin, AdvancedSlimePaperAPI api, SlimeLoader loader) {
        this.plugin = plugin;
        this.api = api;
        this.loader = loader;
    }

    /**
     * Gets the island world name for a player
     */
    public String getIslandName(UUID playerUUID) {
        return ISLAND_PREFIX + playerUUID.toString();
    }

    /**
     * Checks if a player has an island
     */
    public CompletableFuture<Boolean> hasIsland(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loader.worldExists(getIslandName(playerUUID));
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to check island: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Creates a new island for a player
     */
    public CompletableFuture<Boolean> createIsland(UUID playerUUID) {
        String islandName = getIslandName(playerUUID);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Read the template world
                SlimePropertyMap props = createIslandProperties();
                SlimeWorld template = api.readWorld(loader, TEMPLATE_WORLD, true, props);

                // Clone it for the player (persistent clone)
                SlimeWorld island = template.clone(islandName, loader);

                // Load on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    SlimeWorldInstance instance = api.loadWorld(island, true);
                    loadedIslands.put(playerUUID, instance);

                    // Save the island
                    try {
                        api.saveWorld(island);
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to save new island: " + e.getMessage());
                    }
                });

                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create island: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Loads a player's island
     */
    public CompletableFuture<SlimeWorldInstance> loadIsland(UUID playerUUID) {
        String islandName = getIslandName(playerUUID);

        // Check if already loaded
        SlimeWorldInstance existing = loadedIslands.get(playerUUID);
        if (existing != null) {
            return CompletableFuture.completedFuture(existing);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                SlimePropertyMap props = createIslandProperties();
                SlimeWorld world = api.readWorld(loader, islandName, false, props);

                // Must load on main thread
                CompletableFuture<SlimeWorldInstance> future = new CompletableFuture<>();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    SlimeWorldInstance instance = api.loadWorld(world, true);
                    loadedIslands.put(playerUUID, instance);
                    future.complete(instance);
                });

                return future.join();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load island: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Unloads a player's island
     */
    public void unloadIsland(UUID playerUUID) {
        SlimeWorldInstance instance = loadedIslands.remove(playerUUID);
        if (instance == null) return;

        World world = instance.getBukkitWorld();
        if (world == null) return;

        // Teleport players out
        Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        world.getPlayers().forEach(p -> p.teleport(spawn));

        // Save and unload
        try {
            api.saveWorld(instance);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save island: " + e.getMessage());
        }

        Bukkit.unloadWorld(world, true);
    }

    /**
     * Teleports a player to their island
     */
    public void teleportToIsland(Player player) {
        UUID uuid = player.getUniqueId();

        loadIsland(uuid).thenAccept(instance -> {
            if (instance == null) {
                player.sendMessage("Failed to load your island!");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                World world = instance.getBukkitWorld();
                Location spawn = world.getSpawnLocation();
                player.teleport(spawn);
                player.sendMessage("Welcome to your island!");
            });
        });
    }

    /**
     * Deletes a player's island
     */
    public CompletableFuture<Boolean> deleteIsland(UUID playerUUID) {
        String islandName = getIslandName(playerUUID);

        // Unload first if loaded
        unloadIsland(playerUUID);

        return CompletableFuture.supplyAsync(() -> {
            try {
                loader.deleteWorld(islandName);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to delete island: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Saves all loaded islands
     */
    public void saveAllIslands() {
        for (Map.Entry<UUID, SlimeWorldInstance> entry : loadedIslands.entrySet()) {
            try {
                api.saveWorld(entry.getValue());
                plugin.getLogger().info("Saved island: " + entry.getValue().getName());
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save island " +
                    entry.getValue().getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Creates default island properties
     */
    private SlimePropertyMap createIslandProperties() {
        SlimePropertyMap props = new SlimePropertyMap();
        props.setValue(SlimeProperties.SPAWN_X, 0);
        props.setValue(SlimeProperties.SPAWN_Y, 100);
        props.setValue(SlimeProperties.SPAWN_Z, 0);
        props.setValue(SlimeProperties.DIFFICULTY, "normal");
        props.setValue(SlimeProperties.ALLOW_MONSTERS, true);
        props.setValue(SlimeProperties.ALLOW_ANIMALS, true);
        props.setValue(SlimeProperties.PVP, false);
        props.setValue(SlimeProperties.ENVIRONMENT, "normal");
        return props;
    }
}
```

## Island Command

```java
package com.example.skyblock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {

    private final IslandManager islandManager;

    public IslandCommand(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            // Default: go to island
            goToIsland(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> createIsland(player);
            case "home", "go" -> goToIsland(player);
            case "delete" -> deleteIsland(player);
            case "leave" -> leaveIsland(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void createIsland(Player player) {
        player.sendMessage("Creating your island...");

        islandManager.hasIsland(player.getUniqueId()).thenAccept(hasIsland -> {
            if (hasIsland) {
                player.sendMessage("You already have an island! Use /island delete first.");
                return;
            }

            islandManager.createIsland(player.getUniqueId()).thenAccept(success -> {
                if (success) {
                    player.sendMessage("Island created! Teleporting...");
                    islandManager.teleportToIsland(player);
                } else {
                    player.sendMessage("Failed to create island!");
                }
            });
        });
    }

    private void goToIsland(Player player) {
        islandManager.hasIsland(player.getUniqueId()).thenAccept(hasIsland -> {
            if (!hasIsland) {
                player.sendMessage("You don't have an island! Use /island create");
                return;
            }

            player.sendMessage("Teleporting to your island...");
            islandManager.teleportToIsland(player);
        });
    }

    private void deleteIsland(Player player) {
        player.sendMessage("Deleting your island...");

        islandManager.deleteIsland(player.getUniqueId()).thenAccept(success -> {
            if (success) {
                player.sendMessage("Island deleted!");
            } else {
                player.sendMessage("Failed to delete island!");
            }
        });
    }

    private void leaveIsland(Player player) {
        islandManager.unloadIsland(player.getUniqueId());
        player.sendMessage("Island unloaded.");
    }

    private void sendHelp(Player player) {
        player.sendMessage("Island Commands:");
        player.sendMessage("/island create - Create a new island");
        player.sendMessage("/island home - Go to your island");
        player.sendMessage("/island delete - Delete your island");
        player.sendMessage("/island leave - Unload your island");
    }
}
```

## Player Listener

```java
package com.example.skyblock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final IslandManager islandManager;

    public PlayerListener(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Optionally auto-load island on join
        // islandManager.loadIsland(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Unload island when player leaves (optional, saves memory)
        islandManager.unloadIsland(event.getPlayer().getUniqueId());
    }
}
```

## Template World Setup

1. Create your island template in-game or with WorldEdit
2. Import it as a read-only template:

```yaml
# worlds.yml
worlds:
  island_template:
    source: mysql  # or file
    loadOnStartup: true
    readOnly: true
    spawn: 0, 100, 0
    difficulty: normal
    allowMonsters: true
    allowAnimals: true
    pvp: false
```

## Key Points

1. **Template is read-only** - Prevents accidental modifications
2. **Async world reading** - `readWorld()` runs off main thread
3. **Sync world loading** - `loadWorld()` must be on main thread
4. **Unload when not needed** - Saves memory
5. **Save before unload** - Prevents data loss
6. **Use database for multi-server** - MySQL/MongoDB for network setups
