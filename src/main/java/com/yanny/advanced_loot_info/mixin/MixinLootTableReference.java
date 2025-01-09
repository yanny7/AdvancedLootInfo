package com.yanny.advanced_loot_info.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTableReference.class)
public interface MixinLootTableReference {
    @Accessor
    ResourceLocation getName();
}
