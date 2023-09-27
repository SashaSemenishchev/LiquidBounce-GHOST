package net.ccbluex.liquidbounce.features.module.modules.misc

import me.mrunny.RenderHiderWindow
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.vecmath.Vector2d
import javax.vecmath.Vector2f
import kotlin.math.min

object RenderHider : Module("RenderHider", ModuleCategory.MISC) {
    var window: RenderHiderWindow? = null
    override fun onEnable() {
        super.onEnable()
        window?.dispose()
        window = RenderHiderWindow().also {
            if(mc.theWorld == null) return@also
            it.isVisible = true
        }
    }

    override fun onDisable() {
        window?.dispose()
        window = null
    }

    fun clearWindow() {
        val w = window ?: return
        w.graphics?.clearRect(0, 0, mc.displayWidth, mc.displayHeight)
        renderAllNotifications()
    }

    private const val NOTIFICATION_OFFSET = 20
    private var notificationOffset = 20
    private val delegatedNotifications = mutableListOf<Notification>()

    private fun renderNotification(notification: Notification) {
        val window = window ?: return
        val beginY = notificationOffset
        val g = window.graphics ?: return
        g.color = notification.color
        val textStart = mc.displayWidth - notification.textLength
        g.drawRect(textStart - 8, beginY, 8, NOTIFICATION_OFFSET)
        g.color = Color.BLACK
        g.drawRect(textStart, beginY, notification.textLength, NOTIFICATION_OFFSET)
        g.color = Color.WHITE
        g.font = Fonts.font35.defaultFont.font
        g.drawString(notification.message, textStart, beginY)
        notificationOffset += NOTIFICATION_OFFSET + 5
    }

    fun delegateNotification(notification: Notification) {
        delegatedNotifications += notification
        notification.delegatedTimeSpent.reset()
        renderNotification(notification)
    }

    fun renderAllNotifications() {
        delegatedNotifications.removeIf {
            if (it.delegatedTimeSpent.hasTimePassed(1000)) return@removeIf true
            renderNotification(it)
            return@removeIf false
        }
    }
}