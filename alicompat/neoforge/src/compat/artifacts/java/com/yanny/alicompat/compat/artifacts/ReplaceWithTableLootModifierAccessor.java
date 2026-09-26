package com.yanny.alicompat.compat.artifacts;

import artifacts.neoforge.loot.ReplaceWithTableLootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReplaceWithTableLootModifierAccessor extends BaseAccessor<ReplaceWithTableLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ReplaceWithTableLootModifierAccessor(ReplaceWithTableLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (page, c) -> List.of(
                new IOperation.RemoveOperation((itemStack) -> true, (src) -> GlmNodeUtils.keptNode(utils, c, src)),
                new IOperation.AddOperation((itemStack) -> true, GlmNodeUtils.referenceNode(utils, c, parent.table().identifier()))
        )));
    }
}
