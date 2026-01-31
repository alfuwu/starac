package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import com.periut.omni.OmniTesselator;
import com.periut.omni.backend.RenderDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.texture.TextureManager;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public class LoadingScreenMixin {

    @Shadow
    public net.minecraft.client.options.GameOptions options;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    public TextureManager textureManager;

    /**
     * @author omni
     * @reason Replace fixed-function loading screen with GL 3.3 core rendering
     */
    @Overwrite
    private void renderLoadingScreen() {
        Window window = new Window(this.options, this.width, this.height);

        RenderDevice device = RenderDevice.get();
        device.clear(true, true);
        device.setViewport(0, 0, this.width, this.height);
        device.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Set up ortho projection
        OmniMatrixStack.projection().loadIdentity();
        OmniMatrixStack.projection().ortho(0, (float) window.scaledWidth, (float) window.scaledHeight, 0, 1000, 3000);
        OmniMatrixStack.modelview().loadIdentity();
        OmniMatrixStack.modelview().translate(0, 0, -2000);

        OmniShaderManager sm = OmniShaderManager.get();
        sm.useGuiShader();
        sm.setUseTexture(true);
        sm.setUseVertexColor(false);
        sm.setGuiColor(1.0f, 1.0f, 1.0f, 1.0f);
        sm.setAlphaTest(0.0f);

        // Draw black background quad
        sm.setUseTexture(false);
        sm.setUseVertexColor(true);
        OmniTesselator t = OmniTesselator.INSTANCE;
        t.begin(7);
        t.color(255, 255, 255);
        t.vertex(0.0, this.height, 0.0);
        t.vertex(this.width, this.height, 0.0);
        t.vertex(this.width, 0.0, 0.0);
        t.vertex(0.0, 0.0, 0.0);
        t.end();

        // Draw Mojang logo
        sm.setUseTexture(true);
        sm.setUseVertexColor(false);
        sm.setGuiColor(1.0f, 1.0f, 1.0f, 1.0f);
        OmniTextureManager.get().bindTexture(this.textureManager.load("/title/mojang.png"));

        int logoSize = 256;
        int lx = (window.getWidth() - logoSize) / 2;
        int ly = (window.getHeight() - logoSize) / 2;
        float f = 0.00390625f;
        float g = 0.00390625f;

        t.begin(7);
        t.tex(0 * f, (0 + logoSize) * g);
        t.vertex(lx, ly + logoSize, 0.0);
        t.tex((0 + logoSize) * f, (0 + logoSize) * g);
        t.vertex(lx + logoSize, ly + logoSize, 0.0);
        t.tex((0 + logoSize) * f, 0 * g);
        t.vertex(lx + logoSize, ly, 0.0);
        t.tex(0 * f, 0 * g);
        t.vertex(lx, ly, 0.0);
        t.end();

        sm.setAlphaTest(0.1f);

        Display.swapBuffers();
    }
}
