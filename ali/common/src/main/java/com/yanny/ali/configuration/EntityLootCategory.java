package com.yanny.ali.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;

public class EntityLootCategory extends LootCategory<EntityType<?>> {
    private static final Codec<TagKey<EntityType<?>>> ENTITY_TYPE_TAG_CODEC = TagKey.hashedCodec(Registries.ENTITY_TYPE);
    private static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();
    private static final Codec<Either<TagKey<EntityType<?>>, EntityType<?>>> TAG_OR_ENTITY_TYPE_CODEC = Codec.either(ENTITY_TYPE_TAG_CODEC, ENTITY_TYPE_CODEC);
    public static final MapCodec<EntityLootCategory> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("key").forGetter(LootCategory::getKey),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(LootCategory::getIcon),
                    Codec.BOOL.optionalFieldOf("hide", false).forGetter(LootCategory::isHidden),
                    Ingredient.CODEC.listOf().optionalFieldOf("catalysts", Collections.emptyList()).forGetter(LootCategory::getCatalysts),
                    TAG_OR_ENTITY_TYPE_CODEC.listOf().fieldOf("entityTypes").forGetter(src -> src.entityTypes)
            ).apply(instance, EntityLootCategory::new)
    );

    private final List<Either<TagKey<EntityType<?>>, EntityType<?>>> entityTypes;

    public EntityLootCategory(ResourceLocation key, Item icon, boolean hide, List<Ingredient> catalysts, List<Either<TagKey<EntityType<?>>, EntityType<?>>> entityTypes) {
        super(key, icon, Type.ENTITY, hide, catalysts);
        this.entityTypes = entityTypes;
    }

    @Override
    public boolean validate(EntityType<?> entityType) {
        if (entityTypes.isEmpty()) {
            return true;
        }

        return entityTypes.stream().anyMatch((either) -> either.map(
                (tag) -> entityType.builtInRegistryHolder().is(tag),
                (et) -> et == entityType
        ));
    }
}
