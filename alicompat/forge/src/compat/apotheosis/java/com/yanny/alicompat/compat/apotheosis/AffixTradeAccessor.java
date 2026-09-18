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
import dev.shadowsoffire.apotheosis.adventure.affix.trades.AffixTrade;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AffixTradeAccessor extends BaseAccessor<AffixTrade> implements IItemListing {
    private static final int MAX_TRADES = 1;
    private static final int XP = 100;
    private static final float PRICE_MULTIPLIER = 1.0F;

    @FieldAccessor
    private ItemStack price;

    @FieldAccessor
    private ItemStack price2;

    @FieldAccessor
    private RarityClamp.Simple rarities;

    @FieldAccessor
    private List<DynamicHolder<AffixLootEntry>> entries;

    public AffixTradeAccessor(AffixTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode result = TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, entries).build(Lang.Branch.ENTRIES));
        }).build(ApotheosisLang.Entry.RANDOM_AFFIX_ITEM);

        return new ItemsToItemsNode(
                utils,
                Either.left(price),
                new RangeValue(price.getCount()),
                TooltipNode.empty(),
                Either.left(price2),
                new RangeValue(Math.max(1, price2.getCount())),
                TooltipNode.empty(),
                Either.left(ApotheosisUtils.firstEntryStack(entries)),
                new RangeValue(1),
                result,
                MAX_TRADES,
                XP,
                PRICE_MULTIPLIER,
                conditions
        );
    }
}
