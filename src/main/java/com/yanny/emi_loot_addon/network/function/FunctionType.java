package com.yanny.emi_loot_addon.network.function;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

public enum FunctionType {
    SET_COUNT,
    ENCHANT_WITH_LEVELS,
    ENCHANT_RANDOMLY,
    SET_ENCHANTMENTS,
    SET_NBT,
    FURNACE_SMELT,
    LOOTING_ENCHANT,
    SET_DAMAGE,
    SET_ATTRIBUTES,
    SET_NAME,
    EXPLORATION_MAP,
    SET_STEW_EFFECT,
    COPY_NAME,
    SET_CONTENTS,
    LIMIT_COUNT,
    APPLY_BONUS,
    SET_LOOT_TABLE,
    EXPLOSION_DECAY,
    SET_LORE,
    FILL_PLAYER_HEAD,
    COPY_NBT,
    COPY_STATE,
    SET_BANNER_PATTERN,
    SET_POTION,
    SET_INSTRUMENT,
    REFERENCE,

    UNKNOWN
    ;

    public static FunctionType of(LootItemFunctionType type) {
        if (type == LootItemFunctions.SET_COUNT) {
            return SET_COUNT;
        } else if (type == LootItemFunctions.ENCHANT_WITH_LEVELS) {
            return ENCHANT_WITH_LEVELS;
        } else if (type == LootItemFunctions.ENCHANT_RANDOMLY) {
            return ENCHANT_RANDOMLY;
        } else if (type == LootItemFunctions.SET_ENCHANTMENTS) {
            return SET_ENCHANTMENTS;
        } else if (type == LootItemFunctions.SET_NBT) {
            return SET_NBT;
        } else if (type == LootItemFunctions.FURNACE_SMELT) {
            return FURNACE_SMELT;
        } else if (type == LootItemFunctions.LOOTING_ENCHANT) {
            return LOOTING_ENCHANT;
        } else if (type == LootItemFunctions.SET_DAMAGE) {
            return SET_DAMAGE;
        } else if (type == LootItemFunctions.SET_ATTRIBUTES) {
            return SET_ATTRIBUTES;
        } else if (type == LootItemFunctions.SET_NAME) {
            return SET_NAME;
        } else if (type == LootItemFunctions.EXPLORATION_MAP) {
            return EXPLORATION_MAP;
        } else if (type == LootItemFunctions.SET_STEW_EFFECT) {
            return SET_STEW_EFFECT;
        } else if (type == LootItemFunctions.COPY_NAME) {
            return COPY_NAME;
        } else if (type == LootItemFunctions.SET_CONTENTS) {
            return SET_CONTENTS;
        } else if (type == LootItemFunctions.LIMIT_COUNT) {
            return LIMIT_COUNT;
        } else if (type == LootItemFunctions.APPLY_BONUS) {
            return APPLY_BONUS;
        } else if (type == LootItemFunctions.SET_LOOT_TABLE) {
            return SET_LOOT_TABLE;
        } else if (type == LootItemFunctions.EXPLOSION_DECAY) {
            return EXPLOSION_DECAY;
        } else if (type == LootItemFunctions.SET_LORE) {
            return SET_LORE;
        } else if (type == LootItemFunctions.FILL_PLAYER_HEAD) {
            return FILL_PLAYER_HEAD;
        } else if (type == LootItemFunctions.COPY_NBT) {
            return COPY_NBT;
        } else if (type == LootItemFunctions.COPY_STATE) {
            return COPY_STATE;
        } else if (type == LootItemFunctions.SET_BANNER_PATTERN) {
            return SET_BANNER_PATTERN;
        } else if (type == LootItemFunctions.SET_POTION) {
            return SET_POTION;
        } else if (type == LootItemFunctions.SET_INSTRUMENT) {
            return SET_INSTRUMENT;
        } else if (type == LootItemFunctions.REFERENCE) {
            return REFERENCE;
        } else {
            return UNKNOWN;
//            throw new IllegalStateException("Missing type " + type);
        }
    }
}
