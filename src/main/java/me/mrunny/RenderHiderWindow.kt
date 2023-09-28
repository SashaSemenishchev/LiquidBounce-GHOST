package me.mrunny

import io.netty.util.internal.ConcurrentSet
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.Display
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.util.concurrent.ConcurrentSkipListSet
import javax.swing.JFrame
import javax.vecmath.Vector2f

data class Vec2i(val x: Int, val y: Int)

class RenderHiderWindow : JFrame("Overlay") {
    val mc = Minecraft.getMinecraft()

    init {
        setLocationRelativeTo(null)
        setLocation(Display.getX(), Display.getY())
        isAlwaysOnTop = true
        size = Dimension(100, 100)
        isUndecorated = true
        opacity = 1f
        background = Color(0,0, 0, 0)
        focusableWindowState = false
    }

    override fun paint(g: Graphics?) {}

    fun drawAgain() {
        isVisible = true
        size = Dimension(mc.displayWidth, mc.displayHeight - 100)
    }

    fun unDraw() {
        size = Dimension(100, 100)
        isVisible = false
    }
}