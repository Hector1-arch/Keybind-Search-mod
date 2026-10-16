package com.example.keybindsearch.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Read-only access to the {@link KeyMapping} owned by a {@code KeyBindsList.KeyEntry}.
 * Used purely to decide whether the row matches the search filter.
 */
@Mixin(KeyBindsList.KeyEntry.class)
public interface KeyBindsListKeyEntryAccessor {
    @Accessor("key")
    KeyMapping keybindsearch$getKeyMapping();
}
