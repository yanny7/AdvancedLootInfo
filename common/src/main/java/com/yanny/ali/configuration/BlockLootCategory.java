package com.yanny.ali.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockLootCategory extends LootCategory<Block> {
    private final List<Class<?>> classes;

    public BlockLootCategory(ResourceLocation key, Item icon, boolean hide, List<Class<?>> classes) {
        super(key, icon, Type.BLOCK, hide);
        this.classes = classes;
    }

    public BlockLootCategory(JsonObject object) {
        super(Type.BLOCK, object);
        //noinspection unchecked
        classes = (List<Class<?>>) (Object) GsonHelper.getAsJsonArray(object, "classes").asList().stream().map(JsonElement::getAsString).map((String className) -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Failed to resolve class " + className);
            }
        }).toList();
    }

    @Override
    protected void toJson(JsonObject object) {
        JsonArray array = new JsonArray();

        classes.forEach((c) -> array.add(c.getName()));
        object.add("classes", array);
    }

    @Override
    public boolean validate(Block block) {
        return classes.stream().anyMatch((p) -> p.isAssignableFrom(block.getClass()));
    }
}
