/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.DamageEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import java.awt.Color
import java.text.DecimalFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

object Reach : Module("Reach", ModuleCategory.PLAYER) {

    val combatReach by FloatValue("CombatReach", 3.5f, 3f..7f)
    val buildReach by FloatValue("BuildReach", 4.5f, 4.5f..7f)
    val fairplay by BoolValue("FairPlay mode", true)
    val fairplayReachAddition by FloatValue("ReachAddition", 0.01f, 0.0f..3f) { fairplay }
    val fairplayMaxReach by FloatValue("MaxReach", 3.3f, 3f..4.5f) { fairplay }
    val fairplayValues = hashMapOf<UUID, Float>()

    val maxRange
        get() = max(combatReach, buildReach)

    private var lastDamage = 0L
    private var lastDamageType: String? = null
    @EventTarget
    fun onEvent(event: DamageEvent) {
        if(!fairplay) return
        LiquidBounce.executor.execute {
            val currentDamage = event.source.damageType
            val now = System.currentTimeMillis()
            val diff = now - lastDamage
            if(diff < 250 && isBadDamageType(lastDamageType) && currentDamage == "generic") {
                return@execute
            }

            if(isBadDamageType(currentDamage)) return@execute
            val localPlayer = mc.thePlayer
            var attackingPlayer: EntityPlayer? = null
            var closestRotation = Double.MAX_VALUE
            val motionYaw = localPlayer.getMotionYaw()
            var currentDistance: Double
            var distance = .0
            val maxDistance = fairplayMaxReach
            val checkDistance = maxDistance + 1
            for(player in mc.theWorld.playerEntities) {
                if(player == localPlayer) continue
                currentDistance = getDistance(player, localPlayer, checkDistance)
                if(currentDistance > checkDistance) continue
                val currentRotationDistance = abs(motionYaw - player.rotationYawHead)
                if(attackingPlayer == null) {
                    attackingPlayer = player
                    distance = currentDistance
                    closestRotation = currentRotationDistance
                    continue
                }

                if(currentRotationDistance < closestRotation) {
                    closestRotation = currentRotationDistance
                    attackingPlayer = player
                    distance = currentDistance
                }
            }

            if(attackingPlayer == null || distance < 3.0) return@execute
            HUD.addNotification(Notification(
                "${attackingPlayer.name} hit you with ${DecimalFormat("#.##").format(distance)}",
                true,
                Color.RED
            ))
            if(distance > maxDistance) {
                distance = maxDistance.toDouble()
            }
            val attackingUuid = attackingPlayer.uniqueID
            val distanceF = distance.toFloat()
            val rememberedReach = fairplayValues.getOrDefault(attackingUuid, 0f)

            if(distanceF > rememberedReach) {
                fairplayValues[attackingUuid] = distanceF
            }
        }
    }

    private fun getDistance(currentPlayer: EntityPlayer, player: EntityPlayerSP, max: Float): Double {
        val deltaX = player.posX - currentPlayer.posX
        val deltaY = player.posY - currentPlayer.posY
        val deltaZ = player.posZ - currentPlayer.posZ
        val strictlyCalculated = StrictMath.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
        val raytrace = currentPlayer.rayTrace(max.toDouble(), 1f)
        val validateAgainst = if (raytrace.entityHit == null) null else raytrace.entityHit.uniqueID
        if (player.uniqueID != validateAgainst) {
            return strictlyCalculated
        }
        val toEyes = raytrace.hitVec.distanceTo(currentPlayer.getPositionEyes(1f))
        if (abs(toEyes - strictlyCalculated) > player.width / 2) {
            HUD.addNotification(Notification(
                "${currentPlayer.name} maybe using HitBoxes", true, Color.RED
            ))
        }
        return toEyes
    }

    fun EntityPlayerSP.getMotionYaw(): Double {
        val motX = this.motionX
        val motZ = this.motionZ
        var yaw = if (motZ != .0) {
            -atan2(motX, motZ) * 180 / PI
        } else {
            .0
        }
        if (yaw < .0) {
            yaw += 360;
        }

        return MathHelper.wrapAngleTo180_double(yaw + 180)
    }

    private fun isBadDamageType(damage: String?): Boolean {
        if(damage == null) return false
        when (damage.lowercase()) {
            "fall", "fire", "void" -> return true
        }
        return false
    }
}
