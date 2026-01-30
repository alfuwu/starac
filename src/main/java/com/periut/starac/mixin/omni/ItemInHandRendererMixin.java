package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import net.minecraft.client.render.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    // ========== render(MobEntity, ItemStack) ==========

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$renderPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$renderPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$renderBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$renderEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$renderDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$renderTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V"))
    private void omni$renderScale(float x, float y, float z) {
        OmniMatrixStack.modelview().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$renderRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    // ========== renderHand(float) ==========

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$handPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$handPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$handTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$handRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V"))
    private void omni$handScale(float x, float y, float z) {
        OmniMatrixStack.modelview().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$handEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$handDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$handColor(float r, float g, float b, float a) {
        // Color is applied via vertex data
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$handBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glNormal3f(FFF)V"))
    private void omni$handNormal(float x, float y, float z) {
        // Normal via vertex data
    }

    // ========== renderScreenEffects(float) ==========

    @Redirect(method = "renderScreenEffects", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$screenEffectsDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderScreenEffects", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$screenEffectsEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderScreenEffects", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$screenEffectsBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    // ========== renderInWallEffect(float, int) ==========

    @Redirect(method = "renderInWallEffect", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$wallColor(float r, float g, float b, float a) {
        // Color via vertex data
    }

    @Redirect(method = "renderInWallEffect", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$wallPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "renderInWallEffect", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$wallPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
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
            case 774 -> RenderTypes.BlendFactor.DST_ALPHA;
            case 775 -> RenderTypes.BlendFactor.ONE_MINUS_DST_ALPHA;
            case 772 -> RenderTypes.BlendFactor.DST_COLOR;
            default -> RenderTypes.BlendFactor.ONE;
        };
    }
}
