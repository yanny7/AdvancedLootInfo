package com.yanny.ali.neoforge.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AddTableLootModifier.class)
public interface MixinAddTableLootModifier {
    @Accessor
    ResourceKey<LootTable> getTable();
}
