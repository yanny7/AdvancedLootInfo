package com.yanny.alicompat.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.loot.DungeonLootEnhancerModifier;
import com.hollingsworth.arsnouveau.setup.registry.EntitySubPredicatesRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import org.jetbrains.annotations.NotNull;

public class ArsNouveauCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ArsNouveauLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerEntitySubPredicateTooltip(EntitySubPredicatesRegistry.PercentHealthEqualOrLowerPredicate.CODEC, ArsNouveauCompat::getPercentHealthEqualOrLowerTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, DungeonLootEnhancerModifier.class, DungeonLootEnhancerModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getPercentHealthEqualOrLowerTooltip(IServerUtils utils, EntitySubPredicatesRegistry.PercentHealthEqualOrLowerPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.threshold()).build(ArsNouveauLang.Value.THRESHOLD)), ArsNouveauLang.EntitySubPredicates.PERCENT_HEALTH_EQUAL_OR_LOWER);
    }
}
