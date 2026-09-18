package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.IntRange;
import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.*;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MoreJSTooltipTest {
    @Test
    public void testSimpleTradeListing() {
        SimpleTrade trade = new SimpleTrade(inputs(), new TradeItem(new ItemStack(Items.BREAD, 2), new IntRange(2)))
                .maxUses(8)
                .villagerExperience(5)
                .priceMultiplier(0.1F);

        assertTooltip(tooltip(trade), List.of(
                "Uses: 8",
                "XP: 5",
                "Price Multiplier: 0.1"
        ));
    }

    @Test
    public void testStewTradeListing() {
        StewTrade trade = new StewTrade(inputs(), new MobEffect[]{MobEffects.REGENERATION}, 200)
                .maxUses(6)
                .villagerExperience(4)
                .priceMultiplier(0.15F);

        assertTooltip(tooltip(trade), List.of(
                "Uses: 6",
                "XP: 4",
                "Price Multiplier: 0.15"
        ));
    }

    @Test
    public void testEnchantedItemTradeListing() {
        EnchantedItemTrade trade = new EnchantedItemTrade(inputs(), Items.DIAMOND_SWORD)
                .maxUses(4)
                .villagerExperience(9)
                .priceMultiplier(0.2F);

        assertTooltip(tooltip(trade), List.of(
                "Uses: 4",
                "XP: 9",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testPotionTradeListing() {
        PotionTrade trade = new PotionTrade(inputs())
                .item(Items.POTION)
                .potions(Potions.HEALING)
                .maxUses(3)
                .villagerExperience(7)
                .priceMultiplier(0.25F);

        assertTooltip(tooltip(trade), List.of(
                "Uses: 3",
                "XP: 7",
                "Price Multiplier: 0.25"
        ));
    }

    @Test
    public void testTreasureMapTradeListing() {
        TreasureMapTrade trade = new TreasureMapTrade(inputs(), (level, entity) -> null)
                .displayName(Component.literal("Buried Treasure"))
                .marker(MapDecoration.Type.RED_X)
                .scale((byte) 2)
                .maxUses(2)
                .villagerExperience(11)
                .priceMultiplier(0.3F);

        assertTooltip(tooltip(trade), List.of(
                "Uses: 2",
                "XP: 11",
                "Price Multiplier: 0.3"
        ));
    }

    private static TradeItem[] inputs() {
        return new TradeItem[]{
                new TradeItem(new ItemStack(Items.EMERALD, 3), new IntRange(3)),
                new TradeItem(new ItemStack(Items.WHEAT, 5), new IntRange(5))
        };
    }

    @NotNull
    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        return UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip();
    }
}
