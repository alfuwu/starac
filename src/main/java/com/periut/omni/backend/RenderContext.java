package com.periut.omni.backend;

public abstract class RenderContext {
    private static RenderContext instance;

    public abstract void init();
    public abstract void shutdown();
    public abstract void beginFrame();
    public abstract void endFrame();
    public abstract void setVsync(boolean enabled);

    public static RenderContext get() { return instance; }
    public static void setInstance(RenderContext ctx) { instance = ctx; }
    public static boolean hasInstance() { return instance != null; }
}
