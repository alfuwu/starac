package com.periut.starac.mixin.omni;

import com.periut.omni.OmniDisplayList;
import com.periut.omni.OmniMatrixStack;
import com.periut.omni.OmniShaderManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Global override of legacy OpenGL fixed-function calls removed in GL 3.3 core profile.
 * Only overrides methods that are REMOVED in core profile.
 * Valid core functions (glBindTexture, glBlendFunc, glViewport, etc.) are NOT overwritten
 * since they work fine as-is through LWJGL3.
 */
@Mixin(value = GL11.class, remap = false)
public class OmniGL11Mixin {

    // ========== Color (removed in core) ==========

    /** @author omni @reason glColor4f removed in core profile */
    @Overwrite
    public static void glColor4f(float r, float g, float b, float a) {
        OmniShaderManager.get().setGuiColor(r, g, b, a);
        OmniShaderManager.get().setUseVertexColor(false);
    }

    /** @author omni @reason glColor3f removed in core profile */
    @Overwrite
    public static void glColor3f(float r, float g, float b) {
        OmniShaderManager.get().setGuiColor(r, g, b, 1.0f);
        OmniShaderManager.get().setUseVertexColor(false);
    }

    /** @author omni @reason glColor4ub removed in core profile */
    @Overwrite
    public static void glColor4ub(byte r, byte g, byte b, byte a) {
        OmniShaderManager.get().setGuiColor(
                (r & 0xFF) / 255.0f, (g & 0xFF) / 255.0f,
                (b & 0xFF) / 255.0f, (a & 0xFF) / 255.0f);
        OmniShaderManager.get().setUseVertexColor(false);
    }

    // ========== Matrix stack (removed in core) ==========

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glMatrixMode(int mode) {
        OmniMatrixStack.setMatrixMode(mode);
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glLoadIdentity() {
        OmniMatrixStack.current().loadIdentity();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glPushMatrix() {
        if (OmniDisplayList.isRecording()) return;
        OmniMatrixStack.current().push();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glPopMatrix() {
        if (OmniDisplayList.isRecording()) return;
        OmniMatrixStack.current().pop();
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glTranslatef(float x, float y, float z) {
        if (OmniDisplayList.isRecording()) {
            OmniDisplayList.recordTranslate(x, y, z);
            return;
        }
        OmniMatrixStack.current().translate(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glRotatef(float angle, float x, float y, float z) {
        OmniMatrixStack.current().rotate(angle, x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glScalef(float x, float y, float z) {
        if (OmniDisplayList.isRecording()) return;
        OmniMatrixStack.current().scale(x, y, z);
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glScaled(double x, double y, double z) {
        if (OmniDisplayList.isRecording()) return;
        OmniMatrixStack.current().scale((float) x, (float) y, (float) z);
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glOrtho(double left, double right, double bottom, double top, double near, double far) {
        OmniMatrixStack.current().ortho((float) left, (float) right, (float) bottom, (float) top, (float) near, (float) far);
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glMultMatrixf(FloatBuffer m) {
        float[] matrix = new float[16];
        int pos = m.position();
        for (int i = 0; i < 16; i++) {
            matrix[i] = m.get(pos + i);
        }
        OmniMatrixStack.current().multiply(matrix);
        OmniShaderManager.get().updateMatrices();
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glLoadMatrixf(FloatBuffer m) {
        float[] matrix = new float[16];
        int pos = m.position();
        for (int i = 0; i < 16; i++) {
            matrix[i] = m.get(pos + i);
        }
        OmniMatrixStack.current().load(matrix);
        OmniShaderManager.get().updateMatrices();
    }

    // ========== Enable/Disable (valid function, but some caps removed in core) ==========

    /** @author omni @reason Filter removed capabilities */
    @Overwrite
    public static void glEnable(int cap) {
        switch (cap) {
            case 3553 -> OmniShaderManager.get().setUseTexture(true);  // GL_TEXTURE_2D
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.1f);   // GL_ALPHA_TEST
            case 2912 -> OmniShaderManager.get().setFogEnabled(true);   // GL_FOG
            case 2896, 16384, 16385, 2903, 32826, 2977 -> {}           // LIGHTING, LIGHT0/1, COLOR_MATERIAL, RESCALE_NORMAL, NORMALIZE
            default -> GL11C.glEnable(cap);                            // Valid core caps: pass through
        }
    }

    /** @author omni @reason Filter removed capabilities */
    @Overwrite
    public static void glDisable(int cap) {
        switch (cap) {
            case 3553 -> OmniShaderManager.get().setUseTexture(false);
            case 3008 -> OmniShaderManager.get().setAlphaTest(0.0f);
            case 2912 -> OmniShaderManager.get().setFogEnabled(false);  // GL_FOG
            case 2896, 16384, 16385, 2903, 32826, 2977 -> {}
            default -> GL11C.glDisable(cap);
        }
    }

    // ========== Fixed-function state (removed in core) ==========

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glShadeModel(int mode) {}

    /** @author omni @reason removed in core profile; handled by shader */
    @Overwrite
    public static void glAlphaFunc(int func, float ref) {
        if (func == 516) { // GL_GREATER
            OmniShaderManager.get().setAlphaTest(ref);
        } else {
            OmniShaderManager.get().setAlphaTest(0.0f);
        }
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glNormal3f(float x, float y, float z) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glColorMaterial(int face, int mode) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glFogi(int pname, int param) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glFogf(int pname, float param) {
        OmniShaderManager sm = OmniShaderManager.get();
        switch (pname) {
            case 2915 -> sm.updateFog(param, sm.getFogEnd(), sm.getFogR(), sm.getFogG(), sm.getFogB());
            case 2916 -> sm.updateFog(sm.getFogStart(), param, sm.getFogR(), sm.getFogG(), sm.getFogB());
            default -> {}
        }
    }

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glTexEnvi(int target, int pname, int param) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glTexGeni(int coord, int pname, int param) {}

    // ========== Display lists (removed in core, emulated via OmniDisplayList) ==========

    /** @author omni @reason emulated via OmniDisplayList */
    @Overwrite
    public static void glNewList(int list, int mode) {
        OmniDisplayList.beginRecording(list);
    }

    /** @author omni @reason emulated via OmniDisplayList */
    @Overwrite
    public static void glEndList() {
        OmniDisplayList.endRecording();
    }

    /** @author omni @reason emulated via OmniDisplayList */
    @Overwrite
    public static void glCallList(int list) {
        OmniDisplayList.replay(list);
    }

    /** @author omni @reason emulated via OmniDisplayList */
    @Overwrite
    public static void glCallLists(IntBuffer lists) {
        while (lists.hasRemaining()) {
            OmniDisplayList.replay(lists.get());
        }
    }

    /** @author omni @reason emulated via OmniDisplayList */
    @Overwrite
    public static int glGenLists(int s) {
        // Allocation handled by MemoryTrackerMixin; this is a fallback
        int base = OmniDisplayList.allocateIds(s);
        return base;
    }

    /** @author omni @reason emulated via OmniDisplayList */
    @Overwrite
    public static void glDeleteLists(int list, int range) {
        OmniDisplayList.deleteLists(list, range);
    }

    // ========== Immediate mode (removed in core) ==========

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glBegin(int mode) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glEnd() {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glVertex2f(float x, float y) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glVertex3f(float x, float y, float z) {}

    /** @author omni @reason removed in core profile */
    @Overwrite
    public static void glTexCoord2f(float s, float t) {}

}
