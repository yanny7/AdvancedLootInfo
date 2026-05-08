package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

public class ConditionTooltipUtils {
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.####");

    @NotNull
    public static TooltipNode getAllOfTooltip(IServerUtils utils, AllOfCondition cond) {
        return getSubConditionsTooltip(utils, cond.terms).build("ali.type.condition.all_of");
    }

    @NotNull
    public static TooltipNode getAnyOfTooltip(IServerUtils utils, AnyOfCondition cond) {
        return getSubConditionsTooltip(utils, cond.terms).build("ali.type.condition.any_of");
    }

    @NotNull
    public static TooltipNode getBlockStatePropertyTooltip(IServerUtils utils, LootItemBlockStatePropertyCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.block()).build("ali.property.value.block"))
                        .add(utils.getValueTooltip(utils, cond.properties()).build("ali.property.branch.properties"))
                )
                .build("ali.type.condition.block_state_property");
    }

    @NotNull
    public static TooltipNode getDamageSourcePropertiesTooltip(IServerUtils utils, DamageSourceCondition cond) {
        return utils.getValueTooltip(utils, cond.predicate()).build("ali.type.condition.damage_source_properties");
    }

    @NotNull
    public static ITooltipNode getEnchantActiveCheckTooltip(IServerUtils utils, EnchantmentActiveCheck cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.active()).build("ali.property.value.active"))
                )
                .build("ali.type.condition.enchantment_active_check");
    }

    @NotNull
    public static TooltipNode getEntityPropertiesTooltip(IServerUtils utils, LootItemEntityPropertyCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.entityTarget()).build("ali.property.value.target"))
                        .add(utils.getValueTooltip(utils, cond.predicate()).build("ali.property.branch.predicate"))
                )
                .build("ali.type.condition.entity_properties");
    }

    @NotNull
    public static TooltipNode getEntityScoresTooltip(IServerUtils utils, EntityHasScoreCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.entityTarget()).build("ali.property.value.target"))
                        .add(getMapTooltip(utils, cond.scores(), GenericTooltipUtils::getIntRangeEntryTooltip).build("ali.property.branch.scores"))
                )
                .build("ali.type.condition.entity_scores");
    }

    @NotNull
    public static TooltipNode getInvertedTooltip(IServerUtils utils, InvertedLootItemCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getConditionTooltip(utils, cond.term()))
                )
                .build("ali.type.condition.inverted");
    }

    @NotNull
    public static TooltipNode getKilledByPlayerTooltip(IServerUtils ignoredUtils, LootItemKilledByPlayerCondition ignoredCond) {
        return TooltipBuilder.keyOnly("ali.type.condition.killed_by_player").build();
    }

    @NotNull
    public static TooltipNode getLocationCheckTooltip(IServerUtils utils, LocationCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.predicate()).build("ali.property.branch.location"));

            if (!cond.offset().equals(BlockPos.ZERO)) {
                b.add(utils.getValueTooltip(utils, cond.offset()).build("ali.property.multi.offset"));
            }
        }).build("ali.type.condition.location_check");
    }

    @NotNull
    public static TooltipNode getMatchToolTooltip(IServerUtils utils, MatchTool cond) {
        return utils.getValueTooltip(utils, cond.predicate()).build("ali.type.condition.match_tool");
    }

    @NotNull
    public static TooltipNode getRandomChanceTooltip(IServerUtils utils, LootItemRandomChanceCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.chance()).build("ali.property.value.chance"))
                )
                .isAdvancedTooltip()
                .build("ali.type.condition.random_chance");
    }

    @NotNull
    public static TooltipNode getRandomChanceWithLootingTooltip(IServerUtils utils, LootItemRandomChanceWithLootingCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.unenchantedChance()).build("ali.property.value.unenchanted_chance"))
                        .add(utils.getValueTooltip(utils, cond.enchantedChance()).build("ali.property.branch.enchanted_chance"))
                        .add(utils.getValueTooltip(utils, cond.enchantment()).build("ali.property.value.enchantment"))
                )
                .isAdvancedTooltip()
                .build("ali.type.condition.random_chance_with_enchanted_bonus");
    }

    @NotNull
    public static TooltipNode getReferenceTooltip(IServerUtils utils, ConditionReference cond) {
        return utils.getValueTooltip(utils, cond.name()).build("ali.type.condition.reference");
    }

    @NotNull
    public static TooltipNode getSurvivesExplosionTooltip(IServerUtils ignoredUtils, ExplosionCondition ignoredCond) {
        return TooltipBuilder.keyOnly("ali.type.condition.survives_explosion").build();
    }

    @NotNull
    public static TooltipNode getTableBonusTooltip(IServerUtils utils, BonusLevelTableCondition cond) {
        List<String> list = cond.values().stream().mapToDouble(aFloat -> aFloat).mapToObj(FLOAT_FORMAT::format).toList();

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.enchantment()).build("ali.property.value.enchantment"))
                        .add(utils.getValueTooltip(utils, list.toString()).build("ali.property.value.values"))
                )
                .isAdvancedTooltip()
                .build("ali.type.condition.table_bonus");
    }

    @NotNull
    public static TooltipNode getTimeCheckTooltip(IServerUtils utils, TimeCheck cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.period()).build("ali.property.value.period"))
                        .add(utils.getValueTooltip(utils, cond.value()).build("ali.property.value.value"))
                )
                .build("ali.type.condition.time_check");
    }

    @NotNull
    public static TooltipNode getValueCheckTooltip(IServerUtils utils, ValueCheckCondition cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.provider()).build("ali.property.value.provider"))
                        .add(utils.getValueTooltip(utils, cond.range()).build("ali.property.value.range"))
                )
                .build("ali.type.condition.value_check");
    }

    @NotNull
    public static TooltipNode getWeatherCheckTooltip(IServerUtils utils, WeatherCheck cond) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.isRaining()).build("ali.property.value.is_raining"))
                        .add(utils.getValueTooltip(utils, cond.isThundering()).build("ali.property.value.is_thundering"))
                )
                .build("ali.type.condition.weather_check");
    }
}
