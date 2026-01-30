package com.periut.starac.mixin.omni;

import com.periut.omni.OmniShaderManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import net.minecraft.client.gui.GuiElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiElement.class)
public class GuiElementMixin {

    // ========== fill(int, int, int, int, int) ==========

    @Redirect(method = "fill", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$fillEnable(int cap) {
        if (cap == 3042) { // GL_BLEND
            RenderDevice.get().setBlend(true,
                    RenderTypes.BlendFactor.SRC_ALPHA,
                    RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA);
        } else if (cap == 3553) {
            OmniShaderManager.get().setUseTexture(true);
        }
    }

    @Redirect(method = "fill", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$fillDisable(int cap) {
        if (cap == 3042) {
            RenderDevice.get().setBlend(false, RenderTypes.BlendFactor.ZERO, RenderTypes.BlendFactor.ZERO);
        } else if (cap == 3553) {
            OmniShaderManager.get().setUseTexture(false);
        }
    }

    @Redirect(method = "fill", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$fillBlendFunc(int src, int dst) {
        // Already handled by enable redirect
    }

    @Redirect(method = "fill", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$fillColor4f(float r, float g, float b, float a) {
        OmniShaderManager.get().setGuiColor(r, g, b, a);
        OmniShaderManager.get().setUseVertexColor(false);
    }

    // ========== fillGradient(int, int, int, int, int, int) ==========

    @Redirect(method = "fillGradient", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$gradientDisable(int cap) {
        if (cap == 3553) { // GL_TEXTURE_2D
            OmniShaderManager.get().setUseTexture(false);
        } else if (cap == 3008) { // GL_ALPHA_TEST
            OmniShaderManager.get().setAlphaTest(0.0f);
        }
    }

    @Redirect(method = "fillGradient", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$gradientEnable(int cap) {
        if (cap == 3042) { // GL_BLEND
            RenderDevice.get().setBlend(true,
                    RenderTypes.BlendFactor.SRC_ALPHA,
                    RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA);
        } else if (cap == 3553) {
            OmniShaderManager.get().setUseTexture(true);
        } else if (cap == 3008) {
            OmniShaderManager.get().setAlphaTest(0.1f);
        }
    }

    @Redirect(method = "fillGradient", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$gradientBlendFunc(int src, int dst) {
        // Already handled by enable
    }

    @Redirect(method = "fillGradient", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glShadeModel(I)V"))
    private void omni$gradientShadeModel(int mode) {
        // Smooth/flat shading handled by per-vertex colors in shader
    }
}
