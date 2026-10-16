package com.example.keybindsearch.mixin;

import com.example.keybindsearch.KeybindFilter;
import com.example.keybindsearch.KeybindSearchList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Presentation-only filtering for the vanilla key binds list.
 *
 * <p>We keep an untouched snapshot of every row created by the constructor and then swap the
 * list's children for a filtered subset whenever a search field changes. The vanilla
 * {@code Entry} objects (and therefore the {@code KeyMapping} instances and their buttons) are
 * reused as-is, so reassigning keys, collision warnings, per-row reset and scrolling all keep
 * working exactly like vanilla.</p>
 */
@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin implements KeybindSearchList {
    @Unique
    private List<KeyBindsList.Entry> keybindsearch$allEntries = List.of();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void keybindsearch$captureEntries(
            final KeyBindsScreen keyBindsScreen, final Minecraft minecraft, final CallbackInfo callbackInfo) {
        this.keybindsearch$allEntries = new ArrayList<>(this.keybindsearch$self().children());
    }

    @Inject(method = "resetMappingAndUpdateButtons", at = @At("TAIL"))
    private void keybindsearch$reapplyFilterAfterReset(final CallbackInfo callbackInfo) {
        this.keybindsearch$applyFilter(false);
    }

    @Override
    public void keybindsearch$applyFilter(final boolean resetScroll) {
        final KeyBindsList list = this.keybindsearch$self();
        final List<KeyBindsList.Entry> allEntries = this.keybindsearch$allEntries;
        final List<KeyBindsList.Entry> visible = new ArrayList<>();

        for (int index = 0; index < allEntries.size(); index++) {
            final KeyBindsList.Entry entry = allEntries.get(index);

            if (!(entry instanceof KeyBindsList.CategoryEntry)) {
                // Defensive: a row that is not under a category header is kept on its own merit.
                if (this.keybindsearch$entryMatches(entry)) {
                    visible.add(entry);
                }
                continue;
            }

            // A category header is immediately followed by its key rows. Show the header only
            // when at least one of those rows matches the current filter.
            final List<KeyBindsList.Entry> matchingRows = new ArrayList<>();
            int cursor = index + 1;
            while (cursor < allEntries.size() && allEntries.get(cursor) instanceof KeyBindsList.KeyEntry) {
                final KeyBindsList.Entry row = allEntries.get(cursor);
                if (this.keybindsearch$entryMatches(row)) {
                    matchingRows.add(row);
                }
                cursor++;
            }

            if (!matchingRows.isEmpty()) {
                visible.add(entry);
                visible.addAll(matchingRows);
            }

            index = cursor - 1;
        }

        list.replaceEntries(visible);
        if (resetScroll) {
            list.setScrollAmount(0.0);
        } else {
            // Keep the current scroll position, clamped to the (possibly smaller) list.
            list.refreshScrollAmount();
        }

        list.refreshEntries();
    }

    @Override
    public boolean keybindsearch$hasVisibleEntries() {
        for (final KeyBindsList.Entry entry : this.keybindsearch$allEntries) {
            if (this.keybindsearch$entryMatches(entry)) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private boolean keybindsearch$entryMatches(final KeyBindsList.Entry entry) {
        if (entry instanceof KeyBindsList.KeyEntry keyEntry) {
            return KeybindFilter.matches(((KeyBindsListKeyEntryAccessor) keyEntry).keybindsearch$getKeyMapping());
        }

        return true;
    }

    @Unique
    private KeyBindsList keybindsearch$self() {
        return (KeyBindsList) (Object) this;
    }
}
