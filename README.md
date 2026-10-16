# Keybind Search

![Keybind Search icon](src/main/resources/assets/keybindsearch/icon.png)

A small, client-side Fabric mod for **Minecraft Java Edition 26.3** that adds two live search
fields to the top of the vanilla **Options → Controls → Key Binds** screen.

- **Activity Search** – filters by the key bind's name, its category and its translation key.
- **Key Search** – filters by the key that is currently assigned to the binding.

Both fields work together with **AND** logic, update as you type, and are drawn in the screen
header so they **never scroll with the list**.

> The mod is **purely presentational**. It only decides which rows are visible. It never changes,
> replaces, unregisters or resets a key mapping, and it does not interfere with reassigning keys,
> conflict warnings, "Reset" / "Reset All", scrolling, or the Done/Back buttons.

## Features

- Two independent search fields in the header of the key binds screen.
- Activity Search matches:
  - the localised action name (e.g. `Jump`, `Open Inventory`),
  - the localised category label (e.g. `Movement`),
  - the translation key (e.g. `key.jump`),
  - the category identifier (e.g. `minecraft:movement`).
- Key Search matches:
  - the key name currently displayed next to the action (e.g. `Space`, `Left Shift`, `Esc`, `Mouse 4`),
  - the key identifier (e.g. `key.keyboard.space`, `key.mouse.4`),
  - common aliases (`spacebar`, `leftshift`, `lshift`, `esc`, `mouse4`, …),
  - common spellings for unbound keys (`unbound`, `none`, `not bound`, …).
- Matching is case-insensitive and ignores spaces/`._-`, so `left shift`, `leftshift` and
  `lshift` all work.
- Empty fields filter nothing. When a filter hides everything, `No keybinds found` is shown in
  the list area.
- Each field gets a `×` clear button that only appears while the field contains text.
- The header layout adapts to narrow windows.
- Localised in English (`en_us`) and German (`de_de`). No hard-coded UI text.

## Requirements

| Component        | Version        |
| ---------------- | -------------- |
| Minecraft        | 26.3           |
| Fabric Loader    | 0.19.5 or newer (for running) |
| Java             | 25             |
| Fabric API       | **not required** |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) 0.19.5 or newer for Minecraft 26.3.
2. Drop `keybindsearch-1.0.0.jar` into your `.minecraft/mods` folder.
3. Start the game and open **Options → Controls → Key Binds**.

Fabric API is *not* required and is not declared as a dependency.

## Usage

- Type in **Activity Search** to narrow the list down by action/category.
- Type in **Key Search** to narrow the list down by assigned key.
- Combine both to intersect the two filters.
- Click the `×` next to a field (or clear the text) to remove that filter.
- Reassign keys, press **Reset**, or use **Reset All** as usual — the list re-applies the current
  filter afterwards, and your searches stay active.

Example: type `movement` into Activity Search and `shift` into Key Search to see every movement
binding that uses a Shift key.

## Building from source

The project uses the [Fabric Loom](https://fabricmc.net/develop/) Gradle plugin and the Gradle
wrapper (Gradle 9.7.1).

```bat
:: from the project root, with a JDK 25 available
set JAVA_HOME=C:\path\to\jdk-25
gradlew.bat build
```

The build output is written to:

```
build/libs/keybindsearch-1.0.0.jar
```

### Running a development client

```bat
set JAVA_HOME=C:\path\to\jdk-25
gradlew.bat runClient
```

## Technical notes

- Mod id `keybindsearch`, package `com.example.keybindsearch`, version `1.0.0`.
- Client-only (`"environment": "client"`), no entrypoints — the mod is implemented entirely with
  Mixins.
- Minecraft 26.3 is **unobfuscated**, so no refmap is needed and no mappings are applied.
- Mixin config: `keybindsearch.mixins.json` (`compatibilityLevel: JAVA_25`).

### How it works

| Class | Purpose |
| ----- | ------- |
| `KeyBindsScreenMixin` | Grows the header, moves the vanilla title, creates the two `EditBox`es and their clear buttons, positions everything manually after the vanilla layout pass, and draws `No keybinds found` when nothing matches. |
| `KeyBindsListMixin` | Takes an untouched snapshot of every row in the constructor, then swaps the visible rows for a filtered subset. Category headers are only shown when at least one of their rows is visible. |
| `KeyBindsListKeyEntryAccessor` | Read-only accessor for the private `KeyMapping` stored in a `KeyBindsList.KeyEntry`, used purely for matching. |
| `KeybindFilter` | Holds the two normalised queries and the matching logic (no side effects). |
| `KeybindSearchList` | Interface implemented on `KeyBindsList` so the screen can ask the list to re-filter. |

Because the original row objects (and therefore the vanilla buttons and `KeyMapping`s) are reused
verbatim, all vanilla behaviour keeps working.

## Limitations

- The search fields live in the screen header, which is only a few dozen pixels tall; on extremely
  narrow windows the two fields are clamped to a minimum width and may overflow slightly.
- Placeholders (`Search actions...` / `Search keys...`) use the vanilla `EditBox` hint behaviour,
  so they disappear while the field is focused.
- Filtering is applied when the screen is initialised or when a search field changes, and is
  re-applied after a key is reassigned / reset.

## License

MIT. See `LICENSE`.
