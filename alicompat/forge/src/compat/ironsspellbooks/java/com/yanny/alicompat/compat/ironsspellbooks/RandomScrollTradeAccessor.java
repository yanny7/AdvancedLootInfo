package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import io.redspace.ironsspellbooks.player.AdditionalWanderingTrades;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RandomScrollTradeAccessor extends BaseAccessor<AdditionalWanderingTrades.RandomScrollTrade> implements IItemListing {
    @FieldAccessor
    private ItemStack price;
    @FieldAccessor
    private ItemStack forSale;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private int xp;
    @FieldAccessor
    private float priceMult;
    @FieldAccessor
    private SpellFilter spellFilter;
    @FieldAccessor
    private float minQuality;
    @FieldAccessor
    private float maxQuality;

    public RandomScrollTradeAccessor(AdditionalWanderingTrades.RandomScrollTrade parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return SpellScrollTrade.of(spellFilter, minQuality, maxQuality, new ItemStack(price.getItem()), forSale, maxTrades, xp, priceMult)
                .getNode(utils, conditions);
    }
}
