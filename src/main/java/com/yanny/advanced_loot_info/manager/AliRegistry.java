package com.yanny.advanced_loot_info.manager;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.advanced_loot_info.api.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AliRegistry implements IRegistry {
    public final Map<ResourceLocation, Pair<BiFunction<LootContext, LootItemFunction, ILootFunction>, Function<FriendlyByteBuf, ILootFunction>>> functionMap = new HashMap<>();
    public final Map<ResourceLocation, Pair<BiFunction<LootContext, LootItemCondition, ILootCondition>, Function<FriendlyByteBuf, ILootCondition>>> conditionMap = new HashMap<>();
    public final Map<Class<?>, ResourceLocation> functionClassMap = new HashMap<>();
    public final Map<Class<?>, ResourceLocation> conditionClassMap = new HashMap<>();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                           ResourceLocation key,
                                                           BiFunction<LootContext, LootItemFunction, ILootFunction> functionEncoder,
                                                           Function<FriendlyByteBuf, ILootFunction> functionDecoder) {
        functionMap.put(key, new Pair<>(functionEncoder, functionDecoder));
        functionClassMap.put(clazz, key);
    }

    @Override
    public <T extends ILootCondition> void registerCondition(Class<T> clazz,
                                                             ResourceLocation key,
                                                             BiFunction<LootContext, LootItemCondition, ILootCondition> conditionEncoder,
                                                             Function<FriendlyByteBuf, ILootCondition> conditionDecoder) {
        conditionMap.put(key, new Pair<>(conditionEncoder, conditionDecoder));
        conditionClassMap.put(clazz, key);
    }

    @Override
    public ILootCondition getCondition(LootContext lootContext, LootItemCondition condition) {
        ResourceLocation key = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());

        if (key != null) {
            Pair<BiFunction<LootContext, LootItemCondition, ILootCondition>, Function<FriendlyByteBuf, ILootCondition>> pair = conditionMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(lootContext, condition);
            } else {
                LOGGER.warn("Encode condition {} was not registered", key);
            }
        }

        return new UnknownCondition(lootContext, condition);
    }

    @Override
    public ILootCondition getCondition(ResourceLocation key, FriendlyByteBuf buf) {
        if (key != UNKNOWN) {
            Pair<BiFunction<LootContext, LootItemCondition, ILootCondition>, Function<FriendlyByteBuf, ILootCondition>> pair = conditionMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(buf);
            } else {
                LOGGER.warn("Decode condition {} was not registered", key);
            }
        }

        return new UnknownCondition(buf);
    }

    @Override
    public ILootFunction getFunction(LootContext lootContext, LootItemFunction function) {
        ResourceLocation key = BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType());

        if (key != null) {
            Pair<BiFunction<LootContext, LootItemFunction, ILootFunction>, Function<FriendlyByteBuf, ILootFunction>> pair = functionMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(lootContext, function);
            } else {
                LOGGER.warn("Encode function {} was not registered", key);
            }
        }

        return new UnknownFunction(lootContext, function);
    }

    @Override
    public ILootFunction getFunction(ResourceLocation key, FriendlyByteBuf buf) {
        if (key != UNKNOWN) {
            Pair<BiFunction<LootContext, LootItemFunction, ILootFunction>, Function<FriendlyByteBuf, ILootFunction>> pair = functionMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(buf);
            } else {
                LOGGER.warn("Decode function {} was not registered", key);
            }
        }
        
        return new UnknownFunction(buf);
    }
}
