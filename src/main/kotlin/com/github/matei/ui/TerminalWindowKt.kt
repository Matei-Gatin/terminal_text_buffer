package com.github.matei.ui

import com.github.matei.buffer.TerminalBuffer
import com.github.matei.buffer.TerminalColor
import com.github.matei.buffer.TextAttributes
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.TextAttribute
import javax.swing.JFrame
import javax.swing.JPanel

class TerminalWindowKt(buffer: TerminalBuffer) : JFrame() {
    private val panel: TerminalPanel = TerminalPanel(buffer)

    init {
        title = "Java Terminal Emulator"
        defaultCloseOperation = EXIT_ON_CLOSE

        add(panel)
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }

    fun refresh() {
        panel.repaint()
    }

    class TerminalPanel(buffer: TerminalBuffer) : JPanel() {
        private val terminalBuffer: TerminalBuffer = buffer

        private val normalFont: Font = Font(Font.MONOSPACED, Font.PLAIN, 14)
        private val boldFont: Font = normalFont.deriveFont(Font.BOLD)
        private val italicFont: Font = normalFont.deriveFont(Font.ITALIC)
        private val underlineFont: Font = normalFont.deriveFont(
            mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON)
        )

        // TODO: support combinations between fonts

        init {
            background = Color.BLACK
            preferredSize = Dimension(terminalBuffer.width * 8, terminalBuffer.height * 16)
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)

            val g2d: Graphics2D = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

            val metrics: FontMetrics = g2d.getFontMetrics(normalFont)
            val charWidth: Int = metrics.charWidth('W')
            val charHeight: Int = metrics.height
            val fontAscent: Int = metrics.ascent

            for (row in 0..<terminalBuffer.height) {
                for (col in 0..<terminalBuffer.width) {
                    // coordinates
                    val x: Int = col * charWidth
                    val y: Int = row * charHeight

                    val c: Char = terminalBuffer.getCharAt(col, row)
                    val attrs: TextAttributes = terminalBuffer.getAttributesAt(col, row)

                    if (c == '\u0000') continue

                    val currentFont: Font = getTextStyle(attrs)
                    g2d.font = currentFont
                    g2d.color = getAwtColor(attrs.background, false)
                    g2d.fillRect(x, y, charWidth, charHeight)
                    g2d.color = getAwtColor(attrs.foreground, true)
                    g2d.drawString(c.toString(), x, y + fontAscent)
                }
            }
        }

        private fun getTextStyle(attrs: TextAttributes) : Font = when (attrs.styles) {
            1 -> boldFont
            2 -> italicFont
            3 -> underlineFont
            else -> normalFont
        }

        private fun getAwtColor(color: TerminalColor, isForeground: Boolean) : Color {
            if (color == TerminalColor.DEFAULT) return if (isForeground) Color.LIGHT_GRAY else Color.GRAY

            return when (color.index % 8) {
                1 -> Color.RED
                2 -> Color.GREEN
                3 -> Color.YELLOW
                4 -> Color.BLUE
                5 -> Color.MAGENTA
                6 -> Color.CYAN
                7 -> Color.WHITE
                else -> Color.BLACK
            }
        }
    }
}