package com.periut.omni.backend;

public abstract class Texture {
    protected int width;
    protected int height;

    public abstract void create();
    public abstract void destroy();
    public abstract void upload(int width, int height, byte[] rgba, boolean generateMipmaps);
    public abstract void setFilter(RenderTypes.TextureFilter min, RenderTypes.TextureFilter mag);
    public abstract void setWrap(RenderTypes.TextureWrap s, RenderTypes.TextureWrap t);
    public abstract void bind(int unit);
    public abstract void unbind(int unit);

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public abstract boolean isValid();
}
