# Commands & Permissions

This page lists all commands available in AdvancedSlimePaper, along with their permissions and usage.

**Command Aliases:** `/swm`, `/aswm`, `/swp`

**Master Permission:** `swm.*` grants access to all commands.

## Notation

- `<required>` - Required argument
- `[optional]` - Optional argument

---

## General Commands

### /swm help

Shows available commands based on your permissions.

| | |
|---|---|
| **Permission** | None |
| **Usage** | `/swm help` |

---

### /swm version

Shows the plugin version.

| | |
|---|---|
| **Permission** | None |
| **Usage** | `/swm version` |

---

### /swm debug

Toggles debug message output.

| | |
|---|---|
| **Permission** | `swm.debug` |
| **Usage** | `/swm debug` |

---

### /swm reload

Reloads all configuration files.

| | |
|---|---|
| **Permission** | `swm.reload` |
| **Usage** | `/swm reload` |

---

### /swm goto

Teleports you (or another player) to a world.

| | |
|---|---|
| **Permission** | `swm.goto` |
| **Usage** | `/swm goto <world> [player]` |

> Works with both SRF and vanilla worlds.

---

## World Listing

### /swm list

Lists all worlds on the server.

| | |
|---|---|
| **Permission** | `swm.worldlist` |
| **Usage** | `/swm list [slime] [page]` |

Use the `slime` argument to show only SRF worlds.

---

### /swm dslist

Lists all worlds stored in a data source.

| | |
|---|---|
| **Permission** | `swm.dslist` |
| **Usage** | `/swm dslist <data-source> [page]` |

**Example:**
```
/swm dslist mysql
/swm dslist file 2
```

---

## World Creation

### /swm create

Creates a new empty world.

| | |
|---|---|
| **Permission** | `swm.createworld` |
| **Usage** | `/swm create <world> <data-source>` |

**Example:**
```
/swm create my_world file
/swm create arena mysql
```

---

### /swm import

Imports a vanilla world and converts it to SRF.

| | |
|---|---|
| **Permission** | `swm.importworld` |
| **Usage** | `/swm import <path-to-world> <data-source> [new-world-name]` |

**Example:**
```
/swm import world file
/swm import old_world mysql new_world
```

---

## World Loading

### /swm load

Loads a world from the configuration file.

| | |
|---|---|
| **Permission** | `swm.loadworld` |
| **Usage** | `/swm load <world>` |

The world must be configured in `worlds.yml` first.

---

### /swm load-template

Creates a temporary clone of a template world.

| | |
|---|---|
| **Permission** | `swm.loadworld.template` |
| **Usage** | `/swm load-template <template-world> <world-name>` |

> Temporary clones are never saved and are lost on server restart.

**Example:**
```
/swm load-template skyblock_template player_island
```

---

### /swm clone

Creates a persistent clone of a world.

| | |
|---|---|
| **Permission** | `swm.cloneworld` |
| **Usage** | `/swm clone <template-world> <new-world-name> [data-source]` |

If no data source is specified, uses the template's data source.

**Example:**
```
/swm clone skyblock_template island_001 mysql
```

---

### /swm unload

Unloads a world from the server.

| | |
|---|---|
| **Permission** | `swm.unloadworld` |
| **Usage** | `/swm unload <world>` |

> Works with both SRF and vanilla worlds.

---

## World Management

### /swm save

Manually saves a world.

| | |
|---|---|
| **Permission** | `swm.saveworld` |
| **Usage** | `/swm save <world>` |

---

### /swm setspawn

Sets the spawn point for a world.

| | |
|---|---|
| **Permission** | `swm.setspawn` |
| **Usage** | `/swm setspawn <world> <x> <y> <z> [yaw] [pitch]` |

**Example:**
```
/swm setspawn lobby 0 64 0
/swm setspawn lobby 0 64 0 90 0
```

---

### /swm migrate

Moves a world to a different data source.

| | |
|---|---|
| **Permission** | `swm.migrate` |
| **Usage** | `/swm migrate <world> <new-data-source>` |

**Example:**
```
/swm migrate my_world mysql
```

---

### /swm delete

Permanently deletes a world.

| | |
|---|---|
| **Permission** | `swm.deleteworld` |
| **Usage** | `/swm delete <world> [data-source]` |

> **Warning:** This action is permanent! Run the command twice to confirm.

**Example:**
```
/swm delete old_world
/swm delete old_world mysql
```

---

## Permission Summary

| Permission | Commands |
|------------|----------|
| `swm.*` | All commands |
| `swm.debug` | `/swm debug` |
| `swm.reload` | `/swm reload` |
| `swm.goto` | `/swm goto` |
| `swm.worldlist` | `/swm list` |
| `swm.dslist` | `/swm dslist` |
| `swm.createworld` | `/swm create` |
| `swm.importworld` | `/swm import` |
| `swm.loadworld` | `/swm load` |
| `swm.loadworld.template` | `/swm load-template` |
| `swm.cloneworld` | `/swm clone` |
| `swm.unloadworld` | `/swm unload` |
| `swm.saveworld` | `/swm save` |
| `swm.setspawn` | `/swm setspawn` |
| `swm.migrate` | `/swm migrate` |
| `swm.deleteworld` | `/swm delete` |

---

## Example Permission Setup (LuckPerms)

### Server Admin
```
/lp group admin permission set swm.* true
```

### Builder (can load/goto worlds)
```
/lp group builder permission set swm.loadworld true
/lp group builder permission set swm.goto true
/lp group builder permission set swm.worldlist true
```

### Default Player (view only)
```
/lp group default permission set swm.goto true
```
