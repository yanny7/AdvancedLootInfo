package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.getTrimMaterialTooltip;
import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.getTrimPatternTooltip;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static List<Component> getCustomDataTooltip(IClientUtils utils, int pad, CustomData value) {
        return getStringTooltip(utils, pad, "ali.property.value.tag", value.copyTag().getAsString());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntTooltip(IClientUtils utils, int pad, int value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUnbreakableTooltip(IClientUtils utils, int pad, Unbreakable value) {
        return getBooleanTooltip(utils, pad, "ali.property.value.show_in_tooltip", value.showInTooltip());
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

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.lines", "ali.property.value.null", value.lines(), GenericTooltipUtils::getComponentTooltip));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.styled_lines", "ali.property.value.null", value.styledLines(), GenericTooltipUtils::getComponentTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getRarityTooltip(IClientUtils utils, int pad, Rarity value) {
        return getEnumTooltip(utils, pad, "ali.property.value.rarity", value);
    }

    @NotNull
    public static List<Component> getItemEnchantmentsTooltip(IClientUtils utils, int pad, ItemEnchantments value) {
        return getMapTooltip(utils, pad, "ali.property.branch.enchantments", value.enchantments, GenericTooltipUtils::getEnchantmentLevelEntryTooltip);
    }

    @NotNull
    public static List<Component> getAdventureModePredicateTooltip(IClientUtils utils, int pad, AdventureModePredicate value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.blocks", "ali.property.branch.predicate", value.predicates, GenericTooltipUtils::getBlockPredicateTooltip));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.tooltip", "ali.property.value.null", value.tooltip, GenericTooltipUtils::getComponentTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_in_tooltip", value.showInTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getAttributeModifiersTooltip(IClientUtils utils, int pad, ItemAttributeModifiers value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.modifiers", "ali.property.branch.modifier", value.modifiers(), GenericTooltipUtils::getItemAttributeModifiersEntryTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_in_tooltip", value.showInTooltip()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCustomModelDataTooltip(IClientUtils utils, int pad, CustomModelData value) {
        return getIntegerTooltip(utils, pad, "ali.property.value.value", value.value());
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
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.eat_seconds", food.eatSeconds()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.branch.using_converts_to", food.usingConvertsTo(), GenericTooltipUtils::getItemStackTooltip));
        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.effects", "ali.property.branch.effect", food.effects(), GenericTooltipUtils::getPossibleEffectTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getToolTooltip(IClientUtils utils, int pad, Tool tool) {
        List<Component> components = new LinkedList<>();

        components.addAll(getCollectionTooltip(utils, pad, "ali.property.branch.rules", "ali.property.branch.rule", tool.rules(), GenericTooltipUtils::getRuleTooltip));
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.default_mining_speed", tool.defaultMiningSpeed()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.damage_per_block", tool.damagePerBlock()));

        return components;
    }

    @NotNull
    public static List<Component> getDyedColorTooltip(IClientUtils utils, int pad, DyedItemColor value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.rgb", value.rgb()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_in_tooltip", value.showInTooltip()));

        return components;
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
        return getMapTooltip(utils, pad, "ali.property.branch.decorations", value.decorations(), GenericTooltipUtils::getMapDecorationEntryTooltip);
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

        return components;
    }

    @NotNull
    public static List<Component> getSuspiciousStewEffectsTooltip(IClientUtils utils, int pad, SuspiciousStewEffects value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.effects", value.effects(), GenericTooltipUtils::getSuspiciousStewEffectEntryTooltip);
    }

    @NotNull
    public static List<Component> getWritableBookContentTooltip(IClientUtils utils, int pad, WritableBookContent value) {
        return getFilterableTooltip(utils, pad, "ali.property.branch.pages", "ali.property.branch.page", value.pages(), GenericTooltipUtils::getStringTooltip);
    }

    @NotNull
    public static List<Component> getWrittenBookContentTooltip(IClientUtils utils, int pad, WrittenBookContent value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getFilterableTooltip(utils, pad, "ali.property.branch.title", value.title(), GenericTooltipUtils::getStringTooltip));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.author", value.author()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.generation", value.generation()));
        components.addAll(getFilterableTooltip(utils, pad, "ali.property.branch.pages", "ali.property.branch.page", value.pages(), GenericTooltipUtils::getComponentTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.resolved", value.resolved()));

        return components;
    }

    @NotNull
    public static List<Component> getTrimTooltip(IClientUtils utils, int pad, ArmorTrim value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getTrimMaterialTooltip(utils, pad, "ali.property.value.material", value.material().value()));
        components.addAll(getTrimPatternTooltip(utils, pad, "ali.property.value.pattern", value.pattern().value()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_in_tooltip", value.showInTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getDebugStickStateTooltip(IClientUtils utils, int pad, DebugStickState value) {
        return getMapTooltip(utils, pad, "ali.property.branch.properties", value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getInstrumentTooltip(IClientUtils utils, int pad, Holder<Instrument> value) {
        return RegistriesTooltipUtils.getInstrumentTooltip(utils, pad, "ali.property.value.value", value.value());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getJukeboxPlayableTooltip(IClientUtils utils, int pad, JukeboxPlayable value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getEitherHolderTooltip(utils, pad, "ali.property.value.song", value.song(), RegistriesTooltipUtils::getJukeboxSongTooltip));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_in_tooltip", value.showInTooltip()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getRecipesTooltip(IClientUtils utils, int pad, List<ResourceLocation> value) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.recipes", "ali.property.value.null", value, GenericTooltipUtils::getResourceLocationTooltip);
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
        components.addAll(getMapTooltip(utils, pad, "ali.property.branch.properties", value.properties().asMap(), GenericTooltipUtils::getPropertiesEntryTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNoteBlockSoundTooltip(IClientUtils utils, int pad, ResourceLocation value) {
        return getResourceLocationTooltip(utils, pad, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternsTooltip(IClientUtils utils, int pad, BannerPatternLayers value) {
        return getBannerPatternLayersTooltip(utils, pad, "ali.property.branch.banner_patterns", value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBaseColorTooltip(IClientUtils utils, int pad, DyeColor value) {
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
        return getMapTooltip(ignoredUtils, pad, "ali.property.branch.properties", properties.properties(), GenericTooltipUtils::getKeyValueEntryTooltip);
    }

    @NotNull
    public static List<Component> getBeesTooltip(IClientUtils utils, int pad, List<BeehiveBlockEntity.Occupant> properties) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.bees", "ali.property.branch.occupant", properties, GenericTooltipUtils::getBeehiveBlockEntityOccupantTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLockTooltip(IClientUtils utils, int pad, LockCode lockCode) {
        return getStringTooltip(utils, pad, "ali.property.value.value", lockCode.key());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getContainerLootTooltip(IClientUtils utils, int pad, SeededContainerLoot value) {
        List<Component> components = new LinkedList<>();

        components.addAll(getResourceKeyTooltip(utils, pad, "ali.property.value.loot_table", value.lootTable()));
        components.addAll(getLongTooltip(utils, pad, "ali.property.value.seed", value.seed()));

        return components;
    }
}
