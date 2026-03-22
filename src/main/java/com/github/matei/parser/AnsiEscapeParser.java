package com.github.matei.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.matei.buffer.TerminalBuffer;
import com.github.matei.buffer.TextAttributes;
import com.github.matei.buffer.TextStyle;
import com.github.matei.util.Constants;

public class AnsiEscapeParser {
    private TerminalState state = TerminalState.GROUND;

    private final List<Integer> parameters = new ArrayList<>();
    private int currentParam = 0;

    private final TerminalBuffer terminalBuffer = new TerminalBuffer(80, 24, 1000);

    public void process(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (state) {
                case GROUND -> {
                    if (c == Constants.ESCAPE) {
                        state = TerminalState.ESCAPE;
                    } else {
                        terminalBuffer.writeText(String.valueOf(c));
                    }
                }

                case ESCAPE -> {
                    if (c == Constants.OPENING_BRACKET) {
                        state = TerminalState.CSI_ENTRY;
                        parameters.clear();
                        currentParam = 0;
                    } else {
                        state = TerminalState.GROUND;
                    }
                }

                case CSI_ENTRY, CSI_PARAM -> {
                    if (Character.isDigit(c)) {
                        state = TerminalState.CSI_PARAM;
                        currentParam = (currentParam * 10) + Character.getNumericValue(c);

                    } else if (c == ';') {
                        parameters.add(currentParam);
                        currentParam = 0;
                        state = TerminalState.CSI_PARAM;

                    } else {
                        parameters.add(currentParam);

                        // Execute the command
                        if (c == 'A') {
                            moveCursorUp(parameters);
                        } else if (c == 'm') {
                            applyColor(parameters);
                        }

                        state = TerminalState.GROUND;
                    }
                }
            }
        }
    }

    private void applyColor(List<Integer> params) {
        if (params.isEmpty() || (params.size() == 1 && params.get(0) == 0)) {
            terminalBuffer.setCurrentAttributes(TextAttributes.DEFAULT);
            return;
        }

        for (int paramVal : params) {
            switch (paramVal) {
                case 0 -> terminalBuffer.setCurrentAttributes(TextAttributes.DEFAULT);

                case 1 -> terminalBuffer.setCurrentAttributes(new TextAttributes(
                        terminalBuffer.getCurrentAttributes().getForeground(),
                        terminalBuffer.getCurrentAttributes().getBackground(),
                        TextStyle.BOLD));

                case 3 -> terminalBuffer.setCurrentAttributes(new TextAttributes(
                        terminalBuffer.getCurrentAttributes().getForeground(),
                        terminalBuffer.getCurrentAttributes().getBackground(),
                        TextStyle.ITALIC
                ));

                case 4 -> terminalBuffer.setCurrentAttributes(new TextAttributes(
                        terminalBuffer.getCurrentAttributes().getForeground(),
                        terminalBuffer.getCurrentAttributes().getBackground(),
                        TextStyle.UNDERLINE
                ));

                // TODO: Add support for colors (30-37 for Foreground, 40-47 for Background)
            }
        }
    }

    private void moveCursorUp(List<Integer> params) {
        int n = params.isEmpty() || params.get(0) == 0 ? 1 : params.get(0);
        terminalBuffer.moveCursorUp(n);
    }
}
