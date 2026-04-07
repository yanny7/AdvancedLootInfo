package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootModificationByBlock;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinLootModificationByBlock;
import com.yanny.ali.plugin.mods.PluginUtils;
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
            List<BlockStatePredicate> instance = PluginUtils.getCapturedInstances(statePredicate, BlockStatePredicate.class);

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
