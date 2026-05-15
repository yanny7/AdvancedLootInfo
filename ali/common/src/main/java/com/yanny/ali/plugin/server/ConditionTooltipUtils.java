package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class ConditionTooltipUtils {
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.####");

    @NotNull
    public static TooltipBuilder getAllOfTooltip(IServerUtils utils, AllOfCondition cond) {
        return utils.getValueTooltip(utils, cond.terms).key(Lang.Conditions.ALL_OF);
    }

    @NotNull
    public static TooltipBuilder getAnyOfTooltip(IServerUtils utils, AnyOfCondition cond) {
        return utils.getValueTooltip(utils, cond.terms).key(Lang.Conditions.ANY_OF);
    }

    @NotNull
    public static TooltipBuilder getBlockStatePropertyTooltip(IServerUtils utils, LootItemBlockStatePropertyCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.block()).build(Lang.Value.BLOCK));
            b.add(utils.getValueTooltip(utils, cond.properties()).build(Lang.Branch.PROPERTIES));
        }).key(Lang.Conditions.BLOCK_STATE_PROPERTY);
    }

    @NotNull
    public static TooltipBuilder getDamageSourcePropertiesTooltip(IServerUtils utils, DamageSourceCondition cond) {
        return utils.getValueTooltip(utils, cond.predicate()).key(Lang.Conditions.DAMAGE_SOURCE_PROPERTIES);
    }

    @NotNull
    public static TooltipBuilder getEnchantActiveCheckTooltip(IServerUtils utils, EnchantmentActiveCheck cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.active()).build(Lang.Value.ACTIVE)))
                .key(Lang.Conditions.ENCHANTMENT_ACTIVE_CHECK);
    }

    @NotNull
    public static TooltipBuilder getEntityPropertiesTooltip(IServerUtils utils, LootItemEntityPropertyCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.entityTarget()).build(Lang.Value.TARGET));
            b.add(utils.getValueTooltip(utils, cond.predicate()).build(Lang.Branch.CONDITION));
        }).key(Lang.Conditions.ENTITY_PROPERTIES);
    }

    @NotNull
    public static TooltipBuilder getEntityScoresTooltip(IServerUtils utils, EntityHasScoreCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.entityTarget()).build(Lang.Value.TARGET));
            b.add(getMapTooltip(utils, cond.scores(), GenericTooltipUtils::getIntRangeEntryTooltip).build(Lang.Branch.SCORES));
        }).key(Lang.Conditions.ENTITY_SCORES);
    }

    @NotNull
    public static TooltipBuilder getInvertedTooltip(IServerUtils utils, InvertedLootItemCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.term())))
                .key(Lang.Conditions.INVERTED);
    }

    @NotNull
    public static TooltipBuilder getKilledByPlayerTooltip(IServerUtils ignoredUtils, LootItemKilledByPlayerCondition ignoredCond) {
        return TooltipBuilder.keyOnly(Lang.Conditions.KILLED_BY_PLAYER);
    }

    @NotNull
    public static TooltipBuilder getLocationCheckTooltip(IServerUtils utils, LocationCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.predicate()).build(Lang.Branch.LOCATION));

            if (!cond.offset().equals(BlockPos.ZERO)) {
                b.add(utils.getValueTooltip(utils, cond.offset()).build("ali.property.multi.offset"));
            }
        }).key(Lang.Conditions.LOCATION_CHECK);
    }

    @NotNull
    public static TooltipBuilder getMatchToolTooltip(IServerUtils utils, MatchTool cond) {
        return utils.getValueTooltip(utils, cond.predicate()).key(Lang.Conditions.MATCH_TOOL);
    }

    @NotNull
    public static TooltipBuilder getRandomChanceTooltip(IServerUtils utils, LootItemRandomChanceCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.chance()).build(Lang.Value.CHANCE)))
                .isAdvancedTooltip().key(Lang.Conditions.RANDOM_CHANCE);
    }

    @NotNull
    public static TooltipBuilder getRandomChanceWithEnchantedBonusTooltip(IServerUtils utils, LootItemRandomChanceWithEnchantedBonusCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.unenchantedChance()).build(Lang.Value.UNENCHANTED_CHANCE));
            b.add(utils.getValueTooltip(utils, cond.enchantedChance()).build(Lang.Branch.ENCHANTED_CHANCE));
            b.add(utils.getValueTooltip(utils, cond.enchantment()).build(Lang.Value.ENCHANTMENT));
        }).key(Lang.Conditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(IServerUtils utils, ConditionReference cond) {
        return utils.getValueTooltip(utils, cond.name()).key(Lang.Conditions.REFERENCE);
    }

    @NotNull
    public static TooltipBuilder getSurvivesExplosionTooltip(IServerUtils ignoredUtils, ExplosionCondition ignoredCond) {
        return TooltipBuilder.keyOnly(Lang.Conditions.SURVIVES_EXPLOSION);
    }

    @NotNull
    public static TooltipBuilder getTableBonusTooltip(IServerUtils utils, BonusLevelTableCondition cond) {
        List<String> list = cond.values().stream().mapToDouble(aFloat -> aFloat).mapToObj(FLOAT_FORMAT::format).toList();

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.enchantment()).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, list.toString()).build(Lang.Value.VALUES));
        }).isAdvancedTooltip().key(Lang.Conditions.TABLE_BONUS);
    }

    @NotNull
    public static TooltipBuilder getTimeCheckTooltip(IServerUtils utils, TimeCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.period()).build(Lang.Value.PERIOD));
            b.add(utils.getValueTooltip(utils, cond.value()).build(Lang.Value.VALUE));
        }).key(Lang.Conditions.TIME_CHECK);
    }

    @NotNull
    public static TooltipBuilder getValueCheckTooltip(IServerUtils utils, ValueCheckCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.provider()).build(Lang.Value.PROVIDER));
            b.add(utils.getValueTooltip(utils, cond.range()).build(Lang.Value.RANGE));
        }).key(Lang.Conditions.VALUE_CHECK);
    }

    @NotNull
    public static TooltipBuilder getWeatherCheckTooltip(IServerUtils utils, WeatherCheck cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.isRaining()).build(Lang.Value.IS_RAINING));
            b.add(utils.getValueTooltip(utils, cond.isThundering()).build(Lang.Value.IS_THUNDERING));
        }).key(Lang.Conditions.WEATHER_CHECK);
    }
}
