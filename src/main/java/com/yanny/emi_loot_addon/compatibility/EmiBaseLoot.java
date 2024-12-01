package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.network.LootFunction;
import com.yanny.emi_loot_addon.network.LootGroup;
import com.yanny.emi_loot_addon.network.function.ApplyBonusFunction;
import com.yanny.emi_loot_addon.network.function.FunctionType;
import com.yanny.emi_loot_addon.network.function.SetCountFunction;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

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
            widgetHolder.addSlot(EmiStack.of(itemData.item), pos[0], pos[1])
                    .appendTooltip(Component.translatable("emi.description.emi_loot_addon.chance", getChance(itemData)))
                    .appendTooltip(Component.literal("Count: " + getValue(itemData.functions)))
                    .appendTooltip(Component.literal(itemData.conditions.stream().map((c) -> c.type).toList().toString()))
                    .appendTooltip(Component.literal(itemData.functions.stream().map((f) -> f.type).toList().toString()))
                    .recipeContext(this);

            if ((pos[0] + 18) / (9*18) > 0) {
                pos[1] += 18;
            }

            pos[0] = (pos[0] + 18) % (9*18);
        }
    }

    private static int[] getCount(List<LootFunction> functions) {
        int[] count = new int[]{1, -1};

        functions.stream().filter((f) -> f.type == FunctionType.SET_COUNT).forEach((f) -> {
            SetCountFunction function = (SetCountFunction) f;

            if (function.add) {
                count[0] += function.count[0];

                if (count[1] >= 0) {
                    count[1] += function.count[1];
                }
            } else {
                count[0] = function.count[0];
                count[1] = function.count[1];
            }
        });

        functions.stream().filter((f) -> f.type == FunctionType.APPLY_BONUS).forEach((f) -> {
            ApplyBonusFunction function = (ApplyBonusFunction) f;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(function.enchantment);

            if (enchantment != null) {
                //function.formula.calculateCount(count, enchantment.getMaxLevel());
            }
        });

        return count;
    }

    private static String getValue(List<LootFunction> functions) {
        int[] count = getCount(functions);

        if (count[1] < 0) {
            return String.valueOf(Math.max(count[0], 1));
        } else {
            return Math.max(count[0], 1) + "-" + Math.max(count[1], 1);
        }
    }

    private static String getChance(ItemData data) {
        float[] chance = new float[]{data.chance, -1};

        if (data.rolls[1] > 1) {
            chance[0] = data.chance * data.rolls[0];
            chance[1] = data.chance * data.rolls[1];
        } else {
            chance[0] *= data.rolls[0];
        }

        //TODO bonus rolls

        if (chance[1] < 0) {
            return String.valueOf(chance[0]);
        } else {
            return chance[0] + "-" + chance[1];
        }
    }
}
