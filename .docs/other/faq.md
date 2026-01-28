# Frequently Asked Questions

## General Questions

### Is ASWM compatible with Spigot?

**No.** AdvancedSlimePaper is its own Paper fork, not a plugin for Spigot. You must use the ASWM server JAR to run SlimeWorldManager.

### Can I use ASWM with Spigot plugins?

**Yes.** Since ASWM is a Paper fork, it supports all Paper and Spigot plugins.

### What Minecraft versions are supported?

ASWM supports the latest Minecraft versions. Check the [releases page](https://github.com/InfernalSuite/AdvancedSlimePaper/releases) for version-specific builds.

### What Java version do I need?

**Java 21 or higher** is required.

---

## World Management

### Can I override the default world?

**Yes!** You can configure the default `world`, `world_nether`, and `world_the_end` to use SRF format in your `worlds.yml`.

### Can I use ASWM worlds with Multiverse-Core?

**Partially.** Multiverse-Core detects ASWM worlds as unloaded because it cannot find the world directory. It will ignore them, which means Multiverse commands won't work with ASWM worlds. However, there should be no conflicts.

For world management, use the `/swm` commands instead of Multiverse commands.

### What's the maximum world size?

The Slime Region Format can theoretically handle up to **46,340 x 46,340 chunks** (about 741,440 x 741,440 blocks).

However, the practical limit depends on your server's RAM:
- ASWM keeps all chunks in memory until the world is unloaded
- More chunks = more RAM usage
- RAM usage per chunk varies based on the chunk's content

**Recommendation:** Keep worlds reasonably sized and monitor memory usage.

### How do I reduce memory usage?

1. **Use smaller worlds** - Only include necessary chunks
2. **Prune chunks** - Set `pruning: aggressive` in world config
3. **Unload unused worlds** - Don't keep worlds loaded that aren't being used
4. **Use read-only mode** - Read-only worlds use slightly less memory

### Are my worlds automatically saved?

**It depends:**
- Worlds with `readOnly: true` are **never** saved
- Worlds with `readOnly: false` are saved on server shutdown
- Use `/swm save <world>` for manual saves

---

## Data Sources

### Which data source should I use?

| Use Case | Recommended |
|----------|-------------|
| Single server | `file` |
| Multi-server network | `mysql` or `mongodb` |
| High performance | `redis` |
| Cloud infrastructure | `mongodb` |
| Development | `file` |

### Can multiple servers access the same world?

**Yes, with restrictions:**
- Multiple servers can load the same world in **read-only mode**
- Only **one server** should have write access (readOnly: false) at a time
- Use a shared database (MySQL/MongoDB) for multi-server access

### How do I share worlds between servers?

1. Set up a shared database (MySQL or MongoDB)
2. Configure all servers to use the same database in `sources.yml`
3. Import/migrate your worlds to the database
4. Configure worlds with appropriate read-only settings

---

## Troubleshooting

### My server stops with "Failed to find ClassModifier classes"

Make sure you're running the **ASWM server JAR**, not regular Paper or Spigot. See the [installation guide](../usage/install.md).

### Worlds aren't loading on startup

Check these common issues:
1. Verify `loadOnStartup: true` in `worlds.yml`
2. Ensure the world exists in the configured data source
3. Check for typos in world names
4. Review server logs for error messages
5. Run `/swm reload` after configuration changes

### Import command fails with "Invalid world"

The world folder must contain:
- `level.dat` file
- `region/` directory with `.mca` files

Make sure the world was properly saved before import.

### "World is already loaded" error

The world is already loaded on the server. Either:
- Use the existing loaded world
- Unload it first with `/swm unload <world>`

### "World already exists" error

A world with that name already exists in the data source. Either:
- Use a different name
- Delete the existing world first: `/swm delete <world> <source>`

### Database connection errors

1. Verify database credentials in `sources.yml`
2. Check that the database server is running
3. Ensure the database user has proper permissions
4. Check firewall rules for database port access
5. Verify `enabled: true` for your data source

### Out of memory errors

1. Increase server RAM allocation (`-Xmx4G` or higher)
2. Reduce number of loaded worlds
3. Use smaller worlds
4. Enable chunk pruning
5. Unload unused worlds

---

## API Questions

### How do I get the API instance?

```java
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
```

### Can I load worlds asynchronously?

**Partially:**
- `readWorld()` can be called asynchronously
- `loadWorld()` **must** be called on the main thread

```java
// Async read
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    SlimeWorld world = api.readWorld(loader, "world", false, props);

    // Sync load
    Bukkit.getScheduler().runTask(plugin, () -> {
        api.loadWorld(world, true);
    });
});
```

### How do I create a custom data source?

Implement the `SlimeLoader` interface and register it:

```java
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
plugin.registerLoader("my_loader", new MyCustomLoader());
```

See [Custom Data Sources](../api/use-data-source.md) for details.

---

## Performance

### How does SRF compare to vanilla world format?

| Aspect | SRF | Vanilla |
|--------|-----|---------|
| Load time | Faster | Slower |
| File size | Smaller | Larger |
| Database storage | Native support | Not supported |
| Multi-server | Supported | Complex |
| Memory usage | All chunks in RAM | Chunks loaded on demand |

### What are the performance benefits?

1. **Faster loading**: Worlds load significantly faster
2. **Smaller files**: Efficient compression reduces storage
3. **Instant cloning**: Create copies without file I/O
4. **Database storage**: Native support for MySQL, MongoDB, Redis

### What are the trade-offs?

1. **Memory usage**: All chunks stay in RAM until unload
2. **Format conversion**: Must import vanilla worlds
3. **Not a plugin**: Requires using the ASWM server JAR

---

## Migration

### How do I migrate from vanilla worlds?

```
/swm import <world-folder> <data-source>
```

See [Converting Worlds](../config/convert-world-to-srf.md) for details.

### How do I migrate between data sources?

```
/swm migrate <world> <new-data-source>
```

See [Migrating Worlds](../api/migrate-world.md) for details.

### Can I convert SRF back to vanilla format?

**Not directly.** There's no built-in converter from SRF to vanilla. You would need to:
1. Load the world on the server
2. Use external tools to copy chunks to a new vanilla world

---

## Getting Help

### Where can I get support?

- [Discord Server](https://discord.gg/YevvsMa)
- [GitHub Issues](https://github.com/InfernalSuite/AdvancedSlimePaper/issues)

### Where can I report bugs?

Open an issue on [GitHub](https://github.com/InfernalSuite/AdvancedSlimePaper/issues) with:
- ASWM version
- Minecraft version
- Full error log
- Steps to reproduce
