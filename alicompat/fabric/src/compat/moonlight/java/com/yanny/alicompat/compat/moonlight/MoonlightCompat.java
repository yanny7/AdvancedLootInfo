package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.mehvahdjukaar.moonlight.core.loot.ConfigItemPoolEntry;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPoolEntry;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.mehvahdjukaar.moonlight.core.loot.PatternMatchLootItemCondition;
import net.mehvahdjukaar.moonlight.core.loot.ResourceLootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MoonlightCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return MoonlightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, OptionalItemPoolEntry.class, OptionalItemPoolEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, OptionalItemPoolEntry.class, OptionalItemPoolEntryAccessor.class);
        PluginUtils.registerEntry(registry, ConfigItemPoolEntry.class, ConfigItemPoolEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, ConfigItemPoolEntry.class, ConfigItemPoolEntryAccessor.class);

        PluginUtils.registerConditionTooltip(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);
        registry.registerConditionTooltip(ResourceLootItemCondition.class, MoonlightCompat::getDataConditionsTooltip);
        registry.registerConditionTooltip(PatternMatchLootItemCondition.class, MoonlightCompat::getPatternMatchTooltip);

        registry.registerValueTooltip(Pattern.class, MoonlightCompat::getPatternTooltip);

        PluginUtils.registerPageResolver(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getDataConditionsTooltip(IServerUtils utils, ResourceLootItemCondition cond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, MoonlightLang.Conditions.DATA_CONDITIONS);
    }

    @NotNull
    private static TooltipBuilder getPatternMatchTooltip(IServerUtils utils, PatternMatchLootItemCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.patterns()));
            b.showEmpty();
        }, MoonlightLang.Conditions.LOOT_TABLE_ID_PATTERN);
    }

    @NotNull
    private static TooltipBuilder getPatternTooltip(IServerUtils ignoredUtils, Pattern pattern) {
        return TooltipBuilder.value(pattern.pattern());
    }
}
