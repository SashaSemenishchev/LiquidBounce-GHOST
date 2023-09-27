package net.ccbluex.liquidbounce.features.command.special

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.render.BlockESP
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import java.awt.Color

class BlockESPAdd : Command("addblockesp") {
    override fun execute(args: Array<String>) {
        if(args.size < 3) {
            chat(".addblockesp <block> <color>")
            return
        }
        val block = BlockUtils.getBlockByStr(args[1])
        if(block == null) {
            chat("Invalid block ${args[1]}")
            return
        }

        val color = try {
            Color.decode(args[2])
        } catch (e: Exception) {
            chat("Invalid color: ${args[2]}")
            return
        }
        BlockESP.blocks[block] = color
        chat("Added block!")
    }
}