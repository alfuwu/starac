package com.periut.starac.mixin.omni;

import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import com.periut.omni.OmniTesselator;
import net.minecraft.SharedConstants;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.IntBuffer;

@Mixin(TextRenderer.class)
public class TextRendererMixin {

    @Shadow
    private int[] characterWidths;

    @Shadow
    public int boundTexture;

    @Unique
    private float[][] omni$colorPalette;

    // ========== Constructor: no-op all display list GL calls ==========

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V"))
    private void omni$initNewList(int list, int mode) {
        // No-op: display lists not available in core 3.3
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glEndList()V"))
    private void omni$initEndList() {
        // No-op
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V"))
    private void omni$initTranslate(float x, float y, float z) {
        // No-op: character advance is handled in our drawLayer
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor3f(FFF)V"))
    private void omni$initColor3f(float r, float g, float b) {
        // No-op: color palette built in our tail inject
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void omni$initColorPalette(GameOptions options, String fontPath, TextureManager textureManager, CallbackInfo ci) {
        omni$colorPalette = new float[32][3];
        for (int i = 0; i < 32; i++) {
            int base = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + base;
            int green = (i >> 1 & 1) * 170 + base;
            int blue = (i & 1) * 170 + base;
            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            if (options.anaglyph) {
                int anaR = (red * 30 + green * 59 + blue * 11) / 100;
                int anaG = (red * 30 + green * 70) / 100;
                int anaB = (red * 30 + blue * 70) / 100;
                red = anaR;
                green = anaG;
                blue = anaB;
            }

            omni$colorPalette[i][0] = red / 255.0f;
            omni$colorPalette[i][1] = green / 255.0f;
            omni$colorPalette[i][2] = blue / 255.0f;
        }
    }

    // ========== drawLayer: fully replace with direct rendering ==========

    /**
     * @author omni
     * @reason Replace display-list text rendering with direct quad rendering for GL 3.3 core
     */
    @Overwrite
    public void drawLayer(String text, int x, int y, int color, boolean shadow) {
        if (text == null) return;

        if (shadow) {
            int alphaBits = color & 0xFF000000;
            color = (color & 16579836) >> 2;
            color += alphaBits;
        }

        OmniTextureManager.get().bindTexture(this.boundTexture);
        OmniShaderManager sm = OmniShaderManager.get();
        sm.setUseTexture(true);
        sm.setUseVertexColor(false);

        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = (color >> 24 & 0xFF) / 255.0f;
        if (a == 0.0f) a = 1.0f;

        sm.setGuiColor(r, g, b, a);

        float cursorX = x;
        OmniTesselator t = OmniTesselator.INSTANCE;

        for (int j = 0; j < text.length(); j++) {
            // Handle color codes (§ followed by hex digit)
            while (text.length() > j + 1 && text.charAt(j) == 167) {
                int colorIndex = "0123456789abcdef".indexOf(text.toLowerCase().charAt(j + 1));
                if (colorIndex < 0 || colorIndex > 15) {
                    colorIndex = 15;
                }

                int paletteIdx = colorIndex + (shadow ? 16 : 0);
                if (omni$colorPalette != null && paletteIdx < omni$colorPalette.length) {
                    float[] c = omni$colorPalette[paletteIdx];
                    sm.setGuiColor(c[0], c[1], c[2], a);
                }

                j += 2;
            }

            if (j >= text.length()) break;

            int charIndex = SharedConstants.VALID_CHAT_CHARACTERS.indexOf(text.charAt(j));
            if (charIndex >= 0) {
                int charCode = charIndex + 32;
                omni$renderGlyph(t, sm, charCode, cursorX, y);
                cursorX += this.characterWidths[charCode];
            }
        }
    }

    @Unique
    private static void omni$renderGlyph(OmniTesselator t, OmniShaderManager sm, int charCode, float x, float y) {
        int col = charCode % 16;
        int row = charCode / 16;
        float u0 = col * 8 / 128.0f;
        float v0 = row * 8 / 128.0f;
        float u1 = (col * 8 + 7.99f) / 128.0f;
        float v1 = (row * 8 + 7.99f) / 128.0f;
        float size = 7.99f;

        t.begin(7); // QUADS
        t.tex(u0, v1);
        t.vertex(x, y + size, 0.0);
        t.tex(u1, v1);
        t.vertex(x + size, y + size, 0.0);
        t.tex(u1, v0);
        t.vertex(x + size, y, 0.0);
        t.tex(u0, v0);
        t.vertex(x, y, 0.0);
        t.end();
    }
}
