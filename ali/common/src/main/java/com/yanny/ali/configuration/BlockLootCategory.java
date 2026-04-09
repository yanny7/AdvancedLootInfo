package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockLootCategory extends LootCategory<Block> {
    public static final MapCodec<BlockLootCategory> CODEC = RecordCodecBuilder.mapCodec((instance) ->
        instance.group(
                ResourceLocation.CODEC.fieldOf("key").forGetter(LootCategory::getKey),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(LootCategory::getIcon),
                Codec.BOOL.optionalFieldOf("hide", false).forGetter(LootCategory::isHidden),
                Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(LootCategory::getCatalyst),
                Codec.STRING.listOf().fieldOf("classes").forGetter(src -> src.classes.stream().map(Class::getName).toList())
        ).apply(instance, (key, icon, hide, catalyst, classNames) -> {
            //noinspection unchecked
            List<Class<?>> classList = (List<Class<?>>) (Object) classNames.stream().map(name -> {
                try {
                    return Class.forName(name);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            return new BlockLootCategory(key, icon, hide, catalyst, classList);
        })
    );

    private final List<Class<?>> classes;

    public BlockLootCategory(ResourceLocation key, Item icon, boolean hide, Ingredient catalyst, List<Class<?>> classes) {
        super(key, icon, Type.BLOCK, hide, catalyst);
        this.classes = classes;
    }

    @Override
    public boolean validate(Block block) {
        return classes.stream().anyMatch((p) -> p.isAssignableFrom(block.getClass()));
    }
}
