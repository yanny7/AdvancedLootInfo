package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
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
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import twilightforest.advancements.predicate.ItemColorPredicate;
import twilightforest.loot.LootingEnchantNumberProvider;
import twilightforest.loot.MultiplayerBasedAdditionLootFunction;
import twilightforest.loot.MultiplayerBasedNumberProvider;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TwilightForestCompat implements IGlmModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final RangeValue UNKNOWN_PLAYER_COUNT = new RangeValue(false, true);
    private static final String BIGHORN_SHEEP = "twilightforest:bighorn_sheep";

    @NotNull
    @Override
    public String targetModId() {
        return TwilightForestLang.MOD_ID;
    }

    @Override
    public void registerCommon(ICommonRegistry registry) {
        Optional<EntityType<?>> bighorn = EntityType.byString(BIGHORN_SHEEP);

        bighorn.ifPresent((type) -> registry.registerEntityVariants(type, (level) -> getBighornVariants(type, level)));
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(GiantPickUsedCondition.class, TwilightForestCompat::getGiantPickUsedTooltip);
        registry.registerConditionTooltip(IsMinionCondition.class, TwilightForestCompat::getIsMinionTooltip);
        PluginUtils.registerConditionTooltip(registry, ModExistsCondition.class, ModExistsConditionAccessor.class);
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class, TwilightForestCompat::getUncraftingTableEnabledTooltip);

        PluginUtils.registerFunctionTooltip(registry, MultiplayerBasedAdditionLootFunction.class, MultiplayerBasedAdditionAccessor.class);

        registry.registerNumberProvider(MultiplayerBasedNumberProvider.class, TwilightForestCompat::convertMultiplayerRolls);
        PluginUtils.registerNumberProvider(registry, LootingEnchantNumberProvider.class, LootingEnchantNumberProviderAccessor.class);

        registry.registerItemSubPredicateTooltip(ItemColorPredicate.class, TwilightForestCompat::getItemColorTooltip);

        registry.registerDestination(GiantPickUsedCondition.class, TwilightForestCompat::getGiantPickUsedDestination);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GiantToolGroupingModifier.class, GiantToolGroupingModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getGiantPickUsedTooltip(IServerUtils utils, GiantPickUsedCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.target())), TwilightForestLang.Conditions.GIANT_PICK_USED);
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
    private static TooltipBuilder getItemColorTooltip(IServerUtils utils, ItemColorPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.color())), TwilightForestLang.ItemSubPredicates.ITEM_COLOR);
    }

    @NotNull
    private static Destination getGiantPickUsedDestination(IServerUtils ignoredUtils, GiantPickUsedCondition ignoredCond) {
        return new Destination.Blocks(Set.copyOf(GiantToolGroupingModifier.CONVERSIONS.keySet()), false);
    }

    @NotNull
    private static RangeValue convertMultiplayerRolls(IServerUtils utils, MultiplayerBasedNumberProvider provider) {
        RangeValue defaultRolls = utils.convertNumber(utils, provider.defaultRolls());
        RangeValue perPlayer = utils.convertNumber(utils, provider.rollsPerPlayer());

        return defaultRolls.union(defaultRolls.add(perPlayer)).multiply(UNKNOWN_PLAYER_COUNT);
    }

    @NotNull
    private static List<Entity> getBighornVariants(EntityType<?> type, Level level) {
        List<Entity> entities = new ArrayList<>();

        for (DyeColor color : DyeColor.values()) {
            if (createBighorn(type, level) instanceof Sheep bighorn) {
                bighorn.setColor(color);
                entities.add(bighorn);
            }
        }

        if (createBighorn(type, level) instanceof Sheep bighorn) {
            bighorn.setSheared(true);
            entities.add(bighorn);
        }

        return entities;
    }

    @Nullable
    private static Entity createBighorn(EntityType<?> type, Level level) {
        try {
            return type.create(level);
        } catch (Throwable e) {
            LOGGER.warn("Failed to create bighorn sheep: {}", e.getMessage(), e);
            return null;
        }
    }
}
