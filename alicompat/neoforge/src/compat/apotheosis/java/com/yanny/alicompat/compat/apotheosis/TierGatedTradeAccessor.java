package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import dev.shadowsoffire.apotheosis.loot.functions.TierGatedTrade;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import org.jetbrains.annotations.NotNull;

public class TierGatedTradeAccessor extends BaseAccessor<TierGatedTrade> implements IFunctionTooltip {
    @FieldAccessor
    private WorldTier minTier;

    public TierGatedTradeAccessor(TierGatedTrade parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, minTier).build(ApotheosisLang.Value.MIN_WORLD_TIER));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, ApotheosisLang.Functions.TIER_GATED_TRADE);
    }
}
