# Development Server Setup

This guide explains how to set up a development server for testing plugins that use the AdvancedSlimePaper API.

## Using Gradle (Recommended)

You can configure your plugin's Gradle setup to automatically download and run ASWM from your IDE.

### Prerequisites

This guide assumes you're using:
- Gradle with Kotlin DSL (`build.gradle.kts`)
- Shadow plugin (`com.github.johnrengelman.shadow`)

### Step 1: Add Imports

Add these imports to the top of your `build.gradle.kts`:

```kotlin
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import java.io.FileOutputStream
```

### Step 2: Add the Plugin

Add the Minecraft server plugin to your plugins block. Check for the [latest version](https://github.com/sya-ri/minecraft-server-gradle-plugin).

```kotlin
plugins {
    // Your other plugins
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("dev.s7a.gradle.minecraft.server") version "3.0.0"
}
```

### Step 3: Configure the Task

Add this configuration to your `build.gradle.kts`:

```kotlin
task<LaunchMinecraftServerTask>("runServer") {
    dependsOn("shadowJar")

    // IMPORTANT: Replace with a valid build from Discord #new-builds
    val aswmBuild = "INSERT_BUILD_COMMIT_HERE"
    val mcVersion = "1.20.4-R0.1"

    doFirst {
        // Copy your plugin to the server
        copy {
            val shadowJar = tasks.named<AbstractArchiveTask>("shadowJar")
                .flatMap { it.archiveFile }
                .get()
                .asFile
            from(shadowJar)
            into(layout.buildDirectory.dir("MinecraftServer/plugins"))
        }

        // Download ASWM plugin
        val pluginFile = layout.buildDirectory
            .file("MinecraftServer/plugins/SlimeWorldManager.jar")
            .get()
            .asFile
        if (!pluginFile.exists()) {
            uri(ASWM.plugin(aswmBuild, mcVersion))
                .toURL()
                .openStream()
                .use { it.copyTo(FileOutputStream(pluginFile)) }
        }
    }

    // Configure server
    jarUrl.set(ASWM.server(aswmBuild, mcVersion))
    agreeEula.set(true)
}

// ASWM download URLs
class ASWM {
    companion object {
        private const val BASE_URL = "https://dl.rapture.pw/IS/ASP/main"

        fun server(build: String, version: String): String {
            return "$BASE_URL/$build/aspaper-paperclip-$version-SNAPSHOT-reobf.jar"
        }

        fun plugin(build: String, version: String): String {
            return "$BASE_URL/$build/plugin-$version-SNAPSHOT.jar"
        }
    }
}
```

### Step 4: Get a Build ID

Get a valid build ID from the `#new-builds` channel on the [Discord server](https://discord.gg/YevvsMa).

The build ID looks like: `42175f090baf00494c0fb25588f1e22ad4d9558f`

Replace `INSERT_BUILD_COMMIT_HERE` with the actual commit ID.

### Step 5: Run the Server

Run the Gradle task:

```bash
./gradlew runServer
```

Or use your IDE's Gradle panel to run the `runServer` task.

---

## Manual Setup

If you prefer manual setup:

### 1. Download Files

Download from [GitHub Releases](https://github.com/InfernalSuite/AdvancedSlimePaper/releases) or Discord:
- Server JAR: `aspaper-<version>.jar`
- Plugin JAR: `slimeworldmanager-plugin-<version>.jar`

### 2. Create Directory Structure

```
dev-server/
├── aspaper-<version>.jar
├── plugins/
│   ├── slimeworldmanager-plugin-<version>.jar
│   └── your-plugin.jar
└── eula.txt (with eula=true)
```

### 3. Create Run Configuration

In IntelliJ IDEA:
1. Go to **Run > Edit Configurations**
2. Add a new **JAR Application**
3. Set **Path to JAR** to your server JAR
4. Set **Working directory** to your dev-server folder
5. Add VM options: `-Xms2G -Xmx2G`

---

## Adding ASWM API Dependency

Add the API as a dependency in your plugin:

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.infernalsuite.asp:api:1.20.4-R0.1-SNAPSHOT")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    maven { url 'https://repo.infernalsuite.com/repository/maven-snapshots/' }
}

dependencies {
    compileOnly 'com.infernalsuite.asp:api:1.20.4-R0.1-SNAPSHOT'
}
```

### Maven

```xml
<repository>
    <id>infernalsuite-snapshots</id>
    <url>https://repo.infernalsuite.com/repository/maven-snapshots/</url>
</repository>

<dependency>
    <groupId>com.infernalsuite.asp</groupId>
    <artifactId>api</artifactId>
    <version>1.20.4-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

---

## Testing Your Plugin

### Basic API Test

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Get API instance
        AdvancedSlimePaperAPI api = AdvancedSlimePaperAPI.instance();
        getLogger().info("ASWM API loaded: " + (api != null));

        // Get plugin for loader access
        SlimePlugin swm = (SlimePlugin) Bukkit.getPluginManager()
            .getPlugin("SlimeWorldManager");

        if (swm != null) {
            SlimeLoader loader = swm.getLoader("file");
            getLogger().info("File loader available: " + (loader != null));
        }
    }
}
```

### Add Plugin Dependency

In your `plugin.yml`:

```yaml
name: MyPlugin
depend: [SlimeWorldManager]
```

Or as soft dependency:

```yaml
softdepend: [SlimeWorldManager]
```

---

## Troubleshooting

### "Cannot resolve symbol" errors

Make sure the API dependency is correctly added and refresh your Gradle project.

### Server won't start

- Check Java version: `java -version` (must be 21+)
- Verify you're using the ASWM server JAR
- Check that `eula.txt` contains `eula=true`

### Plugin not detecting ASWM

- Ensure SlimeWorldManager is in the `depend` or `softdepend` list
- Verify the plugin JAR is in the `plugins/` folder
- Check server logs for loading errors
