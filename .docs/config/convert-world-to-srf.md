# Converting Worlds to SRF

To load a world with AdvancedSlimePaper, you need to convert it from the vanilla Minecraft format to the Slime Region Format (SRF).

## Method 1: In-Game Command

The easiest way to convert a world is using the `/swm import` command.

### Steps

1. **Place your world folder** in your server's root directory (same level as `server.jar`)

2. **Ensure the world is unloaded**. Loaded worlds cannot be converted.

3. **Run the import command**:
   ```
   /swm import <world-folder> <data-source> [new-name]
   ```

### Examples

```bash
# Import 'my_world' folder to file storage with same name
/swm import my_world file

# Import 'my_world' folder to MySQL with a new name
/swm import my_world mysql skyblock_island

# Import 'lobby_backup' folder to MongoDB
/swm import lobby_backup mongodb lobby
```

### Command Parameters

| Parameter | Required | Description |
|-----------|----------|-------------|
| `world-folder` | Yes | Name of the world folder |
| `data-source` | Yes | Target data source (`file`, `mysql`, `mongodb`, etc.) |
| `new-name` | No | New name for the world (defaults to folder name) |

---

## Method 2: API Import

For programmatic conversion, use the AdvancedSlimePaper API.

### Basic Example

```java
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
SlimeLoader loader = plugin.getLoader("file");

File worldDir = new File("my_world");
String worldName = "my_world";

try {
    SlimeWorld world = api.readVanillaWorld(worldDir, worldName, loader);
    api.loadWorld(world, true);
    api.saveWorld(world);
} catch (InvalidWorldException e) {
    // Invalid world directory
} catch (WorldLoadedException e) {
    // World is currently loaded
} catch (WorldTooBigException e) {
    // World exceeds size limits
} catch (WorldAlreadyExistsException e) {
    // World already exists in loader
} catch (IOException e) {
    // I/O error
}
```

### Async Import

```java
public CompletableFuture<SlimeWorld> importWorldAsync(File worldDir, String name, SlimeLoader loader) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return api.readVanillaWorld(worldDir, name, loader);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    });
}
```

---

## Method 3: Standalone Importer

For importing worlds outside of a running server, use the `SWMImporter` utility.

```java
import com.infernalsuite.asp.importer.SWMImporter;

File worldDir = new File("my_world");
File outputFile = new File("slime_worlds/my_world.slime");

try {
    SWMImporter.importWorld(worldDir, outputFile, true); // true = overwrite
    System.out.println("Import complete!");
} catch (InvalidWorldException e) {
    System.err.println("Invalid world: " + e.getMessage());
} catch (IOException e) {
    System.err.println("I/O error: " + e.getMessage());
}
```

### Automatic Output Path

```java
File worldDir = new File("my_world");
File outputFile = SWMImporter.getDestinationFile(worldDir);
// Returns: my_world/my_world.slime

SWMImporter.importWorld(worldDir, outputFile, true);
```

---

## World Requirements

For a successful import, the world folder must contain:

- `level.dat` - World metadata
- `region/` directory - Contains `.mca` region files

### Valid World Structure

```
my_world/
├── level.dat
├── region/
│   ├── r.0.0.mca
│   ├── r.0.-1.mca
│   └── ...
├── entities/ (optional, 1.17+)
├── poi/ (optional)
└── data/ (optional)
```

---

## Size Limits

| Limit | Value |
|-------|-------|
| Maximum area | 46,340 x 46,340 chunks |
| Maximum file size | ~2GB (MEDIUMBLOB for MySQL) |

For large worlds:
- Use WorldBorder to trim before import
- Consider splitting into multiple smaller worlds
- Ensure sufficient server memory

---

## Troubleshooting

### "Invalid world" Error

- Verify `level.dat` exists
- Check that `region/` folder contains `.mca` files
- Ensure the world was properly saved before import

### "World is loaded" Error

- Unload the world first: `/swm unload <world>` or use Bukkit API
- Check for any plugins keeping the world loaded

### "World too big" Error

- Reduce world size using WorldBorder
- Remove unnecessary chunks with MCEdit or similar tools

### "World already exists" Error

- Delete the existing world: `/swm delete <world> <source>`
- Or use a different name for the import

---

## After Import

1. **Configure the world** in `worlds.yml`:
   ```yaml
   worlds:
     my_world:
       source: file
       loadOnStartup: true
       spawn: 0, 64, 0
       # ... other settings
   ```

2. **Reload configuration**:
   ```
   /swm reload
   ```

3. **Load the world**:
   ```
   /swm load my_world
   ```

4. **Verify the import**:
   ```
   /swm goto my_world
   ```
