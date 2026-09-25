package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
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
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.github.ysbbbbbb.kaleidoscopecookery.event.ModTradesEvent$EnchantedItemForEmeralds")
public class EnchantedItemForEmeraldsAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final int MIN_LEVELS = 5;
    private static final int MAX_LEVELS = 19;
    private static final int MAX_COST = 64;

    @FieldAccessor
    private ItemStack itemStack;

    @FieldAccessor
    private int baseEmeraldCost;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int villagerXp;

    @FieldAccessor
    private float priceMultiplier;

    public EnchantedItemForEmeraldsAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode tooltip = TooltipBuilder.branch((b) -> {
            b.add(utils.getValueTooltip(utils, UniformGenerator.between(MIN_LEVELS, MAX_LEVELS)).build(Lang.Value.LEVELS));
            b.add(utils.getValueTooltip(utils, false).build(Lang.Value.TREASURE));
        }).build(Lang.Functions.ENCHANT_WITH_LEVELS);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(Math.min(baseEmeraldCost + MIN_LEVELS, MAX_COST), Math.min(baseEmeraldCost + MAX_LEVELS, MAX_COST)),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(new ItemStack(itemStack.getItem())),
                new RangeValue(1),
                tooltip,
                maxUses,
                villagerXp,
                priceMultiplier,
                conditions
        );
    }
}
