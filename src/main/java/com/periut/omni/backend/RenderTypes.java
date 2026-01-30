package com.periut.omni.backend;

public final class RenderTypes {
    private RenderTypes() {}

    public enum CompareFunc {
        NEVER, LESS, EQUAL, LEQUAL, GREATER, NOTEQUAL, GEQUAL, ALWAYS
    }

    public enum CullMode {
        NONE, BACK, FRONT
    }

    public enum FrontFace {
        CCW, CW
    }

    public enum BlendFactor {
        ZERO, ONE, SRC_ALPHA, ONE_MINUS_SRC_ALPHA, DST_ALPHA, ONE_MINUS_DST_ALPHA,
        DST_COLOR, SRC_COLOR, ONE_MINUS_DST_COLOR, ONE_MINUS_SRC_COLOR
    }

    public enum BufferUsage {
        STATIC, DYNAMIC, STREAM
    }

    public enum PrimitiveType {
        TRIANGLES, LINES, LINE_STRIP, POINTS
    }

    public enum DrawMode {
        QUADS, TRIANGLES, TRIANGLE_FAN, LINES, LINE_STRIP, LINE_LOOP, POINTS
    }

    public enum TextureFilter {
        NEAREST, LINEAR, NEAREST_MIPMAP_NEAREST, NEAREST_MIPMAP_LINEAR,
        LINEAR_MIPMAP_NEAREST, LINEAR_MIPMAP_LINEAR
    }

    public enum TextureWrap {
        REPEAT, CLAMP_TO_EDGE, MIRRORED_REPEAT
    }

    public static final int VERTEX_STRIDE = 32;
    public static final int ATTRIB_POSITION = 0;  // 3 floats, offset 0
    public static final int ATTRIB_TEXCOORD = 1;  // 2 floats, offset 12
    public static final int ATTRIB_COLOR = 2;     // 4 bytes normalized, offset 20
    public static final int ATTRIB_NORMAL = 3;    // 3 bytes + 1 pad, offset 24
    public static final int ATTRIB_LIGHT = 4;     // 2 bytes + 2 pad, offset 28
}
