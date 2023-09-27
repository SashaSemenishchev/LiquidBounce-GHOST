package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.client.hud.HUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScaledResolution.class)
public class MixinScaledResolution {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void assignTo(Minecraft p_i46445_1_, CallbackInfo ci) {
        HUD.INSTANCE.setScaledResolution((ScaledResolution) (Object) this);
    }
}
