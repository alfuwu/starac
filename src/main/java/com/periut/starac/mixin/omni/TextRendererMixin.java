package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

@Mixin(TextRenderer.class)
public class TextRendererMixin {

    // ========== Constructor display list creation ==========
    // The constructor creates display lists for character glyphs

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V"))
    private void omni$initNewList(int list, int mode) {
        org.lwjgl.opengl.GL11.glNewList(list, mode);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEndList()V"))
    private void omni$initEndList() {
        org.lwjgl.opengl.GL11.glEndList();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$initTranslate(float x, float y, float z) {
        // During display list recording, translate advances the cursor
        org.lwjgl.opengl.GL11.glTranslatef(x, y, z);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V"))
    private void omni$initColor3f(float r, float g, float b) {
        // Color set during list recording for shadow pass
        org.lwjgl.opengl.GL11.glColor3f(r, g, b);
    }

    // ========== drawLayer(String, int, int, int, boolean) ==========

    @Redirect(method = "drawLayer", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$drawBindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
    }

    @Redirect(method = "drawLayer", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$drawColor4f(float r, float g, float b, float a) {
        OmniShaderManager sm = OmniShaderManager.get();
        sm.setGuiColor(r, g, b, a);
        sm.setUseVertexColor(false);
    }

    @Redirect(method = "drawLayer", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$drawPush() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "drawLayer", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$drawTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "drawLayer", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallLists(Ljava/nio/IntBuffer;)V"))
    private void omni$drawCallLists(IntBuffer lists) {
        // Display lists contain pre-recorded glyph rendering.
        // Pass through to GL since display lists are populated during construction.
        org.lwjgl.opengl.GL11.glCallLists(lists);
    }

    @Redirect(method = "drawLayer", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$drawPop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }
}
