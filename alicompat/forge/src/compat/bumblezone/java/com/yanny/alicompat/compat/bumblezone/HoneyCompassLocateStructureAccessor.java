package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.functions.HoneyCompassLocateStructure;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

public class HoneyCompassLocateStructureAccessor extends BaseAccessor<HoneyCompassLocateStructure> implements IFunctionTooltip {
    @FieldAccessor
    private TagKey<Structure> destination;
    @FieldAccessor
    private int searchRadius;
    @FieldAccessor
    private boolean skipKnownStructures;

    public HoneyCompassLocateStructureAccessor(HoneyCompassLocateStructure parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, destination).build(Lang.Value.DESTINATION));
            b.add(utils.getValueTooltip(utils, searchRadius).build(Lang.Value.SEARCH_RADIUS));
            b.add(utils.getValueTooltip(utils, skipKnownStructures).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, BumblezoneLang.Functions.HONEY_COMPASS_LOCATE_STRUCTURE);
    }
}
