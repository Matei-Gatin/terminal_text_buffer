package com.github.matei.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextAttributesTest {
    @Test
    void defaultAttributesHaveDefaultColors() {
        assertEquals(TerminalColor.DEFAULT, TextAttributes.DEFAULT.getForeground());
        assertEquals(TerminalColor.DEFAULT, TextAttributes.DEFAULT.getBackground());
    }

    @Test
    void defaultAttributesHaveNoStyles() {
        assertFalse(TextAttributes.DEFAULT.isBold());
        assertFalse(TextAttributes.DEFAULT.isItalic());
        assertFalse(TextAttributes.DEFAULT.isUnderline());
        assertEquals(TextStyle.NONE, TextAttributes.DEFAULT.getStyles());
    }

    @Test
    void constructorStoresValues() {
        TextAttributes attrs = new TextAttributes(
                TerminalColor.RED, TerminalColor.BLUE,
                TextStyle.BOLD | TextStyle.ITALIC
        );

        assertEquals(TerminalColor.RED, attrs.getForeground());
        assertEquals(TerminalColor.BLUE, attrs.getBackground());
        assertTrue(attrs.isBold());
        assertTrue(attrs.isItalic());
        assertFalse(attrs.isUnderline());
    }

    @Test
    void nullForegroundThrows() {
        assertThrows(NullPointerException.class, () ->
                new TextAttributes(null, TerminalColor.DEFAULT, TextStyle.NONE));
    }

    @Test
    void nullBackgroundThrows() {
        assertThrows(NullPointerException.class, () ->
                new TextAttributes(TerminalColor.DEFAULT, null, TextStyle.NONE)
        );
    }

    @Test
    void equalAttributesAreEquals() {
        TextAttributes a = new TextAttributes(TerminalColor.BLACK, TerminalColor.BRIGHT_WHITE, TextStyle.UNDERLINE);
        TextAttributes b = new TextAttributes(TerminalColor.BLACK, TerminalColor.BRIGHT_WHITE, TextStyle.UNDERLINE);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentAttributesAreNotEqual() {
        TextAttributes a = new TextAttributes(TerminalColor.RED, TerminalColor.DEFAULT, TextStyle.NONE);
        TextAttributes b = new TextAttributes(TerminalColor.BLUE, TerminalColor.DEFAULT, TextStyle.NONE);
        assertNotEquals(a, b);
    }

    @Test
    void toStringContainsAllFields() {
        TextAttributes a = new TextAttributes(TerminalColor.RED, TerminalColor.BLUE, TextStyle.BOLD);
        String toString = a.toString();
        assertTrue(toString.contains("RED"));
        assertTrue(toString.contains("BLUE"));
        assertTrue(toString.contains("BOLD"));
    }
}
