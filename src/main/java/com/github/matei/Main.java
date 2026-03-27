package com.github.matei;

import com.github.matei.buffer.TerminalBuffer;
import com.github.matei.parser.AnsiEscapeParser;

public class Main {
    public static void main(String[] args) {
//        String seq = "\u001b[20;0H";
        String seq = "\u001b[31;44mHello\u001b[31;47mWorld";
        TerminalBuffer terminalBuffer = new TerminalBuffer(80, 24, 1000);
        AnsiEscapeParser ansiEscapeParser = new AnsiEscapeParser(terminalBuffer);
        ansiEscapeParser.process(seq);

        System.out.println(terminalBuffer.getCursorRow());
        System.out.println(terminalBuffer.getCursorCol());
        String line = terminalBuffer.getLineAsString(0);
        System.out.println(line);
    }
}