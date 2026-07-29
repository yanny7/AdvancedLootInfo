package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import org.jetbrains.annotations.NotNull;

public class RootPlacerTooltipUtils {
    @NotNull
    public static TooltipBuilder getMangroveRootPlacerTooltip(IServerUtils utils, MangroveRootPlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseRootPlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.mangroveRootPlacement).build(Lang.Branch.MANGROVE_ROOT_PLACEMENT));
        }, Lang.RootPlacer.MANGROVE_ROOT);
    }

    private static void addBaseRootPlacerTooltip(IServerUtils utils, TooltipBuilder builder, RootPlacer placer) {
        builder.add(utils.getValueTooltip(utils, placer.trunkOffsetY).build(Lang.Branch.TRUNK_OFFSET_Y));
        builder.add(utils.getValueTooltip(utils, placer.rootProvider).build(Lang.Branch.ROOT_PROVIDER));
        builder.add(utils.getValueTooltip(utils, placer.aboveRootPlacement).build(Lang.Branch.ABOVE_ROOT_PLACEMENT));
    }
}
