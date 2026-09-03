package com.yanny.ali.datagen;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.language.CoreLang;
import com.yanny.ali.Utils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.EnumTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
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
        CoreLang.register(TRANSLATION_MAP, Lang.EntitySubPredicates.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Entry.class);
        CoreLang.register(TRANSLATION_MAP, Lang.ConsumeEffects.class);
        CoreLang.register(TRANSLATION_MAP, Lang.SlotSource.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Value.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Branch.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Description.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Group.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Multi.class);
        CoreLang.register(TRANSLATION_MAP, Lang.Error.class);

        put(EquipmentSlotGroup.ANY, "Any");
        put(EquipmentSlotGroup.MAINHAND, "Main Hand");
        put(EquipmentSlotGroup.OFFHAND, "Off Hand");
        put(EquipmentSlotGroup.HAND, "Hand");
        put(EquipmentSlotGroup.FEET, "Feet");
        put(EquipmentSlotGroup.LEGS, "Legs");
        put(EquipmentSlotGroup.CHEST, "Chest");
        put(EquipmentSlotGroup.HEAD, "Head");
        put(EquipmentSlotGroup.ARMOR, "Armor");
        put(EquipmentSlotGroup.BODY, "Body");
        put(EquipmentSlotGroup.SADDLE, "Saddle");

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

        put(Rarity.COMMON, "Common");
        put(Rarity.UNCOMMON, "Uncommon");
        put(Rarity.RARE, "Rare");
        put(Rarity.EPIC, "Epic");

        put(MapPostProcessing.LOCK, "Lock");
        put(MapPostProcessing.SCALE, "Scale");

        put(FireworkExplosion.Shape.SMALL_BALL, "Small Ball");
        put(FireworkExplosion.Shape.LARGE_BALL, "Large Ball");
        put(FireworkExplosion.Shape.STAR, "Star");
        put(FireworkExplosion.Shape.CREEPER, "Creeper");
        put(FireworkExplosion.Shape.BURST, "Burst");

        put(CopyCustomDataFunction.MergeStrategy.REPLACE, "Replace");
        put(CopyCustomDataFunction.MergeStrategy.APPEND, "Append");
        put(CopyCustomDataFunction.MergeStrategy.MERGE, "Merge");

        put(AttributeModifier.Operation.ADD_VALUE, "Add Value");
        put(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "Multiply Base");
        put(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, "Multiply Total");

        put(LootContext.EntityTarget.THIS, "This Entity");
        put(LootContext.EntityTarget.ATTACKER, "Attacker");
        put(LootContext.EntityTarget.DIRECT_ATTACKER, "Direct Attacker");
        put(LootContext.EntityTarget.ATTACKING_PLAYER, "Attacking Player");
        put(LootContext.EntityTarget.TARGET_ENTITY, "Target Entity");
        put(LootContext.EntityTarget.INTERACTING_ENTITY, "Interacting Entity");

        put(SetNameFunction.Target.CUSTOM_NAME, "Custom Name");
        put(SetNameFunction.Target.ITEM_NAME, "Item Name");

        put(ListOperation.Type.REPLACE_ALL, "Replace All");
        put(ListOperation.Type.REPLACE_SECTION, "Replace Section");
        put(ListOperation.Type.INSERT, "Insert");
        put(ListOperation.Type.APPEND, "Append");

        put(ItemUseAnimation.NONE, "None");
        put(ItemUseAnimation.EAT, "Eat");
        put(ItemUseAnimation.DRINK, "Drink");
        put(ItemUseAnimation.BLOCK, "Block");
        put(ItemUseAnimation.BOW, "Bow");
        put(ItemUseAnimation.TRIDENT, "Trident");
        put(ItemUseAnimation.CROSSBOW, "Crossbow");
        put(ItemUseAnimation.SPYGLASS, "Spyglass");
        put(ItemUseAnimation.TOOT_HORN, "Toot Horn");
        put(ItemUseAnimation.BRUSH, "Brush");
        put(ItemUseAnimation.BUNDLE, "Bundle");
        put(ItemUseAnimation.SPEAR, "Spear");

        put(SwingAnimationType.NONE, "None");
        put(SwingAnimationType.WHACK, "Whack");
        put(SwingAnimationType.STAB, "Stab");

        put(Fox.Variant.RED, "Red");
        put(Fox.Variant.SNOW, "Snow");

        put(Salmon.Variant.SMALL, "Small");
        put(Salmon.Variant.MEDIUM, "Medium");
        put(Salmon.Variant.LARGE, "Large");

        put(Parrot.Variant.RED_BLUE, "Red & Blue");
        put(Parrot.Variant.BLUE, "Blue");
        put(Parrot.Variant.GREEN, "Green");
        put(Parrot.Variant.YELLOW_BLUE, "Yellow & Blue");
        put(Parrot.Variant.GRAY, "Gray");

        put(TropicalFish.Pattern.KOB, "Kob");
        put(TropicalFish.Pattern.SUNSTREAK, "Sunstreak");
        put(TropicalFish.Pattern.SNOOPER, "Snooper");
        put(TropicalFish.Pattern.DASHER, "Dasher");
        put(TropicalFish.Pattern.BRINELY, "Brinely");
        put(TropicalFish.Pattern.SPOTTY, "Spotty");
        put(TropicalFish.Pattern.FLOPPER, "Flopper");
        put(TropicalFish.Pattern.STRIPEY, "Stripey");
        put(TropicalFish.Pattern.GLITTER, "Glitter");
        put(TropicalFish.Pattern.BLOCKFISH, "Blockfish");
        put(TropicalFish.Pattern.BETTY, "Betty");
        put(TropicalFish.Pattern.CLAYFISH, "Clayfish");

        put(MushroomCow.Variant.RED, "Red");
        put(MushroomCow.Variant.BROWN, "Brown");

        put(Rabbit.Variant.BROWN, "Brown");
        put(Rabbit.Variant.WHITE, "White");
        put(Rabbit.Variant.BLACK, "Black");
        put(Rabbit.Variant.WHITE_SPLOTCHED, "Black & White");
        put(Rabbit.Variant.GOLD, "Gold");
        put(Rabbit.Variant.SALT, "Salt & Pepper");
        put(Rabbit.Variant.EVIL, "Killer Bunny");

        put(Variant.WHITE, "White");
        put(Variant.CREAMY, "Creamy");
        put(Variant.CHESTNUT, "Chestnut");
        put(Variant.BROWN, "Brown");
        put(Variant.BLACK, "Black");
        put(Variant.GRAY, "Gray");
        put(Variant.DARK_BROWN, "Dark Brown");

        put(Llama.Variant.CREAMY, "Creamy");
        put(Llama.Variant.WHITE, "White");
        put(Llama.Variant.BROWN, "Brown");
        put(Llama.Variant.GRAY, "Gray");

        put(Axolotl.Variant.LUCY, "Pink");
        put(Axolotl.Variant.WILD, "Brown");
        put(Axolotl.Variant.GOLD, "Gold");
        put(Axolotl.Variant.CYAN, "Cyan");
        put(Axolotl.Variant.BLUE, "Blue");

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
        TRANSLATION_MAP.put("emi.category.ali.trial_chambers", "Trial Chambers");
        TRANSLATION_MAP.put("emi.category.ali.panda_sneeze", "Panda Sneeze");
        TRANSLATION_MAP.put("emi.category.ali.shearing", "Shearing");
        TRANSLATION_MAP.put("emi.category.ali.armadillo_shed", "Armadillo Shed");
        TRANSLATION_MAP.put("emi.category.ali.chicken_lay", "Chicken Lay");
        TRANSLATION_MAP.put("emi.category.ali.carving", "Carving");
        TRANSLATION_MAP.put("emi.category.ali.harvesting", "Harvesting");
        TRANSLATION_MAP.put("emi.category.ali.turtle_grow", "Turtle Grow");
        TRANSLATION_MAP.put("emi.category.ali.charged_creeper", "Charged Creeper");

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
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/baby_gift", "Baby Gift");
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
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/unemployed_gift", "Unemployed Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/hero_of_the_village/weaponsmith_gift", "Weaponsmith Gift");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing", "Fishing");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/junk", "Fishing: Junk");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/fish", "Fishing: Fish");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/fishing/treasure", "Fishing: Treasure");

        TRANSLATION_MAP.put("ali/loot_table/gameplay/sniffer_digging", "Sniffer Digging");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/cat_morning_gift", "Cat Morning Gift");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/piglin_bartering", "Piglin Bartering");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/panda_sneeze", "Panda Sneeze");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/armadillo_shed", "Armadillo Shed");
        TRANSLATION_MAP.put("ali/loot_table/gameplay/chicken_lay", "Chicken Lay");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/black", "Black Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/blue", "Blue Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/brown", "Brown Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/cyan", "Cyan Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/gray", "Gray Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/green", "Green Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/light_blue", "Light Blue Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/light_gray", "Light Gray Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/lime", "Lime Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/magenta", "Magenta Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/orange", "Orange Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/pink", "Pink Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/purple", "Purple Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/red", "Red Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/white", "White Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/sheep/yellow", "Yellow Sheep");
        TRANSLATION_MAP.put("ali/loot_table/entities/snow_golem", "Snow Golem");

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

        TRANSLATION_MAP.put("ali/loot_table/shearing/bogged", "Bogged Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/mooshroom", "Mooshroom Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/mooshroom/brown", "Brown Mooshroom Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/mooshroom/red", "Red Mooshroom Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep", "Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/black", "Black Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/blue", "Blue Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/brown", "Brown Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/cyan", "Cyan Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/gray", "Gray Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/green", "Green Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/light_blue", "Light Blue Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/light_gray", "Light Gray Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/lime", "Lime Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/magenta", "Magenta Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/orange", "Orange Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/pink", "Pink Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/purple", "Purple Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/red", "Red Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/white", "White Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/sheep/yellow", "Yellow Sheep Shearing");
        TRANSLATION_MAP.put("ali/loot_table/shearing/snow_golem", "Snow Golem Shearing");
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
