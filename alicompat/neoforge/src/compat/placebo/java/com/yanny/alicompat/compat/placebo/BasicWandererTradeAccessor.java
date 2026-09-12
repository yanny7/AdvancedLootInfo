package com.yanny.alicompat.compat.placebo;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import dev.shadowsoffire.placebo.systems.wanderer.BasicWandererTrade;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;

public class BasicWandererTradeAccessor extends BaseAccessor<BasicWandererTrade> implements IItemListing {
    @FieldAccessor
    private ItemStack price;
    @FieldAccessor
    private ItemStack price2;
    @FieldAccessor
    private ItemStack forSale;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private int xp;
    @FieldAccessor
    private float priceMult;

    public BasicWandererTradeAccessor(BasicWandererTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return utils.getItemListing(utils, new BasicItemListing(price, price2, forSale, maxTrades, xp, priceMult), conditions);
    }
}
