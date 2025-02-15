package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.function.BiFunction;

public interface ICommonRegistry {
    <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                    ResourceLocation key,
                                                    BiFunction<IContext, LootItemFunction, ILootFunction> functionEncoder,
                                                    BiFunction<IContext, FriendlyByteBuf, ILootFunction> functionDecoder);
    <T extends ILootCondition> void registerCondition(Class<T> clazz,
                                                      ResourceLocation key,
                                                      BiFunction<IContext, LootItemCondition, ILootCondition> conditionEncoder,
                                                      BiFunction<IContext, FriendlyByteBuf, ILootCondition> conditionDecoder);
    <T extends ILootEntry> void registerEntry(Class<T> clazz,
                                             ResourceLocation key,
                                             BiFunction<IContext, LootPoolEntryContainer, ILootEntry> entryEncoder,
                                             BiFunction<IContext, FriendlyByteBuf, ILootEntry> entryDecoder);

    void registerNumberProvider(ResourceLocation key, BiFunction<IContext, NumberProvider, RangeValue> converter);
}
