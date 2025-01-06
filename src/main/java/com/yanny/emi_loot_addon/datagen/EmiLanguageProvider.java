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

        add("emi.type.emi_loot_addon.all_of", "All must pass:");
        add("emi.type.emi_loot_addon.any_of", "Any of:");
        add("emi.type.emi_loot_addon.inverted", "Inverted:");
        add("emi.type.emi_loot_addon.survives_explosion", "Must survive explosion");
        add("emi.type.emi_loot_addon.killed_by_player", "Must be killed by player");
        add("emi.type.emi_loot_addon.explosion_decay", "Explosion decreases amount");
        add("emi.type.emi_loot_addon.entity_properties", "Entity Properties:");
        add("emi.type.emi_loot_addon.entity_scores", "Entity Scores:");
        add("emi.type.emi_loot_addon.block_state_property", "Block State Property:");
        add("emi.type.emi_loot_addon.match_tool", "Match Tool:");
        add("emi.type.emi_loot_addon.damage_source_properties", "Damage Source Properties:");
        add("emi.type.emi_loot_addon.location_check", "Location Check:");
        add("emi.type.emi_loot_addon.weather_check", "Weather Check:");
        add("emi.type.emi_loot_addon.reference", "Reference: %s");
        add("emi.type.emi_loot_addon.time_check", "Time Check:");
        add("emi.type.emi_loot_addon.value_check", "Value Check:");
        add("emi.type.emi_loot_addon.loot_condition_type", "Can Tool Perform Action: %s");

        add("emi.enum.target.this", "This entity");
        add("emi.enum.target.killer", "Killer");
        add("emi.enum.target.direct_killer", "Directly killed by");
        add("emi.enum.target.killer_player", "Last damaged by player");

        add("emi.property.predicate.target", "Target: %s");
        add("emi.property.predicate.entity_type", "Entity Type:");
        add("emi.property.predicate.dist_to_player", "Distance:");
        add("emi.property.predicate.location", "Location:");
        add("emi.property.predicate.stepping_on_location", "Stepping on Location:");
        add("emi.property.predicate.effect", "Effects:");
        add("emi.property.predicate.nbt", "NBT: %s");
        add("emi.property.predicate.flags", "Flags:");
        add("emi.property.predicate.equipment", "Equipment:");
        add("emi.property.predicate.entity_sub_type", "Entity Subtype: %s");
        add("emi.property.predicate.vehicle", "Vehicle:");
        add("emi.property.predicate.passenger", "Passenger:");
        add("emi.property.predicate.targeted_entity", "Targeted Entity:");
        add("emi.property.predicate.team", "Team: %s");
        add("emi.property.dist_predicate.horizontal", "Hor.: %s");
        add("emi.property.dist_predicate.absolute", "Abs.: %s");
        add("emi.property.dist_predicate.x", "X: %s");
        add("emi.property.dist_predicate.y", "Y: %s");
        add("emi.property.dist_predicate.z", "Z: %s");
        add("emi.property.location.x", "X: %s");
        add("emi.property.location.y", "Y: %s");
        add("emi.property.location.z", "Z: %s");
        add("emi.property.location.biome", "Biome: %s");
        add("emi.property.location.structure", "Structure: %s");
        add("emi.property.location.dimension", "Dimension: %s");
        add("emi.property.location.smokey", "Above Campfire: %s");
        add("emi.property.location.light", "Light Level: %s");
        add("emi.property.location.block", "Block:");
        add("emi.property.location.fluid", "Fluid:");
        add("emi.property.block.tag", "Tag: %s");
        add("emi.property.block.blocks", "Blocks:");
        add("emi.property.block.state", "Block States:");
        add("emi.property.block.nbt", "NBT: %s");
        add("emi.property.fluid.tag", "Tag: %s");
        add("emi.property.fluid.fluid", "Fluid: %s");
        add("emi.property.fluid.state", "Fluid States:");
        add("emi.property.effect.amplifier", "Amplifier: %s");
        add("emi.property.effect.duration", "Duration: %s");
        add("emi.property.effect.ambient", "Ambient: %s");
        add("emi.property.effect.visible", "Visible: %s");
        add("emi.property.flags.on_fire", "On Fire: %s");
        add("emi.property.flags.is_baby", "Is Baby: %s");
        add("emi.property.flags.is_crouching", "Is Crouching: %s");
        add("emi.property.flags.is_sprinting", "Is Sprinting: %s");
        add("emi.property.flags.is_swimming", "Is Swimming: %s");
        add("emi.property.equipment.head", "Head:");
        add("emi.property.equipment.chest", "Chest:");
        add("emi.property.equipment.legs", "Legs:");
        add("emi.property.equipment.feet", "Feet:");
        add("emi.property.equipment.mainhand", "Mainhand:");
        add("emi.property.equipment.offhand", "Offhand:");
        add("emi.property.item.tag", "Tag: %s");
        add("emi.property.item.items", "Items:");
        add("emi.property.item.count", "Count: %s");
        add("emi.property.item.durability", "Durability: %s");
        add("emi.property.item.enchantment", "Enchantment:");
        add("emi.property.item.stored_enchantment", "Stored Enchantment:");
        add("emi.property.item.potion", "Potion:");
        add("emi.property.item.nbt", "NBT: %s");
        add("emi.property.enchantment.level", "Level: %s");
        add("emi.property.sub_entity.variant", "Variant: %s");
        add("emi.property.sub_entity.blocks_on_fire", "Blocks on Fire: %s");
        add("emi.property.sub_entity.stuck_entity", "Stuck Entity:");
        add("emi.property.sub_entity.in_open_water", "In Open Water: %s");
        add("emi.property.sub_entity.level", "Level: %s");
        add("emi.property.sub_entity.game_type", "Game Type: %s");
        add("emi.property.sub_entity.stats", "Stats:");
        add("emi.property.sub_entity.recipes", "Recipes:");
        add("emi.property.sub_entity.advancements", "Advancements:");
        add("emi.property.sub_entity.advancement.done", "Done: %s");
        add("emi.property.sub_entity.size", "Size: %s");
        add("emi.property.scores.score", "Score:");
        add("emi.property.damage_source.direct_entity", "Direct Entity:");
        add("emi.property.damage_source.source_entity", "Source Entity:");
        add("emi.property.damage_source.tags", "Tags:");
        add("emi.property.location_check.location", "Location:");
        add("emi.property.location_check.offset", "Offset:");
        add("emi.property.location_check.x", "X: %s");
        add("emi.property.location_check.y", "Y: %s");
        add("emi.property.location_check.z", "Z: %s");
        add("emi.property.weather_check.is_raining", "Is Raining: %s");
        add("emi.property.weather_check.is_thundering", "Is Thundering: %s");
        add("emi.property.time_check.period", "Period: %s");
        add("emi.property.time_check.value", "Value: %s");
        add("emi.property.value_check.provider", "Provider: %s");
        add("emi.property.value_check.range", "Range: %s");

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
