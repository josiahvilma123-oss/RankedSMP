package com.blossomsmp.ranks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {

    private static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T"};

    private Text() {
    }

    /** "&#FF69B4Hello" -> coloured component */
    public static Component component(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return AMPERSAND.deserialize(text);
    }

    /** "&#FF69B4Hello" -> "§x§F§F..." text that the TAB scoreboard understands */
    public static String section(String text) {
        return SECTION.serialize(component(text));
    }

    /** 2500 -> $2.5K */
    public static String money(double amount) {
        double value = Math.abs(amount);
        int i = 0;
        while (value >= 1000 && i < SUFFIXES.length - 1) {
            value /= 1000;
            i++;
        }
        String number = (value == Math.floor(value)) ? String.valueOf((long) value)
                : String.format(java.util.Locale.ROOT, "%.1f", value);
        return (amount < 0 ? "-$" : "$") + number + SUFFIXES[i];
    }
}
