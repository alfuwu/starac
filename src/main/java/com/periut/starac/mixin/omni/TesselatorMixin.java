package com.periut.starac.mixin.omni;

import com.periut.omni.OmniTesselator;
import net.minecraft.client.render.vertex.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Tesselator.class)
public class TesselatorMixin {
    @Shadow
    private boolean tesselating;

    /**
     * @author omni
     * @reason Replace legacy Tesselator with OmniTesselator for modern GL 3.3 rendering
     */
    @Overwrite
    public void begin(int drawMode) {
        OmniTesselator.INSTANCE.begin(drawMode);
        tesselating = true;
    }

    /**
     * @author omni
     * @reason Replace legacy Tesselator end/flush with OmniTesselator VBO path
     */
    @Overwrite
    public void end() {
        OmniTesselator.INSTANCE.end();
        tesselating = false;
    }

    /**
     * @author omni
     * @reason Redirect vertex to OmniTesselator
     */
    @Overwrite
    public void vertex(double x, double y, double z) {
        OmniTesselator.INSTANCE.vertex(x, y, z);
    }

    /**
     * @author omni
     * @reason Redirect vertex+UV to OmniTesselator
     */
    @Overwrite
    public void vertex(double x, double y, double z, double u, double v) {
        OmniTesselator.INSTANCE.vertexUV(x, y, z, u, v);
    }

    /**
     * @author omni
     * @reason Redirect texture to OmniTesselator
     */
    @Overwrite
    public void texture(double u, double v) {
        OmniTesselator.INSTANCE.tex(u, v);
    }

    /**
     * @author omni
     * @reason Redirect color(fff) to OmniTesselator
     */
    @Overwrite
    public void color(float r, float g, float b) {
        OmniTesselator.INSTANCE.color(r, g, b);
    }

    /**
     * @author omni
     * @reason Redirect color(ffff) to OmniTesselator
     */
    @Overwrite
    public void color(float r, float g, float b, float a) {
        OmniTesselator.INSTANCE.color(r, g, b, a);
    }

    /**
     * @author omni
     * @reason Redirect color(iii) to OmniTesselator
     */
    @Overwrite
    public void color(int r, int g, int b) {
        OmniTesselator.INSTANCE.color(r, g, b);
    }

    /**
     * @author omni
     * @reason Redirect color(iiii) to OmniTesselator
     */
    @Overwrite
    public void color(int r, int g, int b, int a) {
        OmniTesselator.INSTANCE.color(r, g, b, a);
    }

    /**
     * @author omni
     * @reason Redirect color(int rgba) to OmniTesselator
     */
    @Overwrite
    public void color(int rgb) {
        OmniTesselator.INSTANCE.colorRGBA(rgb);
    }

    /**
     * @author omni
     * @reason Redirect color(int,int) to OmniTesselator
     */
    @Overwrite
    public void color(int rgb, int alpha) {
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        OmniTesselator.INSTANCE.color(r, g, b, alpha);
    }

    /**
     * @author omni
     * @reason Redirect normal to OmniTesselator
     */
    @Overwrite
    public void normal(float x, float y, float z) {
        OmniTesselator.INSTANCE.normal(x, y, z);
    }

    /**
     * @author omni
     * @reason Redirect offset to OmniTesselator
     */
    @Overwrite
    public void offset(double x, double y, double z) {
        OmniTesselator.INSTANCE.offset(x, y, z);
    }

    /**
     * @author omni
     * @reason Redirect uncolored to OmniTesselator
     */
    @Overwrite
    public void uncolored() {
        OmniTesselator.INSTANCE.noColor();
    }

    /**
     * @author omni
     * @reason No-op begin() calls begin(7) which is QUADS
     */
    @Overwrite
    public void begin() {
        OmniTesselator.INSTANCE.begin(7);
        tesselating = true;
    }
}
