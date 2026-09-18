package com.yanny.ali.plugin.glm;

import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.function.Supplier;

public final class GlobalLootModifierWrapper implements IGlobalLootModifierWrapper {
    private final Identifier name;
    private final Object modifier;
    private final Class<?> lootModifierClass;
    private final Supplier<List<LootItemCondition>> conditions;
    private final Supplier<JsonElement> serializer;

    public GlobalLootModifierWrapper(Identifier name, Object modifier, Class<?> lootModifierClass, Supplier<List<LootItemCondition>> conditions, Supplier<JsonElement> serializer) {
        this.name = name;
        this.modifier = modifier;
        this.lootModifierClass = lootModifierClass;
        this.conditions = conditions;
        this.serializer = serializer;
    }

    @Override
    public Identifier getName() {
        return name;
    }

    @Override
    public Object getLootModifier() {
        return modifier;
    }

    @Override
    public Class<?> getLootModifierClass() {
        return lootModifierClass;
    }

    @Override
    public boolean isLootModifier() {
        return lootModifierClass.isInstance(modifier);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return conditions.get();
    }

    @Override
    public JsonElement serialize() {
        return serializer.get();
    }
}
