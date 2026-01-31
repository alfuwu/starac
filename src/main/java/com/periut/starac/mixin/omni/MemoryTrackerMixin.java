package com.periut.starac.mixin.omni;

import net.minecraft.client.render.platform.MemoryTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Replaces display list allocation with virtual IDs for OmniDisplayList emulation.
 * Display lists (glGenLists, glDeleteLists) are removed in OpenGL 3.3 core profile.
 */
@Mixin(MemoryTracker.class)
public class MemoryTrackerMixin {

    private static int nextListId = 1;

    /**
     * @author omni
     * @reason glGenLists does not exist in GL 3.3 core profile; return virtual IDs
     */
    private static int allocCount = 0;

    @Overwrite
    public static synchronized int getLists(int s) {
        int base = nextListId;
        nextListId += s;
        allocCount++;
        if (allocCount <= 5) {
            System.out.println("[MemTracker] getLists(" + s + ") -> " + base + " (nextId=" + nextListId + ")");
        }
        return base;
    }

    /**
     * @author omni
     * @reason glDeleteLists does not exist in GL 3.3 core profile
     */
    @Overwrite
    public static synchronized void releaseList(int list) {
    }

    /**
     * @author omni
     * @reason glDeleteLists does not exist in GL 3.3 core profile
     */
    @Overwrite
    public static synchronized void releaseLists() {
    }
}
