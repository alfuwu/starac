package com.periut.starac.mixin.omni;

import com.periut.omni.OmniLighting;
import net.minecraft.client.render.platform.Lighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Lighting.class)
public class LightingMixin {
    /**
     * @author omni
     * @reason Replace fixed-function GL lighting with shader uniforms
     */
    @Overwrite
    public static void turnOn() {
        OmniLighting.enable();
    }

    /**
     * @author omni
     * @reason Replace fixed-function GL lighting disable with shader uniform
     */
    @Overwrite
    public static void turnOff() {
        OmniLighting.disable();
    }
}
