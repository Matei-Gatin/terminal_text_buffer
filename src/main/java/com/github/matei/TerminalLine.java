package com.github.matei;

/**
 * A single row in the terminal grid.
 * <p>
 * Backed by a fixed-width array of {@link TerminalCell} objects,
 * providing O(1) access to any cell by column index.
 */
public class TerminalLine {

    private final TerminalCell[] cells;

    /**
     * Creates a line of the given width, filled with empty cells.
     *
     * @param width the number of columns
     * @throws IllegalArgumentException if width is not positive
     */
    public TerminalLine(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive: " + width);
        }

        this.cells = new TerminalCell[width];
        for (int i = 0; i < width; i++) {
            cells[i] = new TerminalCell();
        }
    }

    public int getWidth() {
        return cells.length;
    }

    public TerminalCell getCell(int col) {
        validateColumn(col);
        return cells[col];
    }

    public void setCell(int col, char ch, TextAttributes attrs) {
        validateColumn(col);
        cells[col].set(ch, attrs);
    }

    public void fill(char ch, TextAttributes attrs) {
        for (TerminalCell cell : cells) {
            cell.set(ch, attrs);
        }
    }

    public void clear() {
        for (TerminalCell cell : cells) {
            cell.clear();
        }
    }

    /**
     * Writes text starting at the given column, overwriting existing content.
     * Stops at the end of the line — does not wrap.
     *
     * @param col        the starting column (0-based)
     * @param text       the text to write
     * @param attrs the attributes to apply
     * @return the number of characters actually written (may be less than text length
     *         if the line boundary is reached)
     */
    public int write(int col, String text, TextAttributes attrs) {
        validateColumn(col);

        int charsWritten = 0;

        char[] chars = text.toCharArray();
        for (int i = col; i < cells.length && charsWritten < text.length(); i++) {
            cells[i].set(chars[charsWritten], attrs);
            charsWritten++;
        }

        return charsWritten;
    }

    /**
     * Inserts text at the given column, shifting existing content to the right.
     * Characters pushed past the line width are lost.
     *
     * @param col        the starting column (0-based)
     * @param text       the text to insert
     * @param attrs the attributes to apply
     */
    public void insert(int col, String text, TextAttributes attrs) {
        validateColumn(col);
        int insertLen = Math.min(text.length(), cells.length);

        for (int i = cells.length - 1; i >= col + insertLen; i--) {
            TerminalCell cell = cells[i - insertLen];
            cells[i].set(cell.getCharacter(), cell.getAttributes());
        }

        for (int i = 0; i < text.length(); i++) {
            cells[col + i].set(text.charAt(i), attrs);
        }
    }

    /**
     * Returns the line content as a string.
     * Trailing empty cells are trimmed.
     *
     * @return the text content of this line
     */
    public String getText() {
        int end = cells.length;

        for (int i = cells.length - 1; i >= 0; i--) {
            if (!cells[i].isEmpty()) {
                break;
            }
            end--;
        }

        StringBuilder sb = new StringBuilder(end);
        for (int i = 0; i < end; i++) {
            sb.append(cells[i].isEmpty() ? ' ' : cells[i].getCharacter());
        }

        return sb.toString();
    }

    /**
     * Returns the full line content as a string, including trailing spaces.
     * Empty cells are represented as spaces.
     *
     * @return the full-width text content
     */
    public String getFullText() {
        StringBuilder sb = new StringBuilder(cells.length);
        for (TerminalCell cell : cells) {
            sb.append(cell.isEmpty() ? ' ' : cell.getCharacter());
        }
        return sb.toString();
    }

    // === Helper Methods ===
    private void validateColumn(int col) {
        if (col < 0 || col >= cells.length) {
            throw new IndexOutOfBoundsException(
                    "Column " + col + " out of range [0, " + (cells.length - 1) + "]"
            );
        }
    }
}
