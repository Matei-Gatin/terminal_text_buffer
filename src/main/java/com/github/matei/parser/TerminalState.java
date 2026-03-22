package com.github.matei.parser;

import java.lang.management.GarbageCollectorMXBean;

public enum TerminalState {
    GROUND,
    ESCAPE,
    CSI_ENTRY,
    CSI_PARAM;

    public TerminalState terminalState;

    public TerminalState getCurrentState() {
        return terminalState;
    }

    public void changeState(TerminalState terminalState) {
        if (this.terminalState == terminalState) {
            System.err.println("Cannot change state to: " + terminalState + "Current state is already: " + this.terminalState);
            return;
        }

        this.terminalState = terminalState;
    }
}
