package com.periut.omni.backend.gl;

import com.periut.omni.backend.RenderTypes;
import com.periut.omni.backend.IndexBuffer;
import org.lwjgl.opengl.GL33;

import java.nio.IntBuffer;

public class GLIndexBuffer extends IndexBuffer {
    private int ebo;
    private int indexCount;

    @Override
    public void create() {
        if (ebo == 0) {
            ebo = GL33.glGenBuffers();
        }
    }

    @Override
    public void destroy() {
        if (ebo != 0) {
            GL33.glDeleteBuffers(ebo);
            ebo = 0;
        }
        indexCount = 0;
    }

    @Override
    public void upload(IntBuffer data, int count, RenderTypes.BufferUsage usage) {
        indexCount = count;
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, data, toGLUsage(usage));
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo);
    }

    @Override
    public void unbind() {
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public int getCount() {
        return indexCount;
    }

    @Override
    public boolean isValid() {
        return ebo != 0;
    }

    private static int toGLUsage(RenderTypes.BufferUsage usage) {
        return switch (usage) {
            case STATIC -> GL33.GL_STATIC_DRAW;
            case DYNAMIC -> GL33.GL_DYNAMIC_DRAW;
            case STREAM -> GL33.GL_STREAM_DRAW;
        };
    }
}
