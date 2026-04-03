package com.github.matei.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.matei.buffer.TerminalColor;
import com.github.matei.buffer.TextAttributes;
import com.github.matei.buffer.TextStyle;
import com.github.matei.emulator.TerminalEmulator;
import com.github.matei.util.Constants;

public class AnsiEscapeParser {
    private TerminalState state = TerminalState.GROUND;

    private final List<Integer> parameters = new ArrayList<>();
    private int currentParam = 0;

    private final TerminalEmulator terminalEmulator;

    private final Map<Character, Consumer<List<Integer>>> commandRegistry = new HashMap<>();
    private final int[] cursorPosSnapshot  = new int[2]; // row -> 0, col -> 1

    public AnsiEscapeParser(TerminalEmulator terminalEmulator) {
        commandRegistry.put('A', this::moveCursorUp);
        commandRegistry.put('B', this::moveCursorDown);
        commandRegistry.put('C', this::moveCursorForward);
        commandRegistry.put('D', this::moveCursorBack);
        commandRegistry.put('E', this::moveCursorNextLine);
        commandRegistry.put('F', this::moveCursorPreviousLine);
        commandRegistry.put('G', this::moveCursorHorizontal);
        commandRegistry.put('f', this::moveCursorHorizontal);
        commandRegistry.put('H', this::moveCursorToPosition);
        commandRegistry.put('J', this::eraseInDisplay);
        commandRegistry.put('K', this::eraseInLine);
        commandRegistry.put('S', this::scrollUp);
        commandRegistry.put('T', this::scrollDown);
        commandRegistry.put('s', this::saveCursorPosition);
        commandRegistry.put('u', this::restoreCursorPosition);

        commandRegistry.put('m', this::setGraphicsMode);

        this.terminalEmulator = terminalEmulator;
    }

