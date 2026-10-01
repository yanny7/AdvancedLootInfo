package com.yanny.awi.mixin;

import com.yanny.aci.compatibility.ScrollableTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public class MixinGuiGraphics {
    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void onRenderTooltipStart(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(components);
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void onRenderTooltipEnd(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(List.of());
    }
}
