package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.INumberProvider;
import me.drex.villagerconfig.util.loot.number.MultiplyLootNumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MultiplyLootNumberProviderAccessor extends BaseAccessor<MultiplyLootNumberProvider> implements INumberProvider {
    @FieldAccessor
    private NumberProvider[] factors;

    public MultiplyLootNumberProviderAccessor(MultiplyLootNumberProvider parent) {
        super(parent);
    }

    @NotNull
    @Override
    public NumberExpr convertNumber(IServerUtils utils, List<TooltipNode> conditions) {
        return Arrays.stream(factors).map((a) -> utils.convertNumber(utils, a, conditions)).reduce(NumberExpr.constant(1), NumberExpr::mul);
    }
}
