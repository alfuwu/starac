package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$bindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
        OmniShaderManager.get().setUseTexture(true);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$color4f(float r, float g, float b, float a) {
        OmniShaderManager.get().setGuiColor(r, g, b, a);
        OmniShaderManager.get().setUseVertexColor(false);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    private void omni$push() {
        OmniMatrixStack.modelview().push();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    private void omni$pop() {
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$translate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glRotatef(FFFF)V"))
    private void omni$rotate(float angle, float x, float y, float z) {
        OmniMatrixStack.modelview().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V"))
    private void omni$scale(float x, float y, float z) {
        OmniMatrixStack.modelview().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }
}
