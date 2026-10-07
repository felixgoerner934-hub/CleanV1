# 🌸 SpawnerBeacon 1.1.1

Client-side Fabric mod for Minecraft **26.1.2**. SpawnerBeacon shows colored, animated vertical beams above loaded monster spawners and provides a clean Cherry Blossom configuration screen.

## What is included

- Client-only Fabric mod
- Minecraft 26.1.2
- Java 25
- Official Mojang mappings / unobfuscated 26.1 codebase
- Beam through walls using a custom RenderPipeline with no depth/stencil attachment
- Beam height 64–512, default 300
- Beam thickness 0.1–5.0
- Opacity slider
- Render distance in chunks
- Animated beam pulse
- Distance fade
- Per-spawner colors
- Visual color palette + RGB sliders — **no hex input**
- Enable/disable individual spawner types
- Per-type beam thickness
- Rainbow mode
- Overworld / Nether / End filters
- Spawner list sorted by distance
- Copy nearest spawner coordinates
- Cherry Blossom themed GUI with hover/click effects and opening fade
- German and English translations
- Automatic JSON config at `.minecraft/config/spawnerbeacon.json`
- Second unbound hotkey for quick beam toggle

## Important: Minecraft 26.1.2 toolchain

Minecraft 26.1 requires **JDK 25**. Fabric's 26.1 porting guide also states that Minecraft 26.1 is unobfuscated and that projects should use the official Mojang names instead of Yarn. The old `remapJar` workflow was also changed for 26.1. See the official Fabric documentation:

- https://docs.fabricmc.net/versions/26.1.2/develop/getting-started/setting-up/
- https://docs.fabricmc.net/versions/26.1.2/develop/porting/
- https://docs.fabricmc.net/versions/26.1.2/develop/rendering/world/

## Build on GitHub — easiest method

You do **not** need IntelliJ IDEA for this method.

### 1. Create a GitHub repository

1. Open https://github.com/
2. Create a new repository, for example `SpawnerBeacon`.
3. Public or Private is fine.
4. Create the repository.

### 2. Upload this project's files

Upload the **contents** of this project, not an extra outer `SpawnerBeacon` directory.

You should see these files at the root of your GitHub repository:

```text
build.gradle
gradle.properties
settings.gradle
LICENSE
README.md
.github/workflows/build.yml
src/main/java/...
src/main/resources/...
```

### 3. Start the build

Open:

```text
Actions
→ Build SpawnerBeacon
→ Run workflow
```

A normal push/commit also starts the workflow automatically.

The workflow installs Java 25 and Gradle 9.6.0 and runs:

```text
gradle build --no-daemon
```

### 4. Download the JAR

When the workflow has a green check:

```text
Actions
→ successful Build SpawnerBeacon run
→ Artifacts
→ SpawnerBeacon-1.1.1
```

Download the artifact ZIP and extract it. The file inside is:

```text
spawnerbeacon-1.1.1.jar
```

### 5. Install it in Minecraft

Install:

```text
Minecraft 26.1.2
Fabric Loader 0.19.3 or newer
Fabric API for Minecraft 26.1.2
Java 25
```

Put both Fabric API and the SpawnerBeacon JAR into:

```text
%APPDATA%\.minecraft\mods
```

Start the Fabric 26.1.2 profile.

## Open the GUI

Default key on a German QWERTZ keyboard:

```text
Ü
```

The physical key uses the US GLFW `LEFT_BRACKET` code, so it can be changed later in:

```text
Options → Controls → Key Binds → SpawnerBeacon
```

There is also a second key mapping named **Toggle SpawnerBeacon Beams**, which is unbound by default.

## GUI

The menu has four tabs:

### General

- Master switch
- Beam animation
- Thickness
- Maximum Y
- Opacity
- Render distance
- Distance fade
- Rainbow mode
- Dimension filters

### Colors

Choose a spawner color visually. There is no hex field. The screen provides:

- Cherry/Rose/Crimson/Pink/etc. color swatches
- RGB sliders
- Live color preview

### Spawner Types

Each supported type has:

- individual color
- show/hide switch
- individual thickness

### Spawner List

Known loaded spawners are sorted by distance. The nearest spawner's coordinates can be copied directly to the clipboard.

## Config file

The mod automatically creates:

```text
.minecraft/config/spawnerbeacon.json
```

Do not edit it unless you know what you are doing. The GUI is the intended configuration method.

## If GitHub Actions fails

Open:

```text
Actions
→ red workflow run
→ build
```

Copy the section containing the first:

```text
error:
```

and send it back to the developer/chat. Do not randomly change imports or versions first.

If Minecraft itself crashes after a successful build, send:

```text
.minecraft/logs/latest.log
```

and, if generated:

```text
.minecraft/crash-reports/<latest crash>.txt
```

## Local build (optional)

If you install JDK 25 and Gradle locally, from the project directory run:

```text
gradle build
```

The resulting JAR is placed in:

```text
build/libs/
```

Fabric's official 26.1.2 build guide documents the same output location and recommends the Gradle wrapper for local builds when it is available.

## Compatibility notes

The beam renderer follows Fabric's 26.1 world-rendering extraction/drawing model and uses a custom pipeline with no depth/stencil attachment so the beam can be visible through terrain. This is the same general technique shown in Fabric's official rendering documentation.

Sodium/Iris compatibility is expected to be reasonable because the mod uses the modern Fabric level-rendering API instead of legacy OpenGL calls. Shader packs can still alter the final appearance of translucent geometry; if a shader pack causes a problem, test once with shaders disabled before reporting it.

## Version-specific code

The most version-sensitive parts are:

- `BeamRenderer.java` — 26.1 RenderPipeline/GPU API
- `SpawnerTracker.java` — 26.1 block entity and spawner API
- `ConfigScreen.java` and GUI widgets — 26.1 `GuiGraphicsExtractor`/widget API
- `SpawnerBeaconClient.java` — 26.1 key mapping API
- `build.gradle` / `gradle.properties` — Minecraft, Loom, Fabric API and Java versions

For a future Minecraft update, update the Fabric toolchain first and then fix compile errors before changing gameplay code.
