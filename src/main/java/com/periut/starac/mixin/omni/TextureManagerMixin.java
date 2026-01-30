package com.periut.starac.mixin.omni;

import com.periut.omni.OmniTextureManager;
import net.minecraft.client.render.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
    @Redirect(method = "bind(I)V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$redirectBindTexture(int target, int textureId) {
        OmniTextureManager.get().bindTexture(textureId);
    }
}
