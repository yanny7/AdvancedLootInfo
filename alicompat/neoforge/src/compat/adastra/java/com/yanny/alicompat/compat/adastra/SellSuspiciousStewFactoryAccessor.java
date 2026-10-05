package com.yanny.alicompat.compat.adastra;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.server.DataComponentTooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffers$SellSuspiciousStewFactory")
public class SellSuspiciousStewFactoryAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final int MAX_USES = 12;

    @FieldAccessor
    private SuspiciousStewEffects effects;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellSuspiciousStewFactoryAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, effects);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(stew),
                NumberExpr.constant(1),
                DataComponentTooltipUtils.getSuspiciousStewEffectsTooltip(utils, effects).build(),
                MAX_USES,
                experience,
                multiplier,
                conditions
        );
    }
}
