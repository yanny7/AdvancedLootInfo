package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeEntryBase.class)
public interface MixinCompositeEntryBase {
    @Accessor
    LootPoolEntryContainer[] getChildren();
}
