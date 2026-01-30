package com.periut.omni.backend.gl;

import com.periut.omni.backend.*;
import org.lwjgl.opengl.GL33;

public class GLRenderDevice extends RenderDevice {
    private int cachedVAO;
    private boolean vaoCreated;

    @Override
    public boolean init() {
        vaoCreated = false;
        return true;
    }

    @Override
    public void shutdown() {
        if (vaoCreated) {
            GL33.glDeleteVertexArrays(cachedVAO);
            cachedVAO = 0;
            vaoCreated = false;
        }
    }

    @Override
    public void setViewport(int x, int y, int width, int height) {
        GL33.glViewport(x, y, width, height);
    }

    @Override
    public void setClearColor(float r, float g, float b, float a) {
        GL33.glClearColor(r, g, b, a);
    }

    @Override
    public void clear(boolean color, boolean depth) {
        int mask = 0;
        if (color) mask |= GL33.GL_COLOR_BUFFER_BIT;
        if (depth) mask |= GL33.GL_DEPTH_BUFFER_BIT;
        GL33.glClear(mask);
    }

    @Override
    public void setDepthTest(boolean enabled) {
        if (enabled) GL33.glEnable(GL33.GL_DEPTH_TEST);
        else GL33.glDisable(GL33.GL_DEPTH_TEST);
    }

    @Override
    public void setDepthWrite(boolean enabled) {
        GL33.glDepthMask(enabled);
    }

    @Override
    public void setDepthFunc(RenderTypes.CompareFunc func) {
        GL33.glDepthFunc(toGLCompareFunc(func));
    }

    @Override
    public void setCullFace(boolean enabled, RenderTypes.CullMode mode) {
        if (enabled) {
            GL33.glEnable(GL33.GL_CULL_FACE);
            switch (mode) {
                case BACK -> GL33.glCullFace(GL33.GL_BACK);
                case FRONT -> GL33.glCullFace(GL33.GL_FRONT);
                case NONE -> GL33.glDisable(GL33.GL_CULL_FACE);
            }
        } else {
            GL33.glDisable(GL33.GL_CULL_FACE);
        }
    }

    @Override
    public void setFrontFace(RenderTypes.FrontFace face) {
        GL33.glFrontFace(face == RenderTypes.FrontFace.CCW ? GL33.GL_CCW : GL33.GL_CW);
    }

    @Override
    public void setBlend(boolean enabled, RenderTypes.BlendFactor src, RenderTypes.BlendFactor dst) {
        if (enabled) {
            GL33.glEnable(GL33.GL_BLEND);
            GL33.glBlendFunc(toGLBlendFactor(src), toGLBlendFactor(dst));
        } else {
            GL33.glDisable(GL33.GL_BLEND);
        }
    }

    @Override
    public void setPolygonOffset(boolean enabled, float factor, float units) {
        if (enabled) {
            GL33.glEnable(GL33.GL_POLYGON_OFFSET_FILL);
            GL33.glPolygonOffset(factor, units);
        } else {
            GL33.glDisable(GL33.GL_POLYGON_OFFSET_FILL);
        }
    }

    @Override
    public void setLineWidth(float width) {
        GL33.glLineWidth(width);
    }

    @Override
    public void setColorMask(boolean r, boolean g, boolean b, boolean a) {
        GL33.glColorMask(r, g, b, a);
    }

    @Override
    public void setScissorTest(boolean enabled) {
        if (enabled) GL33.glEnable(GL33.GL_SCISSOR_TEST);
        else GL33.glDisable(GL33.GL_SCISSOR_TEST);
    }

    @Override
    public void setScissor(int x, int y, int width, int height) {
        GL33.glScissor(x, y, width, height);
    }

    @Override
    public ShaderPipeline createShaderPipeline() {
        return new GLShaderPipeline();
    }

    @Override
    public VertexBuffer createVertexBuffer() {
        GLVertexBuffer buf = new GLVertexBuffer();
        buf.create();
        return buf;
    }

    @Override
    public IndexBuffer createIndexBuffer() {
        GLIndexBuffer buf = new GLIndexBuffer();
        buf.create();
        return buf;
    }

    @Override
    public Texture createTexture() {
        GLTexture tex = new GLTexture();
        tex.create();
        return tex;
    }

