package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
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
    public <T extends ItemSubPredicate> List<Component> getItemSubPredicateTooltip(IClientUtils utils, int pad, ItemSubPredicate.Type<?> type, T predicate) {
        return PluginManager.CLIENT_REGISTRY.getItemSubPredicateTooltip(utils, pad, type, predicate);
    }

    @Override
    public <T extends EntitySubPredicate> List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, T predicate) {
        return PluginManager.CLIENT_REGISTRY.getEntitySubPredicateTooltip(utils, pad, predicate);
    }

    @Override
    public <T extends LootItemFunction> void applyCount(IClientUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        PluginManager.CLIENT_REGISTRY.applyCount(utils, function, count);
    }

    @Override
    public <T extends LootItemCondition> void applyChance(IClientUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        PluginManager.CLIENT_REGISTRY.applyChance(utils, condition, chance);
    }

    @Override
    public Rect getBounds(IClientUtils registry, List<LootPoolEntryContainer> entries, int x, int y) {
        return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
    }

    @Override
    public @Nullable LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either) {
        return PluginManager.CLIENT_REGISTRY.getLootTable(either);
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
    public List<Item> getItems(ResourceKey<LootTable> location) {
        return PluginManager.CLIENT_REGISTRY.getItems(location);
    }

    @Nullable
    @Override
    public HolderLookup.Provider lookupProvider() {
        return PluginManager.CLIENT_REGISTRY.lookupProvider();
    }
}
