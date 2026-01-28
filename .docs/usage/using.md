# Using AdvancedSlimePaper

This guide explains how to use AdvancedSlimePaper for managing Minecraft worlds.

## What is AdvancedSlimePaper?

AdvancedSlimePaper (ASWM) is a Paper server fork that includes the SlimeWorldManager plugin. It uses the Slime Region Format (SRF) for world storage, which offers several advantages:

- **Faster loading**: Worlds load significantly faster than vanilla format
- **Smaller file sizes**: SRF compresses world data efficiently
- **Database storage**: Store worlds in MySQL, MongoDB, Redis, or custom backends
- **Multi-server support**: Share worlds across multiple servers
- **Fast cloning**: Create copies of worlds almost instantly

## Quick Start

### 1. Import a World

First, convert your vanilla world to SRF format:

```
/swm import my_world file
```

### 2. Configure the World

Edit `plugins/SlimeWorldManager/worlds.yml`:

```yaml
worlds:
  my_world:
    source: file
    loadOnStartup: true
    spawn: 0, 64, 0
    difficulty: normal
```

### 3. Reload and Load

```
/swm reload
/swm load my_world
```

### 4. Teleport to the World

```
/swm goto my_world
```

## Common Operations

### Creating an Empty World

```
/swm create new_world file
```

This creates a blank world with no terrain.

### Cloning a World

**Temporary clone** (not saved):
```
/swm load-template my_template my_clone
```

**Permanent clone** (saved):
```
/swm clone my_template my_clone file
```

### Unloading a World

```
/swm unload my_world
```

### Saving a World

```
/swm save my_world
```

### Deleting a World

```
/swm delete my_world file
```

> **Warning**: This is permanent! You'll need to confirm by running the command twice.

### Moving a World to Another Data Source

```
/swm migrate my_world mysql
```

### Setting Spawn Point

```
/swm setspawn my_world 100 64 -50 0 0
```

## Using Database Storage

### Setting Up MySQL

1. Configure in `sources.yml`:
   ```yaml
   mysql:
     enabled: true
     host: localhost
     port: 3306
     username: minecraft
     password: your_password
     database: slimeworldmanager
   ```

2. Import worlds to MySQL:
   ```
   /swm import my_world mysql
   ```

3. Or migrate existing worlds:
   ```
   /swm migrate my_world mysql
   ```

### Multi-Server Setup

For BungeeCord/Velocity networks:

1. Use the same database configuration on all servers
2. Load worlds in read-only mode to prevent conflicts:
   ```yaml
   worlds:
     lobby:
       source: mysql
       readOnly: true
   ```
3. Only one server should have write access per world

## Use Cases

### Skyblock Server

Use world cloning for per-player islands:

```java
// When player joins, clone the template
SlimeWorld template = api.readWorld(loader, "island_template", true, props);
SlimeWorld island = template.clone("island_" + player.getUUID(), loader);
api.loadWorld(island, true);
api.saveWorld(island);
```

Configure template as read-only:
```yaml
worlds:
  island_template:
    source: file
    readOnly: true
    loadOnStartup: true
```

### Lobby Server

Fast, read-only lobby that never changes:

```yaml
worlds:
  lobby:
    source: file
    loadOnStartup: true
    readOnly: true
    difficulty: peaceful
    allowMonsters: false
    pvp: false
```

### Minigame Arenas

Reset arenas by reloading from template:

```
/swm unload arena_1
/swm load-template arena_template arena_1
```

### Backup Worlds

Migrate to file for backup:
```
/swm migrate survival file
```

Then copy the `.slime` file from `plugins/SlimeWorldManager/slime_worlds/`.

## Performance Tips

1. **Use read-only mode** when worlds don't need saving
2. **Enable database connection pooling** for heavy load
3. **Prune unused chunks** to reduce memory usage
4. **Use async operations** in API code
5. **Monitor memory** - all chunks stay in RAM until world unload

## World Properties

Common properties you can set in `worlds.yml`:

| Property | Description |
|----------|-------------|
| `spawn` | Spawn coordinates |
| `difficulty` | peaceful/easy/normal/hard |
| `allowMonsters` | Mob spawning |
| `allowAnimals` | Animal spawning |
| `pvp` | PvP combat |
| `environment` | NORMAL/NETHER/THE_END |
| `readOnly` | Prevent saves |
| `loadOnStartup` | Auto-load on boot |

See [World Configuration](../config/configure-world.md) for all options.

## Next Steps

- [Commands Reference](commands-and-permissions.md)
- [API Documentation](../api/load-world.md)
- [FAQ](../other/faq.md)
