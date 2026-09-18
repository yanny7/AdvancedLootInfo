package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.conditions.EssenceOnlySpawn;
import com.telepathicgrunt.the_bumblezone.entities.BasicItemTrade;
import com.telepathicgrunt.the_bumblezone.loot.functions.DropContainerLoot;
import com.telepathicgrunt.the_bumblezone.loot.functions.HoneyCompassLocateStructure;
import com.telepathicgrunt.the_bumblezone.loot.functions.TagItemRemovals;
import com.telepathicgrunt.the_bumblezone.loot.functions.UniquifyIfHasItems;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class BumblezoneTooltipTest {
    @Test
    public void testEssenceOnlySpawnCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new EssenceOnlySpawn()).build(), List.of(
                "Player carries an Essence of the Bees"
        ));
    }

    @Test
    public void testDropContainerLootFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, new DropContainerLoot(List.of())).build(), List.of(
                "Drop Container Items:"
        ));
    }

    @Test
    public void testDropContainerLootFunctionWithPredicate() {
        List<LootItemCondition> conditions = List.of(ExplosionCondition.survivesExplosion().build());
        LootItemFunction function = new DropContainerLoot(conditions);

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Drop Container Items:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testUniquifyIfHasItemsFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, new UniquifyIfHasItems(List.of())).build(), List.of(
                "Uniquify If Has Items:"
        ));
    }

    @Test
    public void testUniquifyIfHasItemsFunctionWithPredicate() {
        List<LootItemCondition> conditions = List.of(ExplosionCondition.survivesExplosion().build());
        LootItemFunction function = new UniquifyIfHasItems(conditions);

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Uniquify If Has Items:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testTagItemRemovalsFunction() {
        TagItemRemovals function = new TagItemRemovals(List.of(), ItemTags.BEACON_PAYMENT_ITEMS);

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Tag Item Removals:",
                "  -> Tag: minecraft:beacon_payment_items"
        ));
    }

    @Test
    public void testHoneyCompassLocateStructureFunction() {
        HoneyCompassLocateStructure function = new HoneyCompassLocateStructure(List.of(), StructureTags.VILLAGE, 50, true);

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Honey Compass Locate Structure:",
                "  -> Destination: minecraft:village",
                "  -> Search Radius: 50",
                "  -> Skip Known Structures: true"
        ));
    }

    @Test
    public void testBasicItemTradeListing() {
        BasicItemTrade trade = new BasicItemTrade(Items.EMERALD, Items.HONEYCOMB, 2, 3, 9, 4, 0.5F);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 9",
                "XP: 4",
                "Price Multiplier: 0.5"
        ));
    }
}
