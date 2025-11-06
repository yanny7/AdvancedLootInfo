package com.yanny.ali.plugin.mods.immersive_engineering.functions;

import com.yanny.ali.api.BranchTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("blusunrize.immersiveengineering.common.util.loot.ConveyorCoverLootFunction")
public class ConveyorCoverLootFunction extends ConditionalFunction implements IFunctionTooltip {
    public ConveyorCoverLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch("ali.type.function.conveyor_cover")
                .add(getSubConditionsTooltip(utils, Arrays.asList(predicates)));
    }
}
