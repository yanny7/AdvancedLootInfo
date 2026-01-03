package com.yanny.ali.mixin;

import com.yanny.ali.compatibility.IMouseEvents;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeScreen.class)
public class MixinRecipeScreen {
    @Shadow
    private List<WidgetGroup> currentPage;

    @Inject(at = @At("HEAD"), method = "mouseScrolled(DDDD)Z", cancellable = true)
    private void ali_onMouseScrolled(double mouseX, double mouseY, double horizontal, double amount, CallbackInfoReturnable<Boolean> info) {
        for (WidgetGroup widgetGroup : currentPage) {
            double mx = mouseX - widgetGroup.x();
            double my = mouseY - widgetGroup.y();

            for (Widget widget : widgetGroup.widgets) {
                if (widget instanceof IMouseEvents scrollable) {
                    if (scrollable.onMouseScrolled(mx, my, amount)) {
                        info.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mouseDragged(DDIDD)Z", cancellable = true)
    private void ali_onMouseScrolled(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> info) {
        for (WidgetGroup widgetGroup : currentPage) {
            double mx = mouseX - widgetGroup.x();
            double my = mouseY - widgetGroup.y();

            for (Widget widget : widgetGroup.widgets) {
                if (widget instanceof IMouseEvents scrollable) {
                    if (scrollable.onMouseDragged(mx, my, button, deltaX, deltaY)) {
                        info.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }
}
