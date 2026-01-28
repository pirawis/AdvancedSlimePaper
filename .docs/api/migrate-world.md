# Migrating Worlds

This guide explains how to transfer worlds between different data sources using the AdvancedSlimePaper API.

## Overview

Migration allows you to move worlds from one storage backend to another without data loss. Common use cases include:
- Moving from file storage to a database for multi-server setups
- Switching database providers (MySQL to MongoDB)
- Consolidating worlds into a single data source

## Basic Migration

```java
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

String worldName = "my_world";
SlimeLoader currentLoader = plugin.getLoader("file");
SlimeLoader newLoader = plugin.getLoader("mysql");

try {
    // Note: This method can be called asynchronously
    api.migrateWorld(worldName, currentLoader, newLoader);
    System.out.println("World migrated successfully!");
} catch (IOException e) {
    // Failed to read from source or write to destination
} catch (WorldAlreadyExistsException e) {
    // A world with this name already exists in the target data source
} catch (UnknownWorldException e) {
    // World doesn't exist in the source data source
}
```

## Async Migration

For production servers, run migrations asynchronously to avoid blocking the main thread:

```java
public CompletableFuture<Void> migrateWorldAsync(String worldName, SlimeLoader from, SlimeLoader to) {
    return CompletableFuture.runAsync(() -> {
        try {
            api.migrateWorld(worldName, from, to);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    });
}

// Usage
migrateWorldAsync("my-world", fileLoader, mysqlLoader)
    .thenRun(() -> plugin.getLogger().info("Migration complete!"))
    .exceptionally(e -> {
        plugin.getLogger().severe("Migration failed: " + e.getMessage());
        return null;
    });
```

## Batch Migration

To migrate multiple worlds at once:

```java
public void migrateAllWorlds(SlimeLoader from, SlimeLoader to) {
    try {
        List<String> worlds = from.listWorlds();

        for (String worldName : worlds) {
            try {
                api.migrateWorld(worldName, from, to);
                plugin.getLogger().info("Migrated: " + worldName);
            } catch (WorldAlreadyExistsException e) {
                plugin.getLogger().warning("Skipped (already exists): " + worldName);
            }
        }
    } catch (IOException e) {
        plugin.getLogger().severe("Failed to list worlds: " + e.getMessage());
    }
}
```

## Migration with World Unload

If the world is currently loaded, unload it first for a clean migration:

```java
public void migrateLoadedWorld(String worldName, SlimeLoader from, SlimeLoader to) {
    World bukkitWorld = Bukkit.getWorld(worldName);

    if (bukkitWorld != null) {
        // Teleport players out
        Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        bukkitWorld.getPlayers().forEach(p -> p.teleport(spawn));

        // Unload and save
        Bukkit.unloadWorld(bukkitWorld, true);
    }

    // Now migrate
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
            api.migrateWorld(worldName, from, to);
            plugin.getLogger().info("Migration complete: " + worldName);
        } catch (Exception e) {
            plugin.getLogger().severe("Migration failed: " + e.getMessage());
        }
    });
}
```

## Updating Configuration After Migration

After migrating a world, update your `worlds.yml` to use the new data source:

```yaml
worlds:
  my_world:
    source: mysql  # Changed from 'file'
    loadOnStartup: true
    # ... other settings
```

## Exception Reference

| Exception | Cause | Resolution |
|-----------|-------|------------|
| `IOException` | Network/disk error | Check connections and permissions |
| `WorldAlreadyExistsException` | World exists in target | Delete existing or choose different name |
| `UnknownWorldException` | World not in source | Verify world name and source loader |

## Best Practices

1. **Backup first**: Always backup worlds before migration
2. **Unload worlds**: Unload the world before migrating for data consistency
3. **Run async**: Perform migrations asynchronously on production servers
4. **Update config**: Remember to update `worlds.yml` after migration
5. **Verify**: Load the world from the new source to verify successful migration
6. **Clean up**: Optionally delete the world from the old source after verification

## Common Migration Scenarios

### File to MySQL (Multi-Server Setup)

```java
SlimeLoader fileLoader = plugin.getLoader("file");
SlimeLoader mysqlLoader = plugin.getLoader("mysql");

// Migrate all worlds for multi-server access
for (String world : fileLoader.listWorlds()) {
    api.migrateWorld(world, fileLoader, mysqlLoader);
}
```

### MySQL to MongoDB (Database Change)

```java
SlimeLoader mysqlLoader = plugin.getLoader("mysql");
SlimeLoader mongoLoader = plugin.getLoader("mongodb");

api.migrateWorld("my_world", mysqlLoader, mongoLoader);
```

### To Local File (Backup)

```java
SlimeLoader remoteLoader = plugin.getLoader("mysql");
SlimeLoader fileLoader = plugin.getLoader("file");

// Create local backup
api.migrateWorld("important_world", remoteLoader, fileLoader);
```
