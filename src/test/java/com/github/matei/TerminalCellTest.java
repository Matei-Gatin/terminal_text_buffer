package com.github.matei;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalCellTest {
    @Test
    void defaultCellIsEmpty() {
        TerminalCell cell = new TerminalCell();
        assertTrue(cell.isEmpty());
        assertEquals(TerminalCell.EMPTY_CHAR, cell.getCharacter());
        assertEquals(TextAttributes.DEFAULT, cell.getAttributes());
    }

    @Test
    void filledCellIsNotEmpty() {
        TerminalCell cell = new TerminalCell('A', TextAttributes.DEFAULT);
        assertEquals('A', cell.getCharacter());
        assertEquals(TextAttributes.DEFAULT, cell.getAttributes());
        assertFalse(cell.isEmpty());
    }

    @Test
    void setUpdatesCharacterAndAttributes() {
        TerminalCell cell = new TerminalCell();
        TextAttributes attrs = new TextAttributes(
                TerminalColor.RED, TerminalColor.BLACK, TextStyle.BOLD
        );

        cell.set('X', attrs);

        assertEquals('X', cell.getCharacter());
        assertEquals(attrs, cell.getAttributes());
        assertFalse(cell.isEmpty());
    }

    @Test
    void clearResetsToEmpty() {
        TextAttributes attrs = new TextAttributes(
                TerminalColor.GREEN, TerminalColor.BLUE, TextStyle.ITALIC
        );
        TerminalCell cell = new TerminalCell('Z', attrs);

        cell.clear();

        assertTrue(cell.isEmpty());
        assertEquals(TerminalCell.EMPTY_CHAR, cell.getCharacter());
        assertEquals(TextAttributes.DEFAULT, cell.getAttributes());
    }

    @Test
    void constructorWithArgsSetsValues() {
        TextAttributes attrs = new TextAttributes(
                TerminalColor.CYAN, TerminalColor.WHITE, TextStyle.UNDERLINE
        );
        TerminalCell cell = new TerminalCell('Q', attrs);

        assertEquals('Q', cell.getCharacter());
        assertEquals(attrs, cell.getAttributes());
    }
}
