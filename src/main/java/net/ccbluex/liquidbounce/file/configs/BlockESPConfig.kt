package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.render.BlockESP
import net.ccbluex.liquidbounce.features.module.modules.render.XRay
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.Block
import java.awt.Color
import java.io.File

class BlockESPConfig(file: File): FileConfig(file) {
    override fun loadConfig() {
        val jsonObject = JsonParser().parse(file.bufferedReader()).asJsonObject

        BlockESP.blocks.clear()
        for ((key, jsonElement) in jsonObject.entrySet()) {
            jsonElement.asString
            try {
                val block = BlockUtils.getBlockByStr(key)
//                val block = Block.getBlockFromName(jsonElement.asString)
                if(block == null) {
                    LiquidBounce.info("[FileManager] Unknown BlockESP block: $key")
                    continue
                }
                BlockESP.blocks[block] = try {
                    Color.decode(jsonElement.asString)
                } catch (e: Exception) { Color.BLACK }

            } catch (throwable: Throwable) {
                ClientUtils.LOGGER.error("[FileManager] Failed to add block to BlockESP.", throwable)
            }
        }
    }

    override fun saveConfig() {
        val obj = JsonObject()
        for ((block, color) in BlockESP.blocks) {
            obj.addProperty(Block.getIdFromBlock(block).toString(), "#${color.rgb.toString(16)}")
        }

        file.writeText(FileManager.PRETTY_GSON.toJson(obj))
    }
}