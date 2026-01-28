# Loading Worlds

This guide explains how to load SlimeWorld format (SRF) worlds using the AdvancedSlimePaper API.

## Getting the API Instance

First, retrieve the AdvancedSlimePaperAPI instance:

```java
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
```

## Getting a SlimeLoader

A `SlimeLoader` is a class that reads and stores worlds from a data source. You can obtain a loader from the plugin:

```java
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
SlimeLoader loader = plugin.getLoader("mysql"); // or "file", "mongodb", "redis", "api"
```

Available built-in loaders:
- `file` - File system storage (always available)
- `mysql` - MySQL database storage
- `mongodb` - MongoDB database storage
- `redis` - Redis storage
- `api` - Remote API storage

## Setting Up World Properties

Before loading a world, you need a `SlimePropertyMap` object. Check the [properties documentation](properties.md) for all available options.

```java
SlimePropertyMap props = new SlimePropertyMap();
props.setValue(SlimeProperties.SPAWN_X, 0);
props.setValue(SlimeProperties.SPAWN_Y, 64);
props.setValue(SlimeProperties.SPAWN_Z, 0);
props.setValue(SlimeProperties.DIFFICULTY, "normal");
props.setValue(SlimeProperties.ALLOW_MONSTERS, true);
props.setValue(SlimeProperties.ALLOW_ANIMALS, true);
```

## Loading a World

Loading a world is a two-step process:

1. **Read the world** (can be done asynchronously)
2. **Load the world into the server** (must be done synchronously)

```java
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();

try {
    // Step 1: Read the world (can be async)
    SlimeWorld world = api.readWorld(loader, "my-world", false, props);

    // Step 2: Load into server (MUST be sync - on main thread)
    Bukkit.getScheduler().runTask(plugin, () -> {
        SlimeWorldInstance instance = api.loadWorld(world, true);
        // World is now loaded and accessible
    });
} catch (UnknownWorldException e) {
    // World doesn't exist in the data source
} catch (IOException e) {
    // Failed to read from data source
} catch (CorruptedWorldException e) {
    // World data is corrupted
} catch (NewerFormatException e) {
    // World uses a newer SRF version
}
```

## Read-Only Mode

Setting `readOnly` to `true` prevents the world from being saved and allows multiple servers to load the same world simultaneously:

```java
SlimeWorld world = api.readWorld(loader, "my-world", true, props); // read-only mode
```

## Checking if a World is Loaded

```java
// Check by SlimeWorld object
boolean loaded = api.worldLoaded(world);

// Get a loaded world by name
SlimeWorldInstance loadedWorld = api.getLoadedWorld("my-world");
if (loadedWorld != null) {
    // World is loaded
    World bukkitWorld = loadedWorld.getBukkitWorld();
}

// Get all loaded SlimeWorlds
List<SlimeWorldInstance> allWorlds = api.getLoadedWorlds();
```

## Cloning Worlds

You can create copies of worlds for scenarios like per-player islands:

```java
SlimeWorld template = api.readWorld(loader, "skyblock-template", true, props);

// Temporary clone (not saved anywhere)
SlimeWorld tempClone = template.clone("temp-world");

// Persistent clone (saved to a loader)
SlimeWorld persistentClone = template.clone("player-island", loader);

// Load the clone
api.loadWorld(persistentClone, true);
```

## Unloading Worlds

To unload a world, use standard Bukkit methods:

```java
World bukkitWorld = Bukkit.getWorld("my-world");

// Teleport all players out first
Location spawn = Bukkit.getWorld("world").getSpawnLocation();
bukkitWorld.getPlayers().forEach(player -> player.teleport(spawn));

// Unload the world
Bukkit.unloadWorld(bukkitWorld, true); // true = save chunks
```

## Complete Example

```java
public class WorldManager {

    private final AdvancedSlimePaperAPI api;
    private final SlimeLoader loader;
    private final Plugin plugin;

    public WorldManager(Plugin plugin) {
        this.plugin = plugin;
        this.api = AdvancedSlimePaperAPI.instance();
        SlimePlugin swm = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        this.loader = swm.getLoader("file");
    }

    public CompletableFuture<SlimeWorldInstance> loadWorldAsync(String worldName) {
        CompletableFuture<SlimeWorldInstance> future = new CompletableFuture<>();

        // Read world asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SlimePropertyMap props = new SlimePropertyMap();
                props.setValue(SlimeProperties.DIFFICULTY, "normal");

                SlimeWorld world = api.readWorld(loader, worldName, false, props);

                // Load world synchronously
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        SlimeWorldInstance instance = api.loadWorld(world, true);
                        future.complete(instance);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
```

## Important Notes

- `readWorld()` can be called asynchronously for better performance
- `loadWorld()` **must** be called on the main server thread
- Use read-only mode when you don't need to save changes
- Always handle exceptions appropriately
- Clone worlds for temporary or per-player instances
