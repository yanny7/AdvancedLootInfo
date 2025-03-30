package com.yanny.ali.compatibility.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GenericTooltipUtils;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EmiLootSlotWidget extends SlotWidget {
    @Nullable
    private Component count;
    private boolean isRange = false;

    public EmiLootSlotWidget(IUtils utils, ILootEntry entry, EmiIngredient ingredient, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
                             RangeValue count, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> functions,
                             List<ILootCondition> conditions) {
        super(ingredient, x, y);
        GenericTooltipUtils.getTooltip(utils, entry, chance, bonusChance, count, bonusCount, functions, conditions).forEach(this::appendTooltip);
        setCount(count);
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
                //draw.fill(-font.width(count) - 2, -2, 2, 10, 255<<24 | 0);
                draw.drawString(font, count, -font.width(count), 0, 16777215, false);
                stack.popPose();
            } else {
                stack.translate(x + 18, y + 10, 200);
                draw.drawString(font, count, -font.width(count), 0, 16777215, true);
            }

            stack.popPose();
        }

        super.drawOverlay(draw, mouseX, mouseY, delta);
    }

    private void setCount(RangeValue count) {
        if (count.isRange() || count.min() > 1) {
            this.count = Component.literal(count.toIntString());
            isRange = count.isRange();
        }
    }
}
