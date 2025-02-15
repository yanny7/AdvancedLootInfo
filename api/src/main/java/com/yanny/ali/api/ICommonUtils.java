package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICommonUtils {
    ILootCondition convertCondition(IContext context, LootItemCondition condition);
    List<ILootCondition> convertConditions(IContext context, LootItemCondition[] conditions);
    ILootCondition decodeCondition(IContext context, FriendlyByteBuf buf);
    List<ILootCondition> decodeConditions(IContext context, FriendlyByteBuf buf);
    void encodeCondition(IContext context, FriendlyByteBuf buf, ILootCondition condition);
    void encodeConditions(IContext context, FriendlyByteBuf buf, List<ILootCondition> conditions);

    ILootFunction convertFunction(IContext context, LootItemFunction function);
    List<ILootFunction> convertFunctions(IContext context, LootItemFunction[] functions);
    ILootFunction decodeFunction(IContext context, FriendlyByteBuf buf);
    List<ILootFunction> decodeFunctions(IContext context, FriendlyByteBuf buf);
    void encodeFunction(IContext context, FriendlyByteBuf buf, ILootFunction condition);
    void encodeFunctions(IContext context, FriendlyByteBuf buf, List<ILootFunction> functions);

    ILootEntry convertEntry(IContext context, LootPoolEntryContainer entry);
    List<ILootEntry> convertEntries(IContext context, LootPoolEntryContainer[] entries);
    ILootEntry decodeEntry(IContext context, FriendlyByteBuf buf);
    List<ILootEntry> decodeEntries(IContext context, FriendlyByteBuf buf);
    void encodeEntry(IContext context, FriendlyByteBuf buf, ILootEntry entry);
    void encodeEntries(IContext context, FriendlyByteBuf buf, List<ILootEntry> entries);

    RangeValue convertNumber(IContext context, @Nullable NumberProvider numberProvider);
}
