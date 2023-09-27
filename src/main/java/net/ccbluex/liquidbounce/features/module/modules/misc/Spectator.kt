package net.ccbluex.liquidbounce.features.module.modules.misc

import me.mrunny.SpectatorWindow
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Date
import javax.swing.SwingUtilities

object Spectator : Module("Spectator", ModuleCategory.MISC) {
    var spectatorWindow: SpectatorWindow? = null
    override fun onEnable() {
        SwingUtilities.invokeLater {
            spectatorWindow?.dispose()
            spectatorWindow = SpectatorWindow(this).also {
                it.isVisible = true
            }
        }
    }

    override fun onDisable() {
        SwingUtilities.invokeLater {
            spectatorWindow?.dispose()
            spectatorWindow = null
        }
    }

    fun log(message: String) {
        println("Spectator logged: [${LocalDateTime.now()}] $message")
    }
}