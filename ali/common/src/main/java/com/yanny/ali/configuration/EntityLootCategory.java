package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    public static final MapCodec<EntityLootCategory> CODEC = RecordCodecBuilder.mapCodec((instance) ->
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
                return new EntityLootCategory(key, icon, hide, catalyst, classList);
            })
    );

    private final List<Class<?>> classes;

    public EntityLootCategory(ResourceLocation key, Item icon, boolean hide, Ingredient catalyst, List<Class<?>> classes) {
        super(key, icon, Type.ENTITY, hide, catalyst);
        this.classes = classes;
    }

    @Override
    public boolean validate(EntityType<?> entityType) {
        return classes.stream().anyMatch((p) -> p.isAssignableFrom(entityType.getBaseClass()));
    }
}
