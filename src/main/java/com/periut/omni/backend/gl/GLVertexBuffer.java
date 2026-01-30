package com.periut.omni.backend.gl;

import com.periut.omni.backend.RenderTypes;
import com.periut.omni.backend.VertexBuffer;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class GLVertexBuffer extends VertexBuffer {
    private int vbo;

    @Override
    public void create() {
        if (vbo == 0) {
            vbo = GL33.glGenBuffers();
        }
    }

    @Override
    public void destroy() {
        if (vbo != 0) {
            GL33.glDeleteBuffers(vbo);
            vbo = 0;
        }
    }

    @Override
    public void upload(ByteBuffer data, int sizeBytes, RenderTypes.BufferUsage usage) {
        bind();
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, data, toGLUsage(usage));
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
    }

    @Override
    public void unbind() {
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public boolean isValid() {
        return vbo != 0;
    }

    private static int toGLUsage(RenderTypes.BufferUsage usage) {
        return switch (usage) {
            case STATIC -> GL33.GL_STATIC_DRAW;
            case DYNAMIC -> GL33.GL_DYNAMIC_DRAW;
            case STREAM -> GL33.GL_STREAM_DRAW;
        };
    }
}
