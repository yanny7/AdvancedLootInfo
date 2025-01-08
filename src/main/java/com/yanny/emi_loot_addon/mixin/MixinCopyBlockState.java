package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(CopyBlockState.class)
public interface MixinCopyBlockState {
    @Accessor
    Block getBlock();

    @Accessor
    Set<Property<?>> getProperties();
}
