package com.yanny.advanced_loot_info.manager;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.IRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AliRegistry implements IRegistry {
    public final Map<ResourceLocation, Pair<BiFunction<LootContext, LootItemFunction, ILootFunction>, Function<FriendlyByteBuf, ILootFunction>>> functionMap = new HashMap<>();
    public final Map<Class<?>, ResourceLocation> typeMap = new HashMap<>();

    @Override
    public <T extends ILootFunction> void registerFunction(Class<T> clazz,
                                                           ResourceLocation key,
                                                           BiFunction<LootContext, LootItemFunction, ILootFunction> functionEncoder,
                                                           Function<FriendlyByteBuf, ILootFunction> functionDecoder) {
        functionMap.put(key, new Pair<>(functionEncoder, functionDecoder));
        typeMap.put(clazz, key);
    }
}
