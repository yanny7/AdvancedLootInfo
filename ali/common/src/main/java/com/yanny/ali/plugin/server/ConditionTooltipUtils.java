package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

public class ConditionTooltipUtils {
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.####");

    @NotNull
    public static TooltipBuilder getAllOfTooltip(IServerUtils utils, AllOfCondition cond) {
        return getSubConditionsTooltip(utils, Arrays.asList(cond.terms)).key("ali.type.condition.all_of");
    }

    @NotNull
    public static TooltipBuilder getAnyOfTooltip(IServerUtils utils, AnyOfCondition cond) {
        return getSubConditionsTooltip(utils, Arrays.asList(cond.terms)).key("ali.type.condition.any_of");
    }

    @NotNull
    public static TooltipBuilder getBlockStatePropertyTooltip(IServerUtils utils, LootItemBlockStatePropertyCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.block).build("ali.property.value.block"));
            b.add(utils.getValueTooltip(utils, cond.properties).build("ali.property.branch.properties"));
        }).key("ali.type.condition.block_state_property");
    }

    @NotNull
    public static TooltipBuilder getDamageSourcePropertiesTooltip(IServerUtils utils, DamageSourceCondition cond) {
        return utils.getValueTooltip(utils, cond.predicate).key("ali.type.condition.damage_source_properties");
    }

    @NotNull
    public static TooltipBuilder getEntityPropertiesTooltip(IServerUtils utils, LootItemEntityPropertyCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.entityTarget).build("ali.property.value.target"));
            b.add(utils.getValueTooltip(utils, cond.predicate).build("ali.property.branch.predicate"));
        }).key("ali.type.condition.entity_properties");
    }

    @NotNull
    public static TooltipBuilder getEntityScoresTooltip(IServerUtils utils, EntityHasScoreCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.entityTarget).build("ali.property.value.target"));
            b.add(getMapTooltip(utils, cond.scores, GenericTooltipUtils::getIntRangeEntryTooltip).build("ali.property.branch.scores"));
        }).key("ali.type.condition.entity_scores");
    }

    @NotNull
    public static TooltipBuilder getInvertedTooltip(IServerUtils utils, InvertedLootItemCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getConditionTooltip(utils, cond.term)))
                .key("ali.type.condition.inverted");
    }

    @NotNull
    public static TooltipBuilder getKilledByPlayerTooltip(IServerUtils ignoredUtils, LootItemKilledByPlayerCondition ignoredCond) {
        return TooltipBuilder.keyOnly("ali.type.condition.killed_by_player");
    }

    @NotNull
    public static TooltipBuilder getLocationCheckTooltip(IServerUtils utils, LocationCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.predicate).build("ali.property.branch.location"));

            if (!cond.offset.equals(BlockPos.ZERO)) {
                b.add(utils.getValueTooltip(utils, cond.offset).build("ali.property.multi.offset"));
            }
        }).key("ali.type.condition.location_check");
    }

    @NotNull
    public static TooltipBuilder getMatchToolTooltip(IServerUtils utils, MatchTool cond) {
        return utils.getValueTooltip(utils, cond.predicate).key("ali.type.condition.match_tool");
    }

    @NotNull
    public static TooltipBuilder getRandomChanceTooltip(IServerUtils utils, LootItemRandomChanceCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.probability).build("ali.property.value.probability")))
                .isAdvancedTooltip().key("ali.type.condition.random_chance");
    }

    @NotNull
    public static TooltipBuilder getRandomChanceWithLootingTooltip(IServerUtils utils, LootItemRandomChanceWithLootingCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.percent).build("ali.property.value.percent"));
            b.add(utils.getValueTooltip(utils, cond.lootingMultiplier).build("ali.property.value.multiplier"));
        }).isAdvancedTooltip().key("ali.type.condition.random_chance_with_looting");
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(IServerUtils utils, ConditionReference cond) {
        return utils.getValueTooltip(utils, cond.name).key("ali.type.condition.reference");
    }

    @NotNull
    public static TooltipBuilder getSurvivesExplosionTooltip(IServerUtils ignoredUtils, ExplosionCondition ignoredCond) {
        return TooltipBuilder.keyOnly("ali.type.condition.survives_explosion");
    }

    @NotNull
    public static TooltipBuilder getTableBonusTooltip(IServerUtils utils, BonusLevelTableCondition cond) {
        List<String> list = IntStream.range(0, cond.values.length).mapToDouble(i -> cond.values[i]).mapToObj(FLOAT_FORMAT::format).toList();

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.enchantment).build("ali.property.value.enchantment"));
            b.add(utils.getValueTooltip(utils, list.toString()).build("ali.property.value.values"));
        }).isAdvancedTooltip().key("ali.type.condition.table_bonus");
    }

    @NotNull
    public static TooltipBuilder getTimeCheckTooltip(IServerUtils utils, TimeCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.period).build("ali.property.value.period"));
            b.add(utils.getValueTooltip(utils, cond.value).build("ali.property.value.value"));
        }).key("ali.type.condition.time_check");
    }

    @NotNull
    public static TooltipBuilder getValueCheckTooltip(IServerUtils utils, ValueCheckCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.provider).build("ali.property.value.provider"));
            b.add(utils.getValueTooltip(utils, cond.range).build("ali.property.value.range"));
        }).key("ali.type.condition.value_check");
    }

    @NotNull
    public static TooltipBuilder getWeatherCheckTooltip(IServerUtils utils, WeatherCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.isRaining).build("ali.property.value.is_raining"));
            b.add(utils.getValueTooltip(utils, cond.isThundering).build("ali.property.value.is_thundering"));
        }).key("ali.type.condition.weather_check");
    }
}
