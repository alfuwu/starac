package com.periut.omni;

import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.ShaderPipeline;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class OmniShaderManager {
    private static final OmniShaderManager INSTANCE = new OmniShaderManager();

    private boolean initialized;

    private ShaderPipeline guiShader;
    private ShaderPipeline worldShader;
    private ShaderPipeline activeShader;

    // Shared uniform state
    private boolean useTexture;
    private boolean useVertexColor = true;
    private float guiColorR = 1f, guiColorG = 1f, guiColorB = 1f, guiColorA = 1f;
    private float alphaThreshold;

    // Fog state
    private boolean fogEnabled;
    private float fogStart, fogEnd;
    private float fogR, fogG, fogB;

    private OmniShaderManager() {}

    public static OmniShaderManager get() { return INSTANCE; }

    public void init() {
        if (initialized) return;

        RenderDevice device = RenderDevice.get();

        // Load GUI shader
        guiShader = device.createShaderPipeline();
        String guiVert = loadShaderSource("/assets/omni/shaders/gui.vert");
        String guiFrag = loadShaderSource("/assets/omni/shaders/gui.frag");
        if (guiVert == null || guiFrag == null) {
            System.err.println("[Omni] Failed to load GUI shader sources!");
            return;
        }
        if (!guiShader.loadFromGLSL(guiVert, guiFrag)) {
            System.err.println("[Omni] Failed to compile GUI shader!");
            return;
        }

        // Load world shader
        worldShader = device.createShaderPipeline();
        String worldVert = loadShaderSource("/assets/omni/shaders/world.vert");
        String worldFrag = loadShaderSource("/assets/omni/shaders/world.frag");
        if (worldVert == null || worldFrag == null) {
            System.err.println("[Omni] Failed to load world shader sources!");
            return;
        }
        if (!worldShader.loadFromGLSL(worldVert, worldFrag)) {
            System.err.println("[Omni] Failed to compile world shader!");
            return;
        }

        initialized = true;
        System.out.println("[Omni] Shaders initialized successfully.");
    }

    private static String loadShaderSource(String resourcePath) {
        try (InputStream is = OmniShaderManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[Omni] Shader resource not found: " + resourcePath);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            System.err.println("[Omni] Error loading shader: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    // ========== Shader switching ==========

    public void useGuiShader() {
        if (!initialized || guiShader == null) return;
        guiShader.bind();
        activeShader = guiShader;
        uploadGuiUniforms();
    }

    public void useWorldShader() {
        if (!initialized || worldShader == null) return;
        worldShader.bind();
        activeShader = worldShader;
        uploadWorldUniforms();
    }

    public void useSkyShader() {
        // Sky uses world shader (same fog/MVP support)
        useWorldShader();
    }

    public void useLineShader() {
        useWorldShader();
    }

    // ========== Uniform upload ==========

    private void uploadGuiUniforms() {
        if (activeShader != guiShader) return;
        guiShader.setMat4("uMVP", OmniMatrixStack.getMVP());
        guiShader.setInt("uTexture", 0);
        guiShader.setInt("uUseTexture", useTexture ? 1 : 0);
        guiShader.setInt("uUseVertexColor", useVertexColor ? 1 : 0);
        guiShader.setVec4("uColor", guiColorR, guiColorG, guiColorB, guiColorA);
        guiShader.setFloat("uAlphaTest", alphaThreshold);
    }

    private void uploadWorldUniforms() {
        if (activeShader != worldShader) return;
        worldShader.setMat4("uMVP", OmniMatrixStack.getMVP());
        worldShader.setInt("uTexture", 0);
        worldShader.setInt("uUseTexture", useTexture ? 1 : 0);
        worldShader.setInt("uUseVertexColor", useVertexColor ? 1 : 0);
        worldShader.setVec4("uColor", guiColorR, guiColorG, guiColorB, guiColorA);
        worldShader.setFloat("uAlphaTest", alphaThreshold);
        // Fog
        worldShader.setInt("uUseFog", fogEnabled ? 1 : 0);
        worldShader.setFloat("uFogStart", fogStart);
        worldShader.setFloat("uFogEnd", fogEnd);
        worldShader.setVec3("uFogColor", fogR, fogG, fogB);
    }

    // ========== Uniform setters ==========

    public void updateMatrices() {
        if (!initialized || activeShader == null) return;
        activeShader.setMat4("uMVP", OmniMatrixStack.getMVP());
    }

    public void updateNormalMatrix() {
        // Reserved for future lighting
    }

    public void updateFog(float start, float end, float r, float g, float b) {
        fogStart = start;
        fogEnd = end;
        fogR = r;
        fogG = g;
        fogB = b;
        if (initialized && activeShader == worldShader) {
            worldShader.setFloat("uFogStart", start);
            worldShader.setFloat("uFogEnd", end);
            worldShader.setVec3("uFogColor", r, g, b);
        }
    }

    public void setFogEnabled(boolean enabled) {
        fogEnabled = enabled;
        if (initialized && activeShader == worldShader) {
            worldShader.setInt("uUseFog", enabled ? 1 : 0);
        }
    }

    public void setAlphaTest(float threshold) {
        alphaThreshold = threshold;
        if (initialized && activeShader != null) {
            activeShader.setFloat("uAlphaTest", threshold);
        }
    }

    public void setUseTexture(boolean use) {
        useTexture = use;
        if (initialized && activeShader != null) {
            activeShader.setInt("uUseTexture", use ? 1 : 0);
        }
    }

    public void setGuiColor(float r, float g, float b, float a) {
        guiColorR = r;
        guiColorG = g;
        guiColorB = b;
        guiColorA = a;
        if (initialized && activeShader != null) {
            activeShader.setVec4("uColor", r, g, b, a);
        }
    }

    public void setUseVertexColor(boolean use) {
        useVertexColor = use;
        if (initialized && activeShader != null) {
            activeShader.setInt("uUseVertexColor", use ? 1 : 0);
        }
    }

    public void setSkyColor(float r, float g, float b, float a) {
        setGuiColor(r, g, b, a);
        setUseVertexColor(false);
    }

    // Lighting stubs
    public void enableLighting(boolean enable) {}
    public void setLightDirections(float dir0x, float dir0y, float dir0z,
                                   float dir1x, float dir1y, float dir1z) {}
    public void setLightParams(float ambient, float diffuse) {}
    public void setBrightness(float brightness) {}
    public void setSkyBrightness(float skyBrightness) {}

    // Fog getters
    public float getFogStart() { return fogStart; }
    public float getFogEnd() { return fogEnd; }
    public float getFogR() { return fogR; }
    public float getFogG() { return fogG; }
    public float getFogB() { return fogB; }

    public boolean isInitialized() { return initialized; }
}
