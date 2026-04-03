package com.github.matei;

import com.github.matei.buffer.TerminalBuffer;
import com.github.matei.parser.AnsiEscapeParser;
import com.github.matei.pty.TerminalProcess;
import com.github.matei.ui.TerminalWindow;
import com.github.matei.ui.TerminalWindowKt;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        AnsiEscapeParser ansiEscapeParser = new AnsiEscapeParser(buffer);
        TerminalProcess process = new TerminalProcess(ansiEscapeParser);
        TerminalWindow window = new TerminalWindow(process, buffer);

//        ansiEscapeParser.process("\u001b[31mHello \u001b[1;4;32mWorld\u001b[0m'\n");

//        try (ScheduledExecutorService refresher = Executors.newSingleThreadScheduledExecutor()) {
//            refresher.scheduleAtFixedRate(
//                    window::refresh,
//                    0,
//                    16,
//                    TimeUnit.MILLISECONDS
//            );
//        }

//        process.start();
//
//        Thread.sleep(1000);
//
//        System.out.println("Sending command...");
//
//        process.sendInput("echo -e '\\u001b[31mHello \\u001b[32mWorld\\u001b[0m'\n");
//
//        Thread.sleep(1000);
//
//        System.out.println("\n=== TERMINAL BUFFER SCREEN ===");
//
//        System.out.println(buffer.getLineAsAnsiString(0));
//        System.out.println(buffer.getLineAsAnsiString(1));
//
////        for (int i = 0; i < buffer.getHeight(); i++) {
////            System.out.println(buffer.getLineAsAnsiString(i));
////        }
//
//        System.out.println("=".repeat(30));
//
//        process.destroy();
    }
}