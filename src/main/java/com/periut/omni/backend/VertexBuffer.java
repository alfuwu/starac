package com.periut.omni.backend;

import java.nio.ByteBuffer;

public abstract class VertexBuffer {
    public abstract void create();
    public abstract void destroy();
    public abstract void upload(ByteBuffer data, int sizeBytes, RenderTypes.BufferUsage usage);
    public abstract void bind();
    public abstract void unbind();
    public abstract boolean isValid();
}
