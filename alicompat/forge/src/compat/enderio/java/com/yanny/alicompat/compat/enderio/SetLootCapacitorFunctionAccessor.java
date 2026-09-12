package com.yanny.alicompat.compat.enderio;

import com.enderio.base.common.loot.SetLootCapacitorFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

public class SetLootCapacitorFunctionAccessor extends BaseAccessor<SetLootCapacitorFunction> implements IFunctionTooltip {
    @FieldAccessor
    private NumberProvider range;

    public SetLootCapacitorFunctionAccessor(SetLootCapacitorFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, range).build(Lang.Value.RANGE));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, EnderIoLang.Functions.SET_LOOT_CAPACITOR);
    }
}
