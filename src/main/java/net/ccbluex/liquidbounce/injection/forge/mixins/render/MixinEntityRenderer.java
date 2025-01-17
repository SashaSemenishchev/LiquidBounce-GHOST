/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.base.Predicates;
import me.mrunny.RenderHiderWindow;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack;
import net.ccbluex.liquidbounce.features.module.modules.misc.RenderHider;
import net.ccbluex.liquidbounce.features.module.modules.player.Reach;
import net.ccbluex.liquidbounce.features.module.modules.render.CameraClip;
import net.ccbluex.liquidbounce.features.module.modules.render.NoHurtCam;
import net.ccbluex.liquidbounce.features.module.modules.render.Tracers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static net.minecraft.client.renderer.GlStateManager.rotate;
import static net.minecraft.client.renderer.GlStateManager.translate;
import static org.lwjgl.opengl.GL11.*;

@Mixin(EntityRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEntityRenderer {

    @Shadow
    private Entity pointedEntity;

    @Shadow
    private Minecraft mc;

    @Shadow
    private float thirdPersonDistanceTemp;

    @Shadow
    private float thirdPersonDistance;

    @Shadow
    private boolean cloudFog;

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
//        if(RenderHider.INSTANCE.getState() && mc.currentScreen != null) return;
        RenderHider hider = RenderHider.INSTANCE;
        if(hider.getState()) hider.clearWindow();
        EventManager.INSTANCE.callEvent(new Render3DEvent(partialTicks));
    }

//    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
//    public void hookCheatRendererAfterOverlay(float partialTicks, long p_updateCameraAndRender_2_, CallbackInfo ci) {
//        if(!RenderHider.INSTANCE.getState()) return;
//        if(mc.currentScreen != null) return;
//        try {
//            RenderHiderWindow window = RenderHider.INSTANCE.getWindow();
//            if(window == null) return;
//            if(!window.isVisible()) {
//                window.drawAgain();
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Inject(method = "updateCameraAndRender", at = @At(value = "RETURN"))
//    public void hookCheatRendererAtReturn(float partialTicks, long p_updateCameraAndRender_2_, CallbackInfo ci) {
//        if(!RenderHider.INSTANCE.getState()) return;
//        if(mc.currentScreen == null) return;
//        try {
//            RenderHiderWindow window = RenderHider.INSTANCE.getWindow();
//            if(window == null) return;
//            if(window.isVisible()) {
//                window.unDraw();
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void injectHurtCameraEffect(CallbackInfo callbackInfo) {
        if (NoHurtCam.INSTANCE.getState()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"), cancellable = true)
    private void cameraClip(float partialTicks, CallbackInfo callbackInfo) {
        if (CameraClip.INSTANCE.getState()) {
            callbackInfo.cancel();

            Entity entity = mc.getRenderViewEntity();
            float f = entity.getEyeHeight();

            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
                f += 1;
                translate(0F, 0.3F, 0f);

                if (!mc.gameSettings.debugCamEnable) {
                    BlockPos blockPos = new BlockPos(entity);
                    IBlockState blockState = mc.theWorld.getBlockState(blockPos);
                    net.minecraftforge.client.ForgeHooksClient.orientBedCamera(mc.theWorld, blockPos, blockState, entity);

                    rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180f, 0f, -1f, 0f);
                    rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1f, 0f, 0f);
                }
            } else if (mc.gameSettings.thirdPersonView > 0) {
                double d3 = thirdPersonDistanceTemp + (thirdPersonDistance - thirdPersonDistanceTemp) * partialTicks;

                if (mc.gameSettings.debugCamEnable) {
                    translate(0f, 0f, (float) (-d3));
                } else {
                    float f1 = entity.rotationYaw;
                    float f2 = entity.rotationPitch;

                    if (mc.gameSettings.thirdPersonView == 2) f2 += 180f;

                    if (mc.gameSettings.thirdPersonView == 2) rotate(180f, 0f, 1f, 0f);

                    rotate(entity.rotationPitch - f2, 1f, 0f, 0f);
                    rotate(entity.rotationYaw - f1, 0f, 1f, 0f);
                    translate(0f, 0f, (float) (-d3));
                    rotate(f1 - entity.rotationYaw, 0f, 1f, 0f);
                    rotate(f2 - entity.rotationPitch, 1f, 0f, 0f);
                }
            } else translate(0f, 0f, -0.1F);

            if (!mc.gameSettings.debugCamEnable) {
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180f;
                float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float roll = 0f;
                if (entity instanceof EntityAnimal) {
                    EntityAnimal entityanimal = (EntityAnimal) entity;
                    yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180f;
                }

                Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entity, partialTicks);
                net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup((EntityRenderer) (Object) this, entity, block, partialTicks, yaw, pitch, roll);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                rotate(event.roll, 0f, 0f, 1f);
                rotate(event.pitch, 1f, 0f, 0f);
                rotate(event.yaw, 0f, 1f, 0f);
            }

            translate(0f, -f, 0f);
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + f;
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
            cloudFog = mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
        }
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V", shift = At.Shift.BEFORE))
    private void setupCameraViewBobbingBefore(final CallbackInfo callbackInfo) {
        if (Tracers.INSTANCE.getState()) {
            glPushMatrix();
        }
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V", shift = At.Shift.AFTER))
    private void setupCameraViewBobbingAfter(final CallbackInfo callbackInfo) {
        if (Tracers.INSTANCE.getState()) {
            glPopMatrix();
        }
    }

    @Unique
    private static final Float LEGIT_REACH = 3F;

    /**
     * @author CCBlueX
     */
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    private void getMouseOver(float p_getMouseOver_1_, CallbackInfo ci) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null) {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;

            final Reach reach = Reach.INSTANCE;

            double d0 = reach.getState() ? reach.getMaxRange() : mc.playerController.getBlockReachDistance();
            mc.objectMouseOver = entity.rayTrace(reach.getState() ? reach.getBuildReach() : d0, p_getMouseOver_1_);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
            boolean flag = false;
            if (mc.playerController.extendedReach()) {
                d0 = 6;
                d1 = 6;
            } else if (d0 > 3) {
                flag = true;
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            if (reach.getState()) {
                final MovingObjectPosition movingObjectPosition = entity.rayTrace(reach.getBuildReach(), p_getMouseOver_1_);

                if (movingObjectPosition != null) d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(p_getMouseOver_1_);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1f;
            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.canBeCollidedWith()));
            double d2 = d1;

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();

                final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
                boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));
                Backtrack.INSTANCE.loopThroughBacktrackData(entity1, () -> {
                    boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));
                    return false;
                });

                for (final AxisAlignedBB axisalignedbb : boxes) {
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                    if (axisalignedbb.isVecInside(vec3)) {
                        if (d2 >= 0) {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                        if (d3 < d2 || d2 == 0) {
                            if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0) {
                                    pointedEntity = entity1;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            } else {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            float reachVal = -1;
            boolean fairplay = reach.getFairplay();
            checker: if(reach.getState()) {
                if(!fairplay) {
                    reachVal = reach.getCombatReach();
                    break checker;
                }
                if(pointedEntity == null || !(pointedEntity instanceof EntityPlayer)) break checker;
                reachVal = reach.getFairplayValues().getOrDefault(pointedEntity.getUniqueID(), LEGIT_REACH) + reach.getFairplayReachAddition();
            }
            if(reachVal < 3) {
                reachVal = reach.getState() && fairplay ? reach.getCombatReach() : 3;
            }
            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > reachVal) {
                pointedEntity = null;
                mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, Objects.requireNonNull(vec33), null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mc.pointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();
        }

        ci.cancel();
    }
}
