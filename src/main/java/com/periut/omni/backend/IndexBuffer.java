package com.periut.omni.backend;

import java.nio.IntBuffer;

public abstract class IndexBuffer {
    public abstract void create();
    public abstract void destroy();
    public abstract void upload(IntBuffer data, int count, RenderTypes.BufferUsage usage);
    public abstract void bind();
    public abstract void unbind();
    public abstract int getCount();
    public abstract boolean isValid();
}
