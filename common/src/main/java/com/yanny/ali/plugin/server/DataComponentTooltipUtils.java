package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Rarity;
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

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getTrimMaterialTooltip;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getTrimPatternTooltip;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomDataTooltip(IServerUtils utils, CustomData value) {
        return getStringTooltip(utils, "ali.property.value.tag", value.copyTag().getAsString());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntTooltip(IServerUtils utils, int value) {
        return getIntegerTooltip(utils, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getUnbreakableTooltip(IServerUtils utils, Unbreakable value) {
        return getBooleanTooltip(utils, "ali.property.value.show_in_tooltip", value.showInTooltip());
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
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.blocks", "ali.property.branch.predicate", value.predicates, GenericTooltipUtils::getBlockPredicateTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.tooltip", "ali.property.value.null", value.tooltip, GenericTooltipUtils::getComponentTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.show_in_tooltip", value.showInTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getAttributeModifiersTooltip(IServerUtils utils, ItemAttributeModifiers value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.modifiers", "ali.property.branch.modifier", value.modifiers(), GenericTooltipUtils::getItemAttributeModifiersEntryTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.show_in_tooltip", value.showInTooltip()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomModelDataTooltip(IServerUtils utils, CustomModelData value) {
        return getIntegerTooltip(utils, "ali.property.value.value", value.value());
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
        tooltip.add(getFloatTooltip(utils, "ali.property.value.eat_seconds", food.eatSeconds()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.effects", "ali.property.branch.effect", food.effects(), GenericTooltipUtils::getPossibleEffectTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getToolTooltip(IServerUtils utils, Tool tool) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.rules", "ali.property.branch.rule", tool.rules(), GenericTooltipUtils::getRuleTooltip));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.default_mining_speed", tool.defaultMiningSpeed()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.damage_per_block", tool.damagePerBlock()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDyedColorTooltip(IServerUtils utils, DyedItemColor value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.rgb", value.rgb()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.show_in_tooltip", value.showInTooltip()));

        return tooltip;
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
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.effects", "ali.property.value.null", value.customEffects(), GenericTooltipUtils::getMobEffectInstanceTooltip));

        return tooltip;
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
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.show_in_tooltip", value.showInTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDebugStickStateTooltip(IServerUtils utils, DebugStickState value) {
        return getMapTooltip(utils, "ali.property.branch.properties", value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getInstrumentTooltip(IServerUtils utils, Holder<Instrument> value) {
        return RegistriesTooltipUtils.getInstrumentTooltip(utils, "ali.property.value.value", value.value());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRecipesTooltip(IServerUtils utils, List<ResourceLocation> value) {
        return getCollectionTooltip(utils, "ali.property.branch.recipes", "ali.property.value.null", value, GenericTooltipUtils::getResourceLocationTooltip);
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
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.id", value.id(), GenericTooltipUtils::getUUIDTooltip));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.properties", value.properties().asMap(), GenericTooltipUtils::getPropertiesEntryTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getNoteBlockSoundTooltip(IServerUtils utils, ResourceLocation value) {
        return getResourceLocationTooltip(utils, "ali.property.value.value", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBannerPatternsTooltip(IServerUtils utils, BannerPatternLayers value) {
        return getBannerPatternLayersTooltip(utils, "ali.property.branch.banner_patterns", value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBaseColorTooltip(IServerUtils utils, DyeColor value) {
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
    public static ITooltipNode getBeesTooltip(IServerUtils utils, List<BeehiveBlockEntity.Occupant> properties) {
        return getCollectionTooltip(utils, "ali.property.branch.bees", "ali.property.branch.occupant", properties, GenericTooltipUtils::getBeehiveBlockEntityOccupantTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLockTooltip(IServerUtils utils, LockCode lockCode) {
        return getStringTooltip(utils, "ali.property.value.value", lockCode.key());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getContainerLootTooltip(IServerUtils utils, SeededContainerLoot value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getResourceKeyTooltip(utils, "ali.property.value.loot_table", value.lootTable()));
        tooltip.add(getLongTooltip(utils, "ali.property.value.seed", value.seed()));

        return tooltip;
    }
}
