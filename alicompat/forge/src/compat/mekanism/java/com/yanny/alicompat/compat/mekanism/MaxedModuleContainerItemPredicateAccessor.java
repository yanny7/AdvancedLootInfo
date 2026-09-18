package com.yanny.alicompat.compat.mekanism;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class MaxedModuleContainerItemPredicateAccessor extends BaseAccessor<MaxedModuleContainerItemPredicate<?>> implements IValueTooltip {
    @FieldAccessor
    private Item item;

    public MaxedModuleContainerItemPredicateAccessor(MaxedModuleContainerItemPredicate<?> parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, item).key(MekanismLang.Value.MAXED_MODULE_CONTAINER);
    }
}
