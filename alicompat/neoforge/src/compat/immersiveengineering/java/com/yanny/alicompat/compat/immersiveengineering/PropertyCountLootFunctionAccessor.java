package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.util.loot.PropertyCountLootFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import org.jetbrains.annotations.NotNull;

public class PropertyCountLootFunctionAccessor extends BaseAccessor<PropertyCountLootFunction> implements IFunctionTooltip {
    @FieldAccessor
    private String propertyName;

    public PropertyCountLootFunctionAccessor(PropertyCountLootFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, propertyName).build(Lang.Value.PROPERTY));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, ImmersiveEngineeringLang.Functions.PROPERTY_COUNT);
    }
}
