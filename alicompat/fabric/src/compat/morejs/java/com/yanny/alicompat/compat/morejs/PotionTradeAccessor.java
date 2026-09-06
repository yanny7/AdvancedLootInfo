package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.PotionTrade;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class PotionTradeAccessor extends BaseAccessor<PotionTrade> implements IItemListing {
    @FieldAccessor
    private TradeItem firstInput;

    @FieldAccessor
    private TradeItem secondInput;

    @FieldAccessor
    private List<Potion> potions;

    @FieldAccessor
    private Item itemForPotion;

    @FieldAccessor
    private boolean onlyBrewablePotion;

    @FieldAccessor
    private boolean noBrewablePotion;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int villagerExperience;

    @FieldAccessor
    private float priceMultiplier;

    public PotionTradeAccessor(PotionTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TradeItemAccessor first = TradeItemAccessor.of(firstInput);
        TradeItemAccessor second = TradeItemAccessor.of(secondInput);
        List<Potion> allowed = potions.stream().filter(this::isAllowed).toList();
        long allPotions = BuiltInRegistries.POTION.stream().filter(this::isAllowed).count();
        ItemStack result = new ItemStack(itemForPotion);
        TooltipNode tooltip;

        if (allowed.size() == 1) {
            result = PotionUtils.setPotion(result, allowed.get(0));
            tooltip = TooltipNode.empty();
        } else if (allowed.isEmpty() || allowed.size() >= allPotions) {
            tooltip = TooltipBuilder.keyOnly(MoreJSLang.Functions.RANDOM_POTION).build();
        } else {
            tooltip = TooltipBuilder.array((b) ->
                    allowed.forEach((potion) -> b.add(utils.getValueTooltip(utils, potion).build(Lang.Value.POTION)))
            ).build();
        }

        return new ItemsToItemsNode(
                utils,
                Either.left(first.getStack()),
                first.getCount(),
                TooltipNode.empty(),
                Either.left(second.getStack()),
                second.getCount(),
                TooltipNode.empty(),
                Either.left(result),
                new RangeValue(1),
                tooltip,
                maxUses,
                villagerExperience,
                priceMultiplier,
                conditions
        );
    }

    private boolean isAllowed(Potion potion) {
        if (potion.getEffects().isEmpty()) {
            return false;
        }
        if (onlyBrewablePotion) {
            return PotionBrewing.isBrewablePotion(potion);
        }
        if (noBrewablePotion) {
            return !PotionBrewing.isBrewablePotion(potion);
        }

        return true;
    }
}
