package com.example.keybindsearch.mixin;

import com.example.keybindsearch.KeybindFilter;
import com.example.keybindsearch.KeybindSearchList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the two live search fields ("Activity Search" and "Key Search") to the top of the vanilla
 * Controls / Key Binds screen and keeps them out of the scrolling list.
 *
 * <p>The header is grown to {@value #KEYBINDSEARCH_HEADER_HEIGHT} pixels, the vanilla title is
 * moved to the top of the header and the two fields plus their clear buttons are placed on a
 * second header row that mirrors the width of the key bind list.</p>
 *
 * <p>This mixin is intentionally not extending the target's superclass: only public members of the
 * target hierarchy are used, which keeps the mixin simple and avoids constructor merging.</p>
 */
@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin {
    private static final int KEYBINDSEARCH_HEADER_HEIGHT = 46;
    private static final int KEYBINDSEARCH_TITLE_Y = 6;
    private static final int KEYBINDSEARCH_ROW_Y = 20;
    private static final int KEYBINDSEARCH_ROW_HEIGHT = 20;
    private static final int KEYBINDSEARCH_ROW_WIDTH = 340;
    private static final int KEYBINDSEARCH_SIDE_MARGIN = 40;
    private static final int KEYBINDSEARCH_MIN_TOTAL_WIDTH = 120;
    private static final int KEYBINDSEARCH_MIN_FIELD_WIDTH = 24;
    private static final int KEYBINDSEARCH_CLEAR_WIDTH = 20;
    private static final int KEYBINDSEARCH_CLEAR_GAP = 2;
    private static final int KEYBINDSEARCH_GROUP_GAP = 8;
    private static final float KEYBINDSEARCH_ACTIVITY_FRACTION = 0.6F;
    private static final int KEYBINDSEARCH_NO_RESULTS_COLOR = 0xFF808080;

    @Shadow
    private KeyBindsList keyBindsList;

    @Unique
    private EditBox keybindsearch$activityBox;
    @Unique
    private EditBox keybindsearch$keyBox;
    @Unique
    private Button keybindsearch$activityClear;
    @Unique
    private Button keybindsearch$keyClear;
    @Unique
    private StringWidget keybindsearch$titleWidget;

    @Inject(method = "addContents", at = @At("HEAD"))
    private void keybindsearch$addSearchFields(final CallbackInfo callbackInfo) {
        KeybindFilter.reset();

        final Font font = Minecraft.getInstance().font;
        final HeaderAndFooterLayout layout = this.keybindsearch$layout();
        layout.setHeaderHeight(KEYBINDSEARCH_HEADER_HEIGHT);

        // The vanilla title widget was already added to the header by addTitle(); remember it so
        // it can be moved to the top of the enlarged header.
        layout.visitChildren(element -> {
            if (element instanceof StringWidget stringWidget) {
                this.keybindsearch$titleWidget = stringWidget;
            }
        });

        final EditBox activityBox = new EditBox(
                font, 0, 0, KEYBINDSEARCH_MIN_TOTAL_WIDTH, KEYBINDSEARCH_ROW_HEIGHT, Component.translatable("keybindsearch.field.activity"));
        activityBox.setHint(Component.translatable("keybindsearch.field.activity.hint"));
        activityBox.setMaxLength(64);
        activityBox.setResponder(value -> {
            KeybindFilter.setActivityQuery(value);
            this.keybindsearch$applyFilter();
            this.keybindsearch$updateClearButtons();
        });
        this.keybindsearch$activityBox = activityBox;

        final EditBox keyBox = new EditBox(
                font, 0, 0, KEYBINDSEARCH_MIN_TOTAL_WIDTH, KEYBINDSEARCH_ROW_HEIGHT, Component.translatable("keybindsearch.field.key"));
        keyBox.setHint(Component.translatable("keybindsearch.field.key.hint"));
        keyBox.setMaxLength(64);
        keyBox.setResponder(value -> {
            KeybindFilter.setKeyQuery(value);
            this.keybindsearch$applyFilter();
            this.keybindsearch$updateClearButtons();
        });
        this.keybindsearch$keyBox = keyBox;

        final Button activityClear = Button.builder(
                        Component.translatable("keybindsearch.button.clear"), button -> this.keybindsearch$activityBox.setValue(""))
                .bounds(0, 0, KEYBINDSEARCH_CLEAR_WIDTH, KEYBINDSEARCH_ROW_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("keybindsearch.button.clear.activity")))
                .build();
        this.keybindsearch$activityClear = activityClear;

        final Button keyClear = Button.builder(
                        Component.translatable("keybindsearch.button.clear"), button -> this.keybindsearch$keyBox.setValue(""))
                .bounds(0, 0, KEYBINDSEARCH_CLEAR_WIDTH, KEYBINDSEARCH_ROW_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("keybindsearch.button.clear.key")))
                .build();
        this.keybindsearch$keyClear = keyClear;

        // Header children are registered as renderable widgets by OptionsSubScreen#init through
        // HeaderAndFooterLayout#visitWidgets, so no manual widget registration is required.
        layout.addToHeader(activityBox);
        layout.addToHeader(activityClear);
        layout.addToHeader(keyBox);
        layout.addToHeader(keyClear);

        this.keybindsearch$updateClearButtons();
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void keybindsearch$repositionSearchFields(final CallbackInfo callbackInfo) {
        final EditBox activityBox = this.keybindsearch$activityBox;
        final EditBox keyBox = this.keybindsearch$keyBox;
        final Button activityClear = this.keybindsearch$activityClear;
        final Button keyClear = this.keybindsearch$keyClear;

        if (activityBox == null || keyBox == null || activityClear == null || keyClear == null) {
            return;
        }

        final int width = this.keybindsearch$screen().width;
        final int totalWidth = Math.min(KEYBINDSEARCH_ROW_WIDTH, Math.max(KEYBINDSEARCH_MIN_TOTAL_WIDTH, width - KEYBINDSEARCH_SIDE_MARGIN));
        final int left = (width - totalWidth) / 2;

        final int groupTotal = totalWidth - KEYBINDSEARCH_GROUP_GAP;
        final int leftGroup = Math.round(groupTotal * KEYBINDSEARCH_ACTIVITY_FRACTION);
        final int rightGroup = groupTotal - leftGroup;

        final int activityWidth = Math.max(
                KEYBINDSEARCH_MIN_FIELD_WIDTH, leftGroup - KEYBINDSEARCH_CLEAR_WIDTH - KEYBINDSEARCH_CLEAR_GAP);
        final int keyWidth = Math.max(
                KEYBINDSEARCH_MIN_FIELD_WIDTH, rightGroup - KEYBINDSEARCH_CLEAR_WIDTH - KEYBINDSEARCH_CLEAR_GAP);

        activityBox.setX(left);
        activityBox.setY(KEYBINDSEARCH_ROW_Y);
        activityBox.setWidth(activityWidth);
        activityClear.setX(left + activityWidth + KEYBINDSEARCH_CLEAR_GAP);
        activityClear.setY(KEYBINDSEARCH_ROW_Y);

        final int keyX = left + leftGroup + KEYBINDSEARCH_GROUP_GAP;
        keyBox.setX(keyX);
        keyBox.setY(KEYBINDSEARCH_ROW_Y);
        keyBox.setWidth(keyWidth);
        keyClear.setX(keyX + keyWidth + KEYBINDSEARCH_CLEAR_GAP);
        keyClear.setY(KEYBINDSEARCH_ROW_Y);

        final StringWidget titleWidget = this.keybindsearch$titleWidget;
        if (titleWidget != null) {
            titleWidget.setX((width - titleWidget.getWidth()) / 2);
            titleWidget.setY(KEYBINDSEARCH_TITLE_Y);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void keybindsearch$renderNoResults(
            final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick, final CallbackInfo callbackInfo) {
        final KeyBindsList list = this.keyBindsList;
        if (list == null || ((KeybindSearchList) list).keybindsearch$hasVisibleEntries()) {
            return;
        }

        final Component message = Component.translatable("keybindsearch.noResults");
        final int centerX = list.getX() + list.getWidth() / 2;
        final int centerY = list.getY() + list.getHeight() / 2 - 4;
        graphics.centeredText(Minecraft.getInstance().font, message, centerX, centerY, KEYBINDSEARCH_NO_RESULTS_COLOR);
    }

    @Unique
    private void keybindsearch$applyFilter() {
        if (this.keyBindsList != null) {
            ((KeybindSearchList) this.keyBindsList).keybindsearch$applyFilter(true);
        }
    }

    @Unique
    private void keybindsearch$updateClearButtons() {
        if (this.keybindsearch$activityClear != null && this.keybindsearch$activityBox != null) {
            this.keybindsearch$activityClear.visible = !this.keybindsearch$activityBox.getValue().isEmpty();
        }

        if (this.keybindsearch$keyClear != null && this.keybindsearch$keyBox != null) {
            this.keybindsearch$keyClear.visible = !this.keybindsearch$keyBox.getValue().isEmpty();
        }
    }

    @Unique
    private Screen keybindsearch$screen() {
        return (Screen) (Object) this;
    }

    @Unique
    private HeaderAndFooterLayout keybindsearch$layout() {
        return ((OptionsSubScreen) (Object) this).layout;
    }
}
