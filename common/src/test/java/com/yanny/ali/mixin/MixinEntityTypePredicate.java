package com.yanny.ali.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public interface MixinEntityTypePredicate {
    interface TypePredicate {
        EntityType<?> getType();
    }

    interface TagPredicate {
        TagKey<EntityType<?>> getTag();
    }
}
