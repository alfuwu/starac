package com.periut.omni;

import java.util.ArrayDeque;
import java.util.Deque;

public class OmniMatrixStack {
    private static final OmniMatrixStack PROJECTION = new OmniMatrixStack();
    private static final OmniMatrixStack MODELVIEW = new OmniMatrixStack();

    private final Deque<float[]> stack = new ArrayDeque<>();
    private float[] current;

    private OmniMatrixStack() {
        current = identity();
    }

    // Global matrix mode tracking (GL_MODELVIEW=5888, GL_PROJECTION=5889)
    private static int currentMatrixMode = 5888;

    public static OmniMatrixStack projection() { return PROJECTION; }
    public static OmniMatrixStack modelview() { return MODELVIEW; }

    public static void setMatrixMode(int mode) { currentMatrixMode = mode; }
    public static int getMatrixMode() { return currentMatrixMode; }

    public static OmniMatrixStack current() {
        return currentMatrixMode == 5889 ? PROJECTION : MODELVIEW;
    }

    public void push() {
        stack.push(current.clone());
    }

    public void pop() {
        if (!stack.isEmpty()) {
            current = stack.pop();
        }
    }

    public void loadIdentity() {
        current = identity();
    }

    public void load(float[] matrix) {
        System.arraycopy(matrix, 0, current, 0, 16);
    }

    public void translate(float x, float y, float z) {
        float[] t = identity();
        t[12] = x; t[13] = y; t[14] = z;
        multiply(t);
    }

    public void rotate(float angleDegrees, float x, float y, float z) {
        float rad = (float) Math.toRadians(angleDegrees);
        float c = (float) Math.cos(rad);
        float s = (float) Math.sin(rad);
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len == 0) return;
        x /= len; y /= len; z /= len;
        float nc = 1.0f - c;

        float[] r = new float[16];
        r[0] = x * x * nc + c;
        r[1] = y * x * nc + z * s;
        r[2] = z * x * nc - y * s;
        r[4] = x * y * nc - z * s;
        r[5] = y * y * nc + c;
        r[6] = z * y * nc + x * s;
        r[8] = x * z * nc + y * s;
        r[9] = y * z * nc - x * s;
        r[10] = z * z * nc + c;
        r[15] = 1.0f;
        multiply(r);
    }

    public void scale(float x, float y, float z) {
        float[] s = new float[16];
        s[0] = x; s[5] = y; s[10] = z; s[15] = 1.0f;
        multiply(s);
    }

    public void perspective(float fovDegrees, float aspect, float near, float far) {
        float rad = (float) Math.toRadians(fovDegrees);
        float tanHalfFov = (float) Math.tan(rad / 2.0f);
        float[] p = new float[16];
        p[0] = 1.0f / (aspect * tanHalfFov);
        p[5] = 1.0f / tanHalfFov;
        p[10] = -(far + near) / (far - near);
        p[11] = -1.0f;
        p[14] = -(2.0f * far * near) / (far - near);
        multiply(p);
    }

    public void frustum(float left, float right, float bottom, float top, float near, float far) {
        float[] f = new float[16];
        f[0] = (2.0f * near) / (right - left);
        f[5] = (2.0f * near) / (top - bottom);
        f[8] = (right + left) / (right - left);
        f[9] = (top + bottom) / (top - bottom);
        f[10] = -(far + near) / (far - near);
        f[11] = -1.0f;
        f[14] = -(2.0f * far * near) / (far - near);
        multiply(f);
    }

    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        float[] o = identity();
        o[0] = 2.0f / (right - left);
        o[5] = 2.0f / (top - bottom);
        o[10] = -2.0f / (far - near);
        o[12] = -(right + left) / (right - left);
        o[13] = -(top + bottom) / (top - bottom);
        o[14] = -(far + near) / (far - near);
        multiply(o);
    }

    public void multiply(float[] matrix) {
        current = mul(current, matrix);
    }

    public float[] get() {
        return current;
    }

    public float[] getClone() {
        return current.clone();
    }

    public static float[] getMVP() {
        return mul(PROJECTION.current, MODELVIEW.current);
    }

    public static float[] getNormalMatrix3x3() {
        float[] mv = MODELVIEW.current;
        // Extract upper-left 3x3 and compute inverse transpose
        float a00 = mv[0], a01 = mv[4], a02 = mv[8];
        float a10 = mv[1], a11 = mv[5], a12 = mv[9];
        float a20 = mv[2], a21 = mv[6], a22 = mv[10];

        float det = a00 * (a11 * a22 - a12 * a21)
                  - a01 * (a10 * a22 - a12 * a20)
                  + a02 * (a10 * a21 - a11 * a20);

        if (Math.abs(det) < 1e-10f) {
            return new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        }

        float invDet = 1.0f / det;
        // Inverse transpose = cofactor matrix / det (already transposed due to column-major)
        float[] nm = new float[9];
        nm[0] = (a11 * a22 - a12 * a21) * invDet;
        nm[1] = (a12 * a20 - a10 * a22) * invDet;
        nm[2] = (a10 * a21 - a11 * a20) * invDet;
        nm[3] = (a02 * a21 - a01 * a22) * invDet;
        nm[4] = (a00 * a22 - a02 * a20) * invDet;
        nm[5] = (a01 * a20 - a00 * a21) * invDet;
        nm[6] = (a01 * a12 - a02 * a11) * invDet;
        nm[7] = (a02 * a10 - a00 * a12) * invDet;
        nm[8] = (a00 * a11 - a01 * a10) * invDet;
        return nm;
    }

    // Column-major 4x4 multiply
    private static float[] mul(float[] a, float[] b) {
        float[] r = new float[16];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                r[col * 4 + row] =
                        a[0 * 4 + row] * b[col * 4 + 0] +
                        a[1 * 4 + row] * b[col * 4 + 1] +
                        a[2 * 4 + row] * b[col * 4 + 2] +
                        a[3 * 4 + row] * b[col * 4 + 3];
            }
        }
        return r;
    }

    private static float[] identity() {
        float[] m = new float[16];
        m[0] = 1; m[5] = 1; m[10] = 1; m[15] = 1;
        return m;
    }
}
