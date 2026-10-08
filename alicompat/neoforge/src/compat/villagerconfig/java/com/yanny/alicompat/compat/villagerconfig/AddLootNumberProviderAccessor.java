package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.INumberProvider;
import me.drex.villagerconfig.common.util.loot.number.AddLootNumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddLootNumberProviderAccessor extends BaseAccessor<AddLootNumberProvider> implements INumberProvider {
    private final List<NumberProvider> addends;

    public AddLootNumberProviderAccessor(AddLootNumberProvider parent) {
        super(parent);
        addends = parent.addends();
    }

    @NotNull
    @Override
    public NumberExpr convertNumber(IServerUtils utils, List<TooltipNode> conditions) {
        return addends.stream().map((a) -> utils.convertNumber(utils, a, conditions)).reduce(NumberExpr.constant(0), NumberExpr::add);
    }
}
