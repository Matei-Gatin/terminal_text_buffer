package com.github.matei.emulator;

import com.github.matei.buffer.TextAttributes;

public interface TerminalEmulator {
    void setCurrentAttributes(TextAttributes attrs);

    void writeText(String text);
    void insertText(String text);
    void fillLine(int row, char character);
    void insertEmptyLineAtBottom();

    // Display
    void clearScreen();
    void clearAll();
    void eraseInDisplay(int n);
    void eraseInLine(int n);

    // Cursor
    void moveCursorUp(int n);
    void moveCursorDown(int n);
    void moveCursorLeft(int n);
    void moveCursorRight(int n);
    void setCursorPosition(int col, int row);
    int getCursorCol();
    int getCursorRow();
    void scrollUp(int n);
    void scrollDown(int n);

    // Accessors
    TextAttributes getCurrentAttributes();
    char getCharAt(int col, int row);
    TextAttributes getAttributesAt(int col, int row);
    String getLineAsString(int row);
    String getLineAsAnsiString(int row);
    String getStringContent();
    String getFullContent();
}
