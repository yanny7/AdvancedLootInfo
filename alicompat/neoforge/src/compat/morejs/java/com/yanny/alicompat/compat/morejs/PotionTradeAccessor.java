package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.PotionTrade;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class PotionTradeAccessor extends BaseAccessor<PotionTrade> implements IItemListing {
    @FieldAccessor
    private TradeItem firstInput;

    @FieldAccessor
    private TradeItem secondInput;

    @Nullable
    @FieldAccessor
    private List<Holder<Potion>> potions;

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
        PotionBrewing brewing = utils.getServerLevel().potionBrewing();
        Stream<Holder<Potion>> declared = potions == null ? BuiltInRegistries.POTION.holders().map((h) -> h) : potions.stream();
        List<Holder<Potion>> allowed = declared.filter((potion) -> isAllowed(brewing, potion)).toList();
        long allPotions = BuiltInRegistries.POTION.holders().filter((potion) -> isAllowed(brewing, potion)).count();
        IDataNode result;

        if (allowed.isEmpty() || allowed.size() >= allPotions) {
            result = TradeUtils.getItemSlotNode(List.of(new ItemStack(itemForPotion)), NumberExpr.constant(1), TooltipBuilder.keyOnly(MoreJSLang.Functions.RANDOM_POTION).build());
        } else {
            result = TradeUtils.getItemSlotNode(allowed.stream().map(this::potionStack).toList(), NumberExpr.constant(1), TooltipNode.empty());
        }

        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(first.getStack()), first.getCount(), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(List.of(second.getStack()), second.getCount(), TooltipNode.empty()),
                result,
                maxUses,
                villagerExperience,
                priceMultiplier,
                conditions
        );
    }

    @NotNull
    private ItemStack potionStack(Holder<Potion> potion) {
        ItemStack stack = new ItemStack(itemForPotion);

        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    private boolean isAllowed(PotionBrewing brewing, Holder<Potion> potion) {
        if (potion.value().getEffects().isEmpty()) {
            return false;
        }
        if (onlyBrewablePotion) {
            return brewing.isBrewablePotion(potion);
        }
        if (noBrewablePotion) {
            return !brewing.isBrewablePotion(potion);
        }

        return true;
    }
}