    public void process(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (state) {
                case GROUND -> {
                    if (c == Constants.ESCAPE) {
                        state = TerminalState.ESCAPE;
                    } else {
                        terminalEmulator.writeText(String.valueOf(c)); // TODO: Fix this method to write String
                    }
                }

                case ESCAPE -> {
                    if (c == Constants.OPENING_BRACKET) {
                        state = TerminalState.CSI_ENTRY;
                        parameters.clear();
                        currentParam = 0;
                    } else if (c == ']') {
                        state = TerminalState.OSC_STRING;
                    } else {
                        state = TerminalState.GROUND;
                    }
                }

                case OSC_STRING -> {

                    if (c == '\u0007') { // OSC strings end with BEL or ST
                        state = TerminalState.GROUND;
                    } else if (c == Constants.ESCAPE) {
                        state = TerminalState.ESCAPE;
                    }
                    // Just eat character
                }

                case CSI_ENTRY, CSI_PARAM -> {
                    if (Character.isDigit(c)) {
                        state = TerminalState.CSI_PARAM;
                        currentParam = (currentParam * 10) + Character.getNumericValue(c);

                    } else if (c == ';') {
                        parameters.add(currentParam);
                        currentParam = 0;
                        state = TerminalState.CSI_PARAM;

                    } else if (c == '?' || c == '>' || c == '=' || c == '<') {
                        // Parameter modifiers (like ?2004h), are ignored for now.
                        state = TerminalState.CSI_PARAM;
                    } else {
                        parameters.add(currentParam);

                        Consumer<List<Integer>> action = commandRegistry.get(c);

                        if (action != null) {
                            action.accept(new ArrayList<>(parameters));
                        } else {
                            System.out.println("Unknown ANSI command");
                        }

                        state = TerminalState.GROUND;
                    }
                }
            }
        }
    }

    // === Helper methods ===
    private void setGraphicsMode(List<Integer> params) {
        if (params.isEmpty() || (params.size() == 1 && params.get(0) == 0)) {
            terminalEmulator.setCurrentAttributes(TextAttributes.DEFAULT);
            return;
        }

        for (int paramVal : params) {
            // Foreground color (codes 30 - 37)
            if (paramVal >= 30 && paramVal <= 37) {
                TerminalColor newFg = TerminalColor.fromIndex(paramVal - 30);

                terminalEmulator.setCurrentAttributes(new TextAttributes(
                        newFg,
                        terminalEmulator.getCurrentAttributes().getBackground(),
                        terminalEmulator.getCurrentAttributes().getStyles()
                ));
            // Foreground Bright color (codes 90 - 97)
            } else if (paramVal >= 90 && paramVal <= 97) {
                TerminalColor newBrightFg = TerminalColor.fromIndex((paramVal - 90) + 8);

                terminalEmulator.setCurrentAttributes(new TextAttributes(
                        newBrightFg,
                        terminalEmulator.getCurrentAttributes().getBackground(),
                        terminalEmulator.getCurrentAttributes().getStyles()
                ));

            // Background color (codes 40 - 47)
            } else if (paramVal >= 40 && paramVal <= 47) {
                TerminalColor newBg = TerminalColor.fromIndex(paramVal - 40);

                terminalEmulator.setCurrentAttributes(new TextAttributes(
                        terminalEmulator.getCurrentAttributes().getForeground(),
                        newBg,
                        terminalEmulator.getCurrentAttributes().getStyles()
                ));

            // Background Bright color (codes 90 - 97)
            } else if (paramVal >= 100 && paramVal <= 107) {
                TerminalColor newBrightFg = TerminalColor.fromIndex((paramVal - 100) + 8);

                terminalEmulator.setCurrentAttributes(new TextAttributes(
                        terminalEmulator.getCurrentAttributes().getForeground(),
                        newBrightFg,
                        terminalEmulator.getCurrentAttributes().getStyles()
                ));

            // Styles
            } else {
                switch (paramVal) {
                    case 0 -> terminalEmulator.setCurrentAttributes(TextAttributes.DEFAULT);

                    case 1 -> terminalEmulator.setCurrentAttributes(new TextAttributes(
                            terminalEmulator.getCurrentAttributes().getForeground(),
                            terminalEmulator.getCurrentAttributes().getBackground(),
                            TextStyle.BOLD));

                    case 3 -> terminalEmulator.setCurrentAttributes(new TextAttributes(
                            terminalEmulator.getCurrentAttributes().getForeground(),
                            terminalEmulator.getCurrentAttributes().getBackground(),
                            TextStyle.ITALIC
                    ));

                    case 4 -> terminalEmulator.setCurrentAttributes(new TextAttributes(
                            terminalEmulator.getCurrentAttributes().getForeground(),
                            terminalEmulator.getCurrentAttributes().getBackground(),
                            TextStyle.UNDERLINE
                    ));
                }
            }
        }
    }

    private void moveCursorUp(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.moveCursorUp(n);
    }

    private void moveCursorDown(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.moveCursorDown(n);
    }

    private void moveCursorForward(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.moveCursorRight(n);
    }

    private void moveCursorBack(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.moveCursorLeft(n);
    }

    private void moveCursorNextLine(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.moveCursorDown(n);
        terminalEmulator.setCursorPosition(0, terminalEmulator.getCursorRow());
    }

    private void moveCursorPreviousLine(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.moveCursorUp(n);
        terminalEmulator.setCursorPosition(0, terminalEmulator.getCursorRow());
    }

    private void moveCursorHorizontal(List<Integer> params) {
        int col = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalEmulator.setCursorPosition(col - 1, terminalEmulator.getCursorRow());
    }

    private void moveCursorToPosition(List<Integer> params) {
        int row = !params.isEmpty() && params.get(0) > 0 ? params.get(0) : 1;
        int col = params.size() > 1 && params.get(1) > 0 ? params.get(1) : 1;
        terminalEmulator.setCursorPosition(col - 1, row - 1);
    }

    private void eraseInDisplay(List<Integer> params) {
        int n = params.isEmpty() ? 0 : params.get(0);
        terminalEmulator.eraseInDisplay(n);
    }

    private void eraseInLine(List<Integer> params) {
        int n = params.isEmpty() ? 0 : params.get(0);
        terminalEmulator.eraseInLine(n);
    }

    private void scrollUp(List<Integer> params) {
        int n = params.isEmpty() ? 0 : params.get(0);
        terminalEmulator.scrollUp(n);
    }

    private void scrollDown(List<Integer> params) {
        int n = params.isEmpty() ? 0 : params.get(0);
        terminalEmulator.scrollDown(n);
    }

    private void saveCursorPosition(List<Integer> params) {
        int currentRow = terminalEmulator.getCursorRow();
        int currentCol = terminalEmulator.getCursorCol();

        cursorPosSnapshot[0] = currentRow;
        cursorPosSnapshot[1] = currentCol;
    }

    private void restoreCursorPosition(List<Integer> params) {
        if (cursorPosSnapshot.length != 2) return;

        int savedRow = cursorPosSnapshot[0];
        int savedCol = cursorPosSnapshot[1];

        terminalEmulator.setCursorPosition(savedCol, savedRow);
    }
}
