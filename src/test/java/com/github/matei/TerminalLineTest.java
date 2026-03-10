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

    // Wide Chars
    @Test
    void setCellWideCharOccupiesTwoCells() {
        TerminalLine line = new TerminalLine(10);
        line.setCell(0, '你', TextAttributes.DEFAULT);

        assertEquals('你', line.getCell(0).getCharacter());
        assertTrue(line.getCell(1).isWideContinuation());
    }

    @Test
    void setCellWideCharAtLastColumnWritesSpace() {
        TerminalLine line = new TerminalLine(10);
        line.setCell(9, '你', TextAttributes.DEFAULT);

        assertEquals(' ', line.getCell(9).getCharacter());
        assertFalse(line.getCell(9).isWideContinuation());
    }

    @Test
    void setCellOverwriteContinuationClearsLeftHalf() {
        TerminalLine line = new TerminalLine(10);
        line.setCell(2, '你', TextAttributes.DEFAULT);

        line.setCell(3, 'A', TextAttributes.DEFAULT);

        assertEquals(TerminalCell.EMPTY_CHAR, line.getCell(2).getCharacter());
        assertEquals('A', line.getCell(3).getCharacter());
        assertFalse(line.getCell(3).isWideContinuation());
    }

    @Test
    void setCellOverwriteWideCharClearsContinuation() {
        TerminalLine line = new TerminalLine(10);
        line.setCell(2, '你', TextAttributes.DEFAULT);

        line.setCell(2, 'A', TextAttributes.DEFAULT);

        assertEquals('A', line.getCell(2).getCharacter());
        assertEquals(TerminalCell.EMPTY_CHAR, line.getCell(3).getCharacter());
        assertFalse(line.getCell(3).isWideContinuation());
    }

    @Test
    void setCellWideOverwritesExistingWideChar() {
        TerminalLine line = new TerminalLine(10);
        line.setCell(1, '你', TextAttributes.DEFAULT);

        line.setCell(0, '日', TextAttributes.DEFAULT);

        assertEquals('日', line.getCell(0).getCharacter());
        assertTrue(line.getCell(1).isWideContinuation());
        assertFalse(line.getCell(2).isWideContinuation());
    }

    @Test
    void getTextWithWideCharDoesNotOutputExtraSpace() {
        TerminalLine line = new TerminalLine(10);
        line.setCell(0, '你', TextAttributes.DEFAULT);
        line.setCell(2, 'A', TextAttributes.DEFAULT);

        assertEquals("你A", line.getText());
    }

    @Test
    void writeWideCharStopsAtBoundary() {
        TerminalLine line = new TerminalLine(5);
        int written = line.write(0, "AB你C", TextAttributes.DEFAULT);

        // A at 0, B at 1, 你 at 2-3, C at 4
        assertEquals('A', line.getCell(0).getCharacter());
        assertEquals('B', line.getCell(1).getCharacter());
        assertEquals('你', line.getCell(2).getCharacter());
        assertTrue(line.getCell(3).isWideContinuation());
        assertEquals('C', line.getCell(4).getCharacter());
        assertEquals(4, written);
    }

    @Test
    void writeWideCharDoesNotFitAtEnd() {
        TerminalLine line = new TerminalLine(5);
        int written = line.write(0, "ABCD你", TextAttributes.DEFAULT);

        assertEquals(5, written);
        assertEquals('A', line.getCell(0).getCharacter());
        assertEquals('D', line.getCell(3).getCharacter());
        assertEquals(' ', line.getCell(4).getCharacter());
        assertFalse(line.getCell(4).isWideContinuation());
    }
}
