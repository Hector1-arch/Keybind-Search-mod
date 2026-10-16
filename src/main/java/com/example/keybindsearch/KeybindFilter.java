package com.example.keybindsearch;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;

/**
 * Pure presentation-layer key bind matching.
 *
 * <p>Holds the two independent search queries (activity and key) and decides whether a given
 * {@link KeyMapping} should be visible. Nothing in here mutates key mappings; it only reads
 * their translated name, category and currently assigned key.</p>
 *
 * <p>All queries are normalised once on input so matching is case-insensitive, ignores spaces
 * and separators, and therefore accepts loose input such as {@code left shift}, {@code leftshift}
 * or {@code lshift}.</p>
 */
public final class KeybindFilter {
    private static String activityQuery = "";
    private static String keyQuery = "";

    /** Extra spellings per key identifier ({@link KeyMapping#saveString()}). */
    private static final Map<String, List<String>> KEY_ALIASES = Map.ofEntries(
            Map.entry("key.keyboard.space", List.of("spacebar", "space bar")),
            Map.entry("key.keyboard.escape", List.of("esc")),
            Map.entry("key.keyboard.enter", List.of("return", "cr")),
            Map.entry("key.keyboard.backspace", List.of("back space", "bksp")),
            Map.entry("key.keyboard.tab", List.of("tabulator")),
            Map.entry("key.keyboard.left.shift", List.of("lshift", "shift left")),
            Map.entry("key.keyboard.right.shift", List.of("rshift", "shift right")),
            Map.entry("key.keyboard.left.control", List.of("lctrl", "left ctrl")),
            Map.entry("key.keyboard.right.control", List.of("rctrl", "right ctrl")),
            Map.entry("key.keyboard.left.alt", List.of("lalt", "left alt")),
            Map.entry("key.keyboard.right.alt", List.of("ralt", "right alt")),
            Map.entry("key.keyboard.left.win", List.of("lwin", "super", "meta")),
            Map.entry("key.keyboard.right.win", List.of("rwin")),
            Map.entry("key.keyboard.caps.lock", List.of("caps", "capslock")),
            Map.entry("key.keyboard.num.lock", List.of("numlock", "numpad lock")),
            Map.entry("key.keyboard.scroll.lock", List.of("scrolllock")),
            Map.entry("key.keyboard.print.screen", List.of("prtsc", "printscreen", "print scr")),
            Map.entry("key.keyboard.grave.accent", List.of("grave", "backtick", "tilde")),
            Map.entry("key.keyboard.equals", List.of("equal")),
            Map.entry("key.keyboard.left.bracket", List.of("lbracket", "open bracket")),
            Map.entry("key.keyboard.right.bracket", List.of("rbracket", "close bracket")),
            Map.entry("key.keyboard.apostrophe", List.of("quote")),
            Map.entry("key.keyboard.semicolon", List.of("semi")),
            Map.entry("key.keyboard.slash", List.of("forward slash")),
            Map.entry("key.keyboard.minus", List.of("dash", "hyphen")),
            Map.entry("key.mouse.left", List.of("lmb", "mouse1", "left mouse")),
            Map.entry("key.mouse.right", List.of("rmb", "right mouse")),
            Map.entry("key.mouse.middle", List.of("mmb", "middle mouse")),
            Map.entry("key.mouse.4", List.of("mouse4", "m4")),
            Map.entry("key.mouse.5", List.of("mouse5", "m5")),
            Map.entry("key.mouse.6", List.of("mouse6")),
            Map.entry("key.mouse.7", List.of("mouse7")),
            Map.entry("key.mouse.8", List.of("mouse8"))
    );

    /** Spellings that should match a key that has no key assigned. */
    private static final List<String> UNBOUND_ALIASES = List.of("unbound", "none", "notbound", "unassigned", "unknown");

    private KeybindFilter() {
    }

    /** Clears both queries. Called when the controls screen is (re)initialised. */
    public static void reset() {
        activityQuery = "";
        keyQuery = "";
    }

    public static void setActivityQuery(final String rawQuery) {
        activityQuery = normalize(rawQuery);
    }

    public static void setKeyQuery(final String rawQuery) {
        keyQuery = normalize(rawQuery);
    }

    /** AND of both fields; an empty field matches everything. */
    public static boolean matches(final KeyMapping keyMapping) {
        return matchesActivity(keyMapping) && matchesKey(keyMapping);
    }

    private static boolean matchesActivity(final KeyMapping keyMapping) {
        if (activityQuery.isEmpty()) {
            return true;
        }

        if (contains(normalize(I18n.get(keyMapping.getName())), activityQuery)) {
            return true;
        }
        // Raw translation key, e.g. "key.jump".
        if (contains(normalize(keyMapping.getName()), activityQuery)) {
            return true;
        }
        // Localised category label, e.g. "Movement".
        if (contains(normalize(keyMapping.getCategory().label().getString()), activityQuery)) {
            return true;
        }
        // Category identifier, e.g. "minecraft:movement".
        return contains(normalize(keyMapping.getCategory().id().toString()), activityQuery);
    }

    private static boolean matchesKey(final KeyMapping keyMapping) {
        if (keyQuery.isEmpty()) {
            return true;
        }

        // Currently displayed key name, e.g. "Space", "Left Shift", "Esc", "Mouse 4".
        if (contains(normalize(keyMapping.getTranslatedKeyMessage().getString()), keyQuery)) {
            return true;
        }
        // Key identifier, e.g. "key.keyboard.space" or "key.mouse.4".
        if (contains(normalize(keyMapping.saveString()), keyQuery)) {
            return true;
        }

        for (final String alias : KEY_ALIASES.getOrDefault(keyMapping.saveString(), List.of())) {
            if (contains(normalize(alias), keyQuery)) {
                return true;
            }
        }

        if (keyMapping.isUnbound()) {
            for (final String alias : UNBOUND_ALIASES) {
                if (contains(normalize(alias), keyQuery)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean contains(final String haystack, final String needle) {
        return haystack.contains(needle);
    }

    /**
     * Lower-cases and strips whitespace plus {@code . _ -} so that loose input matches.
     * Anything else (letters, digits, symbols such as {@code =} or {@code /}) is preserved,
     * which keeps symbol-only queries meaningful.
     */
    private static String normalize(final String text) {
        final StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            final char character = text.charAt(i);
            if (character == ' ' || character == '.' || character == '_' || character == '-') {
                continue;
            }

            builder.append(Character.toLowerCase(character));
        }

        return builder.toString().toLowerCase(Locale.ROOT);
    }
}