    @Override
    public void draw(RenderTypes.PrimitiveType primitive, int vertexCount, int startVertex) {
        GL33.glDrawArrays(toGLPrimitive(primitive), startVertex, vertexCount);
    }

    @Override
    public void drawIndexed(RenderTypes.PrimitiveType primitive, int indexCount, int startIndex) {
        GL33.glDrawElements(toGLPrimitive(primitive), indexCount, GL33.GL_UNSIGNED_INT, (long) startIndex * 4);
    }

    @Override
    public void setupVertexAttributes() {
        if (!vaoCreated) {
            cachedVAO = GL33.glGenVertexArrays();
            GL33.glBindVertexArray(cachedVAO);
            vaoCreated = true;
        } else {
            GL33.glBindVertexArray(cachedVAO);
        }

        // Position: 3 floats at offset 0
        GL33.glEnableVertexAttribArray(RenderTypes.ATTRIB_POSITION);
        GL33.glVertexAttribPointer(RenderTypes.ATTRIB_POSITION, 3, GL33.GL_FLOAT, false,
                RenderTypes.VERTEX_STRIDE, 0);

        // TexCoord: 2 floats at offset 12
        GL33.glEnableVertexAttribArray(RenderTypes.ATTRIB_TEXCOORD);
        GL33.glVertexAttribPointer(RenderTypes.ATTRIB_TEXCOORD, 2, GL33.GL_FLOAT, false,
                RenderTypes.VERTEX_STRIDE, 12);

        // Color: 4 bytes (normalized) at offset 20
        GL33.glEnableVertexAttribArray(RenderTypes.ATTRIB_COLOR);
        GL33.glVertexAttribPointer(RenderTypes.ATTRIB_COLOR, 4, GL33.GL_UNSIGNED_BYTE, true,
                RenderTypes.VERTEX_STRIDE, 20);

        // Normal: 3 signed bytes (normalized) at offset 24
        GL33.glEnableVertexAttribArray(RenderTypes.ATTRIB_NORMAL);
        GL33.glVertexAttribPointer(RenderTypes.ATTRIB_NORMAL, 3, GL33.GL_BYTE, true,
                RenderTypes.VERTEX_STRIDE, 24);

        // Light: 2 bytes at offset 28
        GL33.glEnableVertexAttribArray(RenderTypes.ATTRIB_LIGHT);
        GL33.glVertexAttribPointer(RenderTypes.ATTRIB_LIGHT, 2, GL33.GL_UNSIGNED_BYTE, false,
                RenderTypes.VERTEX_STRIDE, 28);
    }

    public static int toGLPrimitive(RenderTypes.PrimitiveType prim) {
        return switch (prim) {
            case TRIANGLES -> GL33.GL_TRIANGLES;
            case LINES -> GL33.GL_LINES;
            case LINE_STRIP -> GL33.GL_LINE_STRIP;
            case POINTS -> GL33.GL_POINTS;
        };
    }

    public static int toGLCompareFunc(RenderTypes.CompareFunc func) {
        return switch (func) {
            case NEVER -> GL33.GL_NEVER;
            case LESS -> GL33.GL_LESS;
            case EQUAL -> GL33.GL_EQUAL;
            case LEQUAL -> GL33.GL_LEQUAL;
            case GREATER -> GL33.GL_GREATER;
            case NOTEQUAL -> GL33.GL_NOTEQUAL;
            case GEQUAL -> GL33.GL_GEQUAL;
            case ALWAYS -> GL33.GL_ALWAYS;
        };
    }

    public static int toGLBlendFactor(RenderTypes.BlendFactor factor) {
        return switch (factor) {
            case ZERO -> GL33.GL_ZERO;
            case ONE -> GL33.GL_ONE;
            case SRC_ALPHA -> GL33.GL_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA -> GL33.GL_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA -> GL33.GL_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA -> GL33.GL_ONE_MINUS_DST_ALPHA;
            case DST_COLOR -> GL33.GL_DST_COLOR;
            case SRC_COLOR -> GL33.GL_SRC_COLOR;
            case ONE_MINUS_DST_COLOR -> GL33.GL_ONE_MINUS_DST_COLOR;
            case ONE_MINUS_SRC_COLOR -> GL33.GL_ONE_MINUS_SRC_COLOR;
        };
    }
}
