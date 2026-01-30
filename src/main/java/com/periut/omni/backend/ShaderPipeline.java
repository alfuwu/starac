package com.periut.omni.backend;

public abstract class ShaderPipeline {
    public abstract boolean loadFromGLSL(String vertexSource, String fragmentSource);
    public abstract void bind();
    public abstract void unbind();

    public abstract void setInt(String name, int value);
    public abstract void setFloat(String name, float value);
    public abstract void setVec2(String name, float x, float y);
    public abstract void setVec3(String name, float x, float y, float z);
    public abstract void setVec4(String name, float x, float y, float z, float w);
    public abstract void setMat3(String name, float[] matrix);
    public abstract void setMat4(String name, float[] matrix);

    public abstract boolean isValid();
}
