package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.tooltip.TooltipNode;
import net.mehvahdjukaar.moonlight.api.trades.SimpleItemListing;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootConditions;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.regex.Pattern;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MoonlightTooltipTest {
    @Test
    public void testOptionalItemEntry() {
        LootPoolEntryContainer entry = OptionalItemPool.lootTableOptionalItem("minecraft:diamond").setWeight(2).setQuality(1).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Optional Item:",
                "  -> Item: minecraft:diamond",
                "  -> Name: minecraft:diamond",
                "  -> Weight: 2",
                "  -> Quality: 1"
        ));
    }

    @Test
    public void testOptionalPropertyCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, optionalProperty()).build(), List.of(
                "Optional Block State Property:",
                "  -> Block: minecraft:oak_leaves"
        ));
    }

    @Test
    public void testPatternMatchCondition() {
        LootItemCondition condition = new ModLootConditions.PatternMatchCondition(List.of(Pattern.compile("minecraft:chests/.*")));

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Loot Table Id Pattern:",
                "  -> minecraft:chests/.*"
        ));
    }

    @Test
    public void testPatternMatchConditionWithoutPatterns() {
        LootItemCondition condition = new ModLootConditions.PatternMatchCondition(List.of());

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Loot Table Id Pattern:"
        ));
    }

    @Test
    public void testDataConditionsCondition() {
        CraftingHelper.register(TrueCondition.Serializer.INSTANCE);

        LootItemCondition condition = new ModLootConditions.IConditionLootCondition(List.of(TrueCondition.INSTANCE));

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Data Conditions:",
                "  -> type: forge:true"
        ));
    }

    @Test
    public void testDataConditionsConditionWithoutConditions() {
        LootItemCondition condition = new ModLootConditions.IConditionLootCondition(List.of());

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Data Conditions:"
        ));
    }

    @Test
    public void testSimpleItemListing() {
        SimpleItemListing listing = new SimpleItemListing(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.OAK_LOG, 8), 12, 4, 0.05F);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 12",
                "XP: 4",
                "Price Multiplier: 0.05"
        ));
    }

    @NotNull
    private static LootItemCondition optionalProperty() {
        try {
            Constructor<OptionalPropertyCondition> constructor = OptionalPropertyCondition.class.getDeclaredConstructor(
                    ResourceLocation.class, net.minecraft.world.level.block.Block.class, StatePropertiesPredicate.class);

            constructor.setAccessible(true);
            return constructor.newInstance(new ResourceLocation("minecraft", "oak_leaves"), Blocks.OAK_LEAVES, StatePropertiesPredicate.ANY);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create OptionalPropertyCondition", e);
        }
    }
}
