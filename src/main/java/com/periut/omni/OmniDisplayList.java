package com.periut.omni;

import com.periut.omni.backend.IndexBuffer;
import com.periut.omni.backend.RenderDevice;
import com.periut.omni.backend.RenderTypes;
import com.periut.omni.backend.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Emulates OpenGL display lists for GL 3.3 core profile.
 * Stores raw vertex data during glNewList(GL_COMPILE).
 * Re-uploads and draws during glCallList.
 */
public class OmniDisplayList {
    private static final Map<Integer, ListData> lists = new HashMap<>();
    private static boolean recording = false;
    private static int currentListId = -1;
    private static float recordedTranslateX, recordedTranslateY, recordedTranslateZ;

    // Shared upload buffers for replay
    private static VertexBuffer sharedVBO;
    private static IndexBuffer sharedIBO;
    private static ByteBuffer uploadBuffer;
    private static IntBuffer indexUploadBuffer;
    private static boolean initialized;

    private static final int VERTEX_STRIDE_INTS = 8;

    public static class ListData {
        int[] vertexData;     // raw vertex ints
        int arrayLengthInts;  // length in ints
        int vertexCount;
        int drawMode;         // original draw mode
        boolean hasColor;     // whether vertices have explicit colors
        float translateX, translateY, translateZ;
    }

    private static void ensureInit() {
        if (!initialized) {
            RenderDevice device = RenderDevice.get();
            sharedVBO = device.createVertexBuffer();
            sharedIBO = device.createIndexBuffer();
            uploadBuffer = ByteBuffer.allocateDirect(4 * 1024 * 1024).order(ByteOrder.nativeOrder());
            indexUploadBuffer = ByteBuffer.allocateDirect(1024 * 1024).order(ByteOrder.nativeOrder()).asIntBuffer();
            initialized = true;
        }
    }

    public static boolean isRecording() {
        return recording;
    }

    public static void beginRecording(int listId) {
        recording = true;
        currentListId = listId;
        recordedTranslateX = 0;
        recordedTranslateY = 0;
        recordedTranslateZ = 0;
        // Clear old data so recompilation starts fresh
        lists.remove(listId);
    }

    public static void endRecording() {
        if (recording && currentListId >= 0) {
            ListData data = lists.get(currentListId);
            if (data != null) {
                data.translateX = recordedTranslateX;
                data.translateY = recordedTranslateY;
                data.translateZ = recordedTranslateZ;
            }
        }
        recording = false;
        currentListId = -1;
    }

    public static void recordTranslate(float x, float y, float z) {
        recordedTranslateX += x;
        recordedTranslateY += y;
        recordedTranslateZ += z;
    }

    private static int storeLogCount = 0;

    public static void storeData(int listId, int[] vertexArray, int arrayLengthInts, int vertexCount, int drawMode) {
        storeData(listId, vertexArray, arrayLengthInts, vertexCount, drawMode, true);
    }

    public static void storeData(int listId, int[] vertexArray, int arrayLengthInts, int vertexCount, int drawMode, boolean hasColor) {
        ListData existing = recording ? lists.get(listId) : null;

        if (existing != null && existing.drawMode == drawMode) {
            // Append to existing data for the same list ID during recording
            int newTotalInts = existing.arrayLengthInts + arrayLengthInts;
            int[] merged = Arrays.copyOf(existing.vertexData, newTotalInts);
            System.arraycopy(vertexArray, 0, merged, existing.arrayLengthInts, arrayLengthInts);
            existing.vertexData = merged;
            existing.arrayLengthInts = newTotalInts;
            existing.vertexCount += vertexCount;
            existing.hasColor = existing.hasColor || hasColor;
        } else {
            ListData data = new ListData();
            data.vertexData = Arrays.copyOf(vertexArray, arrayLengthInts);
            data.arrayLengthInts = arrayLengthInts;
            data.vertexCount = vertexCount;
            data.drawMode = drawMode;
            data.hasColor = hasColor;
            lists.put(listId, data);
        }

        storeLogCount++;
        if (storeLogCount <= 10 || (storeLogCount % 200 == 0)) {
            float x = Float.intBitsToFloat(vertexArray[0]);
            float y = Float.intBitsToFloat(vertexArray[1]);
            float z = Float.intBitsToFloat(vertexArray[2]);
            System.out.println("[OmniDL] Store #" + storeLogCount + " id=" + listId + " verts=" + vertexCount + " mode=" + drawMode
                + " v0=(" + x + "," + y + "," + z + ")"
                + " translate=(" + recordedTranslateX + "," + recordedTranslateY + "," + recordedTranslateZ + ")"
                + " total=" + lists.size());
        }
    }

    public static int getCurrentListId() {
        return currentListId;
    }

    public static void replay(int listId) {
        ListData data = lists.get(listId);
        if (data == null || data.vertexCount == 0) return;

        ensureInit();

        RenderDevice device = RenderDevice.get();

        // Apply the recorded translation
        OmniMatrixStack.modelview().push();
        OmniMatrixStack.modelview().translate(data.translateX, data.translateY, data.translateZ);
        OmniShaderManager.get().updateMatrices();

        // Only override vertex color state if the stored data has explicit colors
        if (data.hasColor) {
            OmniShaderManager.get().setUseVertexColor(true);
        }

        // Upload vertex data to shared VBO
        int sizeBytes = data.arrayLengthInts * 4;
        if (uploadBuffer.capacity() < sizeBytes) {
            uploadBuffer = ByteBuffer.allocateDirect(sizeBytes).order(ByteOrder.nativeOrder());
        }
        uploadBuffer.clear();
        uploadBuffer.asIntBuffer().put(data.vertexData, 0, data.arrayLengthInts);
        uploadBuffer.limit(sizeBytes);
        sharedVBO.upload(uploadBuffer, sizeBytes, RenderTypes.BufferUsage.STREAM);
        sharedVBO.bind();
        device.setupVertexAttributes();

        // Draw with appropriate mode
        if (data.drawMode == 7) { // QUADS -> triangles via IBO
            int numQuads = data.vertexCount / 4;
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
            sharedIBO.upload(indexUploadBuffer, indexCount, RenderTypes.BufferUsage.STREAM);
            sharedIBO.bind();
            device.drawIndexed(RenderTypes.PrimitiveType.TRIANGLES, indexCount, 0);
        } else {
            device.draw(RenderTypes.PrimitiveType.TRIANGLES, data.vertexCount, 0);
        }

        sharedVBO.unbind();

        // Restore matrix
        OmniMatrixStack.modelview().pop();
        OmniShaderManager.get().updateMatrices();
    }

    public static void deleteLists(int listId, int range) {
        for (int i = listId; i < listId + range; i++) {
            lists.remove(i);
        }
    }

    private static int nextAllocId = 100000;

    public static int allocateIds(int count) {
        int base = nextAllocId;
        nextAllocId += count;
        return base;
    }

    public static boolean hasList(int listId) {
        return lists.containsKey(listId);
    }
}
