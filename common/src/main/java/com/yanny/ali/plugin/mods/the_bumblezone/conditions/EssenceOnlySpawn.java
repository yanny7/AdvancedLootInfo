package com.yanny.ali.plugin.mods.the_bumblezone.conditions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@ClassAccessor("com.telepathicgrunt.the_bumblezone.loot.conditions.EssenceOnlySpawn")
public class EssenceOnlySpawn extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    public EssenceOnlySpawn(LootItemCondition parent) {
        super(parent);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return new TooltipNode(translatable("ali.type.condition.essence_only_spawn"));
    }
}
