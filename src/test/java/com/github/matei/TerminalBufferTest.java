package com.github.matei;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TerminalBufferTest {
    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(10, 5, 100);
    }

    // Construction
    @Nested
    class Construction {
        @Test
        void bufferHasCorrectDimensions() {
            assertEquals(10, buffer.getWidth());
            assertEquals(5, buffer.getHeight());
            assertEquals(100, buffer.getMaxScrollback());
        }

        @Test
        void newBufferHasEmptyScreen() {
            for (int row = 0; row < 5; row++) {
                assertEquals("", buffer.getLineAsString(row));
            }
        }

        @Test
        void newBufferCursorAtOrigin() {
            assertEquals(0, buffer.getCursorCol());
            assertEquals(0, buffer.getCursorRow());
        }

        @Test
        void newBufferHasNoScrollback() {
            assertEquals(0, buffer.getScrollbackSize());
        }

        @Test
        void invalidWidthThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TerminalBuffer(0, 5, 10));
        }

        @Test
        void invalidHeightThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TerminalBuffer(10, 0, 10));
        }

        @Test
        void negativeScrollbackThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TerminalBuffer(10, 5, -1));
        }

        @Test
        void zeroScrollbackIsValid() {
            TerminalBuffer buf = new TerminalBuffer(10, 5, 0);
            assertEquals(0, buf.getMaxScrollback());
        }
    }

    // Cursor
    @Nested
    class Cursor {
        @Test
        void setCursorPosition() {
            buffer.setCursorPosition(5, 3);
            assertEquals(5, buffer.getCursorCol());
            assertEquals(3, buffer.getCursorRow());
        }

        @Test
        void setCursorClampsToUpperBounds() {
            buffer.setCursorPosition(100, 100);
            assertEquals(9, buffer.getCursorCol());
            assertEquals(4, buffer.getCursorRow());
        }

        @Test
        void setCursorClampsToLowerBounds() {
            buffer.setCursorPosition(-5, -5);
            assertEquals(0, buffer.getCursorCol());
            assertEquals(0, buffer.getCursorRow());
        }

        @Test
        void moveCursorUp() {
            buffer.setCursorPosition(0, 3);
            buffer.moveCursorUp(2);
            assertEquals(1, buffer.getCursorRow());
        }

        @Test
        void moveCursorUpClampsAtZero() {
            buffer.setCursorPosition(0, 1);
            buffer.moveCursorUp(10);
            assertEquals(0, buffer.getCursorRow());
        }

        @Test
        void moveCursorDown() {
            buffer.setCursorPosition(0, 1);
            buffer.moveCursorDown(2);
            assertEquals(3, buffer.getCursorRow());
        }

        @Test
        void moveCursorDownClampsAtBottom() {
            buffer.setCursorPosition(0, 3);
            buffer.moveCursorDown(10);
            assertEquals(4, buffer.getCursorRow());
        }

        @Test
        void moveCursorLeft() {
            buffer.setCursorPosition(5, 0);
            buffer.moveCursorLeft(3);
            assertEquals(2, buffer.getCursorCol());
        }

        @Test
        void moveCursorLeftClampsAtZero() {
            buffer.setCursorPosition(2, 0);
            buffer.moveCursorLeft(10);
            assertEquals(0, buffer.getCursorCol());
        }

        @Test
        void moveCursorRight() {
            buffer.setCursorPosition(3, 0);
            buffer.moveCursorRight(4);
            assertEquals(7, buffer.getCursorCol());
        }

        @Test
        void moveCursorRightClampsAtEnd() {
            buffer.setCursorPosition(8, 0);
            buffer.moveCursorRight(10);
            assertEquals(9, buffer.getCursorCol());
        }
    }

    // Attributes
    @Nested
    class Attributes {
        @Test
        void defaultAttributesAreDefault() {
            assertEquals(TextAttributes.DEFAULT, buffer.getCurrentAttributes());
        }

        @Test
        void setCurrentAttributesPersists() {
            TextAttributes attrs = new TextAttributes(
                    TerminalColor.RED, TerminalColor.BLUE, TextStyle.BOLD
            );
            buffer.setCurrentAttributes(attrs);
            assertEquals(attrs, buffer.getCurrentAttributes());
        }

        @Test
        void writtenTextUsesCurrentAttributes() {
            TextAttributes attrs = new TextAttributes(
                    TerminalColor.GREEN, TerminalColor.DEFAULT, TextStyle.ITALIC
            );
            buffer.setCurrentAttributes(attrs);
            buffer.writeText("Hi");

            assertEquals(attrs, buffer.getAttributesAt(0, 0));
            assertEquals(attrs, buffer.getAttributesAt(1, 0));
        }
    }

    // Writing
    @Nested
    class Writing {
        @Test
        void writeSimpleText() {
            buffer.writeText("Hello");
            assertEquals("Hello", buffer.getLineAsString(0));
        }

        @Test
        void writeAdvancedCursor() {
            buffer.writeText("Hi");
            assertEquals(2, buffer.getCursorCol());
            assertEquals(0, buffer.getCursorRow());
        }

        @Test
        void writeAtCursorPosition() {
            buffer.setCursorPosition(3, 1);
            buffer.writeText("AB");
            assertEquals("   AB", buffer.getLineAsString(1));
        }

        @Test
        void writeOverwritesExistingContent() {
            buffer.writeText("AAAAA");
            buffer.setCursorPosition(1, 0);
            buffer.writeText("BB");
            assertEquals("ABBAA", buffer.getLineAsString(0));
        }

        @Test
        void writeWrapsToNextLine() {
            buffer.setCursorPosition(8, 0);
            buffer.writeText("ABCDE");
            assertEquals("        AB", buffer.getLineAsString(0));
            assertEquals("CDE", buffer.getLineAsString(1));
            assertEquals(3, buffer.getCursorCol());
            assertEquals(1, buffer.getCursorRow());
        }

        @Test
        void writeScrollsWhenAtBottom() {
            // fills all rows
            for (int i = 0; i < 5; i++) {
                buffer.setCursorPosition(0, i);
                buffer.writeText("Line" + i);
            }
            // Write at end of last row => triggers wrap + scroll
            buffer.setCursorPosition(8, 4);
            buffer.writeText("XXYYY");

            // Line0 now in scrollback
            assertEquals(1, buffer.getScrollbackSize());
            assertEquals("Line0", buffer.getLineAsString(-1));

            // Screen is shifted up
            assertEquals("Line1", buffer.getLineAsString(0));
            assertEquals("YYY", buffer.getLineAsString(4));
        }

        @Test
        void getCharAtReturnsCorrectCharacter() {
            buffer.writeText("ABCDE");
            assertEquals('A', buffer.getCharAt(0, 0));
            assertEquals('C', buffer.getCharAt(2, 0));
            assertEquals('E', buffer.getCharAt(4, 0));
        }

        @Test
        void getCharAtEmptyCell() {
            assertEquals(TerminalCell.EMPTY_CHAR, buffer.getCharAt(0, 0));
        }
    }

    // Inserting
    @Nested
    class Inserting {

        @Test
        void insertShiftsExistingContent() {
            buffer.writeText("ABCDE");
            buffer.setCursorPosition(2, 0);
            buffer.insertText("XX");
            assertEquals("ABXXCDE", buffer.getLineAsString(0));
        }

        @Test
        void insertAdvancesCursor() {
            buffer.insertText("Hi");
            assertEquals(2, buffer.getCursorCol());
            assertEquals(0, buffer.getCursorRow());
        }

        @Test
        void insertWrapsToNextLine() {
            buffer.setCursorPosition(9, 0);
            buffer.insertText("ABC");
            assertEquals(2, buffer.getCursorCol());
            assertEquals(1, buffer.getCursorRow());
        }
    }

    // Fill line
    @Nested
    class FillLine {

        @Test
        void fillLineWithCharacter() {
            buffer.fillLine(0, '-');
            assertEquals("----------", buffer.getLineAsString(0));
        }

        @Test
        void fillLineDoesNotMoveCursor() {
            buffer.setCursorPosition(3, 2);
            buffer.fillLine(0, 'X');
            assertEquals(3, buffer.getCursorCol());
            assertEquals(2, buffer.getCursorRow());
        }

        @Test
        void fillLineUsesCurrentAttributes() {
            TextAttributes attrs = new TextAttributes(
                    TerminalColor.RED, TerminalColor.DEFAULT, TextStyle.BOLD
            );
            buffer.setCurrentAttributes(attrs);
            buffer.fillLine(0, '#');
            assertEquals(attrs, buffer.getAttributesAt(0, 0));
            assertEquals(attrs, buffer.getAttributesAt(9, 0));
        }

        @Test
        void fillLineInvalidRowThrows() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.fillLine(-1, 'X'));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.fillLine(5, 'X'));
        }
    }

    // Insert empty line
    @Nested
    class InsertEmptyLine {

        @Test
        void insertEmptyLineAtBottomScrollsUp() {
            buffer.writeText("First");
            buffer.insertEmptyLineAtBottom();

            assertEquals(1, buffer.getScrollbackSize());
            assertEquals("First", buffer.getLineAsString(-1));
            assertEquals("", buffer.getLineAsString(0));
        }

        @Test
        void insertEmptyLinePreservesOtherContent() {
            buffer.setCursorPosition(0, 0);
            buffer.writeText("Line0");
            buffer.setCursorPosition(0, 1);
            buffer.writeText("Line1");
            buffer.setCursorPosition(0, 2);
            buffer.writeText("Line2");

            buffer.insertEmptyLineAtBottom();

            // Line0 went to scrollback, Lines 1-2 shifted up
            assertEquals("Line0", buffer.getLineAsString(-1));
            assertEquals("Line1", buffer.getLineAsString(0));
            assertEquals("Line2", buffer.getLineAsString(1));
            assertEquals("", buffer.getLineAsString(2));
        }
    }

    // Scrollback
    @Nested
    class Scrollback {
        @Test
        void scrollbackGrowsAsLinesAreScrolledOff() {
            for (int i = 0; i < 8; i++) {
                buffer.setCursorPosition(0, 0);
                buffer.insertEmptyLineAtBottom();
            }

            assertEquals(8, buffer.getScrollbackSize());
        }

        @Test
        void scrollbackRespectsMaxSize() {
            TerminalBuffer smallBuff = new TerminalBuffer(10, 3, 5);
            for (int i = 0; i < 10; i++) {
                smallBuff.setCursorPosition(0, 0);
                smallBuff.writeText("Line" + i);
                smallBuff.insertEmptyLineAtBottom();
            }

            assertEquals(5, smallBuff.getScrollbackSize());
        }

        @Test
        void scrollbackOldestLinesAreDropped() {
            TerminalBuffer smallBuf = new TerminalBuffer(10, 2, 3);
            for (int i = 0; i < 6; i++) {
                smallBuf.setCursorPosition(0, 0);
                smallBuf.writeText("L" + i);
                smallBuf.insertEmptyLineAtBottom();
            }
            assertEquals(3, smallBuf.getScrollbackSize());
            assertEquals("L3", smallBuf.getLineAsString(-3));
            assertEquals("L5", smallBuf.getLineAsString(-1));
        }

        @Test
        void scrollbackZeroMaxDiscardsImmediately() {
            TerminalBuffer noBuf = new TerminalBuffer(10, 3, 0);
            noBuf.writeText("Hello");
            noBuf.insertEmptyLineAtBottom();
            assertEquals(0, noBuf.getScrollbackSize());
        }

        @Test
        void scrollbackNegativeIndexOutOfBoundsThrows() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.getLineAsString(-1));
        }

        @Test
        void scrollbackAccessFromScreen() {
            buffer.writeText("Hello");
            buffer.insertEmptyLineAtBottom();

            // Screen row 0 is now empty
            assertEquals("", buffer.getLineAsString(0));
            // Scrollback row -1 has the content
            assertEquals("Hello", buffer.getLineAsString(-1));
            assertEquals('H', buffer.getCharAt(0, -1));
        }
    }

    // Clear
    @Nested
    class Clear {

        @Test
        void clearScreenResetsAllLines() {
            buffer.writeText("Hello");
            buffer.setCursorPosition(0, 2);
            buffer.writeText("World");

            buffer.clearScreen();

            for (int row = 0; row < 5; row++) {
                assertEquals("", buffer.getLineAsString(row));
            }
        }

        @Test
        void clearScreenResetsCursor() {
            buffer.setCursorPosition(5, 3);
            buffer.clearScreen();
            assertEquals(0, buffer.getCursorCol());
            assertEquals(0, buffer.getCursorRow());
        }

        @Test
        void clearScreenPreservesScrollback() {
            buffer.writeText("Old");
            buffer.insertEmptyLineAtBottom();
            buffer.writeText("New");

            buffer.clearScreen();

            assertEquals(1, buffer.getScrollbackSize());
            assertEquals("Old", buffer.getLineAsString(-1));
        }

        @Test
        void clearAllResetsEverything() {
            buffer.writeText("Text");
            buffer.insertEmptyLineAtBottom();
            buffer.writeText("More");

            buffer.clearAll();

            assertEquals(0, buffer.getScrollbackSize());
            for (int row = 0; row < 5; row++) {
                assertEquals("", buffer.getLineAsString(row));
            }
            assertEquals(0, buffer.getCursorCol());
            assertEquals(0, buffer.getCursorRow());
        }
    }

    // Content Access
    @Nested
    class ContentAccess {
        @Test
        void getScreenContent() {
            buffer.setCursorPosition(0, 0);
            buffer.writeText("Line1");
            buffer.setCursorPosition(0, 1);
            buffer.writeText("Line2");

            String content = buffer.getStringContent();
            String[] lines = content.split("\n");
            assertEquals("Line1", lines[0]);
            assertEquals("Line2", lines[1]);
        }

        @Test
        void getFullContentIncludingScrollback() {
            buffer.setCursorPosition(0, 0);
            buffer.writeText("First");
            buffer.insertEmptyLineAtBottom();
            buffer.setCursorPosition(0, 0);
            buffer.writeText("Second");

            String fullContent = buffer.getFullContent();
            assertTrue(fullContent.startsWith("First"));
            assertTrue(fullContent.contains("Second"));
        }

        @Test
        void getFullContentEmptyBuffer() {
            String content = buffer.getFullContent();
            assertEquals("\n\n\n\n", content);
        }

        @Test
        void screenRowOutOfBoundsThrows() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.getCharAt(0, 5));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.getCharAt(0, -1));
        }

        @Test
        void columnOutOfBoundsThrows() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.getCharAt(-1, 0));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> buffer.getCharAt(10, 0));
        }
    }
}
