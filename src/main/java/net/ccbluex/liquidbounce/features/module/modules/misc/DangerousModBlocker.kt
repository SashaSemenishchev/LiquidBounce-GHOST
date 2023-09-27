package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleManager

object DangerousModBlocker : Module("DangerousModBlocker", ModuleCategory.MISC) {
    override fun onEnable() {
        for (dangerousModule in ModuleManager.dangerousModules) {
            dangerousModule.state = false
        }
    }
}