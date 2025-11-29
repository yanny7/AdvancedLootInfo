package com.yanny.ali.plugin.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class LootConditionTypes {
    public static final LootItemConditionType UNUSED = create();

    @NotNull
    private static LootItemConditionType create() {
        return new LootItemConditionType(new UnusedSerializer());
    }

    private static class UnusedSerializer implements Serializer<LootItemCondition> {
        public void serialize(JsonObject json, LootItemCondition value, JsonSerializationContext context) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        public LootItemCondition deserialize(JsonObject serializer, JsonDeserializationContext context) {
            throw new UnsupportedOperationException();
        }
    }
}
