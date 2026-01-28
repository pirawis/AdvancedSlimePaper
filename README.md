# Advanced Slime Paper (ASP)

[![Build Status](https://ci.infernalsuite.com/app/rest/builds/buildType:(id:AdvancedSlimePaper_Build)/statusIcon)](https://ci.infernalsuite.com/viewType.html?buildTypeId=AdvancedSlimePaper_Build&guest=1)
[![Tests](https://github.com/InfernalSuite/AdvancedSlimePaper/actions/workflows/test.yml/badge.svg)](https://github.com/InfernalSuite/AdvancedSlimePaper/actions/workflows/test.yml)
[![codecov](https://codecov.io/gh/InfernalSuite/AdvancedSlimePaper/branch/main/graph/badge.svg)](https://codecov.io/gh/InfernalSuite/AdvancedSlimePaper)
[![Discord](https://img.shields.io/discord/728826761969426473?color=5865F2&label=Discord&logo=discord&logoColor=white)](https://discord.gg/YevvsMa)

Advanced Slime Paper is a [Paper](https://papermc.io/) fork that implements the **Slime Region Format (SRF)**, originally developed by Hypixel. It provides server administrators with fast world loading, efficient storage, and powerful world management capabilities.

## Features

- **Fast World Loading** - Worlds load significantly faster than vanilla format
- **Smaller File Sizes** - Efficient compression reduces storage requirements
- **Database Storage** - Native support for MySQL, MongoDB, Redis, and custom backends
- **Multi-Server Support** - Share worlds across multiple servers with read-only mode
- **Instant World Cloning** - Create world copies without file I/O operations
- **Full Bukkit Compatibility** - Works with all Paper and Spigot plugins

## Requirements

- **Java 21** or higher
- **Paper-compatible plugins**

## Quick Start

### 1. Download

Get the latest release from [infernalsuite.com/download/asp](https://infernalsuite.com/download/asp) or join our [Discord](https://discord.gg/YevvsMa) for development builds.

### 2. Installation

```
server/
├── aspaper-<version>.jar          # Server JAR
├── plugins/
│   └── slimeworldmanager-plugin.jar
└── eula.txt
```

### 3. Start the Server

```bash
java -Xms4G -Xmx4G -jar aspaper-<version>.jar
```

### 4. Import a World

```
/swm import <world-folder> file
/swm load <world-name>
```

## Documentation

| Resource | Link |
|----------|------|
| Full Documentation | [.docs/index.md](.docs/index.md) |
| API Reference | [.docs/api/](.docs/api/) |
| Configuration Guide | [.docs/config/](.docs/config/) |
| Commands & Permissions | [.docs/usage/commands-and-permissions.md](.docs/usage/commands-and-permissions.md) |
| Examples | [.docs/examples/](.docs/examples/) |
| FAQ | [.docs/other/faq.md](.docs/other/faq.md) |
| Javadocs | [docs.infernalsuite.com](https://docs.infernalsuite.com/) |

## API Usage

### Maven

```xml
<repository>
    <id>infernalsuite-snapshots</id>
    <url>https://repo.infernalsuite.com/repository/maven-snapshots/</url>
</repository>

<dependency>
    <groupId>com.infernalsuite.asp</groupId>
    <artifactId>api</artifactId>
    <version>1.21.4-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.infernalsuite.asp:api:1.21.4-R0.1-SNAPSHOT")
}
```

### Example

```java
// Get the API
AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
SlimeLoader loader = plugin.getLoader("file");

// Configure world properties
SlimePropertyMap props = new SlimePropertyMap();
props.setValue(SlimeProperties.SPAWN_X, 0);
props.setValue(SlimeProperties.SPAWN_Y, 64);
props.setValue(SlimeProperties.SPAWN_Z, 0);
props.setValue(SlimeProperties.DIFFICULTY, "normal");

// Load the world (read async, load sync)
SlimeWorld world = api.readWorld(loader, "my-world", false, props);
api.loadWorld(world, true);
```

## Building from Source

### Requirements

- JDK 21+
- Git

### Build Commands

```bash
git clone https://github.com/InfernalSuite/AdvancedSlimePaper.git
cd AdvancedSlimePaper
./gradlew applyPatches
./gradlew createReobfBundlerJar
```

The server JAR will be in `build/libs/`.

### Building the Plugin

```bash
./gradlew :plugin:shadowJar
```

## Data Sources

| Source | Description |
|--------|-------------|
| `file` | Local file system (default) |
| `mysql` | MySQL/MariaDB database |
| `mongodb` | MongoDB database |
| `redis` | Redis in-memory store |
| `api` | Custom HTTP API |

## Commands

| Command | Description |
|---------|-------------|
| `/swm import <world> <source>` | Import vanilla world to SRF |
| `/swm load <world>` | Load a world |
| `/swm unload <world>` | Unload a world |
| `/swm clone <template> <name>` | Clone a world |
| `/swm create <world> <source>` | Create empty world |
| `/swm delete <world>` | Delete a world |
| `/swm migrate <world> <source>` | Move world to another source |
| `/swm list` | List all worlds |
| `/swm goto <world>` | Teleport to world |

See [Commands & Permissions](.docs/usage/commands-and-permissions.md) for full documentation.

## Support

- **Discord**: [discord.gg/YevvsMa](https://discord.gg/YevvsMa)
- **Issues**: [GitHub Issues](https://github.com/InfernalSuite/AdvancedSlimePaper/issues)

## Credits

- [Paul19988](https://github.com/Paul19988) - ASWM Creator
- [ComputerNerd100](https://github.com/ComputerNerd100) - Maintainer
- [Owen1212055](https://github.com/Owen1212055) - Maintainer
- [Gerolmed](https://github.com/Gerolmed) - Maintainer
- [b0ykoe](https://github.com/b0ykoe) - Build services & repositories
- [Grinderwolf](https://github.com/Grinderwolf) - Original SWM creator
- [Minikloon](https://twitter.com/Minikloon) & [Hypixel](https://twitter.com/HypixelNetwork) - Slime Region Format

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
