package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import io.redspace.ironsspellbooks.loot.*;
import org.jetbrains.annotations.NotNull;

public class IronsSpellbooksCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return IronsSpellbooksLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(FurledMapLootFunction.class, IronsSpellbooksCompat::furledMapLootTooltip);
        registry.registerFunctionTooltip(RandomizeSpellFunction.class, RandomizeSpellFunctionAccessor::getTooltip);
        registry.registerFunctionTooltip(RandomizeRingEnhancementFunction.class, RandomizeRingEnhancementFunctionAccessor::getTooltip);

        registry.registerItemListing(WizardTrade.class, (utils, listing, condition) -> listing.getNode(utils, condition));
        WanderingTrades.register(registry);

        registry.registerTrades(WizardTrades.APOTHECARIST, WizardTrades::apothecarist, WizardTrades::apothecaristLevel);
        registry.registerTrades(WizardTrades.CRYOMANCER, WizardTrades::cryomancer, WizardTrades::cryomancerLevel);
        registry.registerTrades(WizardTrades.PRIEST, WizardTrades::priest, WizardTrades::priestLevel);
        registry.registerTrades(WizardTrades.PYROMANCER, WizardTrades::pyromancer, WizardTrades::pyromancerLevel);

        registry.registerValueTooltip(SpellFilter.class, SpellFilterAccessor::getTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AppendLootModifier.class, AppendLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceLootModifier.class, ReplaceLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder furledMapLootTooltip(IServerUtils utils, FurledMapLootFunction function) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, function.getDestination()).build(Lang.Value.DESTINATION));
            b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(function.getTranslation())).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, function.getDimension()).build(Lang.Value.DIMENSION));
        }, IronsSpellbooksLang.Functions.SET_FURLED_MAP);
    }
}
