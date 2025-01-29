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
import java.util.LinkedList;
import java.util.List;
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
    public ILootCondition convertCondition(LootContext lootContext, LootItemCondition condition) {
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
    public List<ILootCondition> convertConditions(LootContext lootContext, LootItemCondition[] conditions) {
        List<ILootCondition> list = new LinkedList<>();

        for (LootItemCondition condition : conditions) {
            list.add(convertCondition(lootContext, condition));
        }

        return list;
    }

    @Override
    public ILootCondition decodeCondition(FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

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
    public List<ILootCondition> decodeConditions(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootCondition> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeCondition(buf));
        }

        return list;
    }

    @Override
    public void encodeCondition(FriendlyByteBuf buf, ILootCondition condition) {
        ResourceLocation key = conditionClassMap.getOrDefault(condition.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        condition.encode(buf);
    }

    @Override
    public void encodeConditions(FriendlyByteBuf buf, List<ILootCondition> conditions) {
        buf.writeInt(conditions.size());

        for (ILootCondition condition : conditions) {
            encodeCondition(buf, condition);
        }
    }

    @Override
    public ILootFunction convertFunction(LootContext lootContext, LootItemFunction function) {
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
    public List<ILootFunction> convertFunctions(LootContext lootContext, LootItemFunction[] functions) {
        List<ILootFunction> list = new LinkedList<>();

        for (LootItemFunction function : functions) {
            list.add(convertFunction(lootContext, function));
        }

        return list;
    }

    @Override
    public ILootFunction decodeFunction(FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

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

    @Override
    public List<ILootFunction> decodeFunctions(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootFunction> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeFunction(buf));
        }

        return list;
    }

    @Override
    public void encodeFunction(FriendlyByteBuf buf, ILootFunction function) {
        ResourceLocation key = functionClassMap.getOrDefault(function.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        function.encode(buf);
    }

    @Override
    public void encodeFunctions(FriendlyByteBuf buf, List<ILootFunction> functions) {
        buf.writeInt(functions.size());

        for (ILootFunction function : functions) {
            encodeFunction(buf, function);
        }
    }
}
