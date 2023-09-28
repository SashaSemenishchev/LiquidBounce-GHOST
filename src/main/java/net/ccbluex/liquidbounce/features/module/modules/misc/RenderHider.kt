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
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.vecmath.Vector2d
import javax.vecmath.Vector2f
import kotlin.math.min
import kotlin.math.roundToInt

object RenderHider : Module("RenderHider", ModuleCategory.MISC) {
    var window: RenderHiderWindow? = null
    override fun onEnable() {
        super.onEnable()
        notificationFont = Fonts.font35.defaultFont.font.deriveFont(25f)
        window?.dispose()
        window = RenderHiderWindow().also {
            if(mc.theWorld == null) return@also
            it.isVisible = true
        }
        notificationOffset = NOTIFICATION_OFFSET
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

    private const val NOTIFICATION_OFFSET = 40
    private var notificationOffset = NOTIFICATION_OFFSET
    private val delegatedNotifications = mutableListOf<Notification>()
    private val fontRenderContext = FontRenderContext(null, false, false)
    private lateinit var notificationFont: Font
    private fun renderNotification(notification: Notification) {
        val window = window ?: return
        val beginY = notification.delegatedY
        val g = window.graphics ?: return
        val textStart = mc.displayWidth - notification.textLength
        g.color = notification.color
        g.fillRect(textStart - 8, beginY, 8, NOTIFICATION_OFFSET)
        g.color = Color.BLACK
        g.fillRect(textStart, beginY, notification.textLength, NOTIFICATION_OFFSET)
        g.color = Color.WHITE
        g.font = notificationFont
        g.drawString(notification.message, textStart, beginY + (NOTIFICATION_OFFSET + notification.textHeight) / 2)
    }

    fun delegateNotification(notification: Notification) {
        val bounds = notificationFont.getStringBounds(notification.message, fontRenderContext)
        notification.textLength = bounds.width.roundToInt()
        notification.textHeight = bounds.height.roundToInt()
        notification.delegatedY = notificationOffset
        delegatedNotifications += notification
        notification.delegatedTimeSpent.reset()
        renderNotification(notification)
        notificationOffset += NOTIFICATION_OFFSET + 5

    }

    fun renderAllNotifications() {
        delegatedNotifications.removeIf {
            if (it.delegatedTimeSpent.hasTimePassed(1000)) {
                notificationOffset -= NOTIFICATION_OFFSET - 5
                return@removeIf true
            }
            renderNotification(it)
            return@removeIf false
        }
    }
}