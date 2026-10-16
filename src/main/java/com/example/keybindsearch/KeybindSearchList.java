package com.example.keybindsearch;

/**
 * Implemented on {@code KeyBindsList} by {@code KeyBindsListMixin}.
 *
 * <p>This is purely a presentation-layer contract: it lets the search widgets ask the
 * vanilla key binds list to re-render itself using the currently active filter. It never
 * touches {@link net.minecraft.client.KeyMapping} state.</p>
 */
public interface KeybindSearchList {
    /**
     * Rebuilds the visible rows from the cached, unfiltered snapshot of all rows.
     *
     * @param resetScroll {@code true} when the search input changed (jump back to the top),
     *                    {@code false} when only re-validating after a key change (keep the
     *                    current scroll position, clamped to the new bounds)
     */
    void keybindsearch$applyFilter(boolean resetScroll);

    /** @return {@code true} if at least one key bind row is currently visible. */
    boolean keybindsearch$hasVisibleEntries();
}
