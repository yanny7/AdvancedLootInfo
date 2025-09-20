package com.yanny.ali.plugin.mods.the_bumblezone;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.the_bumblezone.conditions.EssenceOnlySpawn;
import com.yanny.ali.plugin.mods.the_bumblezone.functions.DropContainerLoot;
import com.yanny.ali.plugin.mods.the_bumblezone.functions.HoneyCompassLocateStructure;
import com.yanny.ali.plugin.mods.the_bumblezone.functions.TagItemRemovals;
import com.yanny.ali.plugin.mods.the_bumblezone.functions.UniquifyIfHasItems;
import com.yanny.ali.plugin.mods.the_bumblezone.predicates.HoneySlimePredicate;

// @AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "the_bumblezone";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, DropContainerLoot.class);
        PluginUtils.registerFunctionTooltip(registry, HoneyCompassLocateStructure.class);
        PluginUtils.registerFunctionTooltip(registry, TagItemRemovals.class);
        PluginUtils.registerFunctionTooltip(registry, UniquifyIfHasItems.class);

        PluginUtils.registerConditionTooltip(registry, EssenceOnlySpawn.class);

        PluginUtils.registerEntitySubPredicateTooltip(registry, HoneySlimePredicate.class, HoneySlimePredicate.CODEC);
    }
}
