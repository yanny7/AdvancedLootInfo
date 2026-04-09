package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    public static final MapCodec<EntityLootCategory> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("key").forGetter(LootCategory::getKey),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(LootCategory::getIcon),
                    Codec.BOOL.optionalFieldOf("hide", false).forGetter(LootCategory::isHidden),
                    Ingredient.CODEC.optionalFieldOf("catalyst").forGetter((src) -> Optional.ofNullable(src.getCatalyst())),
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
                return new EntityLootCategory(key, icon, hide, catalyst.orElse(null), classList);
            })
    );

    private final List<Class<?>> classes;

    public EntityLootCategory(ResourceLocation key, Item icon, boolean hide, @Nullable Ingredient catalyst, List<Class<?>> classes) {
        super(key, icon, Type.ENTITY, hide, catalyst);
        this.classes = classes;
    }

    @Override
    public boolean validate(EntityType<?> entityType) {
        return classes.stream().anyMatch((p) -> p.isAssignableFrom(entityType.getBaseClass()));
    }
}
