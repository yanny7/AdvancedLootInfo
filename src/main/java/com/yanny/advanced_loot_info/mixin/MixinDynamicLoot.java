package com.yanny.advanced_loot_info.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DynamicLoot.class)
public interface MixinDynamicLoot {
    @Accessor
    ResourceLocation getName();
}
