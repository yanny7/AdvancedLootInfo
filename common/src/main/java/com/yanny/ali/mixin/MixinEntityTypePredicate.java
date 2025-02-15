package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityTypePredicate.class)
public interface MixinEntityTypePredicate {
    @Mixin(EntityTypePredicate.TypePredicate.class)
    interface TypePredicate {
        @Accessor
        EntityType<?> getType();
    }

    @Mixin(EntityTypePredicate.TagPredicate.class)
    interface TagPredicate {
        @Accessor
        TagKey<EntityType<?>> getTag();
    }
}
