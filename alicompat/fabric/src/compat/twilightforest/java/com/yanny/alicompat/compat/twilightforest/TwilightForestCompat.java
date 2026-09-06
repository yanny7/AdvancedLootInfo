package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.ReflectionUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TwilightForestCompat implements IModCompat {
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
        registry.registerConditionTooltip(GiantPickUsedCondition.class,
                (utils, condition) -> utils.getValueTooltip(utils, condition.target()).key(TwilightForestLang.Conditions.GIANT_PICK_USED));
        registry.registerConditionTooltip(IsMinionCondition.class,
                (utils, condition) -> utils.getValueTooltip(utils, !condition.inverse()).key(TwilightForestLang.Conditions.IS_MINION));
        registry.registerConditionTooltip(ModExistsCondition.class,
                (utils, condition) -> ReflectionUtils.copyClassData(ModExistsConditionAccessor.class, condition, ModExistsCondition.class).getTooltip(utils));
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class,
                (ignoredUtils, ignoredCondition) -> TooltipBuilder.keyOnly(TwilightForestLang.Conditions.UNCRAFTING_TABLE_ENABLED));

        registry.registerFunctionTooltip(MultiplayerBasedAdditionLootFunction.class, (utils, function) ->
                ReflectionUtils.copyClassData(MultiplayerBasedAdditionAccessor.class, function, MultiplayerBasedAdditionLootFunction.class).getTooltip(utils));

        registry.registerNumberProvider(MultiplayerBasedNumberProvider.class, TwilightForestCompat::convertMultiplayerRolls);
        registry.registerNumberProvider(LootingEnchantNumberProvider.class, (utils, provider) ->
                ReflectionUtils.copyClassData(LootingEnchantNumberProviderAccessor.class, provider, LootingEnchantNumberProvider.class).convertNumber(utils));

        registry.registerItemSubPredicateTooltip(ItemColorPredicate.class,
                (utils, predicate) -> utils.getValueTooltip(utils, predicate.color()).key(TwilightForestLang.ItemSubPredicates.ITEM_COLOR));
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
