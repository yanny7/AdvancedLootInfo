package com.yanny.advanced_loot_info.plugin;

import com.yanny.advanced_loot_info.api.AliEntrypoint;
import com.yanny.advanced_loot_info.api.IPlugin;
import com.yanny.advanced_loot_info.api.IRegistry;
import com.yanny.advanced_loot_info.plugin.function.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

@AliEntrypoint
public class VanillaPlugin implements IPlugin {
    public static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public void register(IRegistry registry) {
        registry.registerFunction(ApplyBonusFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.APPLY_BONUS), ApplyBonusFunction::new, ApplyBonusFunction::new);
        registry.registerFunction(CopyNameFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.COPY_NAME), CopyNameFunction::new, CopyNameFunction::new);
        registry.registerFunction(CopyNbtFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.COPY_NBT), CopyNbtFunction::new, CopyNbtFunction::new);
        registry.registerFunction(CopyStateFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.COPY_STATE), CopyStateFunction::new, CopyStateFunction::new);
        registry.registerFunction(EnchantRandomlyFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.ENCHANT_RANDOMLY), EnchantRandomlyFunction::new, EnchantRandomlyFunction::new);
        registry.registerFunction(EnchantWithLevelsFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.ENCHANT_WITH_LEVELS), EnchantWithLevelsFunction::new, EnchantWithLevelsFunction::new);
        registry.registerFunction(ExplorationMapFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.EXPLORATION_MAP), ExplorationMapFunction::new, ExplorationMapFunction::new);
        registry.registerFunction(ExplosionDecayFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.EXPLOSION_DECAY), ExplosionDecayFunction::new, ExplosionDecayFunction::new);
        registry.registerFunction(FillPlayerHeadFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.FILL_PLAYER_HEAD), FillPlayerHeadFunction::new, FillPlayerHeadFunction::new);
        registry.registerFunction(FurnaceSmeltFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.FURNACE_SMELT), FurnaceSmeltFunction::new, FurnaceSmeltFunction::new);
        registry.registerFunction(LimitCountFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.LIMIT_COUNT), LimitCountFunction::new, LimitCountFunction::new);
        registry.registerFunction(LootingEnchantFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.LOOTING_ENCHANT), LootingEnchantFunction::new, LootingEnchantFunction::new);
        registry.registerFunction(ReferenceFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.REFERENCE), ReferenceFunction::new, ReferenceFunction::new);
        registry.registerFunction(SetAttributesFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_ATTRIBUTES), SetAttributesFunction::new, SetAttributesFunction::new);
        registry.registerFunction(SetBannerPatternFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_BANNER_PATTERN), SetBannerPatternFunction::new, SetBannerPatternFunction::new);
        registry.registerFunction(SetContentsFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_CONTENTS), SetContentsFunction::new, SetContentsFunction::new);
        registry.registerFunction(SetCountFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_COUNT), SetCountFunction::new, SetCountFunction::new);
        registry.registerFunction(SetDamageFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_DAMAGE), SetDamageFunction::new, SetDamageFunction::new);
        registry.registerFunction(SetEnchantmentsFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_ENCHANTMENTS), SetEnchantmentsFunction::new, SetEnchantmentsFunction::new);
        registry.registerFunction(SetInstrumentFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_INSTRUMENT), SetInstrumentFunction::new, SetInstrumentFunction::new);
        registry.registerFunction(SetLootTableFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_LOOT_TABLE), SetLootTableFunction::new, SetLootTableFunction::new);
        registry.registerFunction(SetLoreFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_LORE), SetLoreFunction::new, SetLoreFunction::new);
        registry.registerFunction(SetNameFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_NAME), SetNameFunction::new, SetNameFunction::new);
        registry.registerFunction(SetNbtFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_NBT), SetNbtFunction::new, SetNbtFunction::new);
        registry.registerFunction(SetPotionFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_POTION), SetPotionFunction::new, SetPotionFunction::new);
        registry.registerFunction(SetStewEffectFunction.class, BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(LootItemFunctions.SET_STEW_EFFECT), SetStewEffectFunction::new, SetStewEffectFunction::new);
        registry.registerFunction(UnknownFunction.class, UNKNOWN, UnknownFunction::new, UnknownFunction::new);
    }
}
