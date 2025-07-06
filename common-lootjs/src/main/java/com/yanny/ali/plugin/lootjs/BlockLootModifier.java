package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.LootModificationByBlock;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinLootModificationByBlock;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Predicate;

public class BlockLootModifier extends LootModifier<Block> {
    private final BlockStatePredicate predicate;

    public BlockLootModifier(IServerUtils utils, LootModificationByBlock byBlock) {
        super(utils, byBlock);
        Predicate<?> statePredicate = ((MixinLootModificationByBlock) byBlock).getPredicate();

        if (statePredicate == null) {
            predicate = BlockStatePredicate.Simple.ALL;
        } else {
            List<BlockStatePredicate> instance = Utils.getCapturedInstances(statePredicate, BlockStatePredicate.class);

            if (instance.size() == 1) {
                predicate = instance.get(0);
            } else {
                throw new IllegalStateException("Invalid predicate type " + statePredicate.getClass().getCanonicalName());
            }
        }
    }

    @Override
    public boolean predicate(Block value) {
        return predicate.testBlock(value);
    }

    @Override
    public IType<Block> getType() {
        return IType.BLOCK;
    }
}
