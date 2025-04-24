package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class ClientUtils implements IWidgetUtils {
    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, functions, conditions);
    }

    @Override
    public <T extends LootItemCondition> List<Component> getConditionTooltip(IClientUtils utils, int pad, T condition) {
        return PluginManager.CLIENT_REGISTRY.getConditionTooltip(utils, pad, condition);
    }

    @Override
    public <T extends LootItemFunction> List<Component> getFunctionTooltip(IClientUtils utils, int pad, T function) {
        return PluginManager.CLIENT_REGISTRY.getFunctionTooltip(utils, pad, function);
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IClientUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        PluginManager.CLIENT_REGISTRY.applyCountModifier(utils, function, count);
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IClientUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        PluginManager.CLIENT_REGISTRY.applyChanceModifier(utils, condition, chance);
    }

    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IClientUtils utils, T function, ItemStack itemStack) {
        return PluginManager.CLIENT_REGISTRY.applyItemStackModifier(utils, function, itemStack);
    }

    @Override
    public Rect getBounds(IClientUtils registry, List<LootPoolEntryContainer> entries, int x, int y) {
        return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
    }

    @Override
    public @Nullable LootTable getLootTable(ResourceLocation resourceLocation) {
        return PluginManager.CLIENT_REGISTRY.getLootTable(resourceLocation);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(LootPoolEntryContainer entry) {
        return PluginManager.CLIENT_REGISTRY.getWidgetDirection(entry);
    }

    @Override
    public LootContext getLootContext() {
        return PluginManager.CLIENT_REGISTRY.getLootContext();
    }

    @Override
    public RangeValue convertNumber(IClientUtils utils, @Nullable NumberProvider numberProvider) {
        return PluginManager.CLIENT_REGISTRY.convertNumber(utils, numberProvider);
    }

    @Override
    public List<Item> getItems(ResourceLocation location) {
        return PluginManager.CLIENT_REGISTRY.getItems(location);
    }
}
