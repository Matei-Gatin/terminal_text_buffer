package com.github.matei.pty;

import com.github.matei.parser.AnsiEscapeParser;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TerminalProcess {
    private PtyProcess ptyProcess;
    private Reader inputReader;
    private Writer outputWriter;
    private final AnsiEscapeParser parser;

    public TerminalProcess(AnsiEscapeParser parser) {
        this.parser = parser;
    }

    public void start() throws Exception {
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("TERM", "xterm-256color");

        String[] command = {"/bin/bash", "-i"};

        ptyProcess = new PtyProcessBuilder()
                .setCommand(command)
                .setEnvironment(env)
                .start();

        inputReader = new InputStreamReader(ptyProcess.getInputStream(), StandardCharsets.UTF_8);
        outputWriter = new OutputStreamWriter(ptyProcess.getOutputStream(), StandardCharsets.UTF_8);

        Thread readThread = new Thread(() -> {
            try {
                char[] buffer = new char[1024];
                int read;

                while ((read = inputReader.read(buffer)) != -1) {
                    String text = new String(buffer, 0, read);

                    parser.process(text);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        readThread.setDaemon(true); // Don't block JVM shutdown
        readThread.start();
    }

    public void sendInput(String input) {
        try {
            if (outputWriter != null) {
                outputWriter.write(input);
                outputWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (ptyProcess != null) {
            ptyProcess.destroy();
        }
    }
}
