package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import twilightforest.entity.passive.Bighorn;
import twilightforest.init.TFEntities;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;
import twilightforest.loot.functions.ModItemSwap;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TwilightForestCompat implements IGlmModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    @Override
    public String targetModId() {
        return TwilightForestLang.MOD_ID;
    }

    @Override
    public void registerCommon(ICommonRegistry registry) {
        registry.registerEntityVariants(TFEntities.BIGHORN_SHEEP.get(), TwilightForestCompat::getBighornVariants);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, GiantPickUsedCondition.class, GiantPickUsedConditionAccessor.class);
        PluginUtils.registerConditionTooltip(registry, ModExistsCondition.class, ModExistsConditionAccessor.class);
        registry.registerConditionTooltip(IsMinionCondition.class, TwilightForestCompat::getIsMinionTooltip);
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class, TwilightForestCompat::getUncraftingTableEnabledTooltip);

        registry.registerDestination(GiantPickUsedCondition.class, TwilightForestCompat::getGiantPickUsedDestination);
        PluginUtils.registerDestination(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);

        PluginUtils.registerFunctionTooltip(registry, ModItemSwap.class, ModItemSwapAccessor.class);

        PluginUtils.registerItemStackModifier(registry, ModItemSwap.class, ModItemSwapAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GiantToolGroupingModifier.class, GiantToolGroupingModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getIsMinionTooltip(IServerUtils utils, IsMinionCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, !cond.inverse())), TwilightForestLang.Conditions.IS_MINION);
    }

    @NotNull
    private static TooltipBuilder getUncraftingTableEnabledTooltip(IServerUtils ignoredUtils, UncraftingTableEnabledCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, TwilightForestLang.Conditions.UNCRAFTING_TABLE_ENABLED);
    }

    @NotNull
    private static Destination getGiantPickUsedDestination(IServerUtils ignoredUtils, GiantPickUsedCondition ignoredCond) {
        return new Destination.Blocks(Set.copyOf(GiantToolGroupingModifier.CONVERSIONS.keySet())::contains, false);
    }

    @NotNull
    private static List<Entity> getBighornVariants(Level level) {
        EntityType<Bighorn> type = TFEntities.BIGHORN_SHEEP.get();
        List<Entity> entities = new ArrayList<>();

        for (DyeColor color : DyeColor.values()) {
            Bighorn bighorn = createBighorn(type, level);

            if (bighorn != null) {
                bighorn.setColor(color);
                entities.add(bighorn);
            }
        }

        Bighorn bighorn = createBighorn(type, level);

        if (bighorn != null) {
            bighorn.setSheared(true);
            entities.add(bighorn);
        }

        return entities;
    }

    @Nullable
    private static Bighorn createBighorn(EntityType<Bighorn> type, Level level) {
        try {
            return type.create(level);
        } catch (Throwable e) {
            LOGGER.warn("Failed to create bighorn sheep: {}", e.getMessage(), e);
            return null;
        }
    }
}
