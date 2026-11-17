<p align="center">
  <img src="docs/logo.png" alt="Blåhaj Reborn" width="256">
</p>

<h1 align="center">Blåhaj Reborn</h1>

<p align="center">
  A squishy Blåhaj for Minecraft. Hold it, wear it on your head, place it, squish it.<br>
  <b>Do not squish the other one.</b>
</p>

<p align="center">
  <a href="https://github.com/iSweat-exe/Blahaj-Reborn/actions/workflows/build.yml"><img alt="Build" src="https://github.com/iSweat-exe/Blahaj-Reborn/actions/workflows/build.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="License: MIT" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
  <img alt="Minecraft 26.3" src="https://img.shields.io/badge/minecraft-26.3-62b47a.svg">
  <img alt="Fabric" src="https://img.shields.io/badge/fabric-supported-dbd0b4.svg">
  <img alt="NeoForge" src="https://img.shields.io/badge/neoforge-supported-f4802a.svg">
  <img alt="Forge" src="https://img.shields.io/badge/forge-supported-5a6a89.svg">
</p>

---

### What is this?

Blåhaj Reborn adds the beloved shark plush to Minecraft, modelled and animated in Blockbench and rendered with
[GeckoLib](https://github.com/bernie-g/geckolib).

- **Blåhaj** – hold it, wear it on your head, or place it on the ground. Right-click a placed one to squish it,
  with a squish animation and a squeaky sound whose pitch changes every time.
- **A hitbox that fits the model** – the selection box follows the shark instead of a full cube, and rotates with it.
- **Creative tab** – every item of the mod lives in the *Blåhaj Reborn* tab.
- **Something else** – there is a second Blåhaj. Its name is hard to read. It comes with a warning.

<p align="center">
  <img src="docs/screenshots/placed.webp" alt="A Blåhaj placed on the ground" width="49%">
  <img src="docs/screenshots/side.webp" alt="A Blåhaj seen from the side" width="49%">
</p>

<p align="center">
  <img src="docs/screenshots/head-front.webp" alt="Wearing a Blåhaj on the head, front" width="32%">
  <img src="docs/screenshots/head-angle.webp" alt="Wearing a Blåhaj on the head, angle" width="32%">
  <img src="docs/screenshots/head-back.webp" alt="Wearing a Blåhaj on the head, back" width="32%">
</p>

<p align="center"><sub>Screenshots taken with Complementary Unbound shaders.</sub></p>

### Recipes

| Item | Recipe |
|---|---|
| Blåhaj | 3 light blue wool, 1 white wool, 1 pink dye, 1 string |

> **Spoilers ahead.** The second Blåhaj is best discovered in game. Open the section below only if you want to know.

<details>
<summary><b>The other Blåhaj (spoilers)</b></summary>

<br>

**Recipe:** 4 diamonds, 4 eyes of ender, 1 Blåhaj.

Place it and squish it. Nothing happens for three seconds. Then it rises about ten blocks into the air and opens a
black hole.

**What the black hole does**

- It pulls in every entity except players, shrinks the creatures it swallows and destroys them in the end.
- It rips blocks out of the ground and drags them into its orbit.
- After about ten seconds of growth it collapses, then explodes: swallowed blocks are flung away, everything it
  swallowed is destroyed, and a scorched crater is left behind. Players are pushed back but not hurt.
- It is single use. The screen glitches, distorts and shakes the whole time (shaders, camera shake, field of view and
  a darkened sky), and there is an advancement for those who witness it.

<p align="center">
  <img src="docs/screenshots/void-hole.webp" alt="The black hole" width="49%">
  <img src="docs/screenshots/void-crater.webp" alt="The crater left behind" width="49%">
</p>

</details>

### Known issues

- **The Blåhaj is not positioned correctly in the player's hand.** When it is held, the arm is raised but the plush
  does not sit perfectly in it yet (third person), and the first-person view has not been adjusted. This will be fixed
  in the next update. See the `FIXME` in `HumanoidModelMixin.java` and the `thirdperson_*` / `firstperson_*` entries
  in `models/item/blahaj.json`.

Found something else? Please [open an issue](https://github.com/iSweat-exe/Blahaj-Reborn/issues).

### Supported versions

| Minecraft | Loader | Loader version | Requires |
|---|---|---|---|
| 26.3 | Fabric | 0.19+ | Fabric API, GeckoLib 5.5.7+ |
| 26.3 | NeoForge | 26.3.0.110+ | GeckoLib 5.5.7+ |
| 26.3 | Forge | 66+ | GeckoLib 5.5.7+ |

### Installation

1. Install a supported mod loader for Minecraft 26.3.
2. Download [GeckoLib](https://modrinth.com/mod/geckolib) for your loader (and [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric).
3. Download the Blåhaj Reborn jar for your loader from the
   [Releases](https://github.com/iSweat-exe/Blahaj-Reborn/releases) page and drop it into your `mods` folder.

### Compatibility

- **Shader packs** – tested with Iris 1.11.7, Sodium 0.9.2, Complementary Reimagined and Complementary Unbound.
  All models and effects render correctly; the sky darkening of the special event is replaced by an on-screen veil
  because shader packs control the sky.
- **Other mods** – no known conflicts. Mixins are only used for client visuals (camera, sky, fog, arm pose).

### Building from source

You need **JDK 25**. The project is a multi-module Gradle build:

| Module | Contents |
|---|---|
| `common` | All mod content: classes, assets, data, mixins. No loader code. |
| `fabric` | Fabric entrypoints and registration |
| `neoforge` | NeoForge entrypoints and registration |
| `forge` | Forge entrypoints and registration |

```bash
git clone https://github.com/iSweat-exe/Blahaj-Reborn.git
cd Blahaj-Reborn
./gradlew build
```

The jars end up in `<loader>/build/libs/`. To build a single loader, run `./gradlew :fabric:build` (or `:neoforge`,
`:forge`).

Run the game from your workspace:

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
./gradlew :forge:runClient
```

Fabric also has an automated client test that boots a world and takes screenshots into
`fabric/build/run/clientGameTest/screenshots`:

```bash
./gradlew :fabric:runClientGameTest
./gradlew :fabric:runClientGameTest -Pshaders=true   # with Iris, Sodium and a shader pack
```

For the shader run, put a shader pack zip in `fabric/run-shaderpacks/` first.

### Contributing

Issues and pull requests are welcome. Please keep new code loader independent (put it in `common`) and register new
content through `BlahajRegistry`. The CI builds all three loaders for every pull request.

### Credits

- [GeckoLib](https://github.com/bernie-g/geckolib) by Tslat and contributors.
- Sound effects: see [CREDITS.md](CREDITS.md).
- Blåhaj is a plush designed by IKEA. This is an unofficial fan project and is not affiliated with IKEA.

### License

Blåhaj Reborn is released under the [MIT License](LICENSE).
Sound effects keep their own licenses, listed in [CREDITS.md](CREDITS.md).
