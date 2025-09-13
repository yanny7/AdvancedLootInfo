package com.yanny.ali.plugin.mods.the_bumblezone.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

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
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.honey_compass_locate_structure"));

        tooltip.add(getTagKeyTooltip(utils, "ali.property.value.destination", destination));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.search_radius", searchRadius));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.skip_known_structures", skipKnownStructures));
        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
