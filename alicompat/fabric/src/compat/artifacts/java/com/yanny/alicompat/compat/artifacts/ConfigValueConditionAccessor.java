package com.yanny.alicompat.compat.artifacts;

import artifacts.config.value.Value;
import artifacts.loot.ConfigValueCondition;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import org.jetbrains.annotations.NotNull;

public class ConfigValueConditionAccessor extends BaseAccessor<ConfigValueCondition> implements IConditionTooltip {
    @FieldAccessor
    private Value<Boolean> value;

    public ConfigValueConditionAccessor(ConfigValueCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, value).build(ArtifactsLang.Value.CONFIG)),
                ArtifactsLang.Conditions.CONFIG_VALUE);
    }
}
