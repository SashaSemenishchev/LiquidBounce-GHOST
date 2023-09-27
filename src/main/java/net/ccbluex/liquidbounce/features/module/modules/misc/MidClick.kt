/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.player.AutoTool
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

object MidClick : Module("MidClick", ModuleCategory.MISC) {
    private var wasDown = false
    private val mode by ListValue("Action", arrayOf("AddFriend", "PickBestTool"), "AddFriend")

    @EventTarget
    fun onRender(event: Render2DEvent) {
        if (mc.currentScreen != null) return
        if(!Mouse.isButtonDown(2)) {
            wasDown = false
            return
        }
        if (wasDown) return
        when(mode)  {
            "AddFriend" -> addFriend()
            "PickBestTool" -> {
                val blockPos = mc.objectMouseOver.blockPos
                if(blockPos != null) {
                    LiquidBounce.runLater(100) {
                        AutoTool.switchSlot(blockPos)
                    }
                }
            }
        }
        wasDown = Mouse.isButtonDown(2)
    }

    fun addFriend() {
        val entity = mc.objectMouseOver.entityHit

        if (entity is EntityPlayer) {
            val playerName = stripColor(entity.name)

            if (!friendsConfig.isFriend(playerName)) {
                friendsConfig.addFriend(playerName)
                saveConfig(friendsConfig)
                displayChatMessage("§a§l$playerName§c was added to your friends.")
            } else {
                friendsConfig.removeFriend(playerName)
                saveConfig(friendsConfig)
                displayChatMessage("§a§l$playerName§c was removed from your friends.")
            }

        } else displayChatMessage("§c§lError: §aYou need to select a player.")
    }
}