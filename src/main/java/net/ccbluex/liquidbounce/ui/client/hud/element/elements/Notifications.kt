/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements


import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.misc.RenderHider
import net.ccbluex.liquidbounce.features.module.modules.misc.Spectator
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import org.apache.commons.lang3.time.StopWatch
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Example Notification", true)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        HUD.notifications.firstOrNull()?.drawNotification()

        if (mc.currentScreen is GuiHudDesigner) {
            if (exampleNotification !in HUD.notifications)
                addNotification(exampleNotification)

            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-95F, -20F, 0F, 0F)
        }

        return null
    }

}

class Notification(val message: String, private val okToShowWhenLocked: Boolean=false, val color: Color=LiquidBounce.color) {
    var x = 0F
    var textLength = 0

    private var stay = 0F
    private var fadeStep = 0F
    val delegatedTimeSpent = MSTimer()
    var fadeState = FadeState.IN

    /**
     * Fade state for animation
     */
    enum class FadeState { IN, STAY, OUT, END }

    init {
        textLength = Fonts.font35.getStringWidth(message)
    }

    /**
     * Draw notification
     */
    fun drawNotification() {
        // Draw notification
        val spectator = Spectator
        if(spectator.state) {
            spectator.log(message)
        }
        if(!okToShowWhenLocked && LiquidBounce.isLocked) {
            HUD.removeNotification(this)
            RenderHider.delegateNotification(this)
            return
        }
        drawRect(-x + 8 + textLength, 0F, -x, -20F, Color.BLACK.rgb)
        drawRect(-x, 0F, -x - 5, -20F, color.rgb)
        Fonts.font35.drawString(message, -x + 4, -14F, Int.MAX_VALUE)
        glColor4f(1f, 1f, 1f, 1f)

        // Animation
        val delta = deltaTime
        val width = textLength + 8F

        when (fadeState) {
            FadeState.IN -> {
                if (x < width) {
                    x = AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep += delta / 4F
                }
                if (x >= width) {
                    fadeState = FadeState.STAY
                    x = width
                    fadeStep = width
                }

                stay = 60F
            }

            FadeState.STAY -> if (stay > 0)
                stay = 0F
            else
                fadeState = FadeState.OUT

            FadeState.OUT -> if (x > 0) {
                x = AnimationUtils.easeOut(fadeStep, width) * width
                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> HUD.removeNotification(this)
        }
    }
}

