package com.yanny.alicompat.compat.gtceu;

import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ICountModifier;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import com.yanny.alicompat.accessor.IItemStackModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RandomWeightLootFunctionAccessor extends BaseAccessor<ChestGenHooks.RandomWeightLootFunction> implements IFunctionTooltip, ICountModifier, IItemStackModifier {
    @FieldAccessor
    private ItemStack stack;

    @FieldAccessor
    private int minAmount;

    @FieldAccessor
    private int maxAmount;

    public RandomWeightLootFunctionAccessor(ChestGenHooks.RandomWeightLootFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, stack).build(Lang.Branch.ITEM));
            b.add(utils.getValueTooltip(utils, new RangeValue(minAmount, maxAmount)).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, GregTechCEuModernLang.Functions.RANDOM_WEIGHT);
    }

    @Override
    public void applyCountModifier(IServerUtils utils, EnchantedRanges count) {
        count.modifyAllEntries((value) -> getCount());
    }

    @Override
    public ItemStack applyItemStackModifier(IServerUtils utils, ItemStack itemStack) {
        ItemStack newStack = itemStack.copy();
        CompoundTag tag = stack.getTag();

        if (stack.getDamageValue() != 0) {
            newStack.setDamageValue(stack.getDamageValue());
        }

        if (tag != null) {
            newStack.setTag(tag.copy());
        }

        return newStack;
    }

    @NotNull
    private RangeValue getCount() {
        if (minAmount == maxAmount) {
            return new RangeValue(minAmount);
        }

        return new RangeValue(Math.min(minAmount, stack.getMaxStackSize()), Math.min(maxAmount, stack.getMaxStackSize()));
    }
}
