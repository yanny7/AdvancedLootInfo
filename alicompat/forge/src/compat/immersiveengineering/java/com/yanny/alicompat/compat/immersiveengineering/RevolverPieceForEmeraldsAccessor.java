package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$RevolverPieceForEmeralds")
public class RevolverPieceForEmeraldsAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    public RevolverPieceForEmeraldsAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(5, 29),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(new ItemStack(Ingredients.GUNPART_BARREL)),
                new RangeValue(1),
                utils.getValueTooltip(utils, List.of(Ingredients.GUNPART_DRUM.asItem(), Ingredients.GUNPART_HAMMER.asItem()))
                        .build(ImmersiveEngineeringLang.Branch.ALTERNATIVE),
                1,
                45,
                0.25F,
                conditions
        );
    }
}
