# Properties API

World properties define the behavior and characteristics of SlimeWorlds. Properties are handled by `SlimeProperty` instances and stored in `SlimePropertyMap` objects.

## Basic Usage

```java
// Create a new property map
SlimePropertyMap properties = new SlimePropertyMap();

// Set property values
properties.setValue(SlimeProperties.DIFFICULTY, "normal");
properties.setValue(SlimeProperties.SPAWN_X, 100);
properties.setValue(SlimeProperties.SPAWN_Y, 64);
properties.setValue(SlimeProperties.SPAWN_Z, 100);
properties.setValue(SlimeProperties.ALLOW_ANIMALS, true);
properties.setValue(SlimeProperties.ALLOW_MONSTERS, true);
properties.setValue(SlimeProperties.PVP, false);

// Get property values
String difficulty = properties.getValue(SlimeProperties.DIFFICULTY);
int spawnX = properties.getValue(SlimeProperties.SPAWN_X);
```

## Available Properties

### Spawn Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `SPAWN_X` | Integer | `0` | X coordinate of the world spawn |
| `SPAWN_Y` | Integer | `255` | Y coordinate of the world spawn |
| `SPAWN_Z` | Integer | `0` | Z coordinate of the world spawn |
| `SPAWN_YAW` | Float | `0.0` | Yaw rotation at spawn point |

### World Behavior

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `DIFFICULTY` | String | `"peaceful"` | World difficulty: `peaceful`, `easy`, `normal`, `hard` |
| `ALLOW_MONSTERS` | Boolean | `true` | Whether monsters can spawn |
| `ALLOW_ANIMALS` | Boolean | `true` | Whether animals can spawn |
| `PVP` | Boolean | `true` | Whether PvP combat is allowed |
| `DRAGON_BATTLE` | Boolean | `false` | Enable dragon battle in End worlds |

### World Environment

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `ENVIRONMENT` | String | `"normal"` | World environment: `normal`, `nether`, `the_end` |
| `WORLD_TYPE` | String | `"default"` | World type (see options below) |
| `DEFAULT_BIOME` | String | `"minecraft:plains"` | Default biome for empty chunks |
| `SEA_LEVEL` | Integer | `-63` | Sea level for mob spawning (use `63` for vanilla behavior) |

**World Type Options:**
- `default` - Standard world generation
- `flat` - Superflat world
- `large_biomes` - Larger biome sizes
- `amplified` - Extreme terrain heights
- `customized` - Custom world settings
- `debug_all_block_states` - Debug world with all block states
- `default_1_1` - Legacy 1.1 world generation

### Data Persistence (Experimental)

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `SAVE_POI` | Boolean | `false` | Save Points of Interest (villager jobs, beds, bee nests, lightning rods) |
| `SAVE_BLOCK_TICKS` | Boolean | `false` | Save block tick data (redstone updates) |
| `SAVE_FLUID_TICKS` | Boolean | `false` | Save fluid tick data (water/lava flow) |

> **Note:** If these are disabled, redstone circuits and fluid flows will pause/reset when worlds reload.

### Save Boundaries (Experimental)

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `SHOULD_LIMIT_SAVE` | Boolean | `false` | Enable save area boundaries |
| `SAVE_MIN_X` | Integer | `0` | Minimum X chunk coordinate to save |
| `SAVE_MIN_Z` | Integer | `0` | Minimum Z chunk coordinate to save |
| `SAVE_MAX_X` | Integer | `0` | Maximum X chunk coordinate to save |
| `SAVE_MAX_Z` | Integer | `0` | Maximum Z chunk coordinate to save |

### Chunk Management (Experimental)

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `CHUNK_PRUNING` | String | `"aggressive"` | Chunk pruning mode: `aggressive` or `never` |
| `CHUNK_SECTION_MIN` | Integer | `-4` | Minimum chunk section (Y level / 16) |
| `CHUNK_SECTION_MAX` | Integer | `19` | Maximum chunk section (Y level / 16) |

## Complete Example

```java
public SlimePropertyMap createSkyblockProperties() {
    SlimePropertyMap props = new SlimePropertyMap();

    // Spawn location
    props.setValue(SlimeProperties.SPAWN_X, 0);
    props.setValue(SlimeProperties.SPAWN_Y, 100);
    props.setValue(SlimeProperties.SPAWN_Z, 0);
    props.setValue(SlimeProperties.SPAWN_YAW, 0.0f);

    // World settings
    props.setValue(SlimeProperties.DIFFICULTY, "normal");
    props.setValue(SlimeProperties.PVP, false);
    props.setValue(SlimeProperties.ALLOW_MONSTERS, true);
    props.setValue(SlimeProperties.ALLOW_ANIMALS, true);

    // Environment
    props.setValue(SlimeProperties.ENVIRONMENT, "normal");
    props.setValue(SlimeProperties.WORLD_TYPE, "flat");
    props.setValue(SlimeProperties.DEFAULT_BIOME, "minecraft:plains");

    // Data persistence for redstone
    props.setValue(SlimeProperties.SAVE_BLOCK_TICKS, true);
    props.setValue(SlimeProperties.SAVE_FLUID_TICKS, true);

    return props;
}
```

## Property Map Operations

```java
SlimePropertyMap props = new SlimePropertyMap();

// Set values
props.setValue(SlimeProperties.PVP, true);

// Get values
boolean pvpEnabled = props.getValue(SlimeProperties.PVP);

// Merge with another property map
SlimePropertyMap otherProps = new SlimePropertyMap();
otherProps.setValue(SlimeProperties.DIFFICULTY, "hard");
props.merge(otherProps);

// Convert to NBT compound for serialization
CompoundBinaryTag nbt = props.toCompound();
```

## Source Code References

- [SlimeProperty.java](../../api/src/main/java/com/infernalsuite/asp/api/world/properties/SlimeProperty.java) - Property interface
- [SlimeProperties.java](../../api/src/main/java/com/infernalsuite/asp/api/world/properties/SlimeProperties.java) - All available properties
- [SlimePropertyMap.java](../../api/src/main/java/com/infernalsuite/asp/api/world/properties/SlimePropertyMap.java) - Property map implementation
