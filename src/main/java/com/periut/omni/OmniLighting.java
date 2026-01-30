package com.periut.omni;

public class OmniLighting {
    private static boolean enabled;

    // Matching Java Lighting class: two directional lights
    // Light 0: (0.2, 1.0, -0.7) normalized
    // Light 1: (-0.2, 1.0, 0.7) normalized
    private static final float[] LIGHT0_DIR;
    private static final float[] LIGHT1_DIR;

    static {
        // Normalize light directions
        float x0 = 0.2f, y0 = 1.0f, z0 = -0.7f;
        float len0 = (float) Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0);
        LIGHT0_DIR = new float[]{x0 / len0, y0 / len0, z0 / len0};

        float x1 = -0.2f, y1 = 1.0f, z1 = 0.7f;
        float len1 = (float) Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
        LIGHT1_DIR = new float[]{x1 / len1, y1 / len1, z1 / len1};
    }

    public static void enable() {
        enabled = true;
        OmniShaderManager sm = OmniShaderManager.get();
        sm.enableLighting(true);
        sm.setLightDirections(
                LIGHT0_DIR[0], LIGHT0_DIR[1], LIGHT0_DIR[2],
                LIGHT1_DIR[0], LIGHT1_DIR[1], LIGHT1_DIR[2]
        );
        sm.setLightParams(0.4f, 0.6f);
        sm.updateNormalMatrix();
    }

    public static void disable() {
        enabled = false;
        OmniShaderManager.get().enableLighting(false);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
