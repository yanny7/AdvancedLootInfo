package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.network.LootGroup;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    protected final LootGroup message;
    protected final List<ItemData> itemDataList;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootGroup message) {
        super(category, id, 9 * 18, 1024);
        this.message = message;
        itemDataList = ItemData.parse(message);
        outputs = itemDataList.stream()
                .map((d) -> d.item)
                .filter(Objects::nonNull)
                .map(EmiStack::of)
                .toList();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addWidgets(widgets, new int[]{0, 0});
    }

    @Override
    public Recipe<?> getBackingRecipe() {
        return null;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        for (ItemData itemData : itemDataList) {
            SlotWidget widget = new LootSlotWidget(EmiStack.of(itemData.item), pos[0], pos[1])
                    .setCount(itemData.count);

            widget.appendTooltip(getChance(itemData));
            getBonusChance(itemData).forEach(widget::appendTooltip);

            widget.appendTooltip(getCount(itemData));
            getBonusCount(itemData).forEach(widget::appendTooltip);

            widget.appendTooltip(Component.literal(itemData.conditions.stream().filter((c) -> !c.HANDLED).map((c) -> c.type).toList().toString()))
                    .appendTooltip(Component.literal(itemData.functions.stream().filter((f) -> !f.HANDLED).map((f) -> f.type).toList().toString()));

            widget.recipeContext(this);
            widgetHolder.add(widget);

            if ((pos[0] + 18) / (9*18) > 0) {
                pos[1] += 18;
            }

            pos[0] = (pos[0] + 18) % (9*18);
        }
    }

    @NotNull
    private static Component getCount(ItemData data) {
        return EmiUtils.translate("emi.description.emi_loot_addon.count", data.count);
    }

    @NotNull
    private static Component getChance(ItemData data) {
        if ((!data.rolls.isRange() && data.rolls.min() > 1) || (data.rolls.isRange() && data.rolls.max() > 1)) {
            return EmiUtils.translate("emi.description.emi_loot_addon.chance_rolls", EmiUtils.value(data.rolls.toIntString(), "x"), EmiUtils.value(data.chance, "%"));
        }

        return EmiUtils.translate("emi.description.emi_loot_addon.chance", EmiUtils.value(data.chance, "%"));
    }

    @NotNull
    private static List<Component> getBonusChance(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusChance != null) {
            data.bonusChance.forEach((level, value) -> components.add(EmiUtils.translate(
                    "emi.description.emi_loot_addon.chance_bonus",
                    value,
                    Component.translatable(Enchantments.MOB_LOOTING.getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            )));
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCount(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusCount != null) {
            data.bonusCount.getValue().forEach((level, value) -> components.add(EmiUtils.translate(
                    "emi.description.emi_loot_addon.count_bonus",
                    value,
                    Component.translatable(data.bonusCount.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            )));
        }

        return components;
    }
}
