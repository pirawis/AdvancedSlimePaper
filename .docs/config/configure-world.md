# World Configuration

This guide explains how to configure SlimeWorlds using the `worlds.yml` configuration file.

## Configuration File Location

The configuration file is located at:
```
plugins/SlimeWorldManager/worlds.yml
```

## Basic Configuration

```yaml
worlds:
  my_world:
    source: file
    loadOnStartup: true
    readOnly: false
    spawn: 0, 64, 0
    difficulty: normal
    allowMonsters: true
    allowAnimals: true
    pvp: true
    environment: NORMAL
    worldType: default
```

After modifying the configuration, reload it with:
```
/swm reload
```

## Configuration Options

### `source`

The data source where the world is stored.

| Value | Description |
|-------|-------------|
| `file` | Local file system (default) |
| `mysql` | MySQL database |
| `mongodb` | MongoDB database |
| `redis` | Redis storage |
| `api` | Remote API |
| *custom* | Any registered custom loader |

**Default:** `file`

---

### `loadOnStartup`

Whether the world should be automatically loaded when the server starts.

| Value | Description |
|-------|-------------|
| `true` | Load world on server start |
| `false` | Don't load automatically |

**Default:** `false`

---

### `readOnly`

If enabled, changes to the world are never saved. Multiple servers can load the same world simultaneously in read-only mode.

| Value | Description |
|-------|-------------|
| `true` | Changes are not saved; world is not locked |
| `false` | Changes are saved; world is locked for this server |

**Default:** `false`

---

### `spawn`

The spawn coordinates for the world.

**Format:** `x, y, z` or `x, y, z, yaw, pitch`

**Examples:**
```yaml
spawn: 0, 64, 0
spawn: 100.5, 72, -50.5
spawn: 0, 64, 0, 90, 0
```

**Default:** `0, 255, 0`

---

### `difficulty`

The world's difficulty level.

| Value | Description |
|-------|-------------|
| `peaceful` | No hostile mobs spawn |
| `easy` | Hostile mobs deal less damage |
| `normal` | Standard difficulty |
| `hard` | Hostile mobs deal more damage |

**Default:** `peaceful`

---

### `allowMonsters`

Whether hostile mobs can spawn.

**Default:** `true`

---

### `allowAnimals`

Whether passive animals can spawn.

**Default:** `true`

---

### `pvp`

Whether player vs. player combat is allowed.

**Default:** `true`

---

### `environment`

The world's dimension type, affecting sky, fog, and mob spawning.

| Value | Description |
|-------|-------------|
| `NORMAL` | Overworld |
| `NETHER` | Nether dimension |
| `THE_END` | End dimension |

**Default:** `NORMAL`

---

### `worldType`

The world generation type.

| Value | Description |
|-------|-------------|
| `default` | Standard terrain generation |
| `flat` | Superflat world |
| `large_biomes` | Larger biome sizes |
| `amplified` | Extreme terrain heights |
| `customized` | Custom world settings |
| `debug_all_block_states` | Debug world |
| `default_1_1` | Legacy 1.1 generation |

**Default:** `default`

---

### `defaultBiome`

The default biome for newly generated chunks.

**Format:** Namespaced biome ID (e.g., `minecraft:plains`)

**Default:** `minecraft:plains`

---

### `dragonBattle`

Whether to enable the Ender Dragon battle in End worlds.

**Default:** `false`

---

## Advanced Options (Experimental)

These options are experimental and may change in future versions.

### Data Persistence

```yaml
worlds:
  my_world:
    savePOI: false        # Points of Interest (villager jobs, beds, bee nests)
    saveBlockTicks: false # Block tick scheduling (redstone)
    saveFluidTicks: false # Fluid tick scheduling (water/lava flow)
```

> **Warning:** Disabling these will cause redstone and fluids to reset when the world reloads.

### Save Boundaries

Limit the area that gets saved:

```yaml
worlds:
  my_world:
    hasSaveBounds: true
    saveMinX: -100    # Minimum chunk X
    saveMinZ: -100    # Minimum chunk Z
    saveMaxX: 100     # Maximum chunk X
    saveMaxZ: 100     # Maximum chunk Z
```

### Chunk Management

```yaml
worlds:
  my_world:
    pruning: aggressive   # 'aggressive' or 'never'
    chunkSectionMin: -4   # Minimum Y section (Y / 16)
    chunkSectionMax: 19   # Maximum Y section (Y / 16)
```

### Sea Level

Affects water mob spawning (squids, turtles, etc.):

```yaml
worlds:
  my_world:
    seaLevel: 63  # Use 63 for vanilla behavior, default is -63
```

## Complete Example

```yaml
worlds:
  # Main survival world
  survival:
    source: mysql
    loadOnStartup: true
    readOnly: false
    spawn: 0, 64, 0
    difficulty: normal
    allowMonsters: true
    allowAnimals: true
    pvp: true
    environment: NORMAL
    worldType: default
    defaultBiome: minecraft:plains
    saveBlockTicks: true
    saveFluidTicks: true
    savePOI: true

  # Skyblock template (read-only)
  skyblock_template:
    source: file
    loadOnStartup: true
    readOnly: true
    spawn: 0, 100, 0
    difficulty: normal
    allowMonsters: true
    allowAnimals: true
    pvp: false
    environment: NORMAL
    worldType: flat

  # Lobby world
  lobby:
    source: file
    loadOnStartup: true
    readOnly: true
    spawn: 0, 65, 0
    difficulty: peaceful
    allowMonsters: false
    allowAnimals: false
    pvp: false
    environment: NORMAL
    worldType: flat

  # Nether world
  survival_nether:
    source: mysql
    loadOnStartup: true
    readOnly: false
    spawn: 0, 64, 0
    difficulty: normal
    allowMonsters: true
    allowAnimals: true
    environment: NETHER

  # End world with dragon
  survival_end:
    source: mysql
    loadOnStartup: true
    readOnly: false
    spawn: 0, 64, 0
    difficulty: normal
    allowMonsters: true
    dragonBattle: true
    environment: THE_END
```

## Tips

1. **Use read-only for templates**: Template worlds should be read-only to prevent accidental modifications
2. **Enable tick saving for redstone**: If your world has redstone, enable `saveBlockTicks`
3. **Database for multi-server**: Use MySQL or MongoDB for worlds shared across servers
4. **Lobby worlds**: Set difficulty to peaceful and disable spawning for lobby worlds
