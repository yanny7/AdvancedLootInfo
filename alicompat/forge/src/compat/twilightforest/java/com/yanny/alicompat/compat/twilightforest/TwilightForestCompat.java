package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
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
        registry.registerConditionTooltip(GiantPickUsedCondition.class,
                (utils, condition) -> ReflectionUtils.copyClassData(GiantPickUsedConditionAccessor.class, condition, GiantPickUsedCondition.class).getTooltip(utils));
        registry.registerConditionTooltip(ModExistsCondition.class,
                (utils, condition) -> ReflectionUtils.copyClassData(ModExistsConditionAccessor.class, condition, ModExistsCondition.class).getTooltip(utils));
        registry.registerConditionTooltip(IsMinionCondition.class,
                (utils, condition) -> utils.getValueTooltip(utils, !condition.inverse()).key(TwilightForestLang.Conditions.IS_MINION));
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class,
                (ignoredUtils, ignoredCondition) -> TooltipBuilder.keyOnly(TwilightForestLang.Conditions.UNCRAFTING_TABLE_ENABLED));

        registry.registerDestination(GiantPickUsedCondition.class, (ignoredUtils, ignoredCondition) ->
                new Destination.Blocks(Set.copyOf(GiantToolGroupingModifier.CONVERSIONS.keySet()), false));

        registry.registerFunctionTooltip(ModItemSwap.class, (utils, function) -> accessor(function).getTooltip(utils));
        registry.registerItemStackModifier(ModItemSwap.class, (utils, function, itemStack) -> accessor(function).applyItemStackModifier(utils, itemStack));
        registry.registerItemCollector(ModItemSwap.class, (utils, items, function) -> accessor(function).collectItems(utils, items));
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GiantToolGroupingModifier.class, GiantToolGroupingModifierAccessor.class);
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

    @NotNull
    private static ModItemSwapAccessor accessor(ModItemSwap function) {
        return ReflectionUtils.copyClassData(ModItemSwapAccessor.class, function, ModItemSwap.class);
    }
}
