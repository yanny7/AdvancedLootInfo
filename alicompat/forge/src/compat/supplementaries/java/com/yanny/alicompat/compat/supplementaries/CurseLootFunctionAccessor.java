package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import org.jetbrains.annotations.NotNull;

public class CurseLootFunctionAccessor extends BaseAccessor<CurseLootFunction> implements IFunctionTooltip {
    @FieldAccessor
    private double chance;

    public CurseLootFunctionAccessor(CurseLootFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, chance).build(Lang.Value.PROBABILITY));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, SupplementariesLang.Functions.CURSE_LOOT);
    }
}
