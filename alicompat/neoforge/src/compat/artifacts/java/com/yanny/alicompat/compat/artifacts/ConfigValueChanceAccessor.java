package com.yanny.alicompat.compat.artifacts;

import artifacts.loot.ConfigValueChance;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IChanceModifier;
import com.yanny.alicompat.accessor.IConditionTooltip;
import org.jetbrains.annotations.NotNull;

public class ConfigValueChanceAccessor extends BaseAccessor<ConfigValueChance> implements IConditionTooltip, IChanceModifier {
    @FieldAccessor(clazz = ChanceConfigAccessor.class)
    private ChanceConfigAccessor chanceConfig;

    public ConfigValueChanceAccessor(ConfigValueChance parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, chanceConfig.getName()).build(ArtifactsLang.Value.CONFIG)),
                ArtifactsLang.Conditions.CONFIG_VALUE_CHANCE);
    }

    @Override
    public void applyChanceModifier(IServerUtils ignoredUtils, EnchantedRanges chance) {
        chance.modifyAllEntries((range) -> range.multiply(chanceConfig.getChance()));
    }
}
