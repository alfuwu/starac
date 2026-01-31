package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.FloatBuffer;

@Mixin(net.minecraft.client.render.Frustum.class)
public class FrustumMixin {

    @Redirect(method = "compute", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glGetFloat(ILjava/nio/FloatBuffer;)V"))
    private void omni$getMatrixFromStack(int pname, FloatBuffer params) {
        float[] m;
        switch (pname) {
            case 2982 -> m = OmniMatrixStack.modelview().get();  // GL_MODELVIEW_MATRIX
            case 2983 -> m = OmniMatrixStack.projection().get(); // GL_PROJECTION_MATRIX
            default -> {
                GL11.glGetFloatv(pname, params);
                return;
            }
        }
        for (int i = 0; i < 16; i++) {
            params.put(params.position() + i, m[i]);
        }
    }
}
