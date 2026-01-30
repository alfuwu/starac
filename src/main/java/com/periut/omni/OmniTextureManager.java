package com.periut.omni;

import org.lwjgl.opengl.GL33;

public class OmniTextureManager {
    private static final OmniTextureManager INSTANCE = new OmniTextureManager();

    private OmniTextureManager() {}

    public static OmniTextureManager get() { return INSTANCE; }

    public void bindTexture(int glTextureId) {
        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, glTextureId);
    }
}
