package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
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

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.*;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static List<Component> getCustomDataTooltip(IClientUtils utils, int pad, CustomData value) {
        return getStringTooltip(utils, pad, "ali.property.value.tag", value.copyTag().toString());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntTooltip(IClientUtils utils, int pad, int value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCustomNameTooltip(IClientUtils utils, int pad, Component value) {
        return getComponentTooltip(utils, pad, "ali.property.value.custom_name", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemNameTooltip(IClientUtils utils, int pad, Component value) {
        return getComponentTooltip(utils, pad, "ali.property.value.item_name", value);
    }

    @NotNull
    public static List<Component> getItemLoreTooltip(IClientUtils utils, int pad, ItemLore value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.lines", value.lines(), (u, i, c) -> List.of(pad(i, c))));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.styled_lines", value.styledLines(), (u, i, c) -> List.of(pad(i, c))));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getRarityTooltip(IClientUtils utils, int pad, Rarity value) {
        return getEnumTooltip(utils, pad, "ali.property.value.rarity", value);
    }

    @NotNull
    public static List<Component> getItemEnchantmentsTooltip(IClientUtils ignoredUtils, int pad, ItemEnchantments value) {
        List<Component> components = new LinkedList<>();

        if (!value.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.enchantments")));
            value.enchantments.forEach((enchantment, level) -> components.add(pad(pad + 1,
                    translatable("ali.property.value.enchantment_with_level", enchantment.value().description(), Component.translatable("enchantment.level." + level)))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getAdventureModePredicateTooltip(IClientUtils utils, int pad, AdventureModePredicate value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.blocks", "ali.property.branch.predicate", value.predicates, GenericTooltipUtils::getBlockPredicateTooltip);
    }

    @NotNull
    public static List<Component> getAttributeModifiersTooltip(IClientUtils utils, int pad, ItemAttributeModifiers value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.modifiers", "ali.property.branch.modifier", value.modifiers(), GenericTooltipUtils::getItemAttributeModifiersEntryTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCustomModelDataTooltip(IClientUtils utils, int pad, CustomModelData value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getStringTooltip(utils, pad, "ali.property.value.floats", value.floats().toString()));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.flags", value.flags().toString()));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.strings", value.strings().toString()));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.colors", value.colors().toString()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTooltipDisplayTooltip(IClientUtils utils, int pad, TooltipDisplay value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.hide_tooltip", value.hideTooltip()));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.hidden_components", "ali.property.value.null", value.hiddenComponents(), RegistriesTooltipUtils::getDataComponentTypeTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEmptyTooltip(IClientUtils ignoredUtils, int ignoredPad, Unit ignoredValue) {
        return List.of();
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBoolTooltip(IClientUtils utils, int pad, boolean value) {
        return getBooleanTooltip(utils, pad, "ali.property.value.value", value);
    }

    @NotNull
    public static List<Component> getFoodTooltip(IClientUtils utils, int pad, FoodProperties food) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.nutrition", food.nutrition()));
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.saturation", food.saturation()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.can_always_eat", food.canAlwaysEat()));

        return components;
    }

    @NotNull
    public static List<Component> getConsumableTooltip(IClientUtils utils, int pad, Consumable consumable) {
        List<Component> components = new LinkedList<>();

        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.consume_seconds", consumable.consumeSeconds()));
        components.addAll(getEnumTooltip(utils, pad, "ali.property.value.animation", consumable.animation()));
        components.addAll(getHolderTooltip(utils, pad, "ali.property.value.sound", consumable.sound(), RegistriesTooltipUtils::getSoundEventTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.has_custom_particles", consumable.hasConsumeParticles()));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.on_consume_effects", consumable.onConsumeEffects(), utils::getConsumeEffectTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getUseRemainderTooltip(IClientUtils utils, int pad, UseRemainder remainder) {
        return getItemStackTooltip(utils, pad, "ali.property.branch.convert_into", remainder.convertInto());
    }

    @NotNull
    public static List<Component> getUseCooldownTooltip(IClientUtils utils, int pad, UseCooldown cooldown) {
        List<Component> components = new LinkedList<>();

        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.seconds", cooldown.seconds()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.cooldown_group", cooldown.cooldownGroup(), GenericTooltipUtils::getResourceLocationTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getDamageResistantTooltip(IClientUtils utils, int pad, DamageResistant resistant) {
        return getTagKeyTooltip(utils, pad, "ali.property.value.type", resistant.types());
    }

    @NotNull
    public static List<Component> getToolTooltip(IClientUtils utils, int pad, Tool tool) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.rules", "ali.property.branch.rule", tool.rules(), GenericTooltipUtils::getRuleTooltip));
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.default_mining_speed", tool.defaultMiningSpeed()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.damage_per_block", tool.damagePerBlock()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.can_destroy_blocks_in_creative", tool.canDestroyBlocksInCreative()));

        return components;
    }

    @NotNull
    public static List<Component> getWeaponTooltip(IClientUtils utils, int pad, Weapon weapon) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.item_damage_per_attack", weapon.itemDamagePerAttack()));
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.disable_blocking_for_seconds", weapon.disableBlockingForSeconds()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantableTooltip(IClientUtils utils, int pad, Enchantable enchantable) {
        return getIntegerTooltip(utils, pad, "ali.property.value.value", enchantable.value());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEquipableTooltip(IClientUtils utils, int pad, Equippable equippable) {
        List<Component> components = new LinkedList<>();

        components.addAll(getEnumTooltip(utils, pad, "ali.property.value.equipment_slot", equippable.slot()));
        components.addAll(getHolderTooltip(utils, pad, "ali.property.value.equip_sound", equippable.equipSound(), RegistriesTooltipUtils::getSoundEventTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.asset_id", equippable.assetId(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.camera_overlay", equippable.cameraOverlay(), GenericTooltipUtils::getResourceLocationTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.allowed_entities", "ali.property.value.null", equippable.allowedEntities(), RegistriesTooltipUtils::getEntityTypeTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.dispensable", equippable.dispensable()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.swappable", equippable.swappable()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.damage_on_hurt", equippable.damageOnHurt()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.equip_on_interact", equippable.equipOnInteract()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getRepairableTooltip(IClientUtils utils, int pad, Repairable repairable) {
        return getHolderSetTooltip(utils, pad, "ali.property.branch.items", "ali.property.value.null", repairable.items(), RegistriesTooltipUtils::getItemTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getDeathProtectionTooltip(IClientUtils utils, int pad, DeathProtection protection) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.death_effects", protection.deathEffects(), utils::getConsumeEffectTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockAttacksTooltip(IClientUtils utils, int pad, BlocksAttacks value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.block_delay_seconds", value.blockDelaySeconds()));
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.disable_cooldown_scale", value.disableCooldownScale()));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.damage_reductions", "ali.property.branch.damage_reduction", value.damageReductions(), GenericTooltipUtils::getDamageReductionTooltip));
        components.addAll(getItemDamageTooltip(utils, pad, "ali.property.branch.item_damage", value.itemDamage()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.bypassed_by", value.bypassedBy(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderTooltip(utils, pad, "ali.property.value.block_sound", value.blockSound(), RegistriesTooltipUtils::getSoundEventTooltip));
        components.addAll(getOptionalHolderTooltip(utils, pad, "ali.property.value.disable_sound", value.disableSound(), RegistriesTooltipUtils::getSoundEventTooltip));

        return components;    }

    @Unmodifiable
    @NotNull
    public static List<Component> getDyedColorTooltip(IClientUtils utils, int pad, DyedItemColor value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.rgb", value.rgb());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMapColorTooltip(IClientUtils utils, int pad, MapItemColor value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.rgb", value.rgb());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMapIdTooltip(IClientUtils utils, int pad, MapId value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.value", value.id());
    }

    @NotNull
    public static List<Component> getMapDecorationsTooltip(IClientUtils utils, int pad, MapDecorations value) {
        List<Component> components = new LinkedList<>();

        if (!value.decorations().isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.decorations")));
            value.decorations().forEach((string, entry) -> {
                components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.decoration", string));
                components.addAll(getMapDecorationEntryTooltip(utils, pad + 2, "ali.property.value.null", entry));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMapPostProcessingTooltip(IClientUtils utils, int pad, MapPostProcessing value) {
        return getEnumTooltip(utils, pad, "ali.property.value.value", value);
    }

    @NotNull
    public static List<Component> getChargedProjectilesTooltip(IClientUtils utils, int pad, ChargedProjectiles value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.items", "ali.property.branch.item", value.getItems(), GenericTooltipUtils::getItemStackTooltip);
    }

    @NotNull
    public static List<Component> getBundleContentsTooltip(IClientUtils utils, int pad, BundleContents value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.items", "ali.property.branch.item", value.items, GenericTooltipUtils::getItemStackTooltip));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.fraction", value.weight().toString()));

        return components;
    }

    @NotNull
    public static List<Component> getPotionContentsTooltip(IClientUtils utils, int pad, PotionContents value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderTooltip(utils, pad, "ali.property.value.potion", value.potion(), RegistriesTooltipUtils::getPotionTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.custom_color", value.customColor(), GenericTooltipUtils::getIntegerTooltip));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.custom_effects", "ali.property.value.null", value.customEffects(), GenericTooltipUtils::getMobEffectInstanceTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.custom_name", value.customName(), GenericTooltipUtils::getStringTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFloatValueTooltip(IClientUtils utils, int pad, float value) {
        return getFloatTooltip(utils, pad, "ali.property.value.value", value);
    }

    @NotNull
    public static List<Component> getSuspiciousStewEffectsTooltip(IClientUtils utils, int pad, SuspiciousStewEffects value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.effects", value.effects(), GenericTooltipUtils::getSuspiciousStewEffectEntryTooltip);
    }

    @NotNull
    public static List<Component> getWritableBookContentTooltip(IClientUtils utils, int pad, WritableBookContent value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.pages", value.pages(),
                (u, i, p) -> getFilterableTooltip(u, i, "ali.property.branch.page", p, GenericTooltipUtils::getStringTooltip));
    }

    @NotNull
    public static List<Component> getWrittenBookContentTooltip(IClientUtils utils, int pad, WrittenBookContent value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getFilterableTooltip(utils, pad, "ali.property.branch.title", value.title(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.author", value.author()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.generation", value.generation()));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.pages", value.pages(),
                (u, i, p) -> getFilterableTooltip(u, i, "ali.property.branch.page", p, GenericTooltipUtils::getComponentTooltip)));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.resolved", value.resolved()));

        return components;
    }

    @NotNull
    public static List<Component> getTrimTooltip(IClientUtils utils, int pad, ArmorTrim value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getTrimMaterialTooltip(utils, pad, "ali.property.value.material", value.material().value()));
        components.addAll(getTrimPatternTooltip(utils, pad, "ali.property.value.pattern", value.pattern().value()));

        return components;
    }

    @NotNull
    public static List<Component> getDebugStickStateTooltip(IClientUtils utils, int pad, DebugStickState value) {
        List<Component> components = new LinkedList<>();

        if (!value.properties().isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.properties")));
            value.properties().forEach((block, property) -> {
                components.addAll(getBlockTooltip(utils, pad + 1, "ali.property.value.block", block.value()));
                components.addAll(getPropertyTooltip(utils, pad + 2, "ali.property.value.property", property));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getInstrumentTooltip(IClientUtils utils, int pad, InstrumentComponent value) {
        return getEitherHolderTooltip(utils, pad, "ali.property.value.value", value.instrument(), RegistriesTooltipUtils::getInstrumentTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getProvidesTrimMaterialTooltip(IClientUtils utils, int pad, ProvidesTrimMaterial value) {
        return getEitherHolderTooltip(utils, pad, "ali.property.value.material", value.material(), RegistriesTooltipUtils::getTrimMaterialTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getOminousBottleAmplifierTooltip(IClientUtils utils, int pad, OminousBottleAmplifier value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.value", value.value());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getJukeboxPlayableTooltip(IClientUtils utils, int pad, JukeboxPlayable value) {
        return getEitherHolderTooltip(utils, pad, "ali.property.value.song", value.song(), RegistriesTooltipUtils::getJukeboxSongTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getProvidesBannerPatternsTooltip(IClientUtils utils, int pad, TagKey<BannerPattern> value) {
        return getTagKeyTooltip(utils, pad, "ali.property.value.banner_pattern", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getRecipesTooltip(IClientUtils utils, int pad, List<ResourceKey<Recipe<?>>> value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.recipes", "ali.property.value.null", value, GenericTooltipUtils::getResourceKeyTooltip);
    }

    @NotNull
    public static List<Component> getLodestoneTrackerTooltip(IClientUtils utils, int pad, LodestoneTracker value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, "ali.property.branch.global_pos", value.target(), GenericTooltipUtils::getGlobalPosTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.tracked", value.tracked()));

        return components;
    }

    @NotNull
    public static List<Component> getFireworkExplosionTooltip(IClientUtils utils, int pad, FireworkExplosion value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getEnumTooltip(utils, pad, "ali.property.value.shape", value.shape()));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.colors", value.colors().toString()));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.fade_colors", value.fadeColors().toString()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.has_trail", value.hasTrail()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.has_twinkle", value.hasTwinkle()));

        return components;
    }

    @NotNull
    public static List<Component> getFireworksTooltip(IClientUtils utils, int pad, Fireworks value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.flight_duration", value.flightDuration()));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.explosions", "ali.property.branch.explosion", value.explosions(), GenericTooltipUtils::getFireworkExplosionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getProfileTooltip(IClientUtils utils, int pad, ResolvableProfile value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.name", value.name(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.uuid", value.id(), GenericTooltipUtils::getUUIDTooltip));

        if (!value.properties().isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.properties")));
            value.properties().forEach((name, property) -> {
                components.add(pad(pad + 1, value(name)));
                components.addAll(getPropertyTooltip(utils, pad + 2, property));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getResourceLocationTooltip(IClientUtils utils, int pad, ResourceLocation value) {
        return GenericTooltipUtils.getResourceLocationTooltip(utils, pad, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternsTooltip(IClientUtils utils, int pad, BannerPatternLayers value) {
        return getBannerPatternLayersTooltip(utils, pad, "ali.property.branch.banner_patterns", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getDyeColorTooltip(IClientUtils utils, int pad, DyeColor value) {
        return getEnumTooltip(utils, pad, "ali.property.value.color", value);
    }

    @NotNull
    public static List<Component> getPotDecorationsTooltip(IClientUtils utils, int pad, PotDecorations value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.back", value.back(), RegistriesTooltipUtils::getItemTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.left", value.left(), RegistriesTooltipUtils::getItemTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.right", value.right(), RegistriesTooltipUtils::getItemTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.front", value.front(), RegistriesTooltipUtils::getItemTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getContainerTooltip(IClientUtils utils, int pad, ItemContainerContents value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.items", "ali.property.branch.item", value.items, GenericTooltipUtils::getItemStackTooltip);
    }

    @NotNull
    public static List<Component> getBlockStateTooltip(IClientUtils ignoredUtils, int pad, BlockItemStateProperties properties) {
        List<Component> components = new LinkedList<>();

        if (!properties.properties().isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.properties")));
            properties.properties().forEach((property, value) -> components.add(pad(pad + 1, keyValue(property, value))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBeesTooltip(IClientUtils utils, int pad, Bees properties) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.bees", "ali.property.branch.occupant", properties.bees(), GenericTooltipUtils::getBeehiveBlockEntityOccupantTooltip);
    }

    @NotNull
    public static List<Component> getLockTooltip(IClientUtils utils, int pad, LockCode lockCode) {
        return getItemPredicateTooltip(utils, pad, "ali.property.branch.predicate", lockCode.predicate());
    }

    @NotNull
    public static List<Component> getContainerLootTooltip(IClientUtils utils, int pad, SeededContainerLoot value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getResourceKeyTooltip(utils, pad, "ali.property.value.loot_table", value.lootTable()));
        components.addAll(getLongTooltip(utils, pad, "ali.property.value.seed", value.seed()));

        return components;
    }

    @NotNull
    public static List<Component> getBreakSoundTooltip(IClientUtils utils, int pad, Holder<SoundEvent> soundEvent) {
        return getHolderTooltip(utils, pad, "ali.property.value.sound", soundEvent, RegistriesTooltipUtils::getSoundEventTooltip);
    }

    @NotNull
    public static List<Component> getVillagerVariantTooltip(IClientUtils utils, int pad, Holder<VillagerType> villager) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", villager, RegistriesTooltipUtils::getVillagerTypeTooltip);
    }

    @NotNull
    public static List<Component> getWolfVariantTooltip(IClientUtils utils, int pad, Holder<WolfVariant> wolf) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", wolf, RegistriesTooltipUtils::getWolfVariantTooltip);
    }

    @NotNull
    public static List<Component> getWolfSoundVariantTooltip(IClientUtils utils, int pad, Holder<WolfSoundVariant> wolfSound) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", wolfSound, RegistriesTooltipUtils::getWolfSoundVariantTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTypeTooltip(IClientUtils utils, int pad, Enum<?> type) {
        return getEnumTooltip(utils, pad, "ali.property.value.type", type);
    }

    @NotNull
    public static List<Component> getPigVariantTooltip(IClientUtils utils, int pad, Holder<PigVariant> pigVariant) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", pigVariant, RegistriesTooltipUtils::getPigVariantTooltip);
    }

    @NotNull
    public static List<Component> getCowVariantTooltip(IClientUtils utils, int pad, Holder<CowVariant> cowVariant) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", cowVariant, RegistriesTooltipUtils::getCowVariantTooltip);
    }

    @NotNull
    public static List<Component> getChickenVariantTooltip(IClientUtils utils, int pad, EitherHolder<ChickenVariant> holder) {
        return getEitherHolderTooltip(utils, pad, "ali.property.value.type", holder, RegistriesTooltipUtils::getChickenVariantTooltip);
    }

    @NotNull
    public static List<Component> getFrogVariantTooltip(IClientUtils utils, int pad, Holder<FrogVariant> frogVariant) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", frogVariant, RegistriesTooltipUtils::getFrogVariantTooltip);
    }

    @NotNull
    public static List<Component> getPaintingVariantTooltip(IClientUtils utils, int pad, Holder<PaintingVariant> paintingVariant) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", paintingVariant, RegistriesTooltipUtils::getPaintingVariantTooltip);
    }

    @NotNull
    public static List<Component> getCatVariantTooltip(IClientUtils utils, int pad, Holder<CatVariant> catVariant) {
        return getHolderTooltip(utils, pad, "ali.property.value.type", catVariant, RegistriesTooltipUtils::getCatVariantTooltip);
    }
}
