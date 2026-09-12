package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.data.ConfigSerializationUtil;
import com.aetherteam.aether.loot.conditions.ConfigEnabled;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public class ConfigEnabledAccessor extends BaseAccessor<ConfigEnabled> implements IConditionTooltip {
    @FieldAccessor
    private ModConfigSpec.ConfigValue<Boolean> config;

    public ConfigEnabledAccessor(ConfigEnabled parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, ConfigSerializationUtil.serialize(config))
                .build(AetherLang.Value.CONFIG)), AetherLang.Conditions.CONFIG_ENABLED);
    }
}
