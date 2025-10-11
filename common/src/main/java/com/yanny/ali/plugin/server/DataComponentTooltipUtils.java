package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.ChickenVariant;
import net.minecraft.world.entity.animal.CowVariant;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getTrimMaterialTooltip;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getTrimPatternTooltip;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomDataTooltip(IServerUtils utils, CustomData value) {
        return getStringTooltip(utils, "ali.property.value.tag", value.copyTag().toString());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntTooltip(IServerUtils utils, int value) {
        return getIntegerTooltip(utils, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomNameTooltip(IServerUtils utils, Component value) {
        return getComponentTooltip(utils, "ali.property.value.custom_name", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getItemNameTooltip(IServerUtils utils, Component value) {
        return getComponentTooltip(utils, "ali.property.value.item_name", value);
    }

    @NotNull
    public static ITooltipNode getItemLoreTooltip(IServerUtils utils, ItemLore value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.lines", "ali.property.value.null", value.lines(), GenericTooltipUtils::getComponentTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.styled_lines", "ali.property.value.null", value.styledLines(), GenericTooltipUtils::getComponentTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRarityTooltip(IServerUtils utils, Rarity value) {
        return getEnumTooltip(utils, "ali.property.value.rarity", value);
    }

    @NotNull
    public static ITooltipNode getItemEnchantmentsTooltip(IServerUtils utils, ItemEnchantments value) {
        return getMapTooltip(utils, "ali.property.branch.enchantments", value.enchantments, GenericTooltipUtils::getEnchantmentLevelEntryTooltip);
    }

    @NotNull
    public static ITooltipNode getAdventureModePredicateTooltip(IServerUtils utils, AdventureModePredicate value) {
        return getCollectionTooltip(utils, "ali.property.branch.blocks", "ali.property.branch.predicate", value.predicates, GenericTooltipUtils::getBlockPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getAttributeModifiersTooltip(IServerUtils utils, ItemAttributeModifiers value) {
        return getCollectionTooltip(utils, "ali.property.branch.modifiers", "ali.property.branch.modifier", value.modifiers(), GenericTooltipUtils::getItemAttributeModifiersEntryTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomModelDataTooltip(IServerUtils utils, CustomModelData value) {
        ITooltipNode tooltip = new TooltipNode();
        
        tooltip.add(getStringTooltip(utils, "ali.property.value.floats", value.floats().toString()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.flags", value.flags().toString()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.strings", value.strings().toString()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.colors", value.colors().toString()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTooltipDisplayTooltip(IServerUtils utils, TooltipDisplay value) {
        ITooltipNode components = new TooltipNode();

        components.add(getBooleanTooltip(utils, "ali.property.value.hide_tooltip", value.hideTooltip()));
        components.add(getCollectionTooltip(utils, "ali.property.branch.hidden_components", "ali.property.value.null", value.hiddenComponents(), RegistriesTooltipUtils::getDataComponentTypeTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEmptyTooltip(IServerUtils ignoredUtils, Unit ignoredValue) {
        return new TooltipNode();
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBoolTooltip(IServerUtils utils, boolean value) {
        return getBooleanTooltip(utils, "ali.property.value.value", value);
    }

    @NotNull
    public static ITooltipNode getFoodTooltip(IServerUtils utils, FoodProperties food) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.nutrition", food.nutrition()));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.saturation", food.saturation()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.can_always_eat", food.canAlwaysEat()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getConsumableTooltip(IServerUtils utils, Consumable consumable) {
        ITooltipNode components = new TooltipNode();

        components.add(getFloatTooltip(utils, "ali.property.value.consume_seconds", consumable.consumeSeconds()));
        components.add(getEnumTooltip(utils, "ali.property.value.animation", consumable.animation()));
        components.add(getHolderTooltip(utils, "ali.property.value.sound", consumable.sound(), RegistriesTooltipUtils::getSoundEventTooltip));
        components.add(getBooleanTooltip(utils, "ali.property.value.has_custom_particles", consumable.hasConsumeParticles()));
        components.add(getCollectionTooltip(utils, "ali.property.branch.on_consume_effects", consumable.onConsumeEffects(), utils::getConsumeEffectTooltip));

        return components;
    }

    @NotNull
    public static ITooltipNode getUseRemainderTooltip(IServerUtils utils, UseRemainder remainder) {
        return getItemStackTooltip(utils, "ali.property.branch.convert_into", remainder.convertInto());
    }

    @NotNull
    public static ITooltipNode getUseCooldownTooltip(IServerUtils utils, UseCooldown cooldown) {
        ITooltipNode components = new TooltipNode();

        components.add(getFloatTooltip(utils, "ali.property.value.seconds", cooldown.seconds()));
        components.add(getOptionalTooltip(utils, "ali.property.value.cooldown_group", cooldown.cooldownGroup(), GenericTooltipUtils::getResourceLocationTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getDamageResistantTooltip(IServerUtils utils, DamageResistant resistant) {
        return getTagKeyTooltip(utils, "ali.property.value.type", resistant.types());
    }

    @NotNull
    public static ITooltipNode getToolTooltip(IServerUtils utils, Tool tool) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.rules", "ali.property.branch.rule", tool.rules(), GenericTooltipUtils::getRuleTooltip));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.default_mining_speed", tool.defaultMiningSpeed()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.damage_per_block", tool.damagePerBlock()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.can_destroy_blocks_in_creative", tool.canDestroyBlocksInCreative()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getWeaponTooltip(IServerUtils utils, Weapon weapon) {
        ITooltipNode components = new TooltipNode();

        components.add(getIntegerTooltip(utils, "ali.property.value.item_damage_per_attack", weapon.itemDamagePerAttack()));
        components.add(getFloatTooltip(utils, "ali.property.value.disable_blocking_for_seconds", weapon.disableBlockingForSeconds()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEnchantableTooltip(IServerUtils utils, Enchantable enchantable) {
        return getIntegerTooltip(utils, "ali.property.value.value", enchantable.value());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEquipableTooltip(IServerUtils utils, Equippable equippable) {
        ITooltipNode components = new TooltipNode();

        components.add(getEnumTooltip(utils, "ali.property.value.equipment_slot", equippable.slot()));
        components.add(getHolderTooltip(utils, "ali.property.value.equip_sound", equippable.equipSound(), RegistriesTooltipUtils::getSoundEventTooltip));
        components.add(getOptionalTooltip(utils, "ali.property.value.asset_id", equippable.assetId(), GenericTooltipUtils::getResourceKeyTooltip));
        components.add(getOptionalTooltip(utils, "ali.property.value.camera_overlay", equippable.cameraOverlay(), GenericTooltipUtils::getResourceLocationTooltip));
        components.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.allowed_entities", "ali.property.value.null", equippable.allowedEntities(), RegistriesTooltipUtils::getEntityTypeTooltip));
        components.add(getBooleanTooltip(utils, "ali.property.value.dispensable", equippable.dispensable()));
        components.add(getBooleanTooltip(utils, "ali.property.value.swappable", equippable.swappable()));
        components.add(getBooleanTooltip(utils, "ali.property.value.damage_on_hurt", equippable.damageOnHurt()));
        components.add(getBooleanTooltip(utils, "ali.property.value.equip_on_interact", equippable.equipOnInteract()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRepairableTooltip(IServerUtils utils, Repairable repairable) {
        return getHolderSetTooltip(utils, "ali.property.branch.items", "ali.property.value.null", repairable.items(), RegistriesTooltipUtils::getItemTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getDeathProtectionTooltip(IServerUtils utils, DeathProtection protection) {
        return getCollectionTooltip(utils, "ali.property.branch.death_effects", protection.deathEffects(), utils::getConsumeEffectTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBlockAttacksTooltip(IServerUtils utils, BlocksAttacks value) {
        ITooltipNode components = new TooltipNode();

        components.add(getFloatTooltip(utils, "ali.property.value.block_delay_seconds", value.blockDelaySeconds()));
        components.add(getFloatTooltip(utils, "ali.property.value.disable_cooldown_scale", value.disableCooldownScale()));
        components.add(getCollectionTooltip(utils, "ali.property.branch.damage_reductions", "ali.property.branch.damage_reduction", value.damageReductions(), GenericTooltipUtils::getDamageReductionTooltip));
        components.add(getItemDamageTooltip(utils, "ali.property.branch.item_damage", value.itemDamage()));
        components.add(getOptionalTooltip(utils, "ali.property.value.bypassed_by", value.bypassedBy(), GenericTooltipUtils::getTagKeyTooltip));
        components.add(getOptionalHolderTooltip(utils, "ali.property.value.block_sound", value.blockSound(), RegistriesTooltipUtils::getSoundEventTooltip));
        components.add(getOptionalHolderTooltip(utils, "ali.property.value.disable_sound", value.disableSound(), RegistriesTooltipUtils::getSoundEventTooltip));

        return components;
    }

    @NotNull
    public static ITooltipNode getDyedColorTooltip(IServerUtils utils, DyedItemColor value) {
        return getIntegerTooltip(utils, "ali.property.value.rgb", value.rgb());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapColorTooltip(IServerUtils utils, MapItemColor value) {
        return getIntegerTooltip(utils, "ali.property.value.rgb", value.rgb());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapIdTooltip(IServerUtils utils, MapId value) {
        return getIntegerTooltip(utils, "ali.property.value.value", value.id());
    }

    @NotNull
    public static ITooltipNode getMapDecorationsTooltip(IServerUtils utils, MapDecorations value) {
        return getMapTooltip(utils, "ali.property.branch.decorations", value.decorations(), GenericTooltipUtils::getMapDecorationEntryTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapPostProcessingTooltip(IServerUtils utils, MapPostProcessing value) {
        return getEnumTooltip(utils, "ali.property.value.value", value);
    }

    @NotNull
    public static ITooltipNode getChargedProjectilesTooltip(IServerUtils utils, ChargedProjectiles value) {
        return getCollectionTooltip(utils, "ali.property.branch.items", "ali.property.branch.item", value.getItems(), GenericTooltipUtils::getItemStackTooltip);
    }

    @NotNull
    public static ITooltipNode getBundleContentsTooltip(IServerUtils utils, BundleContents value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.items", "ali.property.branch.item", value.items, GenericTooltipUtils::getItemStackTooltip));
        tooltip.add(getStringTooltip(utils, "ali.property.value.fraction", value.weight().toString()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getPotionContentsTooltip(IServerUtils utils, PotionContents value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getOptionalHolderTooltip(utils, "ali.property.value.potion", value.potion(), RegistriesTooltipUtils::getPotionTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.custom_color", value.customColor(), GenericTooltipUtils::getIntegerTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.custom_effects", "ali.property.value.null", value.customEffects(), GenericTooltipUtils::getMobEffectInstanceTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.custom_name", value.customName(), GenericTooltipUtils::getStringTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFloatValueTooltip(IServerUtils utils, float value) {
        return getFloatTooltip(utils, "ali.property.value.value", value);
    }

    @NotNull
    public static ITooltipNode getSuspiciousStewEffectsTooltip(IServerUtils utils, SuspiciousStewEffects value) {
        return getCollectionTooltip(utils, "ali.property.branch.effects", value.effects(), GenericTooltipUtils::getSuspiciousStewEffectEntryTooltip);
    }

    @NotNull
    public static ITooltipNode getWritableBookContentTooltip(IServerUtils utils, WritableBookContent value) {
        return getFilterableTooltip(utils, "ali.property.branch.pages", "ali.property.branch.page", value.pages(), GenericTooltipUtils::getStringTooltip);
    }

    @NotNull
    public static ITooltipNode getWrittenBookContentTooltip(IServerUtils utils, WrittenBookContent value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getFilterableTooltip(utils, "ali.property.branch.title", value.title(), GenericTooltipUtils::getStringTooltip));
        tooltip.add(getStringTooltip(utils, "ali.property.value.author", value.author()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.generation", value.generation()));
        tooltip.add(getFilterableTooltip(utils, "ali.property.branch.pages", "ali.property.branch.page", value.pages(), GenericTooltipUtils::getComponentTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.resolved", value.resolved()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getTrimTooltip(IServerUtils utils, ArmorTrim value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getTrimMaterialTooltip(utils, "ali.property.value.material", value.material().value()));
        tooltip.add(getTrimPatternTooltip(utils, "ali.property.value.pattern", value.pattern().value()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDebugStickStateTooltip(IServerUtils utils, DebugStickState value) {
        return getMapTooltip(utils, "ali.property.branch.properties", value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getInstrumentTooltip(IServerUtils utils, InstrumentComponent value) {
        return getEitherHolderTooltip(utils, "ali.property.value.value", value.instrument(), RegistriesTooltipUtils::getInstrumentTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getProvidesTrimMaterialTooltip(IServerUtils utils, ProvidesTrimMaterial value) {
        return getEitherHolderTooltip(utils, "ali.property.value.material", value.material(), RegistriesTooltipUtils::getTrimMaterialTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getOminousBottleAmplifierTooltip(IServerUtils utils, OminousBottleAmplifier value) {
        return getIntegerTooltip(utils, "ali.property.value.value", value.value());
    }

    @NotNull
    public static ITooltipNode getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayable value) {
        return getEitherHolderTooltip(utils, "ali.property.value.song", value.song(), RegistriesTooltipUtils::getJukeboxSongTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getProvidesBannerPatternsTooltip(IServerUtils utils, TagKey<BannerPattern> value) {
        return getTagKeyTooltip(utils, "ali.property.value.banner_pattern", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRecipesTooltip(IServerUtils utils, List<ResourceKey<Recipe<?>>> value) {
        return getCollectionTooltip(utils, "ali.property.branch.recipes", "ali.property.value.null", value, GenericTooltipUtils::getResourceKeyTooltip);
    }

    @NotNull
    public static ITooltipNode getLodestoneTrackerTooltip(IServerUtils utils, LodestoneTracker value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.global_pos", value.target(), GenericTooltipUtils::getGlobalPosTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.tracked", value.tracked()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getEnumTooltip(utils, "ali.property.value.shape", value.shape()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.colors", value.colors().toString()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.fade_colors", value.fadeColors().toString()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.has_trail", value.hasTrail()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.has_twinkle", value.hasTwinkle()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getFireworksTooltip(IServerUtils utils, Fireworks value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.flight_duration", value.flightDuration()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.explosions", "ali.property.branch.explosion", value.explosions(), GenericTooltipUtils::getFireworkExplosionTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getProfileTooltip(IServerUtils utils, ResolvableProfile value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.name", value.name(), GenericTooltipUtils::getStringTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getResourceLocationTooltip(IServerUtils utils, ResourceLocation value) {
        return GenericTooltipUtils.getResourceLocationTooltip(utils, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBannerPatternsTooltip(IServerUtils utils, BannerPatternLayers value) {
        return getBannerPatternLayersTooltip(utils, "ali.property.branch.banner_patterns", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getDyeColorTooltip(IServerUtils utils, DyeColor value) {
        return getEnumTooltip(utils, "ali.property.value.color", value);
    }

    @NotNull
    public static ITooltipNode getPotDecorationsTooltip(IServerUtils utils, PotDecorations value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.back", value.back(), RegistriesTooltipUtils::getItemTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.left", value.left(), RegistriesTooltipUtils::getItemTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.right", value.right(), RegistriesTooltipUtils::getItemTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.front", value.front(), RegistriesTooltipUtils::getItemTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getContainerTooltip(IServerUtils utils, ItemContainerContents value) {
        return getCollectionTooltip(utils, "ali.property.branch.items", "ali.property.branch.item", value.items, GenericTooltipUtils::getItemStackTooltip);
    }

    @NotNull
    public static ITooltipNode getBlockStateTooltip(IServerUtils ignoredUtils, BlockItemStateProperties properties) {
        return getMapTooltip(ignoredUtils, "ali.property.branch.properties", properties.properties(), GenericTooltipUtils::getStringEntryTooltip);
    }

    @NotNull
    public static ITooltipNode getBeesTooltip(IServerUtils utils, Bees bees) {
        return getCollectionTooltip(utils, "ali.property.branch.bees", "ali.property.branch.occupant", bees.bees(), GenericTooltipUtils::getBeehiveBlockEntityOccupantTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLockTooltip(IServerUtils utils, LockCode lockCode) {
        return getItemPredicateTooltip(utils, "ali.property.branch.predicate", lockCode.predicate());
    }

    @NotNull
    public static ITooltipNode getContainerLootTooltip(IServerUtils utils, SeededContainerLoot value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getResourceKeyTooltip(utils, "ali.property.value.loot_table", value.lootTable()));
        tooltip.add(getLongTooltip(utils, "ali.property.value.seed", value.seed()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getBreakSoundTooltip(IServerUtils utils, Holder<SoundEvent> soundEvent) {
        return getHolderTooltip(utils, "ali.property.value.sound", soundEvent, RegistriesTooltipUtils::getSoundEventTooltip);
    }

    @NotNull
    public static ITooltipNode getVillagerVariantTooltip(IServerUtils utils, Holder<VillagerType> villager) {
        return getHolderTooltip(utils, "ali.property.value.type", villager, RegistriesTooltipUtils::getVillagerTypeTooltip);
    }

    @NotNull
    public static ITooltipNode getWolfVariantTooltip(IServerUtils utils, Holder<WolfVariant> wolf) {
        return getHolderTooltip(utils, "ali.property.value.type", wolf, RegistriesTooltipUtils::getWolfVariantTooltip);
    }

    @NotNull
    public static ITooltipNode getWolfSoundVariantTooltip(IServerUtils utils, Holder<WolfSoundVariant> wolfSound) {
        return getHolderTooltip(utils, "ali.property.value.type", wolfSound, RegistriesTooltipUtils::getWolfSoundVariantTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEnumTypeTooltip(IServerUtils utils, Enum<?> type) {
        return getEnumTooltip(utils, "ali.property.value.type", type);
    }

    @NotNull
    public static ITooltipNode getPigVariantTooltip(IServerUtils utils, Holder<PigVariant> pigVariant) {
        return getHolderTooltip(utils, "ali.property.value.type", pigVariant, RegistriesTooltipUtils::getPigVariantTooltip);
    }

    @NotNull
    public static ITooltipNode getCowVariantTooltip(IServerUtils utils, Holder<CowVariant> cowVariant) {
        return getHolderTooltip(utils, "ali.property.value.type", cowVariant, RegistriesTooltipUtils::getCowVariantTooltip);
    }

    @NotNull
    public static ITooltipNode getChickenVariantTooltip(IServerUtils utils, EitherHolder<ChickenVariant> holder) {
        return getEitherHolderTooltip(utils, "ali.property.value.type", holder, RegistriesTooltipUtils::getChickenVariantTooltip);
    }

    @NotNull
    public static ITooltipNode getFrogVariantTooltip(IServerUtils utils, Holder<FrogVariant> frogVariant) {
        return getHolderTooltip(utils, "ali.property.value.type", frogVariant, RegistriesTooltipUtils::getFrogVariantTooltip);
    }

    @NotNull
    public static ITooltipNode getPaintingVariantTooltip(IServerUtils utils, Holder<PaintingVariant> paintingVariant) {
        return getHolderTooltip(utils, "ali.property.value.type", paintingVariant, RegistriesTooltipUtils::getPaintingVariantTooltip);
    }

    @NotNull
    public static ITooltipNode getCatVariantTooltip(IServerUtils utils, Holder<CatVariant> catVariant) {
        return getHolderTooltip(utils, "ali.property.value.type", catVariant, RegistriesTooltipUtils::getCatVariantTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTypedEntityDataTooltip(IServerUtils utils, TypedEntityData<?> value) {
        return getStringTooltip(utils, "ali.property.value.tag", value.copyTagWithoutId().toString());
    }
}
