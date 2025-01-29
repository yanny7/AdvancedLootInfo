package com.yanny.advanced_loot_info.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
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

    ILootCondition convertCondition(LootContext lootContext, LootItemCondition condition);
    List<ILootCondition> convertConditions(LootContext lootContext, LootItemCondition[] conditions);
    ILootCondition decodeCondition(FriendlyByteBuf buf);
    List<ILootCondition> decodeConditions(FriendlyByteBuf buf);
    void encodeCondition(FriendlyByteBuf buf, ILootCondition condition);
    void encodeConditions(FriendlyByteBuf buf, List<ILootCondition> conditions);

    ILootFunction convertFunction(LootContext lootContext, LootItemFunction function);
    List<ILootFunction> convertFunctions(LootContext lootContext, LootItemFunction[] functions);
    ILootFunction decodeFunction(FriendlyByteBuf buf);
    List<ILootFunction> decodeFunctions(FriendlyByteBuf buf);
    void encodeFunction(FriendlyByteBuf buf, ILootFunction condition);
    void encodeFunctions(FriendlyByteBuf buf, List<ILootFunction> functions);
}
