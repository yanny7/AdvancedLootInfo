package com.yanny.ali.datagen;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.ali.Utils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.EnumTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageHolder {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        CoreLang.register(TRANSLATION_MAP, Lang.Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Functions.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Value.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Branch.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Description.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Group.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Multi.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Error.class);

        put(EquipmentSlot.MAINHAND, "Main Hand");
        put(EquipmentSlot.OFFHAND, "Off Hand");
        put(EquipmentSlot.FEET, "Feet");
        put(EquipmentSlot.LEGS, "Legs");
        put(EquipmentSlot.CHEST, "Chest");
        put(EquipmentSlot.HEAD, "Head");

        put(DyeColor.WHITE, "White");
        put(DyeColor.ORANGE, "Orange");
        put(DyeColor.MAGENTA, "Magenta");
        put(DyeColor.LIGHT_BLUE, "Light Blue");
        put(DyeColor.YELLOW, "Yellow");
        put(DyeColor.LIME, "Lime");
        put(DyeColor.PINK, "Pink");
        put(DyeColor.GRAY, "Gray");
        put(DyeColor.LIGHT_GRAY, "Light Gray");
        put(DyeColor.CYAN, "Cyan");
        put(DyeColor.PURPLE, "Purple");
        put(DyeColor.BLUE, "Blue");
        put(DyeColor.BROWN, "Brown");
        put(DyeColor.GREEN, "Green");
        put(DyeColor.RED, "Red");
        put(DyeColor.BLACK, "Black");

        put(GameType.SURVIVAL, "Survival");
        put(GameType.CREATIVE, "Creative");
        put(GameType.ADVENTURE, "Adventure");
        put(GameType.SPECTATOR, "Spectator");

        put(MapDecoration.Type.PLAYER, "Player");
        put(MapDecoration.Type.FRAME, "Item Frame");
        put(MapDecoration.Type.RED_MARKER, "Red Marker");
        put(MapDecoration.Type.BLUE_MARKER, "Blue Marker");
        put(MapDecoration.Type.TARGET_X, "Target X");
        put(MapDecoration.Type.TARGET_POINT, "Target Point");
        put(MapDecoration.Type.PLAYER_OFF_MAP, "Player Off Map");
        put(MapDecoration.Type.PLAYER_OFF_LIMITS, "Player Off Limits");
        put(MapDecoration.Type.MANSION, "Woodland Mansion");
        put(MapDecoration.Type.MONUMENT, "Ocean Monument");
        put(MapDecoration.Type.BANNER_WHITE, "White Banner");
        put(MapDecoration.Type.BANNER_ORANGE, "Orange Banner");
        put(MapDecoration.Type.BANNER_MAGENTA, "Magenta Banner");
        put(MapDecoration.Type.BANNER_LIGHT_BLUE, "Light Blue Banner");
        put(MapDecoration.Type.BANNER_YELLOW, "Yellow Banner");
        put(MapDecoration.Type.BANNER_LIME, "Lime Banner");
        put(MapDecoration.Type.BANNER_PINK, "Pink Banner");
        put(MapDecoration.Type.BANNER_GRAY, "Gray Banner");
        put(MapDecoration.Type.BANNER_LIGHT_GRAY, "Light Gray Banner");
        put(MapDecoration.Type.BANNER_CYAN, "Cyan Banner");
        put(MapDecoration.Type.BANNER_PURPLE, "Purple Banner");
        put(MapDecoration.Type.BANNER_BLUE, "Blue Banner");
        put(MapDecoration.Type.BANNER_BROWN, "Brown Banner");
        put(MapDecoration.Type.BANNER_GREEN, "Green Banner");
        put(MapDecoration.Type.BANNER_RED, "Red Banner");
        put(MapDecoration.Type.BANNER_BLACK, "Black Banner");
        put(MapDecoration.Type.RED_X, "Red X");

        put(CopyNbtFunction.MergeStrategy.REPLACE, "Replace");
        put(CopyNbtFunction.MergeStrategy.APPEND, "Append");
        put(CopyNbtFunction.MergeStrategy.MERGE, "Merge");

        put(AttributeModifier.Operation.ADDITION, "Addition");
        put(AttributeModifier.Operation.MULTIPLY_BASE, "Multiply Base");
        put(AttributeModifier.Operation.MULTIPLY_TOTAL, "Multiply Total");

        put(LootContext.EntityTarget.THIS, "This Entity");
        put(LootContext.EntityTarget.KILLER, "Killer");
        put(LootContext.EntityTarget.DIRECT_KILLER, "Direct Killer");
        put(LootContext.EntityTarget.KILLER_PLAYER, "Killer Player");

        put(CopyNameFunction.NameSource.THIS, "This Entity");
        put(CopyNameFunction.NameSource.KILLER, "Killer");
        put(CopyNameFunction.NameSource.KILLER_PLAYER, "Killer Player");
        put(CopyNameFunction.NameSource.BLOCK_ENTITY, "Block Entity");

        verifyEnumTranslations();

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
    }

    private static void put(Enum<?> value, String english) {
        TRANSLATION_MAP.put(EnumTypes.key(value), english);
    }

    private static void verifyEnumTranslations() {
        List<String> missing = new ArrayList<>();

        EnumTypes.TRANSLATED_ENUMS.forEach((type, owner) -> {
            for (Enum<?> value : type.getEnumConstants()) {
                if (!TRANSLATION_MAP.containsKey(EnumTypes.key(value))) {
                    missing.add(type.getSimpleName() + "." + value.name());
                }
            }
        });

        if (!missing.isEmpty()) {
            LOGGER.warn("Missing enum translations: {}", String.join(", ", missing));
        }
    }
}
