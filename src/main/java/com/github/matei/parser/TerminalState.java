package com.github.matei.parser;

public enum TerminalState {
    GROUND,
    ESCAPE,
    CSI_ENTRY,
    CSI_PARAM,
    OSC_STRING; // Operating System Commands like window titles
}
