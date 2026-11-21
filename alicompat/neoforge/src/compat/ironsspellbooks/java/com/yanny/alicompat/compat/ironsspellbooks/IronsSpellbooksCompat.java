package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import io.redspace.ironsspellbooks.loot.*;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IronsSpellbooksCompat implements IGlmModCompat {
    private static final List<String> BOSS_LOOT_SUFFIXES = List.of("_per_player", "_ominous", "_per_player_ominous");
    private static final List<String> APPENDED_LOOT_TABLES = List.of("entities/additional_blaze_loot", "entities/additional_dragon_loot",
            "entities/additional_evoker_loot", "entities/additional_hoglin_loot", "entities/additional_stray_loot");

    @NotNull
    @Override
    public String targetModId() {
        return IronsSpellbooksLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(FurledMapLootFunction.class, IronsSpellbooksCompat::furledMapLootTooltip);
        PluginUtils.registerFunctionTooltip(registry, RandomizeSpellFunction.class, RandomizeSpellFunctionAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, RandomizeRingEnhancementFunction.class, RandomizeRingEnhancementFunctionAccessor.class);

        PluginUtils.registerSelfItemListing(registry, WizardTrade.class);
        WanderingTrades.register(registry);

        registry.registerTrades(WizardTrades.APOTHECARIST, BuiltInRegistries.ENTITY_TYPE.get(WizardTrades.APOTHECARIST), WizardTrades::apothecarist, WizardTrades::apothecaristLevel);
        registry.registerTrades(WizardTrades.CRYOMANCER, BuiltInRegistries.ENTITY_TYPE.get(WizardTrades.CRYOMANCER), WizardTrades::cryomancer, WizardTrades::cryomancerLevel);
        registry.registerTrades(WizardTrades.PRIEST, BuiltInRegistries.ENTITY_TYPE.get(WizardTrades.PRIEST), WizardTrades::priest, WizardTrades::priestLevel);
        registry.registerTrades(WizardTrades.PYROMANCER, BuiltInRegistries.ENTITY_TYPE.get(WizardTrades.PYROMANCER), WizardTrades::pyromancer, WizardTrades::pyromancerLevel);

        PluginUtils.registerValueTooltip(registry, SpellFilter.class, SpellFilterAccessor.class);

        registerBossLootTables(registry, EntityRegistry.DEAD_KING.get());
        registerBossLootTables(registry, EntityRegistry.FIRE_BOSS.get());
        APPENDED_LOOT_TABLES.forEach((path) -> registry.registerLootTableReference(ResourceLocation.fromNamespaceAndPath(IronsSpellbooksLang.MOD_ID, path)));
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AppendLootModifier.class, AppendLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceLootModifier.class, ReplaceLootModifierAccessor.class);
    }

    private static void registerBossLootTables(IServerRegistry registry, EntityType<?> type) {
        BOSS_LOOT_SUFFIXES.forEach((suffix) -> registry.registerEntityLootTable(type, type.getDefaultLootTable().location().withSuffix(suffix)));
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
