package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    private Minecraft minecraft;

    private int currentMatrixMode = 5888; // GL_MODELVIEW

    private OmniMatrixStack currentStack() {
        return currentMatrixMode == 5889 ? OmniMatrixStack.projection() : OmniMatrixStack.modelview();
    }

    // =====================================================================
    // renderWorld(float, long) - main world rendering pass
    // GL calls: glEnable, glDisable, glColorMask, glViewport, glClear,
    //           glShadeModel, glBindTexture, glDepthMask, glBlendFunc
    // =====================================================================

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void omni$beginRenderWorld(float tickDelta, long nanoTime, CallbackInfo ci) {
        OmniShaderManager.get().useWorldShader();
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$renderWorldEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$renderWorldDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColorMask(ZZZZ)V"))
    private void omni$renderWorldColorMask(boolean r, boolean g, boolean b, boolean a) {
        RenderDevice.get().setColorMask(r, g, b, a);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glViewport(IIII)V"))
    private void omni$renderWorldViewport(int x, int y, int w, int h) {
        RenderDevice.get().setViewport(x, y, w, h);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glClear(I)V"))
    private void omni$renderWorldClear(int mask) {
        boolean color = (mask & 0x4000) != 0; // GL_COLOR_BUFFER_BIT
        boolean depth = (mask & 0x100) != 0;  // GL_DEPTH_BUFFER_BIT
        RenderDevice.get().clear(color, depth);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glShadeModel(I)V"))
    private void omni$renderWorldShadeModel(int mode) {
        // Smooth shading is always on in shader pipeline
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$renderWorldBindTexture(int target, int textureId) {
        OmniTextureManager.get().bindTexture(textureId);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"))
    private void omni$renderWorldDepthMask(boolean flag) {
        RenderDevice.get().setDepthWrite(flag);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$renderWorldBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    // =====================================================================
    // setupCamera(float, int) - projection and modelview matrix setup
    // GL calls: glMatrixMode, glLoadIdentity, glTranslatef, glScaled,
    //           gluPerspective, glRotatef, glScalef
    // =====================================================================

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glMatrixMode(I)V"))
    private void omni$setupCameraMatrixMode(int mode) {
        currentMatrixMode = mode;
    }

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glLoadIdentity()V"))
    private void omni$setupCameraLoadIdentity() {
        currentStack().loadIdentity();
    }

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$setupCameraTranslate(float x, float y, float z) {
        currentStack().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScaled(DDD)V"))
    private void omni$setupCameraScaleD(double x, double y, double z) {
        currentStack().scale((float) x, (float) y, (float) z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/util/glu/GLU;gluPerspective(FFFF)V"))
    private void omni$setupCameraPerspective(float fov, float aspect, float near, float far) {
        OmniMatrixStack.projection().perspective(fov, aspect, near, far);
        OmniMatrixStack.modelview().loadIdentity();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$setupCameraRotate(float angle, float x, float y, float z) {
        currentStack().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "setupCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V"))
    private void omni$setupCameraScale(float x, float y, float z) {
        currentStack().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    // =====================================================================
    // applyHurtCam(float) - hurt camera shake
    // GL calls: glRotatef (x4)
    // =====================================================================

    @Redirect(method = "applyHurtCam", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$hurtCamRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    // =====================================================================
    // applyViewBobbing(float) - view bob effect
    // GL calls: glTranslatef (x1), glRotatef (x3)
    // =====================================================================

    @Redirect(method = "applyViewBobbing", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$viewBobbingTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "applyViewBobbing", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$viewBobbingRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    // =====================================================================
    // transformCamera(float) - camera positioning
    // GL calls: glRotatef (x10+), glTranslatef (x4+)
    // =====================================================================

    @Redirect(method = "transformCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$cameraRotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "transformCamera", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$cameraTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    // =====================================================================
    // renderItemInHand(float, int) - first-person item rendering setup
    // GL calls: glLoadIdentity, glTranslatef, glPushMatrix, glPopMatrix
    // =====================================================================

    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glLoadIdentity()V"))
    private void omni$renderItemLoadIdentity() {
        OmniMatrixStack.modelview().loadIdentity();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$renderItemTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$renderItemPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$renderItemPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    // =====================================================================
    // render(float) - single-arg render (main frame)
    // GL calls: glViewport, glMatrixMode (x2), glLoadIdentity (x2), glClear
    // =====================================================================

    @Redirect(method = "render(F)V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glViewport(IIII)V"))
    private void omni$renderViewport(int x, int y, int w, int h) {
        RenderDevice.get().setViewport(x, y, w, h);
    }

    @Redirect(method = "render(F)V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glMatrixMode(I)V"))
    private void omni$renderMatrixMode(int mode) {
        currentMatrixMode = mode;
    }

    @Redirect(method = "render(F)V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glLoadIdentity()V"))
    private void omni$renderLoadIdentity() {
        currentStack().loadIdentity();
    }

    @Redirect(method = "render(F)V", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glClear(I)V"))
    private void omni$renderClear(int mask) {
        boolean color = (mask & 0x4000) != 0;
        boolean depth = (mask & 0x100) != 0;
        RenderDevice.get().clear(color, depth);
    }

    // =====================================================================
    // setupGuiState() - GUI projection setup
    // GL calls: glClear, glMatrixMode (x2), glLoadIdentity (x2), glOrtho, glTranslatef
    // =====================================================================

    @Redirect(method = "setupGuiState", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glClear(I)V"))
    private void omni$guiClear(int mask) {
        boolean color = (mask & 0x4000) != 0;
        boolean depth = (mask & 0x100) != 0;
        RenderDevice.get().clear(color, depth);
    }

    @Redirect(method = "setupGuiState", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glMatrixMode(I)V"))
    private void omni$guiMatrixMode(int mode) {
        currentMatrixMode = mode;
    }

    @Redirect(method = "setupGuiState", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glLoadIdentity()V"))
    private void omni$guiLoadIdentity() {
        currentStack().loadIdentity();
    }

    @Redirect(method = "setupGuiState", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glOrtho(DDDDDD)V"))
    private void omni$guiOrtho(double left, double right, double bottom, double top, double near, double far) {
        OmniMatrixStack.projection().ortho((float) left, (float) right, (float) bottom, (float) top, (float) near, (float) far);
        OmniMatrixStack.modelview().loadIdentity();
    }

    @Redirect(method = "setupGuiState", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$guiTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().useGuiShader();
    }

    // =====================================================================
    // setupClearColor(float) - set clear color
    // GL calls: glClearColor
    // =====================================================================

    @Redirect(method = "setupClearColor", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glClearColor(FFFF)V"))
    private void omni$clearColor(float r, float g, float b, float a) {
        RenderDevice.get().setClearColor(r, g, b, a);
        OmniShaderManager.get().updateFog(0, 0, r, g, b);
    }

    // =====================================================================
    // setupFog(int, float) - fog configuration
    // GL calls: glFog, glNormal3f, glColor4f, glFogi, glFogf, glEnable, glColorMaterial
    // =====================================================================

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glFog(ILjava/nio/FloatBuffer;)V"))
    private void omni$fogColor(int pname, java.nio.FloatBuffer params) {
        // Fog color is extracted and sent to shader
        if (params.remaining() >= 4) {
            float r = params.get(0), g = params.get(1), b = params.get(2);
            OmniShaderManager sm = OmniShaderManager.get();
            sm.updateFog(sm.getFogStart(), sm.getFogEnd(), r, g, b);
        }
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glNormal3f(FFF)V"))
    private void omni$fogNormal(float x, float y, float z) {
        // Normal state for fog - handled by shader
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$fogColor4f(float r, float g, float b, float a) {
        // Color state during fog setup - no-op in shader pipeline
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glFogi(II)V"))
    private void omni$fogI(int pname, int param) {
        // 2917 = GL_FOG_MODE, 9729 = GL_LINEAR, 2048 = GL_EXP
        // We always use linear fog in the shader
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glFogf(IF)V"))
    private void omni$fogF(int pname, float param) {
        OmniShaderManager sm = OmniShaderManager.get();
        switch (pname) {
            case 2915 -> sm.updateFog(param, sm.getFogEnd(), sm.getFogR(), sm.getFogG(), sm.getFogB()); // GL_FOG_START
            case 2916 -> sm.updateFog(sm.getFogStart(), param, sm.getFogR(), sm.getFogG(), sm.getFogB()); // GL_FOG_END
            case 2914 -> {} // GL_FOG_DENSITY - not used with linear fog
            default -> {}
        }
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$fogEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColorMaterial(II)V"))
    private void omni$fogColorMaterial(int face, int mode) {
        // Fixed-function color material tracking - handled by vertex colors in shader
    }

    // =====================================================================
    // renderSnowAndRain(float) - weather rendering
    // GL calls: glDisable, glEnable, glNormal3f, glBlendFunc, glAlphaFunc,
    //           glBindTexture, glColor4f
    // =====================================================================

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$rainEnable(int cap) {
        handleEnable(cap);
    }

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"))
    private void omni$rainDisable(int cap) {
        handleDisable(cap);
    }

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glNormal3f(FFF)V"))
    private void omni$rainNormal(float x, float y, float z) {
        // Normal state for weather particles
    }

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"))
    private void omni$rainBlendFunc(int src, int dst) {
        RenderDevice.get().setBlend(true, mapBlendFactor(src), mapBlendFactor(dst));
    }

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glAlphaFunc(IF)V"))
    private void omni$rainAlphaFunc(int func, float ref) {
        // 516 = GL_GREATER, 519 = GL_ALWAYS
        if (func == 516) {
            OmniShaderManager.get().setAlphaTest(ref);
        } else {
            OmniShaderManager.get().setAlphaTest(0.0f);
        }
    }

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$rainBindTexture(int target, int textureId) {
        OmniTextureManager.get().bindTexture(textureId);
    }

    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$rainColor(float r, float g, float b, float a) {
        // Weather color - applied via tesselator vertex colors
    }

    // =====================================================================
    // Utility methods
    // =====================================================================

    private static void handleEnable(int cap) {
        switch (cap) {
            case 2929 -> RenderDevice.get().setDepthTest(true); // GL_DEPTH_TEST
            case 3042 -> RenderDevice.get().setBlend(true, // GL_BLEND
                    RenderTypes.BlendFactor.SRC_ALPHA,
                    RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA);
            case 2884 -> RenderDevice.get().setCullFace(true, RenderTypes.CullMode.BACK); // GL_CULL_FACE
            case 3553 -> OmniShaderManager.get().setUseTexture(true); // GL_TEXTURE_2D
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.1f); // GL_ALPHA_TEST
            case 2912 -> {} // GL_FOG - handled by shader
            case 2896, 16384, 16385, 2903 -> {} // GL_LIGHTING, GL_LIGHT0/1, GL_COLOR_MATERIAL
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
            default -> {}
        }
    }

    private static RenderTypes.BlendFactor mapBlendFactor(int glFactor) {
        return switch (glFactor) {
            case 0 -> RenderTypes.BlendFactor.ZERO;          // GL_ZERO
            case 1 -> RenderTypes.BlendFactor.ONE;           // GL_ONE
            case 770 -> RenderTypes.BlendFactor.SRC_ALPHA;   // GL_SRC_ALPHA
            case 771 -> RenderTypes.BlendFactor.ONE_MINUS_SRC_ALPHA;
            case 772 -> RenderTypes.BlendFactor.DST_ALPHA;
            case 773 -> RenderTypes.BlendFactor.ONE_MINUS_DST_ALPHA;
            case 774 -> RenderTypes.BlendFactor.DST_COLOR;
            case 768 -> RenderTypes.BlendFactor.SRC_COLOR;
            case 775 -> RenderTypes.BlendFactor.ONE_MINUS_DST_COLOR;
            case 769 -> RenderTypes.BlendFactor.ONE_MINUS_SRC_COLOR;
            default -> RenderTypes.BlendFactor.ONE;
        };
    }
}
