package com.yanny.advanced_loot_info.manager;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.IRegistry;
import com.yanny.advanced_loot_info.plugin.condition.UnknownCondition;
import com.yanny.advanced_loot_info.plugin.function.UnknownFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class AliRegistry implements IRegistry {
    public final Map<ResourceLocation, Pair<BiFunction<IContext, LootItemFunction, ILootFunction>, BiFunction<IContext, FriendlyByteBuf, ILootFunction>>> functionMap = new HashMap<>();
    public final Map<ResourceLocation, Pair<BiFunction<IContext, LootItemCondition, ILootCondition>, BiFunction<IContext, FriendlyByteBuf, ILootCondition>>> conditionMap = new HashMap<>();
    public final Map<Class<?>, ResourceLocation> functionClassMap = new HashMap<>();
    public final Map<Class<?>, ResourceLocation> conditionClassMap = new HashMap<>();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                           ResourceLocation key,
                                                           BiFunction<IContext, LootItemFunction, ILootFunction> functionEncoder,
                                                           BiFunction<IContext, FriendlyByteBuf, ILootFunction> functionDecoder) {
        functionMap.put(key, new Pair<>(functionEncoder, functionDecoder));
        functionClassMap.put(clazz, key);
    }

    @Override
    public <T extends ILootCondition> void registerCondition(Class<T> clazz,
                                                             ResourceLocation key,
                                                             BiFunction<IContext, LootItemCondition, ILootCondition> conditionEncoder,
                                                             BiFunction<IContext, FriendlyByteBuf, ILootCondition> conditionDecoder) {
        conditionMap.put(key, new Pair<>(conditionEncoder, conditionDecoder));
        conditionClassMap.put(clazz, key);
    }

    @Override
    public ILootCondition convertCondition(IContext context, LootItemCondition condition) {
        ResourceLocation key = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());

        if (key != null) {
            Pair<BiFunction<IContext, LootItemCondition, ILootCondition>, BiFunction<IContext, FriendlyByteBuf, ILootCondition>> pair = conditionMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(context, condition);
            } else {
                LOGGER.warn("Encode condition {} was not registered", key);
            }
        }

        return new UnknownCondition(context, condition);
    }

    @Override
    public List<ILootCondition> convertConditions(IContext context, LootItemCondition[] conditions) {
        List<ILootCondition> list = new LinkedList<>();

        for (LootItemCondition condition : conditions) {
            list.add(convertCondition(context, condition));
        }

        return list;
    }

    @Override
    public ILootCondition decodeCondition(IContext context, FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

        if (key != UNKNOWN) {
            Pair<BiFunction<IContext, LootItemCondition, ILootCondition>, BiFunction<IContext, FriendlyByteBuf, ILootCondition>> pair = conditionMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(context, buf);
            } else {
                LOGGER.warn("Decode condition {} was not registered", key);
            }
        }

        return new UnknownCondition(context, buf);
    }

    @Override
    public List<ILootCondition> decodeConditions(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootCondition> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeCondition(context, buf));
        }

        return list;
    }

    @Override
    public void encodeCondition(IContext context, FriendlyByteBuf buf, ILootCondition condition) {
        ResourceLocation key = conditionClassMap.getOrDefault(condition.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        condition.encode(context, buf);
    }

    @Override
    public void encodeConditions(IContext context, FriendlyByteBuf buf, List<ILootCondition> conditions) {
        buf.writeInt(conditions.size());

        for (ILootCondition condition : conditions) {
            encodeCondition(context, buf, condition);
        }
    }

    @Override
    public ILootFunction convertFunction(IContext context, LootItemFunction function) {
        ResourceLocation key = BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType());

        if (key != null) {
            Pair<BiFunction<IContext, LootItemFunction, ILootFunction>, BiFunction<IContext, FriendlyByteBuf, ILootFunction>> pair = functionMap.get(key);

            if (pair != null) {
                return pair.getFirst().apply(context, function);
            } else {
                LOGGER.warn("Encode function {} was not registered", key);
            }
        }

        return new UnknownFunction(context, function);
    }

    @Override
    public List<ILootFunction> convertFunctions(IContext context, LootItemFunction[] functions) {
        List<ILootFunction> list = new LinkedList<>();

        for (LootItemFunction function : functions) {
            list.add(convertFunction(context, function));
        }

        return list;
    }

    @Override
    public ILootFunction decodeFunction(IContext context, FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();

        if (key != UNKNOWN) {
            Pair<BiFunction<IContext, LootItemFunction, ILootFunction>, BiFunction<IContext, FriendlyByteBuf, ILootFunction>> pair = functionMap.get(key);

            if (pair != null) {
                return pair.getSecond().apply(context, buf);
            } else {
                LOGGER.warn("Decode function {} was not registered", key);
            }
        }
        
        return new UnknownFunction(context, buf);
    }

    @Override
    public List<ILootFunction> decodeFunctions(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootFunction> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            list.add(decodeFunction(context, buf));
        }

        return list;
    }

    @Override
    public void encodeFunction(IContext context, FriendlyByteBuf buf, ILootFunction function) {
        ResourceLocation key = functionClassMap.getOrDefault(function.getClass(), UNKNOWN);
        buf.writeResourceLocation(key);
        function.encode(context, buf);
    }

    @Override
    public void encodeFunctions(IContext context, FriendlyByteBuf buf, List<ILootFunction> functions) {
        buf.writeInt(functions.size());

        for (ILootFunction function : functions) {
            encodeFunction(context, buf, function);
        }
    }
}
