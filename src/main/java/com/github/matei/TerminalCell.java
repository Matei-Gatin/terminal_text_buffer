package com.github.matei;

/**
 * A single cell in the terminal grid.
 * <p>
 * Holds one character and its associated text attributes.
 * A character value of {@code '\0'} represents an empty cell.
 */
public class TerminalCell {
    public static final char EMPTY_CHAR = '\0';

    private char character;
    private TextAttributes attributes;

    /**
     * Creates an empty cell with default attributes.
     */
    public TerminalCell() {
        this.character = EMPTY_CHAR;
        this.attributes = TextAttributes.DEFAULT;
    }

    /**
     * Creates a cell with the given character and attributes
     * @param character the character to display
     * @param attributes the text attributes for this cell
     */
    public TerminalCell(char character, TextAttributes attributes) {
        this.character = character;
        this.attributes = attributes;
    }

    public char getCharacter() {
        return character;
    }

    public TextAttributes getAttributes() {
        return attributes;
    }

    public void set(char character, TextAttributes attributes) {
        this.character = character;
        this.attributes = attributes;
    }

    public void clear() {
        this.character = EMPTY_CHAR;
        this.attributes = TextAttributes.DEFAULT;
    }

    public boolean isEmpty() {
        return character == EMPTY_CHAR;
    }

    @Override
    public String toString() {
        return isEmpty()
                ? "Cell{EMPTY}"
                : "Cell{'" + character + "', " + attributes + '}';
    }
}
