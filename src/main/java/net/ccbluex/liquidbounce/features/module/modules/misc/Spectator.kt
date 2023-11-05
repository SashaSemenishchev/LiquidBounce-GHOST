package net.ccbluex.liquidbounce.features.module.modules.misc

import me.mrunny.SpectatorWindow
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraftforge.client.model.animation.Animation
import java.awt.Color
import java.time.Instant
import java.time.LocalDate
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
        recordedObsidianHolders.clear()
    }

    override fun onDisable() {
        SwingUtilities.invokeLater {
            spectatorWindow?.dispose()
            spectatorWindow = null
        }
    }
    private val recordedObsidianHolders = mutableSetOf<Int>()
    @EventTarget
    fun onBlockUpdate(event: PacketEvent) {
        if(event.eventType != EventState.RECEIVE) return
        val packet = event.packet
        if(packet !is S0BPacketAnimation) return
        if(packet.animationType != 0) return
        val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
        if(entity !is EntityPlayer) return
        if(entity.itemInUse.item != Item.getItemFromBlock(Blocks.obsidian)) return
        if(!recordedObsidianHolders.add(packet.entityID)) return

        val teamName = entity.team?.registeredName ?: ""
        HUD.addNotification(Notification("[$teamName] ${entity.name} got obsidian", false, Color.MAGENTA))
    }

    fun log(message: String) {
        spectatorWindow?.log(LocalDateTime.now(), message)
    }
}