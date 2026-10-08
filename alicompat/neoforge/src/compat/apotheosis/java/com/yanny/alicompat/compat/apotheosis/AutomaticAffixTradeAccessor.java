package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
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
import org.jetbrains.annotations.NotNull;

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
        List<ItemStack> results = getResults();
        TooltipNode result = TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, entries).build(Lang.Branch.ENTRIES));
        }).build(ApotheosisLang.Entry.RANDOM_AFFIX_ITEM);

        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(results.stream().map(ApotheosisUtils::repairMaterial).toList(), NumberExpr.constant(REPAIR_MATERIAL_COUNT), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(List.of(Items.EMERALD.getDefaultInstance()), NumberExpr.add(NumberExpr.constant(1), NumberExpr.mul(NumberExpr.constant(EMERALDS_PER_TIER), NumberExpr.range(0, WorldTier.values().length - 1))), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(results, NumberExpr.constant(1), result),
                MAX_TRADES,
                XP,
                PRICE_MULTIPLIER,
                conditions
        );
    }

    @NotNull
    private List<ItemStack> getResults() {
        List<ItemStack> stacks = entries.stream().filter(DynamicHolder::isBound).map((h) -> h.get().stack().copy()).toList();

        return stacks.isEmpty() ? List.of(ItemStack.EMPTY) : stacks;
    }
}
