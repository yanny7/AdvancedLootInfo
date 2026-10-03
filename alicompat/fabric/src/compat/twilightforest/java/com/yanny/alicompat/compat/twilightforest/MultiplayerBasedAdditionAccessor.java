package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.MultiplayerBasedAdditionLootFunction;

public class MultiplayerBasedAdditionAccessor extends BaseAccessor<MultiplayerBasedAdditionLootFunction> implements IFunctionTooltip {
    @FieldAccessor
    private NumberProvider value;

    public MultiplayerBasedAdditionAccessor(MultiplayerBasedAdditionLootFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b
                .add(TooltipUtils.getNumberTooltip(utils, value).build(TwilightForestLang.Value.EXTRA_COUNT_PER_PLAYER))
                .add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES)),
                TwilightForestLang.Functions.MULTIPLAYER_ADDITION);
    }
}
