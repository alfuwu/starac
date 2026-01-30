package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    // ========== renderShape (static) ==========

    @Redirect(method = "renderShape", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private static void omni$shapeEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderShape", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private static void omni$shapeDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderShape", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private static void omni$shapeColor(float r, float g, float b, float a) {
        // Color applied via tesselator vertex colors
    }

    // ========== renderShadow ==========

    @Redirect(method = "renderShadow", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$shadowEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderShadow", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$shadowDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderShadow", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$shadowBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderShadow", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"))
    private void omni$shadowDepthMask(boolean flag) {
        RenderDevice.get().setDepthWrite(flag);
    }

    @Redirect(method = "renderShadow", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$shadowColor(float r, float g, float b, float a) {
        // Color via vertex data
    }

    // ========== renderOnFire ==========

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$firePush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$firePop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$fireTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V"))
    private void omni$fireScale(float x, float y, float z) {
        OmniMatrixStack.modelview().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$fireRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$fireColor(float r, float g, float b, float a) {
        // Color via vertex data
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$fireEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderOnFire", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$fireDisable(int cap) {
        handleDisable(cap);
    }

    // ========== Helpers ==========

    private static void handleEnable(int cap) {
        switch (cap) {
            case 2929 -> RenderDevice.get().setDepthTest(true);
            case 3042 -> RenderDevice.get().setBlend(true,
                    RenderTypes.BlendFactor.SRC_ALPHA,
                    RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA);
            case 3553 -> OmniShaderManager.get().setUseTexture(true);
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.1f);
            case 2884 -> RenderDevice.get().setCullFace(true, RenderTypes.CullMode.BACK);
            case 2912 -> {}
            case 2896, 16384, 16385, 2903 -> {}
            default -> {}
        }
    }

    private static void handleDisable(int cap) {
        switch (cap) {
            case 2929 -> RenderDevice.get().setDepthTest(false);
            case 3042 -> RenderDevice.get().setBlend(false,
                    RenderTypes.BlendFactor.ZERO, RenderTypes.BlendFactor.ZERO);
            case 3553 -> OmniShaderManager.get().setUseTexture(false);
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.0f);
            case 2884 -> RenderDevice.get().setCullFace(false, RenderTypes.CullMode.NONE);
            case 2912 -> {}
            case 2896, 16384, 16385, 2903 -> {}
            default -> {}
        }
    }

    private static RenderTypes.BlendFactor mapBlendFactor(int glFactor) {
        return switch (glFactor) {
            case 0 -> RenderTypes.BlendFactor.ZERO;
            case 1 -> RenderTypes.BlendFactor.ONE;
            case 770 -> RenderTypes.BlendFactor.SRC_ALPHA;
            case 771 -> RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA;
            default -> RenderTypes.BlendFactor.ONE;
        };
    }
}
