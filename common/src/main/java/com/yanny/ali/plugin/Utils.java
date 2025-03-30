package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Utils implements IWidgetUtils {
    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<ILootEntry> entries, int x, int y, List<ILootFunction> functions, List<ILootCondition> conditions) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, functions, conditions);
    }

    @Override
    public <T extends ILootCondition> List<Component> getConditionTooltip(Class<T> clazz, IUtils utils, int pad, ILootCondition condition) {
        return PluginManager.CLIENT_REGISTRY.getConditionTooltip(clazz, utils, pad, condition);
    }

    @Override
    public <T extends ILootFunction> List<Component> getFunctionTooltip(Class<T> clazz, IUtils utils, int pad, ILootFunction function) {
        return PluginManager.CLIENT_REGISTRY.getFunctionTooltip(clazz, utils, pad, function);
    }

    @Override
    public <T extends ILootEntry> List<Component> getEntryTooltip(Class<T> clazz, IUtils utils, int pad, ILootEntry entry) {
        return PluginManager.CLIENT_REGISTRY.getEntryTooltip(clazz, utils, pad, entry);
    }

    @Override
    public Rect getBounds(IClientUtils registry, List<ILootEntry> entries, int x, int y) {
        return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(ILootEntry entry) {
        return PluginManager.CLIENT_REGISTRY.getWidgetDirection(entry);
    }

    @Override
    public ILootCondition convertCondition(IContext context, LootItemCondition condition) {
        return null;
    }

    @Override
    public List<ILootCondition> convertConditions(IContext context, LootItemCondition[] conditions) {
        return PluginManager.COMMON_REGISTRY.convertConditions(context, conditions);
    }

    @Override
    public ILootCondition decodeCondition(IContext context, FriendlyByteBuf buf) {
        return PluginManager.COMMON_REGISTRY.decodeCondition(context, buf);
    }

    @Override
    public List<ILootCondition> decodeConditions(IContext context, FriendlyByteBuf buf) {
        return PluginManager.COMMON_REGISTRY.decodeConditions(context, buf);
    }

    @Override
    public void encodeCondition(IContext context, FriendlyByteBuf buf, ILootCondition condition) {
        PluginManager.COMMON_REGISTRY.encodeCondition(context, buf, condition);
    }

    @Override
    public void encodeConditions(IContext context, FriendlyByteBuf buf, List<ILootCondition> conditions) {
        PluginManager.COMMON_REGISTRY.encodeConditions(context, buf, conditions);
    }

    @Override
    public ILootFunction convertFunction(IContext context, LootItemFunction function) {
        return PluginManager.COMMON_REGISTRY.convertFunction(context, function);
    }

    @Override
    public List<ILootFunction> convertFunctions(IContext context, LootItemFunction[] functions) {
        return PluginManager.COMMON_REGISTRY.convertFunctions(context, functions);
    }

    @Override
    public ILootFunction decodeFunction(IContext context, FriendlyByteBuf buf) {
        return PluginManager.COMMON_REGISTRY.decodeFunction(context, buf);
    }

    @Override
    public List<ILootFunction> decodeFunctions(IContext context, FriendlyByteBuf buf) {
        return PluginManager.COMMON_REGISTRY.decodeFunctions(context, buf);
    }

    @Override
    public void encodeFunction(IContext context, FriendlyByteBuf buf, ILootFunction condition) {
        PluginManager.COMMON_REGISTRY.encodeFunction(context, buf, condition);
    }

    @Override
    public void encodeFunctions(IContext context, FriendlyByteBuf buf, List<ILootFunction> functions) {
        PluginManager.COMMON_REGISTRY.encodeFunctions(context, buf, functions);
    }

    @Override
    public ILootEntry convertEntry(IContext context, LootPoolEntryContainer entry) {
        return PluginManager.COMMON_REGISTRY.convertEntry(context, entry);
    }

    @Override
    public List<ILootEntry> convertEntries(IContext context, LootPoolEntryContainer[] entries) {
        return PluginManager.COMMON_REGISTRY.convertEntries(context, entries);
    }

    @Override
    public ILootEntry decodeEntry(IContext context, FriendlyByteBuf buf) {
        return PluginManager.COMMON_REGISTRY.decodeEntry(context, buf);
    }

    @Override
    public List<ILootEntry> decodeEntries(IContext context, FriendlyByteBuf buf) {
        return PluginManager.COMMON_REGISTRY.decodeEntries(context, buf);
    }

    @Override
    public void encodeEntry(IContext context, FriendlyByteBuf buf, ILootEntry entry) {
        PluginManager.COMMON_REGISTRY.encodeEntry(context, buf, entry);
    }

    @Override
    public void encodeEntries(IContext context, FriendlyByteBuf buf, List<ILootEntry> entries) {
        PluginManager.COMMON_REGISTRY.encodeEntries(context, buf, entries);
    }

    @Override
    public RangeValue convertNumber(IContext context, @Nullable NumberProvider numberProvider) {
        return PluginManager.COMMON_REGISTRY.convertNumber(context, numberProvider);
    }
}
