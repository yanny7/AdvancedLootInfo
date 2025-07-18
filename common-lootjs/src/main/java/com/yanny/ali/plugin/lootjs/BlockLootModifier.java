package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.yanny.ali.api.IServerUtils;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BlockLootModifier extends AbstractLootModifier<Block> {
    private final BlockStatePredicate predicate;

    public BlockLootModifier(IServerUtils utils, LootModifier modifier, LootModifier.BlockFiltered blockFiltered) {
        super(utils, modifier);
        List<BlockStatePredicate> instance = Utils.getCapturedInstances(blockFiltered.predicate(), BlockStatePredicate.class);

        if (instance.size() == 1) {
            predicate = instance.getFirst();
        } else {
            throw new IllegalStateException("Invalid predicate type " + blockFiltered.predicate().getClass().getCanonicalName());
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
