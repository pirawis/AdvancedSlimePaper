# AdvancedSlimePaper Documentation

Welcome to the AdvancedSlimePaper (ASWM) documentation. ASWM is a Paper server fork that uses the Slime Region Format (SRF) for fast, efficient world management.

## Quick Links

| I want to... | Go to |
|--------------|-------|
| Install ASWM | [Installation Guide](usage/install.md) |
| Learn the basics | [Using ASWM](usage/using.md) |
| See all commands | [Commands & Permissions](usage/commands-and-permissions.md) |
| Use the API | [Loading Worlds](api/load-world.md) |
| Configure worlds | [World Configuration](config/configure-world.md) |
| Get help | [FAQ](other/faq.md) |

---

## Getting Started

1. **[Install ASWM](usage/install.md)** - Download and set up the server
2. **[Import your worlds](config/convert-world-to-srf.md)** - Convert vanilla worlds to SRF
3. **[Configure worlds](config/configure-world.md)** - Set up world properties
4. **[Learn commands](usage/commands-and-permissions.md)** - Manage worlds in-game

---

## Documentation Sections

### Usage
- [Installation](usage/install.md) - How to install and run ASWM
- [Using ASWM](usage/using.md) - Basic usage guide
- [Commands & Permissions](usage/commands-and-permissions.md) - All commands and permissions

### Configuration
- [World Configuration](config/configure-world.md) - Configure world properties in `worlds.yml`
- [Data Sources](config/setup-data-sources.md) - Set up MySQL, MongoDB, Redis, or API storage
- [Converting Worlds](config/convert-world-to-srf.md) - Import vanilla worlds to SRF

### API Reference
- [Loading Worlds](api/load-world.md) - Load and manage worlds programmatically
- [World Properties](api/properties.md) - All available world properties
- [Importing Worlds](api/import-world.md) - Convert vanilla worlds via API
- [Migrating Worlds](api/migrate-world.md) - Move worlds between data sources
- [Custom Data Sources](api/use-data-source.md) - Create custom storage backends
- [Events](api/events.md) - Listen to world events
- [Building from Source](api/custom-build-preparation.md) - Compile ASWM yourself
- [Dev Server Setup](api/setup-a-devserver.md) - Set up a development environment

### Examples
- [Skyblock Plugin](examples/skyblock-plugin.md) - Per-player island system
- [Lobby System](examples/lobby-system.md) - Multi-server read-only lobby
- [Minigame Arenas](examples/minigame-arenas.md) - Resettable game arenas

### Other
- [FAQ](other/faq.md) - Frequently asked questions

---

## Key Features

| Feature | Description |
|---------|-------------|
| **Fast Loading** | Worlds load significantly faster than vanilla format |
| **Smaller Files** | Efficient compression reduces storage requirements |
| **Database Storage** | Native support for MySQL, MongoDB, Redis |
| **Multi-Server** | Share worlds across multiple servers |
| **Instant Cloning** | Create world copies without file I/O |
| **Read-Only Mode** | Load worlds without locking for multiple servers |

---

## Requirements

- **Java 21** or higher
- **Paper-compatible plugins** (ASWM is a Paper fork)

---

## Links

- [GitHub Repository](https://github.com/InfernalSuite/AdvancedSlimePaper)
- [Discord Server](https://discord.gg/YevvsMa)
- [Issue Tracker](https://github.com/InfernalSuite/AdvancedSlimePaper/issues)

---

## Quick Example

```java
// Get API
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
SlimeLoader loader = plugin.getLoader("file");

// Load a world
SlimePropertyMap props = new SlimePropertyMap();
props.setValue(SlimeProperties.DIFFICULTY, "normal");

SlimeWorld world = api.readWorld(loader, "my-world", false, props);
api.loadWorld(world, true);
```

See [Loading Worlds](api/load-world.md) for complete documentation.
