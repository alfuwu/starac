package com.periut.omni.backend.gl;

import com.periut.omni.backend.RenderContext;

public class GLRenderContext extends RenderContext {
    @Override
    public void init() {
        // OpenGL context is already set up by LWJGL/GLFW in the compatibility layer
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void beginFrame() {
    }

    @Override
    public void endFrame() {
    }

    @Override
    public void setVsync(boolean enabled) {
        org.lwjgl.glfw.GLFW.glfwSwapInterval(enabled ? 1 : 0);
    }
}
