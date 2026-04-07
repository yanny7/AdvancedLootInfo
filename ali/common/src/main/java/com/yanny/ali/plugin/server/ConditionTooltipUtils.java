package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
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
    public static ITooltipNode getAllOfTooltip(IServerUtils utils, AllOfCondition cond) {
        return getSubConditionsTooltip(utils, Arrays.asList(cond.terms)).build("ali.type.condition.all_of");
    }

    @NotNull
    public static ITooltipNode getAnyOfTooltip(IServerUtils utils, AnyOfCondition cond) {
        return getSubConditionsTooltip(utils, Arrays.asList(cond.terms)).build("ali.type.condition.any_of");
    }

    @NotNull
    public static ITooltipNode getBlockStatePropertyTooltip(IServerUtils utils, LootItemBlockStatePropertyCondition cond) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.block).build("ali.property.value.block"))
                .add(utils.getValueTooltip(utils, cond.properties).build("ali.property.branch.properties"))
                .build("ali.type.condition.block_state_property");
    }

    @NotNull
    public static ITooltipNode getDamageSourcePropertiesTooltip(IServerUtils utils, DamageSourceCondition cond) {
        return utils.getValueTooltip(utils, cond.predicate).build("ali.type.condition.damage_source_properties");
    }

    @NotNull
    public static ITooltipNode getEntityPropertiesTooltip(IServerUtils utils, LootItemEntityPropertyCondition cond) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.entityTarget).build("ali.property.value.target"))
                .add(utils.getValueTooltip(utils, cond.predicate).build("ali.property.branch.predicate"))
                .build("ali.type.condition.entity_properties");
    }

    @NotNull
    public static ITooltipNode getEntityScoresTooltip(IServerUtils utils, EntityHasScoreCondition cond) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.entityTarget).build("ali.property.value.target"))
                .add(getMapTooltip(utils, cond.scores, GenericTooltipUtils::getIntRangeEntryTooltip).build("ali.property.branch.scores"))
                .build("ali.type.condition.entity_scores");
    }

    @NotNull
    public static ITooltipNode getInvertedTooltip(IServerUtils utils, InvertedLootItemCondition cond) {
        return BranchTooltipNode.branch()
                .add(utils.getConditionTooltip(utils, cond.term))
                .build("ali.type.condition.inverted");
    }

    @NotNull
    public static ITooltipNode getKilledByPlayerTooltip(IServerUtils ignoredUtils, LootItemKilledByPlayerCondition ignoredCond) {
        return LiteralTooltipNode.translatable("ali.type.condition.killed_by_player");
    }

    @NotNull
    public static ITooltipNode getLocationCheckTooltip(IServerUtils utils, LocationCheck cond) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        tooltip.add(utils.getValueTooltip(utils, cond.predicate).build("ali.property.branch.location"));

        if (!cond.offset.equals(BlockPos.ZERO)) {
            tooltip.add(utils.getValueTooltip(utils, cond.offset).build("ali.property.multi.offset"));
        }

        return tooltip.build("ali.type.condition.location_check");
    }

    @NotNull
    public static ITooltipNode getMatchToolTooltip(IServerUtils utils, MatchTool cond) {
        return utils.getValueTooltip(utils, cond.predicate).build("ali.type.condition.match_tool");
    }

    @NotNull
    public static ITooltipNode getRandomChanceTooltip(IServerUtils utils, LootItemRandomChanceCondition cond) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, cond.probability).build("ali.property.value.probability"))
                .build("ali.type.condition.random_chance");
    }

    @NotNull
    public static ITooltipNode getRandomChanceWithLootingTooltip(IServerUtils utils, LootItemRandomChanceWithLootingCondition cond) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, cond.percent).build("ali.property.value.percent"))
                .add(utils.getValueTooltip(utils, cond.lootingMultiplier).build("ali.property.value.multiplier"))
                .build("ali.type.condition.random_chance_with_looting");
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, ConditionReference cond) {
        return utils.getValueTooltip(utils, cond.name).build("ali.type.condition.reference");
    }

    @NotNull
    public static ITooltipNode getSurvivesExplosionTooltip(IServerUtils ignoredUtils, ExplosionCondition ignoredCond) {
        return LiteralTooltipNode.translatable("ali.type.condition.survives_explosion");
    }

    @NotNull
    public static ITooltipNode getTableBonusTooltip(IServerUtils utils, BonusLevelTableCondition cond) {
        List<String> list = IntStream.range(0, cond.values.length).mapToDouble(i -> cond.values[i]).mapToObj(FLOAT_FORMAT::format).toList();

        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, cond.enchantment).build("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, list.toString()).build("ali.property.value.values"))
                .build("ali.type.condition.table_bonus");
    }

    @NotNull
    public static ITooltipNode getTimeCheckTooltip(IServerUtils utils, TimeCheck cond) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.period).build("ali.property.value.period"))
                .add(utils.getValueTooltip(utils, cond.value).build("ali.property.value.value"))
                .build("ali.type.condition.time_check");
    }

    @NotNull
    public static ITooltipNode getValueCheckTooltip(IServerUtils utils, ValueCheckCondition cond) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.provider).build("ali.property.value.provider"))
                .add(utils.getValueTooltip(utils, cond.range).build("ali.property.value.range"))
                .build("ali.type.condition.value_check");
    }

    @NotNull
    public static ITooltipNode getWeatherCheckTooltip(IServerUtils utils, WeatherCheck cond) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.isRaining).build("ali.property.value.is_raining"))
                .add(utils.getValueTooltip(utils, cond.isThundering).build("ali.property.value.is_thundering"))
                .build("ali.type.condition.weather_check");
    }
}
