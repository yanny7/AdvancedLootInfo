package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionItemCollector;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import com.yanny.alicompat.accessor.IItemStackModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import twilightforest.loot.functions.ModItemSwap;

import java.util.List;

public class ModItemSwapAccessor extends BaseAccessor<ModItemSwap> implements IFunctionTooltip, IItemStackModifier, IFunctionItemCollector {
    @FieldAccessor
    private Item item;
    @FieldAccessor
    private Item oldItem;

    public ModItemSwapAccessor(ModItemSwap parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, item).build(Lang.Value.ITEM));
            b.add(utils.getValueTooltip(utils, oldItem).build(TwilightForestLang.Value.DEFAULT_ITEM));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, TwilightForestLang.Functions.MOD_ITEM_SWAP);
    }

    @Override
    public ItemStack applyItemStackModifier(IServerUtils utils, ItemStack itemStack) {
        ItemStack newStack = new ItemStack(item, itemStack.getCount());

        newStack.setTag(itemStack.getTag());
        return newStack;
    }

    @Override
    public List<Item> collectItems(IServerUtils utils, List<Item> items) {
        return List.of(item);
    }
}
