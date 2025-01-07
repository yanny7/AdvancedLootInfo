package com.yanny.emi_loot_addon.datagen;

import com.yanny.emi_loot_addon.EmiLootMod;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

class EmiLanguageProvider extends LanguageProvider {
    public EmiLanguageProvider(PackOutput output, String locale) {
        super(output, EmiLootMod.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("emi.category.emi_loot_addon.block_loot", "Block Drops");
        add("emi.category.emi_loot_addon.entity_loot", "Entity Drops");
        add("emi.category.emi_loot_addon.gameplay_loot", "Gameplay Drops");

        add("emi.description.emi_loot_addon.chance", "Chance: %s");
        add("emi.description.emi_loot_addon.chance_rolls", "Chance: [%s] %s");
        add("emi.description.emi_loot_addon.chance_bonus", "%s (%s %s)");
        add("emi.description.emi_loot_addon.count", "Count: %s");
        add("emi.description.emi_loot_addon.count_bonus", "%s (%s %s)");

        add("emi.type.emi_loot_addon.condition.all_of", "All must pass:");
        add("emi.type.emi_loot_addon.condition.any_of", "Any of:");
        add("emi.type.emi_loot_addon.condition.inverted", "Inverted:");
        add("emi.type.emi_loot_addon.condition.survives_explosion", "Must survive explosion");
        add("emi.type.emi_loot_addon.condition.killed_by_player", "Must be killed by player");
        add("emi.type.emi_loot_addon.condition.explosion_decay", "Explosion decreases amount");
        add("emi.type.emi_loot_addon.condition.entity_properties", "Entity Properties:");
        add("emi.type.emi_loot_addon.condition.entity_scores", "Entity Scores:");
        add("emi.type.emi_loot_addon.condition.block_state_property", "Block State Property:");
        add("emi.type.emi_loot_addon.condition.match_tool", "Match Tool:");
        add("emi.type.emi_loot_addon.condition.damage_source_properties", "Damage Source Properties:");
        add("emi.type.emi_loot_addon.condition.location_check", "Location Check:");
        add("emi.type.emi_loot_addon.condition.weather_check", "Weather Check:");
        add("emi.type.emi_loot_addon.condition.reference", "Reference: %s");
        add("emi.type.emi_loot_addon.condition.time_check", "Time Check:");
        add("emi.type.emi_loot_addon.condition.value_check", "Value Check:");
        add("emi.type.emi_loot_addon.condition.loot_condition_type", "Can Tool Perform Action: %s");

        add("emi.type.emi_loot_addon.function.enchant_with_levels", "Enchanted With Levels:");
        add("emi.type.emi_loot_addon.function.enchant_randomly", "Enchanted Randomly");
        add("emi.type.emi_loot_addon.function.set_enchantments", "Set Enchantments:");
        add("emi.type.emi_loot_addon.function.set_nbt", "Set Nbt:");

        add("emi.enum.target.this", "This entity");
        add("emi.enum.target.killer", "Killer");
        add("emi.enum.target.direct_killer", "Directly killed by");
        add("emi.enum.target.killer_player", "Last damaged by player");

        add("emi.property.condition.predicate.target", "Target: %s");
        add("emi.property.condition.predicate.entity_type", "Entity Type:");
        add("emi.property.condition.predicate.dist_to_player", "Distance:");
        add("emi.property.condition.predicate.location", "Location:");
        add("emi.property.condition.predicate.stepping_on_location", "Stepping on Location:");
        add("emi.property.condition.predicate.effect", "Effects:");
        add("emi.property.condition.predicate.nbt", "NBT: %s");
        add("emi.property.condition.predicate.flags", "Flags:");
        add("emi.property.condition.predicate.equipment", "Equipment:");
        add("emi.property.condition.predicate.entity_sub_type", "Entity Subtype: %s");
        add("emi.property.condition.predicate.vehicle", "Vehicle:");
        add("emi.property.condition.predicate.passenger", "Passenger:");
        add("emi.property.condition.predicate.targeted_entity", "Targeted Entity:");
        add("emi.property.condition.predicate.team", "Team: %s");
        add("emi.property.condition.dist_predicate.horizontal", "Hor.: %s");
        add("emi.property.condition.dist_predicate.absolute", "Abs.: %s");
        add("emi.property.condition.dist_predicate.x", "X: %s");
        add("emi.property.condition.dist_predicate.y", "Y: %s");
        add("emi.property.condition.dist_predicate.z", "Z: %s");
        add("emi.property.condition.location.x", "X: %s");
        add("emi.property.condition.location.y", "Y: %s");
        add("emi.property.condition.location.z", "Z: %s");
        add("emi.property.condition.location.biome", "Biome: %s");
        add("emi.property.condition.location.structure", "Structure: %s");
        add("emi.property.condition.location.dimension", "Dimension: %s");
        add("emi.property.condition.location.smokey", "Above Campfire: %s");
        add("emi.property.condition.location.light", "Light Level: %s");
        add("emi.property.condition.location.block", "Block:");
        add("emi.property.condition.location.fluid", "Fluid:");
        add("emi.property.condition.block.tag", "Tag: %s");
        add("emi.property.condition.block.blocks", "Blocks:");
        add("emi.property.condition.block.state", "Block States:");
        add("emi.property.condition.block.nbt", "NBT: %s");
        add("emi.property.condition.fluid.tag", "Tag: %s");
        add("emi.property.condition.fluid.fluid", "Fluid: %s");
        add("emi.property.condition.fluid.state", "Fluid States:");
        add("emi.property.condition.effect.amplifier", "Amplifier: %s");
        add("emi.property.condition.effect.duration", "Duration: %s");
        add("emi.property.condition.effect.ambient", "Ambient: %s");
        add("emi.property.condition.effect.visible", "Visible: %s");
        add("emi.property.condition.flags.on_fire", "On Fire: %s");
        add("emi.property.condition.flags.is_baby", "Is Baby: %s");
        add("emi.property.condition.flags.is_crouching", "Is Crouching: %s");
        add("emi.property.condition.flags.is_sprinting", "Is Sprinting: %s");
        add("emi.property.condition.flags.is_swimming", "Is Swimming: %s");
        add("emi.property.condition.equipment.head", "Head:");
        add("emi.property.condition.equipment.chest", "Chest:");
        add("emi.property.condition.equipment.legs", "Legs:");
        add("emi.property.condition.equipment.feet", "Feet:");
        add("emi.property.condition.equipment.mainhand", "Mainhand:");
        add("emi.property.condition.equipment.offhand", "Offhand:");
        add("emi.property.condition.item.tag", "Tag: %s");
        add("emi.property.condition.item.items", "Items:");
        add("emi.property.condition.item.count", "Count: %s");
        add("emi.property.condition.item.durability", "Durability: %s");
        add("emi.property.condition.item.enchantment", "Enchantment:");
        add("emi.property.condition.item.stored_enchantment", "Stored Enchantment:");
        add("emi.property.condition.item.potion", "Potion:");
        add("emi.property.condition.item.nbt", "NBT: %s");
        add("emi.property.condition.enchantment.level", "Level: %s");
        add("emi.property.condition.sub_entity.variant", "Variant: %s");
        add("emi.property.condition.sub_entity.blocks_on_fire", "Blocks on Fire: %s");
        add("emi.property.condition.sub_entity.stuck_entity", "Stuck Entity:");
        add("emi.property.condition.sub_entity.in_open_water", "In Open Water: %s");
        add("emi.property.condition.sub_entity.level", "Level: %s");
        add("emi.property.condition.sub_entity.game_type", "Game Type: %s");
        add("emi.property.condition.sub_entity.stats", "Stats:");
        add("emi.property.condition.sub_entity.recipes", "Recipes:");
        add("emi.property.condition.sub_entity.advancements", "Advancements:");
        add("emi.property.condition.sub_entity.advancement.done", "Done: %s");
        add("emi.property.condition.sub_entity.size", "Size: %s");
        add("emi.property.condition.scores.score", "Score:");
        add("emi.property.condition.damage_source.direct_entity", "Direct Entity:");
        add("emi.property.condition.damage_source.source_entity", "Source Entity:");
        add("emi.property.condition.damage_source.tags", "Tags:");
        add("emi.property.condition.location_check.location", "Location:");
        add("emi.property.condition.location_check.offset", "Offset:");
        add("emi.property.condition.location_check.x", "X: %s");
        add("emi.property.condition.location_check.y", "Y: %s");
        add("emi.property.condition.location_check.z", "Z: %s");
        add("emi.property.condition.weather_check.is_raining", "Is Raining: %s");
        add("emi.property.condition.weather_check.is_thundering", "Is Thundering: %s");
        add("emi.property.condition.time_check.period", "Period: %s");
        add("emi.property.condition.time_check.value", "Value: %s");
        add("emi.property.condition.value_check.provider", "Provider: %s");
        add("emi.property.condition.value_check.range", "Range: %s");

        add("emi.property.function.enchant_with_levels.levels", "Levels: %s");
        add("emi.property.function.enchant_with_levels.treasure", "Treasure: %s");
        add("emi.property.function.enchant_randomly.enchantments", "Available Enchantments:");
        add("emi.property.function.set_enchantments.enchantments", "Available Enchantments:");
        add("emi.property.function.set_enchantments.enchantment", "%s [%s]");
        add("emi.property.function.set_enchantments.add", "Add: %s");

        add("emi.util.emi_loot_addon.two_values", "%s%s");
        add("emi.util.emi_loot_addon.two_values_with_space", "%s %s");
        add("emi.util.emi_loot_addon.pad.1", "  ->");
        add("emi.util.emi_loot_addon.pad.2", "    ->");
        add("emi.util.emi_loot_addon.pad.3", "      ->");
        add("emi.util.emi_loot_addon.pad.4", "        ->");
        add("emi.util.emi_loot_addon.pad.5", "          ->");
        add("emi.util.emi_loot_addon.key_value", "%s: %s");
    }
}
