package com.periut.starac.mixin.omni;

import com.periut.omni.OmniShaderManager;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.gl.GLRenderDevice;
import com.periut.omni.backend.gl.GLRenderContext;
import com.periut.omni.backend.RenderContext;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.periut.omni.backend.RenderTypes;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class WindowMixin {
    @Inject(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;create()V",
            shift = At.Shift.AFTER))
    private void omni$initRenderDevice(CallbackInfo ci) {
        if (!RenderDevice.hasInstance()) {
            GLRenderDevice device = new GLRenderDevice();
            device.init();
            RenderDevice.setInstance(device);

            GLRenderContext context = new GLRenderContext();
            context.init();
            RenderContext.setInstance(context);

            OmniShaderManager.get().init();
        }
    }

    // Redirect GL1.x fixed-function calls in Minecraft.init() that don't exist in core 3.3
    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"))
    private void omni$initEnable(int cap) {
        switch (cap) {
            case 3553 -> {} // GL_TEXTURE_2D - always enabled in shader pipeline
            case 2929 -> RenderDevice.get().setDepthTest(true); // GL_DEPTH_TEST
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.1f); // GL_ALPHA_TEST
            default -> {}
        }
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glShadeModel(I)V"))
    private void omni$initShadeModel(int mode) {
        // Always smooth in shader pipeline
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glClearDepth(D)V"))
    private void omni$initClearDepth(double depth) {
        org.lwjgl.opengl.GL33.glClearDepth(depth);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V"))
    private void omni$initDepthFunc(int func) {
        RenderDevice.get().setDepthFunc(RenderTypes.CompareFunc.LEQUAL);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glAlphaFunc(IF)V"))
    private void omni$initAlphaFunc(int func, float ref) {
        OmniShaderManager.get().setAlphaTest(ref);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCullFace(I)V"))
    private void omni$initCullFace(int mode) {
        RenderDevice.get().setCullFace(true, RenderTypes.CullMode.BACK);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glMatrixMode(I)V"))
    private void omni$initMatrixMode(int mode) {
        // No-op - matrix mode is tracked in GameRendererMixin
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glLoadIdentity()V"))
    private void omni$initLoadIdentity() {
        // No-op - matrices managed by OmniMatrixStack
    }
}
