package com.yanny.advanced_loot_info.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface IRegistry {
    <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                    ResourceLocation key,
                                                    BiFunction<LootContext, LootItemFunction, ILootFunction> functionEncoder,
                                                    Function<FriendlyByteBuf, ILootFunction> functionDecoder);
    <T extends ILootCondition> void registerCondition(Class<T> clazz,
                                                      ResourceLocation key,
                                                      BiFunction<LootContext, LootItemCondition, ILootCondition> conditionEncoder,
                                                      Function<FriendlyByteBuf, ILootCondition> conditionDecoder);

    ILootCondition getCondition(LootContext lootContext, LootItemCondition condition);
    ILootCondition getCondition(ResourceLocation key, FriendlyByteBuf buf);
    ILootFunction getFunction(LootContext lootContext, LootItemFunction function);
    ILootFunction getFunction(ResourceLocation key, FriendlyByteBuf buf);
}
