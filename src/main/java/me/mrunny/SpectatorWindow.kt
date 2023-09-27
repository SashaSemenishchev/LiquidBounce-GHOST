package me.mrunny

import net.ccbluex.liquidbounce.features.module.Module
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.time.LocalDateTime
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea


class SpectatorWindow(private val respectiveModule: Module) : JFrame() {

    private val logs: JTextArea = JTextArea().also { it.isEditable = false }

    init {
        title = "Spectator"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(800, 400)
        layout = GridLayout(1, 1)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                respectiveModule.state = false
            }
        })
        val scroll = JScrollPane(logs)
        add(scroll, BorderLayout.CENTER)
    }

    fun log(date: LocalDateTime, message: String) {
        logs.append("[${date.toLocalTime()}] $message\n")
    }
}