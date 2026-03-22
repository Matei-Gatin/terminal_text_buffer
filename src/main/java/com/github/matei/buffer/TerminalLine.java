package com.github.matei.buffer;

import com.github.matei.util.CharWidthUtil;

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

        int codePoint = ch;
        boolean isWide = CharWidthUtil.isWide(codePoint);

        // if writing wide char, col + 1 needs to exist
        if (isWide && col + 1 >= cells.length) {
            // wide char doesn't fit, write a space instead
            cells[col].set(' ', attrs);
            return;
        }

        // if writing at continuation cell clear the left half
        if (cells[col].isWideContinuation() && col > 0) {
            cells[col - 1].clear();
        }

        // cell[col] is wide char clear continuation at col + 1
        if (!cells[col].isEmpty() && col + 1 < cells.length && cells[col + 1].isWideContinuation()) {
            cells[col + 1].clear();
        }

        // write wide char. If col + 1 has wide char clear continuation at col + 2
        if (isWide && col + 1 < cells.length) {
            if (!cells[col + 1].isWideContinuation() && col + 2 < cells.length
                    && cells[col + 2].isWideContinuation()) {
                cells[col + 2].clear();
            }
        }

        cells[col].set(ch, attrs);
        if (isWide) {
            cells[col + 1].setWideContinuation(true);
        }
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
            char candidate = chars[charsWritten];

            if (CharWidthUtil.isWide(candidate)) {
                if (i + 1 >= cells.length) {
                    setCell(i, ' ', attrs);
                    charsWritten++;
                    break;
                }

                setCell(i, candidate, attrs);
                charsWritten++;
                i++;
                continue;
            }

            setCell(i, candidate, attrs);
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

        int visualWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            visualWidth += CharWidthUtil.isWide(text.charAt(i)) ? 2 : 1;
        }

        int insertLen = Math.min(visualWidth, cells.length - col); // room for insertion

        for (int i = cells.length - 1; i >= col + insertLen; i--) {
            TerminalCell cell = cells[i - insertLen];
            setCell(i, cell.getCharacter(), cell.getAttributes());
            if (cell.isWideContinuation()) {
                cells[i].setWideContinuation(true);
            }
        }

        if (cells.length > 0 && cells[cells.length - 1].getCharacter() != TerminalCell.EMPTY_CHAR
                && !cells[cells.length - 1].isWideContinuation()) {
            if (CharWidthUtil.isWide(cells[cells.length - 1].getCharacter())) {
                cells[cells.length - 1].clear();
            }
        }

        int currentCol = col;
        for (int i = 0; i < text.length() && currentCol < cells.length; i++) {
            char ch = text.charAt(i);
            setCell(currentCol, ch, attrs);

            currentCol += CharWidthUtil.isWide(ch) ? 2 : 1;
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
            if (!cells[i].isEmpty() && !cells[i].isWideContinuation()) {
                break;
            }
            end--;
        }

        StringBuilder sb = new StringBuilder(end);
        for (int i = 0; i < end; i++) {
            if (cells[i].isWideContinuation()) continue;
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
            if (cell.isWideContinuation()) continue;
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
