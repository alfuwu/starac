package com.periut.omni;

import com.periut.omni.OmniShaderManager;
import com.periut.omni.backend.IndexBuffer;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import com.periut.omni.backend.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class OmniTesselator {
    public static final OmniTesselator INSTANCE = new OmniTesselator();

    private static final int MAX_VERTICES = 524288;
    private static final int VERTEX_STRIDE_INTS = 8; // 8 ints = 32 bytes

    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;
    private boolean vaoInitialized;

    private final int[] array;
    private int p; // position in array (in ints)
    private int vertices;

    // Current state
    private double u, v;
    private int col;
    private int normalValue;
    private int lightValue;
    private double xo, yo, zo;

    // Flags
    private boolean hasColor;
    private boolean hasTexture;
    private boolean hasNormal;
    private boolean noColorFlag;
    private boolean tesselating;

    private RenderTypes.DrawMode mode;

    // Reusable NIO buffers
    private ByteBuffer uploadBuffer;
    private IntBuffer indexUploadBuffer;

    public OmniTesselator() {
        array = new int[2097152]; // Same as MC Tesselator
        col = 0xFFFFFFFF;
        lightValue = 0x0F0F; // max sky=15, max block=15
        mode = RenderTypes.DrawMode.QUADS;
        uploadBuffer = ByteBuffer.allocateDirect(MAX_VERTICES * 32).order(ByteOrder.nativeOrder());
        indexUploadBuffer = ByteBuffer.allocateDirect(MAX_VERTICES * 6 / 4 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    public void init() {
        if (vaoInitialized) return;
        RenderDevice device = RenderDevice.get();
        vertexBuffer = device.createVertexBuffer();
        indexBuffer = device.createIndexBuffer();
        vaoInitialized = true;
    }

    public void begin(int drawMode) {
        if (tesselating) {
            throw new IllegalStateException("Already tesselating!");
        }
        tesselating = true;
        clear();
        // Map MC draw modes: 7=QUADS, 4=TRIANGLES, 6=TRIANGLE_FAN, 3=LINE_STRIP, 1=LINES, 5=TRIANGLE_STRIP
        mode = switch (drawMode) {
            case 7 -> RenderTypes.DrawMode.QUADS;
            case 4 -> RenderTypes.DrawMode.TRIANGLES;
            case 6 -> RenderTypes.DrawMode.TRIANGLE_FAN;
            case 3 -> RenderTypes.DrawMode.LINE_STRIP;
            case 1 -> RenderTypes.DrawMode.LINES;
            case 0 -> RenderTypes.DrawMode.POINTS;
            case 5 -> RenderTypes.DrawMode.TRIANGLES; // triangle strip -> handled as triangles
            default -> RenderTypes.DrawMode.QUADS;
        };
        hasNormal = false;
        hasColor = false;
        hasTexture = false;
        noColorFlag = false;
        lightValue = 0x0F0F;
    }

    public void end() {
        if (!tesselating) {
            throw new IllegalStateException("Not tesselating!");
        }
        tesselating = false;
        if (vertices > 0) {
            if (OmniDisplayList.isRecording()) {
                int drawModeInt = switch (mode) {
                    case QUADS -> 7;
                    case TRIANGLES -> 4;
                    case TRIANGLE_FAN -> 6;
                    case LINE_STRIP -> 3;
                    case LINES -> 1;
                    case POINTS -> 0;
                    default -> 7;
                };
                OmniDisplayList.storeData(OmniDisplayList.getCurrentListId(), array, p, vertices, drawModeInt);
            } else {
                draw();
            }
        }
        clear();
    }

    public void vertex(double x, double y, double z) {
        if (hasTexture) {
            array[p + 3] = Float.floatToRawIntBits((float) u);
            array[p + 4] = Float.floatToRawIntBits((float) v);
        } else {
            array[p + 3] = 0;
            array[p + 4] = 0;
        }

        array[p + 5] = hasColor ? col : 0xFFFFFFFF;
        array[p + 6] = hasNormal ? normalValue : 0;
        array[p + 7] = lightValue;

        array[p + 0] = Float.floatToRawIntBits((float) (x + xo));
        array[p + 1] = Float.floatToRawIntBits((float) (y + yo));
        array[p + 2] = Float.floatToRawIntBits((float) (z + zo));

        p += VERTEX_STRIDE_INTS;
        vertices++;

        // Auto-flush if buffer getting full
        if (vertices % 4 == 0 && p >= array.length - 32) {
            end();
            tesselating = true;
        }
    }

    public void vertexUV(double x, double y, double z, double u, double v) {
        tex(u, v);
        vertex(x, y, z);
    }

    public void tex(double u, double v) {
        hasTexture = true;
        this.u = u;
        this.v = v;
    }

    public void color(int r, int g, int b, int a) {
        if (noColorFlag) return;
        r = clamp(r, 0, 255);
        g = clamp(g, 0, 255);
        b = clamp(b, 0, 255);
        a = clamp(a, 0, 255);
        hasColor = true;
        col = (a << 24) | (b << 16) | (g << 8) | r; // ABGR for GL
    }

    public void color(int r, int g, int b) {
        color(r, g, b, 255);
    }

    public void color(float r, float g, float b) {
        color((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f));
    }

    public void color(float r, float g, float b, float a) {
        color((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), (int) (a * 255.0f));
    }

    public void colorRGBA(int rgba) {
        int r = (rgba >> 16) & 255;
        int g = (rgba >> 8) & 255;
        int b = rgba & 255;
        color(r, g, b);
    }

    public void setColorOpaque_F(float r, float g, float b) {
        color((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f));
    }

    public void normal(float x, float y, float z) {
        hasNormal = true;
        byte xx = (byte) (x * 128.0f);
        byte yy = (byte) (y * 127.0f);
        byte zz = (byte) (z * 127.0f);
        normalValue = (xx & 0xFF) | ((yy & 0xFF) << 8) | ((zz & 0xFF) << 16);
    }

    public void lightLevel(int skyLight, int blockLight) {
        skyLight = clamp(skyLight, 0, 15);
        blockLight = clamp(blockLight, 0, 15);
        lightValue = skyLight | (blockLight << 8);
    }

    public void offset(double x, double y, double z) {
        xo = x;
        yo = y;
        zo = z;
    }

    public void noColor() {
        noColorFlag = true;
    }

    public boolean isTesselating() {
        return tesselating;
    }

    public int getVertexCount() {
        return vertices;
    }

    private void draw() {
        if (vertices == 0) return;
        if (!vaoInitialized) init();

        // Tell the shader whether to use per-vertex colors or the uniform color
        OmniShaderManager.get().setUseVertexColor(hasColor);

        RenderDevice device = RenderDevice.get();

        // Upload vertex data
        int sizeBytes = p * 4;
        if (uploadBuffer.capacity() < sizeBytes) {
            uploadBuffer = ByteBuffer.allocateDirect(sizeBytes).order(ByteOrder.nativeOrder());
        }
        uploadBuffer.clear();
        uploadBuffer.asIntBuffer().put(array, 0, p);
        uploadBuffer.limit(sizeBytes);
        vertexBuffer.upload(uploadBuffer, sizeBytes, RenderTypes.BufferUsage.STREAM);
        vertexBuffer.bind();
        device.setupVertexAttributes();

        if (mode == RenderTypes.DrawMode.QUADS) {
            int numQuads = vertices / 4;
            int indexCount = numQuads * 6;
            if (indexUploadBuffer.capacity() < indexCount) {
                indexUploadBuffer = ByteBuffer.allocateDirect(indexCount * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
            }
            indexUploadBuffer.clear();
            for (int i = 0; i < numQuads; i++) {
                int base = i * 4;
                indexUploadBuffer.put(base);
                indexUploadBuffer.put(base + 1);
                indexUploadBuffer.put(base + 2);
                indexUploadBuffer.put(base);
                indexUploadBuffer.put(base + 2);
                indexUploadBuffer.put(base + 3);
            }
            indexUploadBuffer.flip();
            indexBuffer.upload(indexUploadBuffer, indexCount, RenderTypes.BufferUsage.STREAM);
            indexBuffer.bind();
            device.drawIndexed(RenderTypes.PrimitiveType.TRIANGLES, indexCount, 0);
        } else if (mode == RenderTypes.DrawMode.TRIANGLE_FAN) {
            if (vertices >= 3) {
                int indexCount = (vertices - 2) * 3;
                if (indexUploadBuffer.capacity() < indexCount) {
                    indexUploadBuffer = ByteBuffer.allocateDirect(indexCount * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
                }
                indexUploadBuffer.clear();
                for (int i = 1; i < vertices - 1; i++) {
                    indexUploadBuffer.put(0);
                    indexUploadBuffer.put(i);
                    indexUploadBuffer.put(i + 1);
                }
                indexUploadBuffer.flip();
                indexBuffer.upload(indexUploadBuffer, indexCount, RenderTypes.BufferUsage.STREAM);
                indexBuffer.bind();
                device.drawIndexed(RenderTypes.PrimitiveType.TRIANGLES, indexCount, 0);
            }
        } else if (mode == RenderTypes.DrawMode.TRIANGLES) {
            device.draw(RenderTypes.PrimitiveType.TRIANGLES, vertices, 0);
        } else if (mode == RenderTypes.DrawMode.LINES) {
            device.draw(RenderTypes.PrimitiveType.LINES, vertices, 0);
        } else if (mode == RenderTypes.DrawMode.LINE_STRIP) {
            device.draw(RenderTypes.PrimitiveType.LINE_STRIP, vertices, 0);
        } else if (mode == RenderTypes.DrawMode.POINTS) {
            device.draw(RenderTypes.PrimitiveType.POINTS, vertices, 0);
        }

        vertexBuffer.unbind();
    }

    private void clear() {
        vertices = 0;
        p = 0;
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
