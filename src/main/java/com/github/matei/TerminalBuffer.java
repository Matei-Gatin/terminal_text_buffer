package com.github.matei;

import com.github.matei.util.CharWidthUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Core terminal text buffer implementation.
 * <p>
 * Manages a screen grid of configurable dimensions and a bounded
 * scrollback history. Provides cursor management, text editing,
 * and content access operations.
 * <p>
 * Coordinate system:
 * <ul>
 *   <li>Screen rows: 0 to height-1 (editable)</li>
 *   <li>Scrollback rows: -1 to -scrollbackSize (read-only history)</li>
 *   <li>Columns: 0 to width-1</li>
 * </ul>
 */
public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollback;

    private final List<TerminalLine> screen;
    private final Deque<TerminalLine> scrollBack;

    private int cursorCol;
    private int cursorRow;
    private TextAttributes currentAttributes;

    /**
     * Creates a new terminal buffer.
     *
     * @param width         number of columns (must be positive)
     * @param height        number of screen rows (must be positive)
     * @param maxScrollback maximum number of scrollback lines (0 or positive)
     */
    public TerminalBuffer(int width, int height, int maxScrollback) {
        validateParams(width, height, maxScrollback);

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;

        this.screen = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            screen.add(new TerminalLine(width));
        }

        this.scrollBack = new ArrayDeque<>();
        this.cursorCol = 0;
        this.cursorRow = 0;
        this.currentAttributes = TextAttributes.DEFAULT;
    }

    // Dimensions
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxScrollback() {
        return maxScrollback;
    }

    public int getScrollbackSize() {
        return scrollBack.size();
    }

    public TextAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    // Attributes
    public void setCurrentAttributes(TextAttributes attrs) {
        this.currentAttributes = attrs;
    }

    // Cursor
    public int getCursorCol() {
        return cursorCol;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public void setCursorPosition(int col, int row) {
        this.cursorCol = clampCol(col);
        this.cursorRow = clampRow(row);
    }

    /**
     * Moves the cursor up by n rows, clamped to row 0.
     */
    public void moveCursorUp(int n) {
        cursorRow = clampRow(cursorRow - n);
    }

    /**
     * Moves the cursor down by n rows, clamped to height-1.
     */
    public void moveCursorDown(int n) {
        cursorRow = clampRow(cursorRow + n);
    }

    /**
     * Moves the cursor left by n columns, clamped to column 0.
     */
    public void moveCursorLeft(int n) {
        cursorCol = clampCol(cursorCol - n);
    }

    /**
     * Moves the cursor right by n columns, clamped to width-1.
     */
    public void moveCursorRight(int n) {
        cursorCol = clampCol(cursorCol + n);
    }

    private int clampCol(int col) {
        return Math.max(0, Math.min(col, width - 1));
    }

    private int clampRow(int row) {
        return Math.max(0, Math.min(row, height - 1));
    }

    // scrolling

    /**
     * Scrolls the screen up by one line: the top screen line moves
     * into scrollback, and a new empty line appears at the bottom.
     */
    private void scrollUp() {
        TerminalLine topLine = screen.removeFirst();
        scrollBack.addLast(topLine);

        while (scrollBack.size() > maxScrollback) {
            scrollBack.removeFirst();
        }

        screen.add(new TerminalLine(width));
    }

    // Editing

    /**
     * Writes text at the current cursor position using current attributes.
     * Overwrites existing content. Advances the cursor.
     * Wraps to the next line if text exceeds the line width.
     * Scrolls the screen if wrapping goes past the last row.
     *
     * @param text the text to write
     */
    public void writeText(String text) {
        for (int i = 0; i < text.length(); i++) {
            boolean isWide = CharWidthUtil.isWide(text.charAt(i));

            if (isWide && cursorCol == width - 1) {
                cursorCol = 0;
                if (cursorRow == height - 1) {
                    scrollUp();
                } else {
                    cursorRow++;
                }
            }

            if (cursorCol >= width) {
                cursorCol = 0;
                if (cursorRow == height - 1) {
                    scrollUp();
                } else {
                    cursorRow++;
                }
            }

            TerminalLine line = screen.get(cursorRow);
            line.setCell(cursorCol, text.charAt(i), currentAttributes);

            if (isWide) cursorCol += 2;
            else cursorCol++;
        }
    }

    public void insertText(String text) {
        for (int i = 0; i < text.length(); i++) {
            boolean isWide = CharWidthUtil.isWide(text.charAt(i));

            if (isWide && cursorCol == width - 1) {
                cursorCol = 0;
                if (cursorRow == height - 1) {
                    scrollUp();
                } else {
                    cursorRow++;
                }
            }

            if (cursorCol >= width) {
                cursorCol = 0;
                if (cursorRow == height - 1) {
                    scrollUp();
                } else {
                    cursorRow++;
                }
            }

            TerminalLine line = screen.get(cursorRow);
            line.insert(cursorCol, String.valueOf(text.charAt(i)), currentAttributes);

            if (isWide) cursorCol += 2;
            else cursorCol++;
        }
    }

    public void fillLine(int row, char character) {
        validateScreenRow(row);
        screen.get(row).fill(character, currentAttributes);
    }

    public void insertEmptyLineAtBottom() {
        scrollUp();
    }

    public void clearScreen() {
        screen.forEach(TerminalLine::clear);
        cursorCol = 0;
        cursorRow = 0;
    }

    public void clearAll() {
        clearScreen();
        scrollBack.clear();
    }

    // Content Access
    public char getCharAt(int col, int row) {
        return getLine(row).getCell(col).getCharacter();
    }

    public TextAttributes getAttributesAt(int col, int row) {
        return getLine(row).getCell(col).getAttributes();
    }

    public String getLineAsString(int row) {
        return getLine(row).getText();
    }

    /**
     * Returns the entire screen content as a string.
     * Lines are separated by newline characters.
     * Trailing empty lines are included.
     *
     * @return the screen content
     */
    public String getStringContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            if (i > 0) sb.append('\n');
            sb.append(screen.get(i).getText());
        }

        return sb.toString();
    }

    /**
     * Returns the full content (scrollback + screen) as a string.
     * Scrollback lines appear first (oldest to newest), followed by screen lines.
     * Lines are separated by newline characters.
     *
     * @return the full buffer content
     */
    public String getFullContent() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (TerminalLine line : scrollBack) {
            if (!first) sb.append('\n');
            sb.append(line.getText());
            first = false;
        }

        for (TerminalLine line : screen) {
            if (!first) sb.append('\n');
            sb.append(line.getText());
            first = false;
        }

        return sb.toString();
    }

    // === Helpers ===
    private void validateParams(int width, int height, int maxScrollback) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive: " + width);
        }

        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive: " + height);
        }

        if (maxScrollback < 0) {
            throw new IllegalArgumentException("Max scrollback must be non-negative: " + maxScrollback);
        }
    }

    private void validateScreenRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException(
                    "Screen row " + row + " out of range [0, " + (height - 1) + "]"
            );
        }
    }

    private TerminalLine getLine(int row) {
        if (row >= 0) {
            validateScreenRow(row);
            return screen.get(row);
        }

        int scrollbackIndex = scrollBack.size() + row;
        if (scrollbackIndex < 0) {
            throw new IndexOutOfBoundsException(
                    "Scrollback row " + row + " out of range [-" + scrollBack.size() + ", -1]"
            );
        }

        int i = 0;
        for (TerminalLine line : scrollBack) {
            if (i == scrollbackIndex) {
                return line;
            }
            i++;
        }

        throw new IllegalStateException("Failed to resolve scrollback row: " + row);
    }
}
