package com.yanny.advanced_loot_info.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.function.BiFunction;

public interface IRegistry {
    <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                    ResourceLocation key,
                                                    BiFunction<IContext, LootItemFunction, ILootFunction> functionEncoder,
                                                    BiFunction<IContext, FriendlyByteBuf, ILootFunction> functionDecoder);
    <T extends ILootCondition> void registerCondition(Class<T> clazz,
                                                      ResourceLocation key,
                                                      BiFunction<IContext, LootItemCondition, ILootCondition> conditionEncoder,
                                                      BiFunction<IContext, FriendlyByteBuf, ILootCondition> conditionDecoder);

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
}
