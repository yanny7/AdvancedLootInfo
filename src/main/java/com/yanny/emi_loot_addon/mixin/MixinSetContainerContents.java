package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetContainerContents.class)
public interface MixinSetContainerContents {
    @Accessor
    BlockEntityType<?> getType();

    @Accessor
    List<LootPoolEntryContainer> getEntries();
}
