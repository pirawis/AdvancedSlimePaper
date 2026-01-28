# Installing AdvancedSlimePaper

This guide explains how to install and run AdvancedSlimePaper on your server.

## Requirements

- **Java 21** or higher
- **Paper-compatible plugins** (ASWM is a Paper fork, not a Spigot plugin)

## Download

### Official Releases

Download the latest release from:
- [GitHub Releases](https://github.com/InfernalSuite/AdvancedSlimePaper/releases)

### Development Builds

More recent development builds are available in the [Discord](https://discord.gg/YevvsMa) under the `#new-builds` channel.

## Installation Steps

### 1. Download the Files

You need two files:
- **Server JAR**: `aspaper-<version>.jar` (the Paper fork)
- **Plugin JAR**: `slimeworldmanager-plugin-<version>.jar`

### 2. Set Up Directory Structure

```
server/
├── aspaper-<version>.jar      # Server JAR
├── plugins/
│   └── slimeworldmanager-plugin-<version>.jar
├── eula.txt
└── server.properties
```

### 3. Accept EULA

Create or edit `eula.txt`:
```
eula=true
```

### 4. Start the Server

**Linux/macOS:**
```bash
java -Xms2G -Xmx4G -jar aspaper-<version>.jar
```

**Windows:**
```cmd
java -Xms2G -Xmx4G -jar aspaper-<version>.jar
```

### 5. Initial Setup

On first run, the plugin creates:
```
plugins/SlimeWorldManager/
├── worlds.yml      # World configurations
├── sources.yml     # Data source settings
└── slime_worlds/   # Default file storage
```

## Recommended JVM Flags

For better performance, use these flags:

```bash
java -Xms4G -Xmx4G \
  -XX:+UseG1GC \
  -XX:+ParallelRefProcEnabled \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+DisableExplicitGC \
  -XX:+AlwaysPreTouch \
  -XX:G1NewSizePercent=30 \
  -XX:G1MaxNewSizePercent=40 \
  -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 \
  -XX:G1HeapWastePercent=5 \
  -XX:G1MixedGCCountTarget=4 \
  -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:G1MixedGCLiveThresholdPercent=90 \
  -XX:G1RSetUpdatingPauseTimePercent=5 \
  -XX:SurvivorRatio=32 \
  -XX:+PerfDisableSharedMem \
  -XX:MaxTenuringThreshold=1 \
  -jar aspaper-<version>.jar
```

## Startup Script Examples

### Linux (start.sh)

```bash
#!/bin/bash
java -Xms4G -Xmx4G -jar aspaper-<version>.jar nogui
```

### Windows (start.bat)

```batch
@echo off
java -Xms4G -Xmx4G -jar aspaper-<version>.jar nogui
pause
```

### Systemd Service (Linux)

Create `/etc/systemd/system/minecraft.service`:

```ini
[Unit]
Description=Minecraft Server
After=network.target

[Service]
User=minecraft
WorkingDirectory=/opt/minecraft
ExecStart=/usr/bin/java -Xms4G -Xmx4G -jar aspaper-<version>.jar nogui
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable minecraft
sudo systemctl start minecraft
```

## Verifying Installation

1. **Check server log** for successful load:
   ```
   [INFO] [SlimeWorldManager] Enabled SlimeWorldManager vX.X.X
   ```

2. **Run version command**:
   ```
   /swm version
   ```

3. **List data sources**:
   ```
   /swm dslist file
   ```

## Migrating from Other Servers

### From Paper/Spigot

1. Stop your current server
2. Replace server JAR with ASWM JAR
3. Add the plugin JAR to plugins folder
4. Start the server
5. Import existing worlds: `/swm import <world> file`

### From Vanilla

1. Set up ASWM as described above
2. Copy your world folder to server root
3. Import: `/swm import <world-folder> file`
4. Configure in `worlds.yml`
5. Load: `/swm load <world>`

## Troubleshooting

### Server Won't Start

- Verify Java 21+ is installed: `java -version`
- Check for port conflicts
- Ensure enough RAM is allocated

### Plugin Not Loading

- Verify the plugin JAR is in `plugins/` folder
- Check server log for errors
- Ensure you're using the ASWM server JAR, not standard Paper

### Worlds Not Loading

- Check `worlds.yml` configuration
- Verify the data source is configured in `sources.yml`
- Check file permissions for the `slime_worlds/` directory

## Next Steps

1. [Configure data sources](../config/setup-data-sources.md)
2. [Import your worlds](../config/convert-world-to-srf.md)
3. [Configure world settings](../config/configure-world.md)
4. [Learn the commands](commands-and-permissions.md)
