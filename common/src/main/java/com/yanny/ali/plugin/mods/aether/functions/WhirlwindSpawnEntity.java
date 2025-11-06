package com.yanny.ali.plugin.mods.aether.functions;

import com.yanny.ali.api.BranchTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity")
public class WhirlwindSpawnEntity extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private EntityType<?> entityType;
    @FieldAccessor
    private int count;

    public WhirlwindSpawnEntity(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch("ali.type.function.whirlwind_spawn_entity")
                .add(utils.getValueTooltip(utils, entityType).key("ali.property.value.entity_type"))
                .add(utils.getValueTooltip(utils, count).key("ali.property.value.count"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(predicates)));
    }
}
