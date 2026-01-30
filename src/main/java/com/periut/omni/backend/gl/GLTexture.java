package com.periut.omni.backend.gl;

import com.periut.omni.backend.RenderTypes;
import com.periut.omni.backend.Texture;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class GLTexture extends Texture {
    private int textureId;

    @Override
    public void create() {
        if (textureId == 0) {
            textureId = GL33.glGenTextures();
        }
    }

    @Override
    public void destroy() {
        if (textureId != 0) {
            GL33.glDeleteTextures(textureId);
            textureId = 0;
        }
        width = 0;
        height = 0;
    }

    @Override
    public void upload(int w, int h, byte[] rgba, boolean generateMipmaps) {
        width = w;
        height = h;

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
        ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(rgba.length);
        buf.put(rgba).flip();
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, width, height, 0,
                GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buf);

        if (generateMipmaps) {
            GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);
        }
    }

    @Override
    public void setFilter(RenderTypes.TextureFilter min, RenderTypes.TextureFilter mag) {
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, toGLFilter(min));
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, toGLFilter(mag));
    }

    @Override
    public void setWrap(RenderTypes.TextureWrap s, RenderTypes.TextureWrap t) {
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, toGLWrap(s));
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, toGLWrap(t));
    }

    @Override
    public void bind(int unit) {
        GL33.glActiveTexture(GL33.GL_TEXTURE0 + unit);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureId);
    }

    @Override
    public void unbind(int unit) {
        GL33.glActiveTexture(GL33.GL_TEXTURE0 + unit);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
    }

    @Override
    public boolean isValid() {
        return textureId != 0;
    }

    public int getTextureId() {
        return textureId;
    }

    private static int toGLFilter(RenderTypes.TextureFilter filter) {
        return switch (filter) {
            case NEAREST -> GL33.GL_NEAREST;
            case LINEAR -> GL33.GL_LINEAR;
            case NEAREST_MIPMAP_NEAREST -> GL33.GL_NEAREST_MIPMAP_NEAREST;
            case NEAREST_MIPMAP_LINEAR -> GL33.GL_NEAREST_MIPMAP_LINEAR;
            case LINEAR_MIPMAP_NEAREST -> GL33.GL_LINEAR_MIPMAP_NEAREST;
            case LINEAR_MIPMAP_LINEAR -> GL33.GL_LINEAR_MIPMAP_LINEAR;
        };
    }

    private static int toGLWrap(RenderTypes.TextureWrap wrap) {
        return switch (wrap) {
            case REPEAT -> GL33.GL_REPEAT;
            case CLAMP_TO_EDGE -> GL33.GL_CLAMP_TO_EDGE;
            case MIRRORED_REPEAT -> GL33.GL_MIRRORED_REPEAT;
        };
    }
}
