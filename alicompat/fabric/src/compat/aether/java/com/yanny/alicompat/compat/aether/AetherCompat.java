package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.conditions.ConfigEnabled;
import com.aetherteam.aether.loot.functions.DoubleDrops;
import com.aetherteam.aether.loot.functions.SpawnTNT;
import com.aetherteam.aether.loot.functions.SpawnXP;
import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import com.aetherteam.aether.loot.modifiers.AetherLootTableModifications;
import com.aetherteam.aether.loot.modifiers.DoubleDropsModifier;
import com.aetherteam.aether.loot.modifiers.EnchantedGrassModifier;
import com.aetherteam.aether.loot.modifiers.GlovesLootModifier;
import com.aetherteam.aether.loot.modifiers.PigDropsModifier;
import com.aetherteam.aether.loot.modifiers.RemoveSeedsModifier;
import com.aetherteam.aetherfabric.common.loot.IGlobalLootModifier;
import com.aetherteam.aetherfabric.common.loot.LootModifier;
import com.google.gson.JsonElement;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierCollector;
import com.yanny.ali.plugin.glm.GlobalLootModifierWrapper;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IGlobalLootModifierWrapper;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AetherCompat implements IGlmModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    @Override
    public String targetModId() {
        return AetherLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, ConfigEnabled.class, ConfigEnabledAccessor.class);
        PluginUtils.registerConditionTooltip(registry, AetherLootTableModifications.LootTableCondition.class, LootTableConditionAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, DoubleDrops.class, DoubleDropsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, SpawnTNT.class, SpawnTNTAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, SpawnXP.class, SpawnXPAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, WhirlwindSpawnEntity.class, WhirlwindSpawnEntityAccessor.class);

        PluginUtils.registerDestination(registry, AetherLootTableModifications.LootTableCondition.class, LootTableConditionAccessor.class);

        registry.registerValueTooltip(IntProvider.class, AetherCompat::getIntProviderTooltip);

        registry.registerLootModifiers(AetherCompat::registerLootModifiers);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, DoubleDropsModifier.class, DoubleDropsModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, EnchantedGrassModifier.class, EnchantedGrassModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GlovesLootModifier.class, GlovesLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, PigDropsModifier.class, PigDropsModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, RemoveSeedsModifier.class, RemoveSeedsModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        return utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()));
    }

    @NotNull
    private static List<ILootModifier<?>> registerLootModifiers(IServerUtils utils) {
        List<IGlobalLootModifierWrapper> modifiers = new ArrayList<>();

        AetherLootTableModifications.LOOT_MODIFIERS.forEach((id, factory) -> {
            try {
                modifiers.add(wrap(id, factory.apply(utils.lookupProvider())));
            } catch (Throwable e) {
                LOGGER.warn("Failed to create GLM {} with error {}", id, e.getMessage(), e);
            }
        });

        return GlobalLootModifierCollector.collect(utils, modifiers);
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(ResourceLocation id, IGlobalLootModifier modifier) {
        return new GlobalLootModifierWrapper(
                id,
                modifier,
                LootModifier.class,
                () -> Arrays.asList(ReflectionUtils.copyClassData(LootModifierAccessor.class, modifier, LootModifier.class).getConditions()),
                AetherCompat::serialize
        );
    }

    @NotNull
    private static JsonElement serialize() {
        throw new IllegalStateException("Not implemented");
    }
}
