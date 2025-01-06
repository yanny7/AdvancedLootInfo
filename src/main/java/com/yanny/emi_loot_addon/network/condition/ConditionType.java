package com.yanny.emi_loot_addon.network.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.loot.CanToolPerformAction;

public enum ConditionType {
    INVERTED,
    ANY_OF,
    ALL_OF,
    RANDOM_CHANCE,
    RANDOM_CHANCE_WITH_LOOTING,
    ENTITY_PROPERTIES,
    KILLED_BY_PLAYER,
    ENTITY_SCORES,
    BLOCK_STATE_PROPERTY,
    MATCH_TOOL,
    TABLE_BONUS,
    SURVIVES_EXPLOSION,
    DAMAGE_SOURCE_PROPERTIES,
    LOCATION_CHECK,
    WEATHER_CHECK,
    REFERENCE,
    TIME_CHECK,
    VALUE_CHECK,
    LOOT_CONDITION_TYPE,

    UNKNOWN
    ;

    public int getBitIndex() {
        return ordinal() + 32;
    }

    public static ConditionType of (int bitIndex) {
        bitIndex -= 32;

        if (ConditionType.values().length > bitIndex && bitIndex > 0) {
            return ConditionType.values()[bitIndex];
        } else {
            return ConditionType.UNKNOWN;
        }
    }

    public static ConditionType of(LootItemConditionType type) {
        if (type == LootItemConditions.INVERTED) {
            return INVERTED;
        } else if (type == LootItemConditions.ANY_OF) {
            return ANY_OF;
        } else if (type == LootItemConditions.ALL_OF) {
            return ALL_OF;
        } else if (type == LootItemConditions.RANDOM_CHANCE) {
            return RANDOM_CHANCE;
        } else if (type == LootItemConditions.RANDOM_CHANCE_WITH_LOOTING) {
            return RANDOM_CHANCE_WITH_LOOTING;
        } else if (type == LootItemConditions.ENTITY_PROPERTIES) {
            return ENTITY_PROPERTIES;
        } else if (type == LootItemConditions.KILLED_BY_PLAYER) {
            return KILLED_BY_PLAYER;
        } else if (type == LootItemConditions.ENTITY_SCORES) {
            return ENTITY_SCORES;
        } else if (type == LootItemConditions.BLOCK_STATE_PROPERTY) {
            return BLOCK_STATE_PROPERTY;
        } else if (type == LootItemConditions.MATCH_TOOL) {
            return MATCH_TOOL;
        } else if (type == LootItemConditions.TABLE_BONUS) {
            return TABLE_BONUS;
        } else if (type == LootItemConditions.SURVIVES_EXPLOSION) {
            return SURVIVES_EXPLOSION;
        } else if (type == LootItemConditions.DAMAGE_SOURCE_PROPERTIES) {
            return DAMAGE_SOURCE_PROPERTIES;
        } else if (type == LootItemConditions.LOCATION_CHECK) {
            return LOCATION_CHECK;
        } else if (type == LootItemConditions.WEATHER_CHECK) {
            return WEATHER_CHECK;
        } else if (type == LootItemConditions.REFERENCE) {
            return REFERENCE;
        } else if (type == LootItemConditions.TIME_CHECK) {
            return TIME_CHECK;
        } else if (type == LootItemConditions.VALUE_CHECK) {
            return VALUE_CHECK;
        } else if (type == CanToolPerformAction.LOOT_CONDITION_TYPE) {
            return LOOT_CONDITION_TYPE;
        } else {
            return UNKNOWN;
        }
    }
}
