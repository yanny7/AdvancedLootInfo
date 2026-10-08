package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import me.drex.villagerconfig.common.util.loot.function.SetDyeFunction;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SetDyeFunctionAccessor extends BaseAccessor<SetDyeFunction> implements IFunctionTooltip {
    @FieldAccessor
    private Optional<List<DyeColor>> dyeColors;
    @FieldAccessor
    private boolean add;

    public SetDyeFunctionAccessor(SetDyeFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, dyeColors).build(VillagerConfigLang.Branch.DYE_COLORS));
            b.add(utils.getValueTooltip(utils, add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, VillagerConfigLang.Functions.SET_DYE);
    }
}
