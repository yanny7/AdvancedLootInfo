package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPool.class)
public interface MixinLootPool {
    @Accessor
    LootPoolEntryContainer[] getEntries();

    @Accessor
    NumberProvider getRolls();

    @Accessor
    NumberProvider getBonusRolls();
}
