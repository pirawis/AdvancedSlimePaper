# Events API

AdvancedSlimePaper provides events that allow plugins to react to world-related actions.

## Available Events

### LoadSlimeWorldEvent

Fired when a SlimeWorld is loaded into the server.

```java
import com.infernalsuite.asp.api.events.LoadSlimeWorldEvent;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WorldListener implements Listener {

    @EventHandler
    public void onSlimeWorldLoad(LoadSlimeWorldEvent event) {
        SlimeWorldInstance world = event.getSlimeWorld();

        String worldName = world.getName();
        boolean readOnly = world.isReadOnly();

        System.out.println("SlimeWorld loaded: " + worldName);
        System.out.println("Read-only: " + readOnly);
    }
}
```

#### Event Properties

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getSlimeWorld()` | `SlimeWorldInstance` | The loaded world instance |

#### Event Characteristics

| Property | Value |
|----------|-------|
| Cancellable | No |
| Async | No (fires on main thread) |

## Registering Listeners

Register your listener in your plugin's `onEnable()`:

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
    }
}
```

## Use Cases

### Logging World Loads

```java
@EventHandler
public void onWorldLoad(LoadSlimeWorldEvent event) {
    SlimeWorldInstance world = event.getSlimeWorld();
    getLogger().info("World loaded: " + world.getName());
}
```

### Setting Up World-Specific Features

```java
@EventHandler
public void onWorldLoad(LoadSlimeWorldEvent event) {
    SlimeWorldInstance world = event.getSlimeWorld();
    World bukkitWorld = world.getBukkitWorld();

    // Set world border
    WorldBorder border = bukkitWorld.getWorldBorder();
    border.setCenter(0, 0);
    border.setSize(1000);

    // Set game rules
    bukkitWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    bukkitWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
}
```

### Player Island Initialization (Skyblock)

```java
@EventHandler
public void onWorldLoad(LoadSlimeWorldEvent event) {
    SlimeWorldInstance world = event.getSlimeWorld();
    String worldName = world.getName();

    // Check if this is a player island
    if (worldName.startsWith("island_")) {
        String playerUUID = worldName.replace("island_", "");

        // Initialize island data
        islandManager.initializeIsland(playerUUID, world.getBukkitWorld());
    }
}
```

### Metrics and Monitoring

```java
@EventHandler
public void onWorldLoad(LoadSlimeWorldEvent event) {
    SlimeWorldInstance world = event.getSlimeWorld();

    // Track loaded worlds
    metrics.increment("worlds.loaded");
    metrics.gauge("worlds.total", api.getLoadedWorlds().size());

    // Log memory usage
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
    getLogger().info("Memory after loading " + world.getName() + ": " + usedMemory + "MB");
}
```

## Combining with API

```java
public class WorldManager implements Listener {

    private final AdvancedSlimePaperAPI api;
    private final Map<String, Long> loadTimes = new HashMap<>();

    public WorldManager() {
        this.api = AdvancedSlimePaperAPI.instance();
    }

    public void loadWorldWithTracking(SlimeWorld world) {
        loadTimes.put(world.getName(), System.currentTimeMillis());
        api.loadWorld(world, true);
    }

    @EventHandler
    public void onWorldLoad(LoadSlimeWorldEvent event) {
        String name = event.getSlimeWorld().getName();
        Long startTime = loadTimes.remove(name);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            getLogger().info("World " + name + " loaded in " + duration + "ms");
        }
    }
}
```

## Notes

Currently, `LoadSlimeWorldEvent` is the only SlimeWorld-specific event available. For other world events, you can use standard Bukkit events like `WorldLoadEvent`, `WorldUnloadEvent`, and `WorldSaveEvent`.
