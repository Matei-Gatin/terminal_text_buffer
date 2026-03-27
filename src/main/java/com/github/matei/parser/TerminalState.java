package com.github.matei.parser;

public enum TerminalState {
    GROUND,
    ESCAPE,
    CSI_ENTRY,
    CSI_PARAM;
}
