package com.github.matei;

/**
 * Represents terminal text style flags using a bitmask.
 * <p>
 * Each style is a single bit in an integer, allowing efficient
 * combination and checking of multiple styles.
 * <p>
 * Usage:
 * <pre>
 *     int styles = TextStyle.BOLD | TextStyle.UNDERLINE;
 *     boolean isBold = TextStyle.has(styles, TextStyle.BOLD); // true
 *     boolean isItalic = TextStyle.has(styles, TextStyle.ITALIC); // false
 * </pre>
 */
public final class TextStyle {
    public static final int NONE = 0;
    public static final int BOLD = 1; // bit 0
    public static final int ITALIC = 1 << 1; // bit 1, value 2
    public static final int UNDERLINE = 1 << 2; // bit 2, value 4

    public TextStyle() { }

    /**
     * Checks whether the given style flag is set in the bitmask.
     *
     * @param styles the combined style bitmask
     * @param flag   the individual style flag to check
     * @return true if the flag is set
     */
    public static boolean has(int styles, int flag) {
         /*
            0101 (BOLD + UNDERLINE)
          & 0001 (BOLD)
          --------------------------
            0001 (r = 1 != 0 => true)
         */
        return (styles & flag) != 0;
    }


    /**
     * Returns a human-readable representation of the style bitmask.
     *
     * @param styles the combined style bitmask
     * @return a string like "BOLD|UNDERLINE" or "NONE"
     */
    public static String toString(int styles) {
        if (styles == NONE) return "NONE";

        StringBuilder sb = new StringBuilder();

        if (has(styles, BOLD)) sb.append("BOLD");

        if (has(styles, ITALIC)) {
            if (!sb.isEmpty()) sb.append("|");
            sb.append("ITALIC");
        }

        if (has(styles, UNDERLINE)) {
            if (!sb.isEmpty()) sb.append("|");
            sb.append("UNDERLINE");
        }

        return sb.toString();
    }
}
