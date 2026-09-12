package com.yanny.alicompat.compat.repurposedstructures;

import com.telepathicgrunt.repurposedstructures.misc.forge.lootmanager.DetectRSLootTables;
import com.telepathicgrunt.repurposedstructures.misc.maptrades.StructureSpecificMaps;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class RepurposedStructuresTooltipTest {
    @Test
    public void testDetectLootTablesCondition() {
        LootItemCondition condition = DetectRSLootTables.builder(Set.of(new ResourceLocation("repurposed_structures", "chests/village_badlands/house"))).build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Detect Repurposed Structures Loot Tables:",
                "  -> Blacklisted Loot Tables:",
                "    -> repurposed_structures:chests/village_badlands/house"
        ));
    }

    @Test
    public void testTreasureMapForEmeraldsListing() {
        StructureSpecificMaps.TreasureMapForEmeralds listing = new StructureSpecificMaps.TreasureMapForEmeralds(
                8, "repurposed_structures:village_badlands", "filled_map.buriedtreasure", MapDecoration.Type.RED_X, 6, 5, 100);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 6",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
    }
}
