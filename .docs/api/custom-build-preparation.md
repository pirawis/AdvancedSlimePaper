# Building AdvancedSlimePaper

This guide explains how to build AdvancedSlimePaper from source.

## Prerequisites

- **JDK 21** or higher
- **Git** for cloning the repository
- **Internet connection** for downloading dependencies

## Building the Server

ASWM uses modern Paperweight tooling to interact with the server source.

### 1. Clone the Repository

```bash
git clone https://github.com/InfernalSuite/AdvancedSlimePaper.git
cd AdvancedSlimePaper
```

### 2. Apply Patches

```bash
./gradlew applyPatches
```

This command applies all necessary patches to the Paper source code.

### 3. Build the Server JAR

```bash
./gradlew createReobfBundlerJar
```

The compiled server JAR will be located in:
```
build/libs/
```

### 4. View All Available Tasks

```bash
./gradlew tasks
```

## Building the Plugin

The ASWM plugin is built separately from the server.

### Build Command

```bash
cd plugin
../gradlew clean shadowJar
```

Or from the project root:

```bash
./gradlew :plugin:shadowJar
```

The plugin JAR will be located in:
```
plugin/build/libs/
```

## Development Builds

### Running Tests

```bash
./gradlew test
```

### Creating a Development Build

For quick development iterations:

```bash
./gradlew build
```

## Build Artifacts

| Artifact | Location | Description |
|----------|----------|-------------|
| Server JAR | `build/libs/` | Main server executable |
| Plugin JAR | `plugin/build/libs/` | SlimeWorldManager plugin |
| API JAR | `api/build/libs/` | API for developers |

## Troubleshooting

### Common Issues

**"Could not find tools.jar"**
- Ensure you're using JDK, not JRE
- Set `JAVA_HOME` to your JDK installation

**"Patch failed to apply"**
- Run `./gradlew cleanCache` and try again
- Ensure you have the latest source from git

**"Out of memory"**
- Increase Gradle memory: `export GRADLE_OPTS="-Xmx4g"`
- Or add to `gradle.properties`: `org.gradle.jvmargs=-Xmx4g`

### Clean Build

If you encounter issues, try a clean build:

```bash
./gradlew clean
./gradlew applyPatches
./gradlew createReobfBundlerJar
```

## Continuous Integration

For CI/CD pipelines, use:

```bash
./gradlew build --no-daemon
```

The `--no-daemon` flag prevents Gradle daemon issues in containerized environments.
