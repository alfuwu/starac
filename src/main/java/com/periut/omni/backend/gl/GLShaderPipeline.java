package com.periut.omni.backend.gl;

import com.periut.omni.GLSLTranspiler;
import com.periut.omni.backend.ShaderPipeline;
import org.lwjgl.opengl.GL33;

import java.util.HashMap;
import java.util.Map;

public class GLShaderPipeline extends ShaderPipeline {
    private int program;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    @Override
    public boolean loadFromGLSL(String vertexSource, String fragmentSource) {
        // Always transpile 450→330 since we target GL 3.3
        String transpiledVertex = GLSLTranspiler.transpile450to330(vertexSource);
        String transpiledFragment = GLSLTranspiler.transpile450to330(fragmentSource);

        int vertexShader = compileShader(GL33.GL_VERTEX_SHADER, transpiledVertex);
        if (vertexShader == 0) {
            System.err.println("Vertex shader compilation failed!");
            return false;
        }

        int fragmentShader = compileShader(GL33.GL_FRAGMENT_SHADER, transpiledFragment);
        if (fragmentShader == 0) {
            System.err.println("Fragment shader compilation failed!");
            GL33.glDeleteShader(vertexShader);
            return false;
        }

        program = GL33.glCreateProgram();
        GL33.glAttachShader(program, vertexShader);
        GL33.glAttachShader(program, fragmentShader);
        GL33.glLinkProgram(program);

        if (GL33.glGetProgrami(program, GL33.GL_LINK_STATUS) == 0) {
            String log = GL33.glGetProgramInfoLog(program);
            System.err.println("Shader program linking failed:\n" + log);
            GL33.glDeleteProgram(program);
            program = 0;
            return false;
        }

        GL33.glDeleteShader(vertexShader);
        GL33.glDeleteShader(fragmentShader);

        return program != 0;
    }

    private int compileShader(int type, String source) {
        int shader = GL33.glCreateShader(type);
        GL33.glShaderSource(shader, source);
        GL33.glCompileShader(shader);

        if (GL33.glGetShaderi(shader, GL33.GL_COMPILE_STATUS) == 0) {
            String log = GL33.glGetShaderInfoLog(shader);
            System.err.println("Shader compilation failed (" +
                    (type == GL33.GL_VERTEX_SHADER ? "vertex" : "fragment") + "):\n" + log);
            System.err.println("Source:\n" + source);
            GL33.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    @Override
    public void bind() {
        GL33.glUseProgram(program);
    }

    @Override
    public void unbind() {
        GL33.glUseProgram(0);
    }

    private int getUniformLocation(String name) {
        return uniformLocations.computeIfAbsent(name, n -> GL33.glGetUniformLocation(program, n));
    }

    @Override
    public void setInt(String name, int value) {
        GL33.glUniform1i(getUniformLocation(name), value);
    }

    @Override
    public void setFloat(String name, float value) {
        GL33.glUniform1f(getUniformLocation(name), value);
    }

    @Override
    public void setVec2(String name, float x, float y) {
        GL33.glUniform2f(getUniformLocation(name), x, y);
    }

    @Override
    public void setVec3(String name, float x, float y, float z) {
        GL33.glUniform3f(getUniformLocation(name), x, y, z);
    }

    @Override
    public void setVec4(String name, float x, float y, float z, float w) {
        GL33.glUniform4f(getUniformLocation(name), x, y, z, w);
    }

    @Override
    public void setMat3(String name, float[] matrix) {
        GL33.glUniformMatrix3fv(getUniformLocation(name), false, matrix);
    }

    @Override
    public void setMat4(String name, float[] matrix) {
        GL33.glUniformMatrix4fv(getUniformLocation(name), false, matrix);
    }

    @Override
    public boolean isValid() {
        return program != 0;
    }

    public void destroy() {
        if (program != 0) {
            GL33.glDeleteProgram(program);
            program = 0;
        }
        uniformLocations.clear();
    }
}
