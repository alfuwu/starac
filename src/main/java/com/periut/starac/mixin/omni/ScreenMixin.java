package com.periut.starac.mixin.omni;

import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void omni$beforeScreenRender(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        if (OmniShaderManager.get().isInitialized()) {
            OmniShaderManager.get().useGuiShader();
        }
    }

    // drawBackgroundTexture(int) has glDisable, glBindTexture, glColor4f
    @Redirect(method = "drawBackgroundTexture", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$bgDisable(int cap) {
        switch (cap) {
            case 2896 -> {} // GL_LIGHTING
            case 2912 -> {} // GL_FOG
            case 3553 -> OmniShaderManager.get().setUseTexture(false);
            case 3042 -> RenderDevice.get().setBlend(false,
                    RenderTypes.BlendFactor.ZERO, RenderTypes.BlendFactor.ZERO);
            default -> {}
        }
    }

    @Redirect(method = "drawBackgroundTexture", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$bgBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
        OmniShaderManager.get().setUseTexture(true);
    }

    @Redirect(method = "drawBackgroundTexture", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$bgColor(float r, float g, float b, float a) {
        OmniShaderManager.get().setGuiColor(r, g, b, a);
    }
}
