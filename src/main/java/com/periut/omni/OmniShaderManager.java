package com.periut.omni;

import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.ShaderPipeline;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class OmniShaderManager {
    private static final OmniShaderManager INSTANCE = new OmniShaderManager();

    private ShaderPipeline worldShader;
    private ShaderPipeline skyShader;
    private ShaderPipeline guiShader;
    private ShaderPipeline lineShader;

    private ShaderPipeline currentShader;
    private boolean initialized;

    private float fogStart, fogEnd;
    private float fogR, fogG, fogB;
    private float alphaThreshold;

    private OmniShaderManager() {}

    public static OmniShaderManager get() { return INSTANCE; }

    public void init() {
        if (initialized) return;

        RenderDevice device = RenderDevice.get();

        worldShader = device.createShaderPipeline();
        if (!worldShader.loadFromGLSL(loadResource("world.vert"), loadResource("world.frag"))) {
            System.err.println("Failed to load world shader");
        }

        skyShader = device.createShaderPipeline();
        if (!skyShader.loadFromGLSL(loadResource("sky.vert"), loadResource("sky.frag"))) {
            System.err.println("Failed to load sky shader");
        }

        guiShader = device.createShaderPipeline();
        if (!guiShader.loadFromGLSL(loadResource("gui.vert"), loadResource("gui.frag"))) {
            System.err.println("Failed to load gui shader");
        }

        lineShader = device.createShaderPipeline();
        if (!lineShader.loadFromGLSL(loadResource("line.vert"), loadResource("line.frag"))) {
            System.err.println("Failed to load line shader");
        }

        initialized = true;
    }

    private String loadResource(String name) {
        String path = "/assets/omni/shaders/" + name;
        try (InputStream is = OmniShaderManager.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Shader resource not found: " + path);
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to read shader: " + path);
            return "";
        }
    }

    public ShaderPipeline getWorldShader() { return worldShader; }
    public ShaderPipeline getSkyShader() { return skyShader; }
    public ShaderPipeline getGuiShader() { return guiShader; }
    public ShaderPipeline getLineShader() { return lineShader; }
    public ShaderPipeline getCurrentShader() { return currentShader; }

    public void useWorldShader() {
        worldShader.bind();
        currentShader = worldShader;
        updateMatrices();
        worldShader.setInt("uTexture", 0);
        worldShader.setInt("uUseTexture", 1);
        worldShader.setFloat("uFogStart", fogStart);
        worldShader.setFloat("uFogEnd", fogEnd);
        worldShader.setVec3("uFogColor", fogR, fogG, fogB);
        worldShader.setFloat("uAlphaTest", alphaThreshold);
        worldShader.setFloat("uBrightness", 1.0f);
        worldShader.setFloat("uSkyBrightness", 1.0f);
        worldShader.setInt("uEnableLighting", 0);
    }

    public void useSkyShader() {
        skyShader.bind();
        currentShader = skyShader;
        updateMatrices();
        skyShader.setInt("uTexture", 0);
    }

    public void useGuiShader() {
        guiShader.bind();
        currentShader = guiShader;
        updateMatrices();
        guiShader.setInt("uTexture", 0);
        guiShader.setFloat("uAlphaTest", 0.01f);
    }

    public void useLineShader() {
        lineShader.bind();
        currentShader = lineShader;
        updateMatrices();
    }

    public void updateMatrices() {
        if (currentShader == null) return;
        float[] mvp = OmniMatrixStack.getMVP();
        float[] mv = OmniMatrixStack.modelview().get();
        currentShader.setMat4("uMVP", mvp);
        currentShader.setMat4("uModelView", mv);
    }

    public void updateFog(float start, float end, float r, float g, float b) {
        fogStart = start;
        fogEnd = end;
        fogR = r;
        fogG = g;
        fogB = b;
        if (currentShader == worldShader) {
            worldShader.setFloat("uFogStart", fogStart);
            worldShader.setFloat("uFogEnd", fogEnd);
            worldShader.setVec3("uFogColor", fogR, fogG, fogB);
        }
    }

    public void setAlphaTest(float threshold) {
        alphaThreshold = threshold;
        if (currentShader == worldShader) {
            worldShader.setFloat("uAlphaTest", threshold);
        } else if (currentShader == guiShader) {
            guiShader.setFloat("uAlphaTest", threshold);
        }
    }

    public void setUseTexture(boolean use) {
        if (currentShader != null) {
            currentShader.setInt("uUseTexture", use ? 1 : 0);
            if (use && (currentShader == skyShader || currentShader == guiShader)) {
                currentShader.setInt("uUseUniformColor", 0);
            }
        }
    }

    public void setSkyColor(float r, float g, float b, float a) {
        if (currentShader == skyShader) {
            skyShader.setVec4("uColor", r, g, b, a);
            skyShader.setInt("uUseUniformColor", 1);
        }
    }

    public void setGuiColor(float r, float g, float b, float a) {
        if (currentShader == guiShader) {
            guiShader.setVec4("uColor", r, g, b, a);
            guiShader.setInt("uUseUniformColor", 1);
        }
    }

    public void setUseVertexColor(boolean use) {
        if (currentShader != null) {
            currentShader.setInt("uUseUniformColor", use ? 0 : 1);
        }
    }

    public void enableLighting(boolean enable) {
        if (currentShader == worldShader) {
            worldShader.setInt("uEnableLighting", enable ? 1 : 0);
        }
    }

    public void setLightDirections(float dir0x, float dir0y, float dir0z,
                                   float dir1x, float dir1y, float dir1z) {
        if (currentShader == worldShader) {
            worldShader.setVec3("uLightDir0", dir0x, dir0y, dir0z);
            worldShader.setVec3("uLightDir1", dir1x, dir1y, dir1z);
        }
    }

    public void setLightParams(float ambient, float diffuse) {
        if (currentShader == worldShader) {
            worldShader.setFloat("uAmbient", ambient);
            worldShader.setFloat("uDiffuse", diffuse);
        }
    }

    public void setBrightness(float brightness) {
        if (currentShader == worldShader) {
            worldShader.setFloat("uBrightness", brightness);
        }
    }

    public void setSkyBrightness(float skyBrightness) {
        if (currentShader == worldShader) {
            worldShader.setFloat("uSkyBrightness", skyBrightness);
        }
    }

    public void updateNormalMatrix() {
        if (currentShader == null) return;
        float[] nm = OmniMatrixStack.getNormalMatrix3x3();
        currentShader.setMat3("uNormalMatrix", nm);
    }

    public float getFogStart() { return fogStart; }
    public float getFogEnd() { return fogEnd; }
    public float getFogR() { return fogR; }
    public float getFogG() { return fogG; }
    public float getFogB() { return fogB; }

    public boolean isInitialized() { return initialized; }
}
