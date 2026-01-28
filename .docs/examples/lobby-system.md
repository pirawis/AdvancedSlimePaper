# Example: Lobby System

This example demonstrates how to set up a read-only lobby world that multiple servers can share.

## Overview

- Lobby world is read-only (never saves changes)
- Multiple servers can load the same lobby
- Fast loading with no locking conflicts

## Configuration

### worlds.yml

```yaml
worlds:
  lobby:
    source: mysql          # Shared database for multi-server
    loadOnStartup: true    # Auto-load on server start
    readOnly: true         # Never save changes
    spawn: 0, 65, 0
    difficulty: peaceful   # No hostile mobs
    allowMonsters: false
    allowAnimals: false
    pvp: false
    environment: NORMAL
    worldType: flat
```

### sources.yml

```yaml
mysql:
  enabled: true
  host: your-database-host
  port: 3306
  username: minecraft
  password: your-password
  database: slimeworldmanager
```

## Plugin Implementation

### Main Plugin

```java
package com.example.lobby;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyPlugin extends JavaPlugin {

    private static final String LOBBY_WORLD = "lobby";

    @Override
    public void onEnable() {
        // Wait for world to be loaded by ASWM
        Bukkit.getScheduler().runTaskLater(this, () -> {
            World lobby = Bukkit.getWorld(LOBBY_WORLD);

            if (lobby == null) {
                getLogger().severe("Lobby world not loaded!");
                return;
            }

            // Set up lobby world
            setupLobbyWorld(lobby);

            // Register listeners
            getServer().getPluginManager().registerEvents(
                new LobbyListener(lobby), this
            );

            getLogger().info("Lobby system ready!");
        }, 20L); // Wait 1 second for world loading
    }

    private void setupLobbyWorld(World world) {
        // Set world properties
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(org.bukkit.GameRule.DO_FIRE_TICK, false);

        // Set time to noon
        world.setTime(6000);

        // Set weather to clear
        world.setStorm(false);
        world.setThundering(false);

        getLogger().info("Lobby world configured!");
    }
}
```

### Lobby Listener

```java
package com.example.lobby;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LobbyListener implements Listener {

    private final World lobbyWorld;
    private final Location spawnLocation;

    public LobbyListener(World lobbyWorld) {
        this.lobbyWorld = lobbyWorld;
        this.spawnLocation = lobbyWorld.getSpawnLocation();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Teleport to lobby
        player.teleport(spawnLocation);

        // Set adventure mode to prevent breaking blocks
        player.setGameMode(GameMode.ADVENTURE);

        // Reset player state
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.getInventory().clear();

        // Welcome message
        player.sendMessage("Welcome to the lobby!");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Always respawn in lobby
        event.setRespawnLocation(spawnLocation);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getWorld().equals(lobbyWorld)) {
            // Only allow ops to break blocks
            if (!event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getWorld().equals(lobbyWorld)) {
            if (!event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld().equals(lobbyWorld)) {
            // No damage in lobby
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity().getWorld().equals(lobbyWorld)) {
            // No hunger in lobby
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getWorld().equals(lobbyWorld)) {
            // No item dropping in lobby
            event.setCancelled(true);
        }
    }
}
```

## Server Selector

```java
package com.example.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

public class ServerSelector implements Listener {

    private static final String SELECTOR_TITLE = "Server Selector";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.COMPASS) {
            openServerSelector(player);
        }
    }

    private void openServerSelector(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, SELECTOR_TITLE);

        // Survival server
        gui.setItem(11, createItem(Material.GRASS_BLOCK, "Survival", "Click to join survival!"));

        // Skyblock server
        gui.setItem(13, createItem(Material.OAK_SAPLING, "Skyblock", "Click to join skyblock!"));

        // Minigames server
        gui.setItem(15, createItem(Material.GOLDEN_SWORD, "Minigames", "Click to join minigames!"));

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SELECTOR_TITLE)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        switch (clicked.getType()) {
            case GRASS_BLOCK -> connectToServer(player, "survival");
            case OAK_SAPLING -> connectToServer(player, "skyblock");
            case GOLDEN_SWORD -> connectToServer(player, "minigames");
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private void connectToServer(Player player, String server) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(
                Bukkit.getPluginManager().getPlugin("LobbyPlugin"),
                "BungeeCord",
                bytes.toByteArray()
            );
        } catch (Exception e) {
            player.sendMessage("Failed to connect to " + server);
        }
    }
}
```

## Multi-Server Setup

### Network Architecture

```
                    ┌─────────────┐
                    │  BungeeCord │
                    │  / Velocity │
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           │               │               │
      ┌────▼────┐    ┌─────▼─────┐   ┌─────▼─────┐
      │ Lobby 1 │    │  Lobby 2  │   │  Lobby 3  │
      │(readOnly)│   │(readOnly) │   │(readOnly) │
      └────┬────┘    └─────┬─────┘   └─────┬─────┘
           │               │               │
           └───────────────┼───────────────┘
                           │
                    ┌──────▼──────┐
                    │    MySQL    │
                    │  (shared)   │
                    └─────────────┘
```

### Key Points

1. **All servers use same MySQL** - Configure identical `sources.yml`
2. **All load lobby as readOnly** - No locking conflicts
3. **Changes don't persist** - Perfect for lobbies
4. **Instant updates** - Change lobby once, all servers get it

### Updating the Lobby

To update the lobby world:

1. Load it on one server with `readOnly: false`
2. Make your changes
3. Save: `/swm save lobby`
4. Set back to `readOnly: true`
5. Restart other servers to load the new version

Or use the API:
```java
// On the admin server only
SlimeWorld lobby = api.readWorld(loader, "lobby", false, props); // NOT read-only
api.loadWorld(lobby, true);
// Make changes...
api.saveWorld(lobby);
```
