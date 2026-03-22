# Terminal Text Buffer

A core terminal text buffer implementation in Java, designed to replicate the foundational data structures and logic used inside real terminal emulators.

This project manages a configurable 2D screen grid, cursor state, string insertions, and a bounded scrollback history, accurately reflecting standard terminal margin and layout behaviors.

## Key Features

- **Text Navigation & Bounding**: Handles automatic line wrapping, forced wrapping, and boundary checking for terminal widths and heights.
- **Scrollback Buffer**: Efficient `ArrayDeque`-based FIFO scrollback history that seamlessly absorbs terminal rows pushed off the top margin.
- **Advanced Text Editing**: Replicates standard ICH (Insert Character) and write sequence flows.
- **Bitmask Styling System**: Lightweight style representation strictly using bitwise operators (`BOLD = 1`, `ITALIC = 1 << 1`) alongside standard ANSI colors.

## Wide Character Support (Bonus)
A major focal point of this buffer is the robust handling of **double-width elements** like CJK ideographs and Emojis.
- Relies on custom East Asian Width (`CharWidthUtil`) logic mapped to specific Unicode codepoints instead of simplistic `String.length()`.
- Implements `wideContinuation` flags across the internal `TerminalCell[]` array to track invisible "right halves" of wide elements.
- Handles complex 2D clipping scenarios (e.g. sheared Emojis at the right margin correctly resolve to literal spacing).

## Core Architecture Design
- `TerminalCell`: The smallest mutable state, heavily reused to avoid Object allocation sprawl.
- `TerminalLine`: A 1D array of cells providing exact O(1) shifting arithmetic for inserts.
- `TerminalBuffer`: The 2D orchestrator.

## Testing
The `TerminalBuffer` is thoroughly evaluated across complex array-boundary and line-shear edge cases using **JUnit 5**.
