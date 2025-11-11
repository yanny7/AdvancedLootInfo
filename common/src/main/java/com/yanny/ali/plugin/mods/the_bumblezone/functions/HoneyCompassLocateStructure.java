package com.yanny.ali.plugin.mods.the_bumblezone.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("com.telepathicgrunt.the_bumblezone.loot.functions.HoneyCompassLocateStructure")
public class HoneyCompassLocateStructure extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private TagKey<Structure> destination;
    @FieldAccessor
    private int searchRadius;
    @FieldAccessor
    private boolean skipKnownStructures;

    public HoneyCompassLocateStructure(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, destination).build("ali.property.value.destination"))
                .add(utils.getValueTooltip(utils, searchRadius).build("ali.property.value.search_radius"))
                .add(utils.getValueTooltip(utils, skipKnownStructures).build("ali.property.value.skip_known_structures"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.honey_compass_locate_structure");
    }
}
