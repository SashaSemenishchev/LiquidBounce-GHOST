package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import kotlin.math.*
import kotlin.random.Random

object AimAssist : Module("AimAssist", ModuleCategory.COMBAT) {
    private val minStrength by FloatValue("MinStrength", 30F, 1F..100F)
    val maxStrength by FloatValue("MaxStrength", 30F, 1F..100F)
    private val fov by FloatValue("FOV", 120F, 0F..360F)
    private val onRotate by BoolValue("OnRotate", true)
    private val predict by BoolValue("Predict", true)

    private var rotations: Rotation? = null
    private var lastRotations: Rotation? = null

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(event.eventState != EventState.PRE) return
        lastRotations = rotations
        rotations = null
        val player = mc.thePlayer
        if(mc.objectMouseOver.typeOfHit === MovingObjectPosition.MovingObjectType.ENTITY) return
        val entity = mc.theWorld.loadedEntityList.filter {
            EntityUtils.isSelected(
                it, true
            ) && player.canEntityBeSeen(it) && player.getDistanceToEntityBox(it) <= 5 && RotationUtils.getRotationDifference(
                it
                ) <= fov
        }.minByOrNull { RotationUtils.getRotationDifference(it) } ?: return

        val hitbox = entity.hitBox
        rotations = RotationUtils.toRotation(
            entity.customPositionVector(
                .0,
                max(.0, min(player.posY - entity.posY, (hitbox.maxY - hitbox.minY) * 0.9)),
                .0
            ),
            predict,
            player
        )
    }

    @EventTarget
    fun onRender(event: Render2DEvent) {
        val helper = mc.mouseHelper
        val lastRotation = this.lastRotations
        val rotation = this.rotations
        if (rotation == null || lastRotation == null || (onRotate && helper.deltaX == 0 && helper.deltaY == 0)) {
            return
        }

        val rotations = Rotation(lastRotation.yaw + (rotation.yaw - lastRotation.yaw) * mc.timer.renderPartialTicks, 0f)
        val strength = Random.nextDouble(minStrength.toDouble(), maxStrength.toDouble()).toFloat()

        val sens = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F
        val gcd = sens * sens * sens * 0.8F

        val yaw = helper.deltaX + ((rotations.yaw - mc.thePlayer.rotationYaw) * (strength / 100) - helper.deltaX) * gcd
        val pitch = helper.deltaY - helper.deltaY * gcd
        mc.thePlayer.setAngles(yaw, pitch)
    }

    private fun Entity.customPositionVector(x: Double, y: Double, z: Double) =
        Vec3(this.posX + x, this.posY + y, this.posZ + z)
}