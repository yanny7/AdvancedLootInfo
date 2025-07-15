package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.LootModificationByEntity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashSet;

@Mixin(LootModificationByEntity.class)
public interface MixinLootModificationByEntity {
    @Accessor
    HashSet<EntityType<?>> getEntities();
}
