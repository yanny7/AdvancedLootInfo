package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.misc.maptrades.StructureSpecificMaps;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class RepurposedStructuresTooltipTest {
    @Test
    public void testTreasureMapForEmeraldsListing() {
        StructureSpecificMaps.TreasureMapForEmeralds listing = new StructureSpecificMaps.TreasureMapForEmeralds(
                8, "repurposed_structures:mineshafts/birch", "filled_map.mineshaft", MapDecoration.Type.RED_X, 5, 12, 2);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 5",
                "XP: 12",
                "Price Multiplier: 0.2"
        ));
    }
}
