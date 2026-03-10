package com.github.matei.util;

/**
 * Utility for determining the display width of characters in a terminal.
 */
public final class CharWidthUtil {

    private CharWidthUtil() {
        throw new RuntimeException("Cannot be instantiated");
    }

    public static boolean isWide(int codePoint) {
        return isWideCodePoint(codePoint);
    }

    private static boolean isWideCodePoint(int codePoint) {
        // CJK Unified Ideographs
        if (codePoint >= 0x4E00 && codePoint <= 0x9FFF) return true;
        // CJK Unified Ideographs Extension A
        if (codePoint >= 0x3400 && codePoint <= 0x4DBF) return true;
        // CJK Unified Ideographs Extension B
        if (codePoint >= 0x20000 && codePoint <= 0x2A6DF) return true;
        // CJK Compatibility Ideographs
        if (codePoint >= 0xF900 && codePoint <= 0xFAFF) return true;
        // Fullwidth Forms
        if (codePoint >= 0xFF01 && codePoint <= 0xFF60) return true;
        // Fullwidth Forms
        if (codePoint >= 0xFFE0 && codePoint <= 0xFFE6) return true;
        // CJK Radicals / Kangxi Radicals
        if (codePoint >= 0x2E80 && codePoint <= 0x2FDF) return true;
        // CJK Symbols and Punctuation, Hiragana, Katakana
        if (codePoint >= 0x3000 && codePoint <= 0x303F) return true;
        if (codePoint >= 0x3040 && codePoint <= 0x309F) return true;
        if (codePoint >= 0x30A0 && codePoint <= 0x30FF) return true;
        // Hangul Syllables (Korean)
        if (codePoint >= 0xAC00 && codePoint <= 0xD7AF) return true;
        // Common emoji ranges
        if (codePoint >= 0x1F600 && codePoint <= 0x1F64F) return true;
        if (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) return true;
        if (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) return true;
        if (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) return true;

        return false;
    }
}
