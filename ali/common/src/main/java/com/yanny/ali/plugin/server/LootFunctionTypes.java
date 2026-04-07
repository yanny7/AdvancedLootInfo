package com.yanny.ali.plugin.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;

public class LootFunctionTypes {
    public static final LootItemFunctionType UNUSED = create();

    @NotNull
    private static LootItemFunctionType create() {
        return new LootItemFunctionType(new UnusedSerializer());
    }

    private static class UnusedSerializer implements Serializer<LootItemFunction> {
        public void serialize(JsonObject json, LootItemFunction value, JsonSerializationContext context) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        public LootItemFunction deserialize(JsonObject serializer, JsonDeserializationContext context) {
            throw new UnsupportedOperationException();
        }
    }
}
