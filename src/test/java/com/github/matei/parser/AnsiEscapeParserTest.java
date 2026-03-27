package com.github.matei.parser;

import com.github.matei.buffer.TerminalBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnsiEscapeParserTest {
    private TerminalBuffer buffer;
    private AnsiEscapeParser ansiEscapeParser;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(80,24, 1000);
        ansiEscapeParser = new AnsiEscapeParser(buffer);
    }

    @Test
    void moveCursorUpTest() {
        final String seq = "\u001b[1A";
        buffer.setCursorPosition(0, 2);

        ansiEscapeParser.process(seq);

        assertEquals(1, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorCol());
    }

    @Test
    void moveCursorDownTest() {
        final String seq = "\u001b[1B";
        buffer.setCursorPosition(0, 2);

        ansiEscapeParser.process(seq);

        assertEquals(3, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorCol());
    }

    @Test
    void moveCursorForwardTest() {
        final String seq = "\u001b[1C";
        buffer.setCursorPosition(0, 2);

        ansiEscapeParser.process(seq);

        assertEquals(1, buffer.getCursorCol());
        assertEquals(2, buffer.getCursorRow());
    }

    @Test
    void moveCursorForwardClampTest() {
        final String seq = "\u001b[1C";
        buffer.setCursorPosition(79, 0);

        ansiEscapeParser.process(seq);

        assertEquals(79, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void moveCursorBackTest() {
        final String seq = "\u001b[1D";
        buffer.setCursorPosition(1, 0);

        ansiEscapeParser.process(seq);

        assertEquals(0, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void moveCursorBackClampTest() {
        final String seq = "\u001b[1D";
        buffer.setCursorPosition(0, 0);

        ansiEscapeParser.process(seq);

        assertEquals(0, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorCol());
    }

    @Test
    void moveCursorNextLineTest() {
        final String seq = "\u001b[3E";
        buffer.setCursorPosition(20, 0);

        ansiEscapeParser.process(seq);

        assertEquals(3, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorCol());
    }

    @Test
    void moveCursorPreviousLineTest() {
        final String seq = "\u001b[3F";
        buffer.setCursorPosition(20, 3);

        ansiEscapeParser.process(seq);

        assertEquals(0, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorCol());
    }

    // TODO: check for column 0 based index
    @Test
    void moveCursorHorizontalTest() {
        final String seq1 = "\u001b[7G";
        final String seq2 = "\u001b[8f";
        buffer.setCursorPosition(5, 0);

        ansiEscapeParser.process(seq1);

        assertEquals(6, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());

        ansiEscapeParser.process(seq2);

        assertEquals(7, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void moveCursorToPositionTest() {
        final String seq = "\u001b[3;2H";

        ansiEscapeParser.process(seq);

        assertEquals(2, buffer.getCursorRow());
        assertEquals(1, buffer.getCursorCol());
    }

    @Test
    void eraseInDisplayFromCursorPosToEndTest() {
        TerminalBuffer buff = new TerminalBuffer(12,6, 1000);
        AnsiEscapeParser ansiEscapeParser = new AnsiEscapeParser(buff);

        final String seq = "\u001b[0J";
        final String toWrite = "A".repeat(3);

        buff.writeText(toWrite);
        buff.setCursorPosition(0, 1);
        buff.writeText(toWrite);

        assertEquals("AAA", buff.getLineAsString(0));
        assertEquals("AAA", buff.getLineAsString(1));

        buff.setCursorPosition(0, 0);
        ansiEscapeParser.process(seq);

        assertEquals("", buff.getLineAsAnsiString(0));
        assertEquals("", buff.getLineAsString(1));
    }

    @Test
    void eraseInDisplayFromTopToCursorPosTest() {
        TerminalBuffer buff = new TerminalBuffer(12, 6, 1000);
        AnsiEscapeParser ansiEscapeParser = new AnsiEscapeParser(buff);

        final String seq = "\u001b[1J";
        final String toWrite = "A".repeat(3);
        buff.writeText(toWrite);

        assertEquals("AAA", buff.getLineAsString(0));

        ansiEscapeParser.process(seq);

        assertEquals("", buff.getLineAsAnsiString(0));
        assertEquals(3, buff.getCursorCol());
        assertEquals(0, buff.getCursorRow());
    }

    @Test
    void eraseInDisplayClearScreenTest() {
        TerminalBuffer buff = new TerminalBuffer(12, 6, 1000);
        AnsiEscapeParser ansiEscapeParser = new AnsiEscapeParser(buff);

        final String seq = "\u001b[2J";
        final String toWrite = "A".repeat(3);
        buff.writeText(toWrite);

        assertEquals("AAA", buff.getLineAsString(0));

        ansiEscapeParser.process(seq);

        assertEquals("", buff.getLineAsString(0));
        assertEquals(0, buff.getCursorRow());
        assertEquals(0, buff.getCursorCol());
    }

    @Test
    void eraseInLineClearFromCursorToEndOfLineTest() {
        final String seq = "\u001b[0K";
        final String toWrite = "A".repeat(3);
      
        buffer.writeText(toWrite);
        buffer.setCursorPosition(0, 1);
        buffer.writeText(toWrite);

        buffer.setCursorPosition(0, 0);
        ansiEscapeParser.process(seq);

        assertEquals("", buffer.getLineAsString(0));
        assertEquals("AAA", buffer.getLineAsString(1));
    }

    @Test
    void eraseInLineClearFromBeginningOfLineToCursorTest() {
        final String seq = "\u001b[1K";
        final String toWrite = "A".repeat(3);

        buffer.writeText(toWrite);
        buffer.setCursorPosition(1, 0);

        ansiEscapeParser.process(seq);

        assertEquals("  A", buffer.getLineAsString(0));
    }

    @Test
    void eraseInLineClearEntireLineTest() {
        final String seq = "\u001b[2K";
        buffer.fillLine(0, 'A');

        ansiEscapeParser.process(seq);

        assertEquals("", buffer.getLineAsString(0));
    }

    @Test
    void scrollUpTest() {
        final String seq = "\u001b[3S";

        ansiEscapeParser.process(seq);

        assertEquals(3, buffer.getScrollbackSize());
    }

    @Test
    void scrollDownTest() {
        TerminalBuffer smallBuff = new TerminalBuffer(10, 10, 100);
        AnsiEscapeParser ansiEscapeParser = new AnsiEscapeParser(smallBuff);

        final String seq = "\u001b[1T";

        smallBuff.fillLine(9, 'A');
        smallBuff.fillLine(0, 'A');

        assertEquals("AAAAAAAAAA", smallBuff.getLineAsString(9));
        assertEquals("AAAAAAAAAA", smallBuff.getLineAsString(0));

        ansiEscapeParser.process(seq);

        assertEquals("", smallBuff.getLineAsString(9));
        assertEquals("", smallBuff.getLineAsString(0));
    }

    @Test
    void saveAndSetCursorPositionTest() {
        final String saveCursor = "\u001b[s";
        final String setCursor = "\u001b[u";

        buffer.setCursorPosition(1, 0);
        ansiEscapeParser.process(saveCursor);
        buffer.setCursorPosition(3, 0);
        ansiEscapeParser.process(setCursor);

        assertEquals(1, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }
}
