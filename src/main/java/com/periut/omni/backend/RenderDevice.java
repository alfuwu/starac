package com.periut.omni.backend;

public abstract class RenderDevice {
    private static RenderDevice instance;

    // Lifecycle
    public abstract boolean init();
    public abstract void shutdown();

    // Viewport and clear
    public abstract void setViewport(int x, int y, int width, int height);
    public abstract void setClearColor(float r, float g, float b, float a);
    public abstract void clear(boolean color, boolean depth);

    // Depth state
    public abstract void setDepthTest(boolean enabled);
    public abstract void setDepthWrite(boolean enabled);
    public abstract void setDepthFunc(RenderTypes.CompareFunc func);

    // Culling
    public abstract void setCullFace(boolean enabled, RenderTypes.CullMode mode);
    public abstract void setFrontFace(RenderTypes.FrontFace face);

    // Blending
    public abstract void setBlend(boolean enabled, RenderTypes.BlendFactor src, RenderTypes.BlendFactor dst);

    // Polygon offset
    public abstract void setPolygonOffset(boolean enabled, float factor, float units);

    // Line width
    public abstract void setLineWidth(float width);

    // Color mask
    public abstract void setColorMask(boolean r, boolean g, boolean b, boolean a);

    // Scissor test
    public abstract void setScissorTest(boolean enabled);
    public abstract void setScissor(int x, int y, int width, int height);

    // Factory methods
    public abstract ShaderPipeline createShaderPipeline();
    public abstract VertexBuffer createVertexBuffer();
    public abstract IndexBuffer createIndexBuffer();
    public abstract Texture createTexture();

    // Draw commands
    public abstract void draw(RenderTypes.PrimitiveType primitive, int vertexCount, int startVertex);
    public abstract void drawIndexed(RenderTypes.PrimitiveType primitive, int indexCount, int startIndex);

    // Setup vertex attributes for the standard vertex format
    public abstract void setupVertexAttributes();

    // Singleton
    public static RenderDevice get() { return instance; }
    public static void setInstance(RenderDevice device) { instance = device; }
    public static boolean hasInstance() { return instance != null; }
}
