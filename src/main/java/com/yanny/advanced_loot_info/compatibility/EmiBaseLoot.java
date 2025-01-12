package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.network.LootGroup;
import com.yanny.advanced_loot_info.network.TooltipUtils;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    protected final LootGroup message;
    protected final List<ItemData> itemDataList;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootGroup message) {
        super(category, id, 9 * 18, 256);
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
            SlotWidget widget = new LootSlotWidget(EmiStack.of(itemData.item), pos[0], pos[1]).setCount(itemData.count);

            if (AdvancedLootInfoMod.CONFIGURATION.isDebug()) {
                widget.appendTooltip(translatable("emi.debug.rolls", itemData.rawRolls));
                widget.appendTooltip(translatable("emi.debug.bonus_rolls", itemData.rawBonusRolls));
                widget.appendTooltip(translatable("emi.debug.chance", itemData.rawChance));
            }

            widget.appendTooltip(getRolls(itemData));

            widget.appendTooltip(getChance(itemData));
            getBonusChance(itemData).forEach(widget::appendTooltip);

            widget.appendTooltip(getCount(itemData));
            getBonusCount(itemData).forEach(widget::appendTooltip);

            TooltipUtils.getConditions(itemData.conditions, 0).forEach(widget::appendTooltip);
            TooltipUtils.getFunctions(itemData.functions, 0).forEach(widget::appendTooltip);

            widget.recipeContext(this);
            widgetHolder.add(widget);

            if ((pos[0] + 18) / (9*18) > 0) {
                pos[1] += 18;
            }

            pos[0] = (pos[0] + 18) % (9*18);
        }
    }

    @NotNull
    private static Component getRolls(ItemData data) {
        return translatable("emi.description.advanced_loot_info.rolls", value(data.rolls.toIntString(), "x"));
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
            data.bonusChance.getValue().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.chance_bonus",
                    value(value, "%"),
                    Component.translatable(data.bonusChance.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCount(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusCount != null) {
            data.bonusCount.getValue().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.count_bonus",
                    value,
                    Component.translatable(data.bonusCount.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }
}
