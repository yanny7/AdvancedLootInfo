package com.yanny.ali.mixin;

import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WhirlwindSpawnEntity.class)
public interface MixinWhirlwindSpawnEntity {
    @Accessor
    EntityType<?> getEntityType();

    @Accessor
    int getCount();
}
