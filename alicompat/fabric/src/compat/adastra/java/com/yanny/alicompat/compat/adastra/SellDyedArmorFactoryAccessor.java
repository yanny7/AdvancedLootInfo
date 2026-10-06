package com.yanny.alicompat.compat.adastra;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffers$SellDyedArmorFactory")
public class SellDyedArmorFactoryAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final float MULTIPLIER = 0.2F;

    @FieldAccessor
    private Item sell;

    @FieldAccessor
    private int price;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    public SellDyedArmorFactoryAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(price),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(sell.getDefaultInstance()),
                NumberExpr.constant(1),
                sell.getDefaultInstance().is(ItemTags.DYEABLE) ? TooltipBuilder.keyOnly(Lang.Functions.DYED_RANDOMLY).build() : TooltipNode.empty(),
                maxUses,
                experience,
                MULTIPLIER,
                conditions
        );
    }
}
