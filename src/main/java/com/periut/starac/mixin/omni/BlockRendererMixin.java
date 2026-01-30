package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import net.minecraft.client.render.block.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
    // renderAsItem uses GL11.glTranslatef and GL11.glColor4f
    @Redirect(method = "renderAsItem", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$redirectTranslate(float x, float y, float z) {
        OmniMatrixStack.modelview().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    @Redirect(method = "renderAsItem", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$redirectColor(float r, float g, float b, float a) {
        // Block colors are applied via Tesselator vertex colors
    }

}
