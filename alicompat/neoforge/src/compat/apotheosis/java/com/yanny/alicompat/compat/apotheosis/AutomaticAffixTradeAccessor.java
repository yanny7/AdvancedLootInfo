package com.yanny.alicompat.compat.apotheosis;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import dev.shadowsoffire.apotheosis.affix.trades.AutomaticAffixTrade;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;

public class AutomaticAffixTradeAccessor extends BaseAccessor<AutomaticAffixTrade> implements IItemListing {
    private static final int REPAIR_MATERIAL_COUNT = 5;
    private static final int EMERALDS_PER_TIER = 7;
    private static final int MAX_TRADES = 1;
    private static final int XP = 100;
    private static final float PRICE_MULTIPLIER = 1.0F;

    @FieldAccessor
    private Set<LootRarity> rarities;

    @FieldAccessor
    private List<DynamicHolder<AffixLootEntry>> entries;

    public AutomaticAffixTradeAccessor(AutomaticAffixTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        ItemStack affixStack = ApotheosisUtils.firstEntryStack(entries);
        TooltipNode result = TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, entries).build(Lang.Branch.ENTRIES));
        }).build(ApotheosisLang.Entry.RANDOM_AFFIX_ITEM);

        return new ItemsToItemsNode(
                utils,
                Either.left(ApotheosisUtils.repairMaterial(affixStack)),
                new RangeValue(REPAIR_MATERIAL_COUNT),
                TooltipNode.empty(),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(1, (WorldTier.values().length - 1) * EMERALDS_PER_TIER + 1),
                TooltipNode.empty(),
                Either.left(affixStack),
                new RangeValue(1),
                result,
                MAX_TRADES,
                XP,
                PRICE_MULTIPLIER,
                conditions
        );
    }
}
