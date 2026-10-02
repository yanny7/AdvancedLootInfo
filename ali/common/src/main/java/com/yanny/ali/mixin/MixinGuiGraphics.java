package com.yanny.ali.mixin;

import com.yanny.aci.compatibility.ScrollableTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class MixinGuiGraphics {
    @Inject(method = "tooltip", at = @At("HEAD"))
    private void onRenderTooltipStart(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier background, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(components);
    }

    @Inject(method = "tooltip", at = @At("RETURN"))
    private void onRenderTooltipEnd(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier background, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(List.of());
    }
}
