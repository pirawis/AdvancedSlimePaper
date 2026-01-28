# Importing Worlds

This guide explains how to convert vanilla Minecraft worlds to the Slime Region Format (SRF) using the AdvancedSlimePaper API.

## Prerequisites

To import a world, you need:
- A vanilla world folder (containing `region/` directory with `.mca` files)
- A world name for the imported world
- A `SlimeLoader` to store the converted world (optional for temporary worlds)

## Basic Import

```java
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

File worldDir = new File("my_vanilla_world");
String worldName = "my_world";
SlimeLoader loader = plugin.getLoader("file"); // or "mysql", "mongodb", etc.

try {
    // Note: This method can be called asynchronously
    SlimeWorld world = api.readVanillaWorld(worldDir, worldName, loader);

    // Load the world (must be sync)
    Bukkit.getScheduler().runTask(plugin, () -> {
        api.loadWorld(world, true);

        // Save the world to the data source
        try {
            api.saveWorld(world);
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
} catch (WorldAlreadyExistsException e) {
    // A world with this name already exists in the loader
} catch (InvalidWorldException e) {
    // The provided directory doesn't contain a valid world
} catch (WorldLoadedException e) {
    // The world is currently loaded on the server
} catch (WorldTooBigException e) {
    // The world exceeds the SRF size limit
} catch (IOException e) {
    // Failed to read/write the world
}
```

## Temporary Import (No Persistence)

To import a world without saving it to any data source, pass `null` as the loader:

```java
SlimeWorld tempWorld = api.readVanillaWorld(worldDir, worldName, null);
api.loadWorld(tempWorld, true);
// This world won't be saved anywhere
```

## Using the Importer Tool Directly

For programmatic imports outside the plugin context, you can use the `SWMImporter` class:

```java
import com.infernalsuite.asp.importer.SWMImporter;

File worldDir = new File("my_vanilla_world");
File outputFile = SWMImporter.getDestinationFile(worldDir);

try {
    // Import with overwrite enabled
    SWMImporter.importWorld(worldDir, outputFile, true);
    System.out.println("World imported to: " + outputFile.getAbsolutePath());
} catch (IOException e) {
    // Failed to read/write files
} catch (InvalidWorldException e) {
    // Invalid world directory
}
```

## Handling Large Worlds

Large worlds may take time to import. Consider running the import asynchronously:

```java
public CompletableFuture<SlimeWorld> importWorldAsync(File worldDir, String worldName, SlimeLoader loader) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return api.readVanillaWorld(worldDir, worldName, loader);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    });
}

// Usage
importWorldAsync(worldDir, "my-world", loader)
    .thenAccept(world -> {
        Bukkit.getScheduler().runTask(plugin, () -> {
            api.loadWorld(world, true);
        });
    })
    .exceptionally(e -> {
        plugin.getLogger().severe("Failed to import world: " + e.getMessage());
        return null;
    });
```

## World Size Limits

The SRF format can theoretically handle worlds up to **46,340 x 46,340 chunks**. However, practical limits depend on:
- Available server memory
- All chunks are kept in memory until unload
- Import time scales with world size

For very large worlds, consider:
- Using WorldBorder to trim unused chunks before import
- Importing only necessary regions
- Ensuring adequate server memory

## Exception Reference

| Exception | Cause |
|-----------|-------|
| `WorldAlreadyExistsException` | A world with the same name exists in the data source |
| `InvalidWorldException` | The directory doesn't contain a valid vanilla world |
| `WorldLoadedException` | The world is currently loaded on the server |
| `WorldTooBigException` | The world exceeds maximum SRF dimensions |
| `IOException` | General I/O error during read/write |

## Important Notes

- **Unload the world first**: Vanilla worlds must be unloaded before importing
- **Backup your worlds**: The import process is one-way; keep your original files
- **Memory usage**: Large worlds require significant memory during import
- **Async recommended**: Import operations can be performed asynchronously for better performance
- **Save after import**: Call `api.saveWorld(world)` to persist the imported world
