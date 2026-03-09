package com.github.matei;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalLineTest {
    @Test
    void newLineHasCorrectWidth() {
        TerminalLine line = new TerminalLine(80);
        assertEquals(80, line.getWidth());
    }

    @Test
    void validateNewLineIsEmpty() {
        TerminalLine line = new TerminalLine(10);

        for (int i = 0; i < 10; i++) {
            assertTrue(line.getCell(i).isEmpty());
        }

        assertEquals("", line.getText());
    }

    @Test
    void invalidWidthThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalLine(0));
        assertThrows(IllegalArgumentException.class, () -> new TerminalLine(-1));
    }

    @Test
    void setCellUpdatesCell() {
        TerminalLine terminalLine = new TerminalLine(10);
        TextAttributes attrs = new TextAttributes(TerminalColor.WHITE, TerminalColor.BLACK, TextStyle.BOLD);

        terminalLine.setCell(5, 'X', attrs);
        assertEquals('X', terminalLine.getCell(5).getCharacter());
        assertEquals(attrs, terminalLine.getCell(5).getAttributes());
    }

    @Test
    void setCellOutOfBoundsThrows() {
        TerminalLine line = new TerminalLine(10);
        assertThrows(IndexOutOfBoundsException.class,
                () -> line.setCell(-1, 'A', TextAttributes.DEFAULT));
        assertThrows(IndexOutOfBoundsException.class,
                () -> line.setCell(10, 'A', TextAttributes.DEFAULT));
    }

    @Test
    void writeOverwritesContent() {
        TerminalLine line = new TerminalLine(10);
        line.write(0, "Hello", TextAttributes.DEFAULT);

        assertEquals("Hello", line.getText());
        assertEquals('H', line.getCell(0).getCharacter());
        assertEquals('o', line.getCell(4).getCharacter());
    }

    @Test
    void writeAtOffset() {
        TerminalLine line = new TerminalLine(10);
        line.write(3, "Hi", TextAttributes.DEFAULT);

        assertEquals("   Hi", line.getText());
    }

    @Test
    void writeClipsAtLineEnd() {
        TerminalLine line = new TerminalLine(5);
        int written = line.write(3, "ABCDE", TextAttributes.DEFAULT);

        assertEquals(2, written);
        assertEquals("   AB", line.getText());
    }

    @Test
    void writeReturnsCharacterCount() {
        TerminalLine line = new TerminalLine(10);
        assertEquals(5, line.write(0, "Hello", TextAttributes.DEFAULT));
        assertEquals(3, line.write(7, "ABCDE", TextAttributes.DEFAULT));
    }

    @Test
    void insertShiftsContentRight() {
        TerminalLine line = new TerminalLine(10);
        line.write(0, "ABCDE", TextAttributes.DEFAULT);

        line.insert(2, "MA", TextAttributes.DEFAULT);

        assertEquals("ABMACDE", line.getText());
        assertEquals("ABMACDE   ", line.getFullText());
    }

    @Test
    void insertPushesContentOffEnd() {
        TerminalLine line = new TerminalLine(5);
        line.write(0, "ABCDE", TextAttributes.DEFAULT);

        line.insert(1, "M", TextAttributes.DEFAULT);

        assertEquals("AMBCD", line.getText());
    }

    @Test
    void fillSetsAllCells() {
        TerminalLine line = new TerminalLine(5);
        TextAttributes attrs = new TextAttributes(
                TerminalColor.GREEN, TerminalColor.DEFAULT, TextStyle.NONE
        );

        line.fill('-', attrs);

        assertEquals("-----", line.getText());
        IntStream.range(0, 5)
                .forEach(i -> {
                    assertEquals('-', line.getCell(i).getCharacter());
                    assertEquals(attrs, line.getCell(i).getAttributes());
                });
    }

    @Test
    void clearResetsAllCells() {
        TerminalLine line = new TerminalLine(5);

        line.fill('-', TextAttributes.DEFAULT);

        line.clear();

        assertEquals("", line.getText());

        IntStream.range(0, 5)
                .forEach(i -> {
                    assertTrue(line.getCell(i).isEmpty());
                });
    }

    @Test
    void getTextTrimsTrailingEmpties() {
        TerminalLine line = new TerminalLine(10);
        line.write(0, "Hi", TextAttributes.DEFAULT);

        assertEquals("Hi", line.getText());
        assertEquals(10, line.getFullText().length());
    }

    @Test
    void getFullTextIncludesTrailingSpaces() {
        TerminalLine line = new TerminalLine(5);
        line.write(0, "AB", TextAttributes.DEFAULT);

        assertEquals("AB   ", line.getFullText());
    }
}
