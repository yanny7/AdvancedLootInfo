package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IClientUtils extends ICommonUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions);

    <T extends LootItemCondition> List<Component> getConditionTooltip(IClientUtils utils, int pad, T condition);
    <T extends LootItemFunction> List<Component> getFunctionTooltip(IClientUtils utils, int pad, T function);

    <T extends LootItemFunction> void applyCountModifier(IClientUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count);
    <T extends LootItemCondition> void applyChanceModifier(IClientUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance);

    <T extends LootItemFunction> ItemStack applyItemStackModifier(IClientUtils utils, T function, ItemStack itemStack);

    Rect getBounds(IClientUtils registry, List<LootPoolEntryContainer> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(LootPoolEntryContainer entry);

    LootContext getLootContext();

    List<Item> getItems(ResourceLocation location);

    RangeValue convertNumber(IClientUtils utils, @Nullable NumberProvider numberProvider);
}
