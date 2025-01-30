package com.yanny.advanced_loot_info.compatibility;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.plugin.function.LootConditionalFunction;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class LootSlotWidget extends SlotWidget {
    @Nullable
    private Component count;
    boolean isRange = false;

    public LootSlotWidget(ItemData itemData, int x, int y) {
        super(itemData.item != null ? EmiStack.of(itemData.item) : (itemData.tag != null ? EmiIngredient.of(itemData.tag) : EmiStack.EMPTY), x, y);

        appendTooltip(getChance(itemData));
        getBonusChance(itemData).forEach(this::appendTooltip);

        appendTooltip(getCount(itemData));
        getBonusCount(itemData).forEach(this::appendTooltip);

        getConditions(itemData.conditions, 0).forEach(this::appendTooltip);
        getFunctions(itemData.functions, 0).forEach(this::appendTooltip);
    }

    public LootSlotWidget setCount(RangeValue count) {
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

    @NotNull
    private static Component getCount(ItemData data) {
        return translatable("emi.description.advanced_loot_info.count", data.count);
    }

    @NotNull
    private static Component getChance(ItemData data) {
        return translatable("emi.description.advanced_loot_info.chance", value(data.chance, "%"));
    }

    @NotNull
    private static List<Component> getBonusChance(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusChance != null) {
            data.bonusChance.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.chance_bonus",
                    value(value, "%"),
                    Component.translatable(data.bonusChance.getFirst().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCount(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusCount != null) {
            data.bonusCount.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.count_bonus",
                    value,
                    Component.translatable(data.bonusCount.getFirst().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getConditions(List<ILootCondition> conditions, int pad) {
        List<Component> components = new LinkedList<>();

        conditions.forEach((condition) -> components.addAll(condition.getTooltip(pad)));

        return components;
    }

    @NotNull
    private static List<Component> getFunctions(List<ILootFunction> functions, int pad) {
        List<Component> components = new LinkedList<>();

        functions.forEach((function) -> {
            components.addAll(function.getTooltip(pad));

            if (function instanceof LootConditionalFunction conditionalFunction) {
                components.addAll(getConditionalFunction(conditionalFunction, pad + 1));
            }
        });

        return components;
    }

    @NotNull
    private static List<Component> getConditionalFunction(LootConditionalFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        if (!function.conditions.isEmpty()) {
            components.add(pad(pad, translatable("emi.property.function.conditions")));
            components.addAll(getConditions(function.conditions, pad + 1));
        }

        return components;
    }
}
