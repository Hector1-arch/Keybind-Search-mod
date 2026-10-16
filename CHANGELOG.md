# Changelog

## 1.0.0 — 2026-10-16

Initial release.

### Added

- **Activity Search** field at the top of the vanilla **Options → Controls → Key Binds**
  screen — filters by action name, category label, translation key
  (`key.jump`) and category identifier (`minecraft:movement`).
- **Key Search** field — filters by the displayed key name (`Space`, `Left Shift`,
  `Esc`, `Mouse 4`), the key identifier (`key.keyboard.space`, `key.mouse.4`),
  common aliases (`spacebar`, `leftshift`, `lshift`, `esc`, `mouse4`, …) and
  unbound spellings (`unbound`, `none`, `not bound`, …).
- **AND logic** between the two fields; empty fields filter nothing.
- Case-insensitive matching that ignores spaces / `.` / `_` / `-`, so
  `left shift`, `leftshift` and `lshift` all match.
- Category headers are hidden when none of their key binds match.
- **`No keybinds found`** message drawn in the list area when nothing matches.
- `×` clear button per search field, visible only while the field has text.
- Search fields are placed in the screen header and **do not scroll** with the list;
  layout adapts to narrow windows.
- Localisation: **English** (`en_us`) and **German** (`de_de`), no hard-coded UI strings.
- Mod icon (`assets/keybindsearch/icon.png`).

### Notes

- **Purely presentational**: key mappings are never changed, replaced, unregistered
  or reset. Key reassignment, conflict warnings, Reset / Reset All, scrolling and the
  Done/Back buttons keep their vanilla behaviour.
- **Client-side only** (`"environment": "client"`), implemented with 3 mixins and
  **no entrypoints**.
- **No dependencies** — Fabric API is not required. Needs Fabric Loader 0.19.5+ and
  Minecraft 26.3 (Java 25).
- License: MIT.
