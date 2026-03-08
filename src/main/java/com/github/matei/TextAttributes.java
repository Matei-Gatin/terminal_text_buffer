package com.github.matei;

import java.util.Objects;

/**
 * Immutable set of text attributes for a terminal cell.
 * <p>
 * Combines foreground color, background color, and style flags.
 * Instances are immutable so they can be safely shared across many cells.
 */
public final class TextAttributes {
    // Default attributes: default colors, no styles
    public static final TextAttributes DEFAULT =
            new TextAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, TextStyle.NONE);

    private final TerminalColor foreground;
    private final TerminalColor background;
    private final int styles;

    /**
     * Creates a new set of text attributes.
     *
     * @param foreground the foreground color
     * @param background the background color
     * @param styles     combined style bitmask (see {@link TextStyle})
     */
    public TextAttributes(TerminalColor foreground, TerminalColor background, int styles) {
        this.foreground = Objects.requireNonNull(foreground, "foreground must not be null");
        this.background = Objects.requireNonNull(background, "background must not be null");
        this.styles = styles;
    }

    public TerminalColor getForeground() {
        return foreground;
    }

    public TerminalColor getBackground() {
        return background;
    }

    public int getStyles() {
        return styles;
    }

    public boolean isBold() {
        return TextStyle.has(this.styles, TextStyle.BOLD);
    }

    public boolean isItalic() {
        return TextStyle.has(this.styles, TextStyle.ITALIC);
    }

    public boolean isUnderline() {
        return TextStyle.has(this.styles, TextStyle.UNDERLINE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextAttributes that)) return false;
        return Objects.equals(this.styles, that.styles)
                && Objects.equals(this.foreground, that.foreground)
                && Objects.equals(this.background, that.background);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, styles);
    }

    @Override
    public String toString() {
        return "TextAttributes{" +
                "fg=" + foreground +
                ", bg=" + background +
                ", styles=" + TextStyle.toString(styles) +
                '}';
    }
}
