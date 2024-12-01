package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public final class ItemData {
    public final Item item;
    public final float chance;
    public final int[] rolls;
    public final float[] bonusRolls;
    public final List<LootFunction> functions;
    public final List<LootCondition> conditions;

    public ItemData(ResourceLocation item, float chance, int[] rolls, float[] bonusRolls, List<LootFunction> functions, List<LootCondition> conditions) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.chance = chance;
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
        this.functions = functions;
        this.conditions = conditions;
    }

    public static List<ItemData> parse(LootGroup message) {
        return parse(message, new int[]{1, -1}, new float[]{0, -1}, new LinkedList<>(message.functions), new LinkedList<>(message.conditions));
    }

    private static List<ItemData> parse(LootGroup message, int[] rolls, float[] bonusRolls, List<LootFunction> functions, List<LootCondition> conditions) {
        List<ItemData> list = new LinkedList<>();

        for (LootEntry entry : message.entries()) {
            List<LootFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
            List<LootCondition> allConditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();

            switch (entry.getType()) {
                case INFO -> list.add(new ItemData(((LootInfo) entry).item, ((LootInfo) entry).chance, rolls, bonusRolls, allFunctions, allConditions));
                case GROUP -> list.addAll(parse((LootGroup) entry, rolls, bonusRolls, allFunctions, allConditions));
                case POOL -> list.addAll(parse((LootPoolEntry) entry, ((LootPoolEntry) entry).rolls, ((LootPoolEntry) entry).bonusRolls, allFunctions, allConditions));
            }
        }

        return list;
    }
}
