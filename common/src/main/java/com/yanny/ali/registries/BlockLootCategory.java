package com.yanny.ali.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockLootCategory extends LootCategory<Block> {
    private final List<Class<?>> classes;

    public BlockLootCategory(ResourceLocation key, ItemStack icon, boolean hide, List<Class<?>> classes) {
        super(key, icon, Type.BLOCK, hide);
        this.classes = classes;
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
