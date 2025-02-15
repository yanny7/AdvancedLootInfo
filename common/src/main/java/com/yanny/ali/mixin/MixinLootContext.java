package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootContext.class)
public interface MixinLootContext {
    @Mixin(LootContext.EntityTarget.class)
    interface EntityTarget {
        @Accessor
        String getName();
    }
}
