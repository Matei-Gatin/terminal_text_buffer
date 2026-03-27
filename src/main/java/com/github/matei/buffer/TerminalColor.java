package com.github.matei.buffer;

/**
 * Standard terminal colors.
 * <p>
 * Includes the 16 ANSI colors (8 normal + 8 bright) and a
 * {@link #DEFAULT} value representing the terminal's default color.
 */
public enum TerminalColor {
    DEFAULT(-1),

    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),

    BRIGHT_BLACK(8),
    BRIGHT_RED(9),
    BRIGHT_GREEN(10),
    BRIGHT_YELLOW(11),
    BRIGHT_BLUE(12),
    BRIGHT_MAGENTA(13),
    BRIGHT_CYAN(14),
    BRIGHT_WHITE(15);

    private final int index;

    TerminalColor(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public String getForegroundCode() {
        if (this == DEFAULT) return "\u001B[39m";
        if (index < 8) return "\u001B[3" + index + "m";
        return "\u001B[9" + (index - 8) + "m";
    }

    public String getBackgroundCode() {
        if (this == DEFAULT) return "\u001B[49m";
        if (index < 8) return "\u001B[4" + index + "m";
        return "\u001B[10" + (index - 8) + "m";
    }

    public static TerminalColor fromIndex(int index)      {
        for (TerminalColor color : values()) {
            if (color.index == index) {
                return color;
            }
        }

        return DEFAULT;
    }
}
