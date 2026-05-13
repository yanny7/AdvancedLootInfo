package com.yanny.ali.datagen;

import com.yanny.aci.language.CoreLang;
import com.yanny.ali.language.Lang;

import java.util.HashMap;
import java.util.Map;

public class LanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP;

    static {
        CoreLang.register(Lang.Conditions.class);
        CoreLang.register(Lang.Functions.class);
        CoreLang.register(Lang.Value.class);
        CoreLang.register(Lang.Branch.class);
        CoreLang.register(Lang.Description.class);
        CoreLang.register(Lang.Group.class);
        CoreLang.register(Lang.Error.class);

        TRANSLATION_MAP = new HashMap<>(CoreLang.TRANSLATION_MAP);

        TRANSLATION_MAP.put("emi.category.ali.block_loot", "Block Drops");
        TRANSLATION_MAP.put("emi.category.ali.plant_loot", "Plant Drops");
        TRANSLATION_MAP.put("emi.category.ali.entity_loot", "Entity Drops");
        TRANSLATION_MAP.put("emi.category.ali.chest_loot", "Chest Loot");
        TRANSLATION_MAP.put("emi.category.ali.fishing_loot", "Fishing Loot");
        TRANSLATION_MAP.put("emi.category.ali.archaeology_loot", "Archaeology Loot");
        TRANSLATION_MAP.put("emi.category.ali.hero_loot", "Hero of the Village Loot");
        TRANSLATION_MAP.put("emi.category.ali.gameplay_loot", "Gameplay Loot");
        TRANSLATION_MAP.put("emi.category.ali.trade_loot", "Trading Loot");
        TRANSLATION_MAP.put("emi.category.ali.cat_morning_gift", "Cat Morning Gift");
        TRANSLATION_MAP.put("emi.category.ali.piglin_bartering", "Piglin Bartering");
        TRANSLATION_MAP.put("emi.category.ali.sniffer_digging", "Sniffer Digging");
        TRANSLATION_MAP.put("emi.category.ali.trial_chambers", "Trial Chambers");
        TRANSLATION_MAP.put("emi.category.ali.panda_sneeze", "Panda Sneeze");
        TRANSLATION_MAP.put("emi.category.ali.shearing", "Shearing");

        TRANSLATION_MAP.put("ali.property.multi.offset", "Offset: [X: %s, Y: %s, Z: %s]");
        TRANSLATION_MAP.put("ali.property.multi.position", "Position: [X: %s, Y: %s, Z: %s]");

        TRANSLATION_MAP.put("ali.util.advanced_loot_info.delimiter.functions", "----- Modifiers -----");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.delimiter.conditions", "----- Predicates -----");
        TRANSLATION_MAP.put("ali.util.advanced_loot_info.accepts", "Accepts:");

        TRANSLATION_MAP.put("ali/loot_table/chests/abandoned_mineshaft", "Abandoned Mineshaft Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/ancient_city", "Ancient City Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/ancient_city_ice_box", "Ancient City Ice Box Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_bridge", "Bastion Bridge Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_hoglin_stable", "Bastion Hoglin Stable Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_other", "Bastion Other Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/bastion_treasure", "Bastion Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/buried_treasure", "Buried Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/desert_pyramid", "Desert Pyramid Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/end_city_treasure", "End City Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/igloo_chest", "Igloo Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/jungle_temple", "Jungle Temple Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/jungle_temple_dispenser", "Jungle Temple Dispenser Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/nether_bridge", "Nether Bridge Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/pillager_outpost", "Pillager Outpost Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/ruined_portal", "Ruined Portal Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/shipwreck_map", "Shipwreck Map Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/shipwreck_supply", "Shipwreck Supply Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/shipwreck_treasure", "Shipwreck Treasure Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/simple_dungeon", "Simple Dungeon Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/spawn_bonus_chest", "Spawn Bonus Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/stronghold_corridor", "Stronghold Corridor Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/stronghold_crossing", "Stronghold Crossing Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/stronghold_library", "Stronghold Library Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/underwater_ruin_big", "Underwater Ruin Big Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/underwater_ruin_small", "Underwater Ruin Small Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_armorer", "Village Armorer Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_butcher", "Village Butcher Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_cartographer", "Village Cartographer Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_desert_house", "Village Desert House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_fisher", "Village Fisher Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_fletcher", "Village Fletcher Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_mason", "Village Mason Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_plains_house", "Village Plains House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_savanna_house", "Village Savanna House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_shepherd", "Village Shepherd Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_snowy_house", "Village Snowy House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_taiga_house", "Village Taiga House Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_tannery", "Village Tannery Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_temple", "Village Temple Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_toolsmith", "Village Toolsmith Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/village/village_weaponsmith", "Village Weaponsmith Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/woodland_mansion", "Woodland Mansion Chest");

        TRANSLATION_MAP.put("ali/loot_table/archaeology/desert_pyramid", "Desert Pyramid");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/desert_well", "Desert Well");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/ocean_ruin_cold", "Ocean Ruin Cold");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/ocean_ruin_warm", "Ocean Ruins Warm");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/trail_ruins_common", "Trail Ruins Common");
        TRANSLATION_MAP.put("ali/loot_table/archaeology/trail_ruins_rare", "Trail Ruins Rare");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/armorer_gift", "Armorer Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/butcher_gift", "Butcher Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/cartographer_gift", "Cartographer Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/cleric_gift", "Cleric Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/farmer_gift", "Farmer Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/fisherman_gift", "Fisherman Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/fletcher_gift", "Fletcher Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/leatherworker_gift", "Leatherworker Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/librarian_gift", "Librarian Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/mason_gift", "Mason Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/shepherd_gift", "Shepherd Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/toolsmith_gift", "Toolsmith Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/weaponsmith_gift", "Weaponsmith Gift");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing", "Fishing");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/junk", "Fishing: Junk");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/fish", "Fishing: Fish");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/treasure", "Fishing: Treasure");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/sniffer_digging", "Sniffer Digging");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/cat_morning_gift", "Cat Morning Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/piglin_bartering", "Piglin Bartering");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/panda_sneeze", "Panda Sneeze");
        TRANSLATION_MAP.put("ali/loot_table/shearing/bogged", "Bogged Shearing");

        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/corridor", "Corridor Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/entrance", "Entrance Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/intersection", "Intersection Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/intersection_barrel", "Intersection Barrel Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward", "Reward Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_common", "Reward Common Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_ominous", "Reward Ominous Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_ominous_common", "Reward Ominous Common Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_ominous_rare", "Reward Ominous Rare Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_ominous_unique", "Reward Ominous Unique Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_rare", "Reward Rare Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/reward_unique", "Reward Unique Chest");
        TRANSLATION_MAP.put("ali/loot_table/chests/trial_chambers/supply", "Supply Chest");
        TRANSLATION_MAP.put("ali/loot_table/pots/trial_chambers/corridor", "Corridor Pots");
        TRANSLATION_MAP.put("ali/loot_table/dispensers/trial_chambers/chamber", "Chamber Dispensers");
        TRANSLATION_MAP.put("ali/loot_table/dispensers/trial_chambers/corridor", "Corridor Dispensers");
        TRANSLATION_MAP.put("ali/loot_table/dispensers/trial_chambers/water", "Water Dispensers");
        TRANSLATION_MAP.put("ali/loot_table/spawners/ominous/trial_chamber/consumables", "Ominous Consumables Spawners");
        TRANSLATION_MAP.put("ali/loot_table/spawners/ominous/trial_chamber/key", "Ominous Key Spawners");
        TRANSLATION_MAP.put("ali/loot_table/spawners/trial_chamber/consumables", "Consumables Spawners");
        TRANSLATION_MAP.put("ali/loot_table/spawners/trial_chamber/items_to_drop_when_ominous", "Items To Drop When Ominous Spawners");
        TRANSLATION_MAP.put("ali/loot_table/spawners/trial_chamber/key", "Key Spawners");
        TRANSLATION_MAP.put("ali/loot_table/equipment/trial_chamber", "Equipment");
        TRANSLATION_MAP.put("ali/loot_table/equipment/trial_chamber_melee", "Melee Equipment");
        TRANSLATION_MAP.put("ali/loot_table/equipment/trial_chamber_ranged", "Ranged Equipment");
    }
}
