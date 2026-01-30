package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import net.minecraft.client.render.world.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // ========== renderSky(float) ==========

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void omni$beginSky(float tickDelta, CallbackInfo ci) {
        OmniShaderManager.get().useSkyShader();
    }

    @Inject(method = "renderSky", at = @At("RETURN"))
    private void omni$endSky(float tickDelta, CallbackInfo ci) {
        OmniShaderManager.get().useWorldShader();
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$skyDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$skyEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V"))
    private void omni$skyColor3f(float r, float g, float b) {
        OmniShaderManager.get().setSkyColor(r, g, b, 1.0f);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$skyColor4f(float r, float g, float b, float a) {
        OmniShaderManager.get().setSkyColor(r, g, b, a);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"))
    private void omni$skyDepthMask(boolean flag) {
        RenderDevice.get().setDepthWrite(flag);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V"))
    private void omni$skyCallList(int list) {
        // Display lists contain pre-recorded Tesselator geometry.
        // With OmniTesselator, the geometry was drawn during list creation.
        // Call the display list to replay any captured GL commands.
        org.lwjgl.opengl.GL11.glCallList(list);
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$skyBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$skyPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$skyPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$skyRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$skyTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$skyBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    // ========== renderClouds(float) ==========

    @Inject(method = "renderClouds", at = @At("HEAD"))
    private void omni$beginClouds(float tickDelta, CallbackInfo ci) {
        OmniShaderManager.get().useSkyShader();
    }

    @Inject(method = "renderClouds", at = @At("RETURN"))
    private void omni$endClouds(float tickDelta, CallbackInfo ci) {
        OmniShaderManager.get().useWorldShader();
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$cloudsDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$cloudsEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$cloudsBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$cloudsBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$cloudsColor(float r, float g, float b, float a) {
        OmniShaderManager.get().setSkyColor(r, g, b, a);
    }

    // glScalef and glColorMask are only in renderFancyClouds, not renderClouds.

    // ========== renderFancyClouds(float) ==========

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$fancyCloudsDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$fancyCloudsEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$fancyCloudsBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$fancyCloudsBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$fancyCloudsColor(float r, float g, float b, float a) {
        OmniShaderManager.get().setSkyColor(r, g, b, a);
    }

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V"))
    private void omni$fancyCloudsScale(float x, float y, float z) {
        OmniMatrixStack.modelview().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColorMask(ZZZZ)V"))
    private void omni$fancyCloudsColorMask(boolean r, boolean g, boolean b, boolean a) {
        RenderDevice.get().setColorMask(r, g, b, a);
    }

    // ========== renderMiningProgress ==========

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$miningEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$miningDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$miningBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$miningColor(float r, float g, float b, float a) {
        // Color applied via vertex data
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$miningBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$miningPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$miningPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPolygonOffset(FF)V"))
    private void omni$miningPolyOffset(float factor, float units) {
        RenderDevice.get().setPolygonOffset(true, factor, units);
    }

    @Redirect(method = "renderMiningProgress", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"))
    private void omni$miningDepthMask(boolean flag) {
        RenderDevice.get().setDepthWrite(flag);
    }

    // ========== renderBlockOutline ==========

    @Redirect(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$outlineEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$outlineDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$outlineBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$outlineColor(float r, float g, float b, float a) {
        // Color via vertex data
    }

    @Redirect(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glLineWidth(F)V"))
    private void omni$outlineLineWidth(float width) {
        RenderDevice.get().setLineWidth(width);
    }

    @Redirect(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"))
    private void omni$outlineDepthMask(boolean flag) {
        RenderDevice.get().setDepthWrite(flag);
    }

    // renderOutline(Box) has NO GL calls — only Tesselator.
    // The GL calls (glEnable, glDisable, glBlendFunc, glColor4f, glLineWidth, glDepthMask)
    // are all in renderBlockOutline, which is handled above.

    // ========== Occlusion query rendering (render method with chunks) ==========

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$chunkRenderDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$chunkRenderEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColorMask(ZZZZ)V"))
    private void omni$chunkRenderColorMask(boolean r, boolean g, boolean b, boolean a) {
        RenderDevice.get().setColorMask(r, g, b, a);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"))
    private void omni$chunkRenderDepthMask(boolean flag) {
        RenderDevice.get().setDepthWrite(flag);
    }

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$chunkRenderPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$chunkRenderPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render(Lnet/minecraft/entity/mob/MobEntity;ID)I", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$chunkRenderTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    // ========== Constructor display list creation ==========
    // The GL calls (glPushMatrix, glPopMatrix, glNewList, glEndList) are in the
    // constructor <init>, not in renderStars. renderStars only uses Tesselator.

    // ========== Helpers ==========

    private static void handleEnable(int cap) {
        switch (cap) {
            case 2929 -> RenderDevice.get().setDepthTest(true);
            case 3042 -> RenderDevice.get().setBlend(true,
                    RenderTypes.BlendFactor.SRC_ALPHA,
                    RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA);
            case 2884 -> RenderDevice.get().setCullFace(true, RenderTypes.CullMode.BACK);
            case 3553 -> OmniShaderManager.get().setUseTexture(true);
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.1f);
            case 2912 -> {} // GL_FOG
            case 2896, 16384, 16385, 2903 -> {} // Lighting
            case 32823 -> RenderDevice.get().setPolygonOffset(true, -3.0f, -3.0f); // GL_POLYGON_OFFSET_FILL
            default -> {}
        }
    }

    private static void handleDisable(int cap) {
        switch (cap) {
            case 2929 -> RenderDevice.get().setDepthTest(false);
            case 3042 -> RenderDevice.get().setBlend(false,
                    RenderTypes.BlendFactor.ZERO, RenderTypes.BlendFactor.ZERO);
            case 2884 -> RenderDevice.get().setCullFace(false, RenderTypes.CullMode.NONE);
            case 3553 -> OmniShaderManager.get().setUseTexture(false);
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.0f);
            case 2912 -> {} // GL_FOG
            case 2896, 16384, 16385, 2903 -> {} // Lighting
            case 32823 -> RenderDevice.get().setPolygonOffset(false, 0, 0); // GL_POLYGON_OFFSET_FILL
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
            case 768 -> RenderTypes.BlendFactor.SRC_COLOR;
            default -> RenderTypes.BlendFactor.ONE;
        };
    }
}
