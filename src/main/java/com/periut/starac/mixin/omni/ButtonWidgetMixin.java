package com.periut.starac.mixin.omni;

import com.periut.omni.OmniShaderManager;
import com.periut.omni.OmniTextureManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ButtonWidget.class)
public class ButtonWidgetMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private void omni$bindTexture(int target, int id) {
        OmniTextureManager.get().bindTexture(id);
        OmniShaderManager.get().setUseTexture(true);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V"))
    private void omni$color4f(float r, float g, float b, float a) {
        OmniShaderManager.get().setGuiColor(r, g, b, a);
        OmniShaderManager.get().setUseVertexColor(false);
    }
}
