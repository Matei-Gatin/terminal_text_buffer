package com.github.matei;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextStyleTest {
    @Test
    void noneHasNoStyle() {
        assertFalse(TextStyle.has(TextStyle.NONE, TextStyle.BOLD));
        assertFalse(TextStyle.has(TextStyle.NONE, TextStyle.ITALIC));
        assertFalse(TextStyle.has(TextStyle.NONE, TextStyle.UNDERLINE));
    }

    @Test
    void singleStyleIsDetected() {
        assertTrue(TextStyle.has(TextStyle.BOLD, TextStyle.BOLD));
        assertFalse(TextStyle.has(TextStyle.BOLD, TextStyle.ITALIC));
        assertFalse(TextStyle.has(TextStyle.BOLD, TextStyle.UNDERLINE));
    }

    @Test
    void combinedStylesAreDetected() {
        int boldItalic = TextStyle.BOLD | TextStyle.ITALIC;
        assertTrue(TextStyle.has(boldItalic, TextStyle.BOLD));
        assertTrue(TextStyle.has(boldItalic, TextStyle.ITALIC));
        assertFalse(TextStyle.has(boldItalic, TextStyle.UNDERLINE));
    }

    @Test
    void allStylesCombined() {
        int all = TextStyle.BOLD | TextStyle.ITALIC | TextStyle.UNDERLINE;
        assertTrue(TextStyle.has(all, TextStyle.BOLD));
        assertTrue(TextStyle.has(all, TextStyle.ITALIC));
        assertTrue(TextStyle.has(all, TextStyle.UNDERLINE));
    }

    @Test
    void stylesDoNotOverlap() {
        assertNotEquals(TextStyle.BOLD, TextStyle.ITALIC);
        assertNotEquals(TextStyle.ITALIC, TextStyle.UNDERLINE);
        assertNotEquals(TextStyle.BOLD, TextStyle.UNDERLINE);
    }

    @Test
    void toStringName() {
        assertEquals("NONE", TextStyle.toString(TextStyle.NONE));
    }

    @Test
    void toStringSingleStyle() {
        assertEquals("BOLD", TextStyle.toString(TextStyle.BOLD));
        assertEquals("ITALIC", TextStyle.toString(TextStyle.ITALIC));
        assertEquals("UNDERLINE", TextStyle.toString(TextStyle.UNDERLINE));
    }

    @Test
    void toStringCombinedStyles() {
        int boldItalic = TextStyle.BOLD | TextStyle.ITALIC;
        assertEquals("BOLD|ITALIC", TextStyle.toString(boldItalic));

        int all = TextStyle.BOLD | TextStyle.ITALIC | TextStyle.UNDERLINE;
        assertEquals("BOLD|ITALIC|UNDERLINE", TextStyle.toString(all));
    }
}
