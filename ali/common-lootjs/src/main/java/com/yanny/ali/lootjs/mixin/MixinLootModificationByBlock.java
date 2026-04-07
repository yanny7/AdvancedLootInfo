package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.LootModificationByBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(LootModificationByBlock.class)
public interface MixinLootModificationByBlock {
    @Accessor
    Predicate<BlockState> getPredicate();
}
