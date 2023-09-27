package me.mrunny

import io.netty.util.internal.ConcurrentSet
import net.minecraft.client.Minecraft
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
        setLocation(0, 0)
        isAlwaysOnTop = true
        size = Dimension(100, 100)
        isUndecorated = true
        opacity = 1f
        background = Color(0,0, 0, 0)
        focusableWindowState = false
//        pack()
    }

    private val lock = Object()
    private val vertices: ConcurrentSet<Pair<Vec2i, Vec2i>> = ConcurrentSet()

    fun addVertex(vec1: Vec2i, vec2: Vec2i) {
        synchronized(lock) {
            vertices += Pair(vec1, vec2)
        }
    }

    fun clearVertices() {
        synchronized(lock) {
            vertices.clear()
        }
    }

    override fun paint(g: Graphics?) {
        if(g == null) return
//        println("painting")
        g.color = Color.WHITE
        synchronized(lock) {
            for (vertex in vertices) {
                val v1 = vertex.first
                val v2 = vertex.second
//                println(v1)
                g.drawLine(v1.x, v1.y, v2.x, v2.y)
            }
            vertices.clear()
        }
    }

    fun drawAgain() {
        isVisible = true
        size = Dimension(mc.displayWidth, mc.displayHeight - 100)
    }

    fun unDraw() {
        size = Dimension(100, 100)
        isVisible = false
    }
}