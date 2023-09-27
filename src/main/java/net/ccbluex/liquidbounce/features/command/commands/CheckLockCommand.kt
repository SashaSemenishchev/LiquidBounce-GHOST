package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command

class CheckLockCommand : Command("checklock") {
    override fun execute(args: Array<String>) {
        if(args.isNotEmpty() && !LiquidBounce.hardLocked) {
            LiquidBounce.lockfile.createNewFile()
            LiquidBounce.hardLocked = true
            return
        }
        if(!LiquidBounce.checkLock()) {
            chat("§aClient is unlocked!")
        }
    }
}