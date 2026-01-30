package com.periut.starac.mixin.omni;

import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import net.minecraft.client.ParticleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void omni$beforeParticles(Entity entity, float tickDelta, CallbackInfo ci) {
        if (OmniShaderManager.get().isInitialized()) {
            OmniShaderManager.get().useWorldShader();
            OmniShaderManager.get().setUseTexture(true);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$renderBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }
}
