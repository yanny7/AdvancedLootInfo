package com.yanny.ali.plugin.server;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getRegistryTooltip;

public class RegistriesTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getEntryTypeTooltip(IServerUtils utils, LootPoolEntryType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getFunctionTypeTooltip(IServerUtils utils, LootItemFunctionType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_FUNCTION_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getConditionTypeTooltip(IServerUtils utils, LootItemConditionType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_CONDITION_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getConsumeEffectTypeTooltip(IServerUtils utils, ConsumeEffect.Type<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.CONSUME_EFFECT_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getBlockTooltip(IServerUtils utils, Block block) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.value(TooltipBuilder.translate(block.getDescriptionId()));
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Block name: {}", BuiltInRegistries.BLOCK.getKey(block), e);
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block);
    }

    @NotNull
    public static TooltipBuilder getItemTooltip(IServerUtils utils, Item item) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.value(TooltipBuilder.translate(item.getDescriptionId()));
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Item name: {}", BuiltInRegistries.ITEM.getKey(item), e);
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ITEM, item);
    }

    @NotNull
    public static TooltipBuilder getEntityTypeTooltip(IServerUtils utils, EntityType<?> entityType) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.value(TooltipBuilder.translate(entityType.getDescriptionId()));
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized EntityType name: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType), e);
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENTITY_TYPE, entityType);
    }

    @NotNull
    public static TooltipBuilder getBannerPatternTooltip(IServerUtils utils, BannerPattern bannerPattern) {
        return getRegistryTooltip(utils, Registries.BANNER_PATTERN, bannerPattern);
    }

    @NotNull
    public static TooltipBuilder getBlockEntityTypeTooltip(IServerUtils utils, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType);
    }

    @NotNull
    public static TooltipBuilder getPotionTooltip(IServerUtils utils, Potion potion) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.POTION, potion);
    }

    @NotNull
    public static TooltipBuilder getMobEffectTooltip(IServerUtils utils, MobEffect mobEffect) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.value(TooltipBuilder.translate(mobEffect.getDescriptionId()));
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized MobEffect name: {}", BuiltInRegistries.MOB_EFFECT.getKey(mobEffect), e);
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.MOB_EFFECT, mobEffect);
    }

    @NotNull
    public static TooltipBuilder getLootNbtProviderTypeTooltip(IServerUtils utils, LootNbtProviderType providerType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, providerType);
    }

    @NotNull
    public static TooltipBuilder getFluidTooltip(IServerUtils utils, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FLUID, fluid);
    }

    @NotNull
    public static TooltipBuilder getEnchantmentTooltip(IServerUtils utils, Enchantment enchantment) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.component(utils.lookupProvider(), enchantment.description());
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Enchantment name", e);
            }
        }

        return getRegistryTooltip(utils, Registries.ENCHANTMENT, enchantment);
    }

    @NotNull
    public static TooltipBuilder getAttributeTooltip(IServerUtils utils, Attribute attribute) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.value(TooltipBuilder.translate(attribute.getDescriptionId()));
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Attribute name: {}", BuiltInRegistries.ATTRIBUTE.getKey(attribute), e);
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ATTRIBUTE, attribute);
    }

    @NotNull
    public static TooltipBuilder getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.DATA_COMPONENT_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getInstrumentTooltip(IServerUtils utils, Instrument value) {
        return getRegistryTooltip(utils, Registries.INSTRUMENT, value);
    }

    @NotNull
    public static TooltipBuilder getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate predicate) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, predicate.codec());
    }

    @NotNull
    public static TooltipBuilder getSlotSourceTooltip(IServerUtils utils, SlotSource slotSource) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.SLOT_SOURCE_TYPE, slotSource.codec());
    }

    @NotNull
    public static TooltipBuilder getCatVariantTooltip(IServerUtils utils, CatVariant catVariant) {
        return getRegistryTooltip(utils, Registries.CAT_VARIANT, catVariant);
    }

    @NotNull
    public static TooltipBuilder getPaintingVariantTooltip(IServerUtils utils, PaintingVariant paintingVariant) {
        return getRegistryTooltip(utils, Registries.PAINTING_VARIANT, paintingVariant);
    }

    @NotNull
    public static TooltipBuilder getFrogVariantTooltip(IServerUtils utils, FrogVariant frogVariant) {
        return getRegistryTooltip(utils, Registries.FROG_VARIANT, frogVariant);
    }

    @NotNull
    public static TooltipBuilder getWolfVariantTooltip(IServerUtils utils, WolfVariant wolfVariant) {
        return getRegistryTooltip(utils, Registries.WOLF_VARIANT, wolfVariant);
    }

    @NotNull
    public static TooltipBuilder getMapDecorationTypeTooltip(IServerUtils utils, MapDecorationType mapDecorationType) {
        return getRegistryTooltip(utils, Registries.MAP_DECORATION_TYPE, mapDecorationType);
    }

    @NotNull
    public static TooltipBuilder getBiomeTooltip(IServerUtils utils, Biome biome) {
        return getRegistryTooltip(utils, Registries.BIOME, biome);
    }

    @NotNull
    public static TooltipBuilder getStructureTooltip(IServerUtils utils, Structure structure) {
        return getRegistryTooltip(utils, Registries.STRUCTURE, structure);
    }

    @NotNull
    public static TooltipBuilder getTrimMaterialTooltip(IServerUtils utils, TrimMaterial material) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.component(utils.lookupProvider(), material.description());
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized TrimMaterial name", e);
            }
        }

        return getRegistryTooltip(utils, Registries.TRIM_MATERIAL, material);
    }

    @NotNull
    public static TooltipBuilder getTrimPatternTooltip(IServerUtils utils, TrimPattern pattern) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.component(utils.lookupProvider(), pattern.description());
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized TrimPattern name", e);
            }
        }

        return getRegistryTooltip(utils, Registries.TRIM_PATTERN, pattern);
    }

    @NotNull
    public static TooltipBuilder getJukeboxSongTooltip(IServerUtils utils, JukeboxSong song) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return TooltipBuilder.component(utils.lookupProvider(), song.description());
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized JukeboxSong name", e);
            }
        }

        return getRegistryTooltip(utils, Registries.JUKEBOX_SONG, song);
    }

    @NotNull
    public static TooltipBuilder getSoundEventTooltip(IServerUtils utils, SoundEvent soundEvent) {
        return getRegistryTooltip(utils, Registries.SOUND_EVENT, soundEvent);
    }

    @NotNull
    public static TooltipBuilder getDamageTypeTooltip(IServerUtils utils, DamageType damageType) {
        return getRegistryTooltip(utils, Registries.DAMAGE_TYPE, damageType);
    }

    @NotNull
    public static TooltipBuilder getVillagerTypeTooltip(IServerUtils utils, VillagerType villagerType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.VILLAGER_TYPE, villagerType);
    }

    @NotNull
    public static TooltipBuilder getWolfSoundVariantTooltip(IServerUtils utils, WolfSoundVariant wolfSoundVariant) {
        return getRegistryTooltip(utils, Registries.WOLF_SOUND_VARIANT, wolfSoundVariant);
    }

    @NotNull
    public static TooltipBuilder getPigVariantTooltip(IServerUtils utils, PigVariant pigVariant) {
        return getRegistryTooltip(utils, Registries.PIG_VARIANT, pigVariant);
    }

    @NotNull
    public static TooltipBuilder getCowVariantTooltip(IServerUtils utils, CowVariant cowVariant) {
        return getRegistryTooltip(utils, Registries.COW_VARIANT, cowVariant);
    }

    @NotNull
    public static TooltipBuilder getChickenVariantTooltip(IServerUtils utils, ChickenVariant chickenVariant) {
        return getRegistryTooltip(utils, Registries.CHICKEN_VARIANT, chickenVariant);
    }

    @NotNull
    public static TooltipBuilder getDataComponentPredicateTypeTooltip(IServerUtils utils, DataComponentPredicate.Type<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getZombieNautilusVariantTooltip(IServerUtils utils, ZombieNautilusVariant zombieNautilusVariant) {
        return getRegistryTooltip(utils, Registries.ZOMBIE_NAUTILUS_VARIANT, zombieNautilusVariant);
    }
}
