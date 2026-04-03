package com.github.matei.ui;

import com.github.matei.buffer.*;
import com.github.matei.pty.TerminalProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class TerminalWindow extends JFrame {
    private final TerminalPanel panel;

    public TerminalWindow(TerminalProcess ptyProcess, TerminalBuffer buffer) throws Exception {
        setTitle("Java Terminal Emulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.panel = new TerminalPanel(ptyProcess, buffer);
        add(panel);
        setVisible(true);

        pack();
        setLocationRelativeTo(null);

        ptyProcess.start();
    }

    static class TerminalPanel extends JPanel implements ActionListener, KeyListener {
        private final TerminalBuffer buffer;
        private final TerminalProcess ptyProcess;
        //
        private final Map<? extends AttributedCharacterIterator.Attribute, Object> underlineFontMap
                = Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

        private final Font normalFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        private final Font boldFont = normalFont.deriveFont(Font.BOLD);
        private final Font italicFont = normalFont.deriveFont(Font.ITALIC);
        private final Font underlineFont = normalFont.deriveFont(
                Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
        );
        private final Font boldItalicFont = normalFont.deriveFont(Font.ITALIC | Font.BOLD);
        private final Font boldUnderlineFont = boldFont.deriveFont(underlineFontMap);
        private final Font italicUnderlineFont = italicFont.deriveFont(underlineFontMap);

        //
        private final Timer timer;

        TerminalPanel(TerminalProcess ptyProcess, TerminalBuffer buffer) {
            this.buffer = buffer;
            this.ptyProcess = ptyProcess;

            setBackground(Color.BLACK);

            setPreferredSize(new Dimension(buffer.getWidth() * 8, buffer.getHeight() * 16)); // assume chars are 8 by 16

            this.addKeyListener(this);

            timer = new Timer(16, this);
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics metrics = g2d.getFontMetrics(normalFont);
            int charWidth = metrics.charWidth('W');
            int charHeight = metrics.getHeight();
            int fontAscent = metrics.getAscent();

            for (int row = 0; row < buffer.getHeight(); row++) {
                for (int col = 0; col < buffer.getWidth(); col++) {
                    // coordinates to draw character
                    int x = col * charWidth;
                    int y = row * charHeight;
                    //

                    char c = buffer.getCharAt(col, row);
                    TextAttributes attrs = buffer.getAttributesAt(col, row);

                    if (c == '\0') continue;

                    Font currentFont = getTextStyle(attrs);
                    g2d.setFont(currentFont); // set the font
                    g2d.setColor(getAwtColor(attrs.getBackground(), false)); // paintbrush to background color
                    g2d.fillRect(x, y, charWidth, charHeight); // fill cell background block
                    g2d.setColor(getAwtColor(attrs.getForeground(), true)); // paintbrush to foreground color
                    g2d.drawString(String.valueOf(c), x, y + fontAscent);
                }
            }
        }

        // this method is invoked/called by the timer
        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            String c = String.valueOf(e.getKeyChar());

            ptyProcess.sendInput(c);
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }

        // === Helper Methods ===
        private Font getTextStyle(TextAttributes attrs) {
            return switch (attrs.getStyles()) {
                case 1 -> boldFont;
                case 2 -> italicFont;
                case 3 -> boldItalicFont;
                case 4 -> underlineFont;
                case 5 -> boldUnderlineFont;
                case 6 -> italicUnderlineFont;
                default -> normalFont;
            };
        }

        private Color getAwtColor(TerminalColor terminalColor, boolean isForeground) {
            if (terminalColor == TerminalColor.DEFAULT) {
                return isForeground ? Color.LIGHT_GRAY : Color.GRAY;
            }

            return switch (terminalColor.getIndex() % 8) {
                case 1 -> Color.RED;
                case 2 -> Color.GREEN;
                case 3 -> Color.YELLOW;
                case 4 -> Color.BLUE;
                case 5 -> Color.MAGENTA;
                case 6 -> Color.CYAN;
                case 7 -> Color.WHITE;
                default -> Color.BLACK;
            };
        }
    }
}
