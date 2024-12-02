package com.yanny.emi_loot_addon.compatibility;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.emi_loot_addon.network.value.RangeValue;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class LootSlotWidget extends SlotWidget {
    @Nullable private Component count;
    boolean isRange = false;

    public LootSlotWidget(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
    }

    LootSlotWidget setCount(RangeValue count) {
        if (count.isRange() || count.min() > 1) {
            this.count = Component.literal(count.toIntString());
            isRange = count.isRange();
        }

        return this;
    }

    @Override
    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        if (count != null) {
            Font font = Minecraft.getInstance().font;
            PoseStack stack = draw.pose();

            stack.pushPose();

            if (isRange) {
                stack.translate(x + 17, y + 13, 200);
                stack.pushPose();
                stack.scale(0.5f, 0.5f, 0.5f);
                draw.drawString(font, count, -font.width(count), 0, 16777215, true);
                stack.popPose();
            } else {
                stack.translate(x + 18, y + 10, 200);
                draw.drawString(font, count, -font.width(count), 0, 16777215, true);
            }

            stack.popPose();
        }

        super.drawOverlay(draw, mouseX, mouseY, delta);
    }
}
