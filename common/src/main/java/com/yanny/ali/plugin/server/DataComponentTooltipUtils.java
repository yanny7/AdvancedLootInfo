package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
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

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomDataTooltip(IServerUtils utils, CustomData value) {
        return utils.getValueTooltip(utils, value.copyTag().getAsString()).key("ali.property.value.tag");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntTooltip(IServerUtils utils, int value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getUnbreakableTooltip(IServerUtils utils, Unbreakable value) {
        return utils.getValueTooltip(utils, value.showInTooltip()).key("ali.property.value.show_in_tooltip");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.custom_name");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getItemNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.item_name");
    }

    @NotNull
    public static ITooltipNode getItemLoreTooltip(IServerUtils utils, ItemLore value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.lines()).key("ali.property.branch.lines"))
                .add(utils.getValueTooltip(utils, value.styledLines()).key("ali.property.branch.styled_lines"));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRarityTooltip(IServerUtils utils, Rarity value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.rarity");
    }

    @NotNull
    public static ITooltipNode getItemEnchantmentsTooltip(IServerUtils utils, ItemEnchantments value) {
        return getMapTooltip(utils, value.enchantments, GenericTooltipUtils::getEnchantmentLevelEntryTooltip).key("ali.property.branch.enchantments");
    }

    @NotNull
    public static ITooltipNode getAdventureModePredicateTooltip(IServerUtils utils, AdventureModePredicate value) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.predicate", value.predicates).key("ali.property.branch.blocks"))
                .add(getCollectionTooltip(utils, "ali.property.value.null", value.tooltip).key("ali.property.branch.tooltip"))
                .add(utils.getValueTooltip(utils, value.showInTooltip).key("ali.property.value.show_in_tooltip"));
    }

    @NotNull
    public static ITooltipNode getAttributeModifiersTooltip(IServerUtils utils, ItemAttributeModifiers value) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.modifier", value.modifiers()).key("ali.property.branch.modifiers"))
                .add(utils.getValueTooltip(utils, value.showInTooltip()).key("ali.property.value.show_in_tooltip"));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomModelDataTooltip(IServerUtils utils, CustomModelData value) {
        return utils.getValueTooltip(utils, value.value()).key("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEmptyTooltip(IServerUtils ignoredUtils, Unit ignoredValue) {
        return EmptyTooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBoolTooltip(IServerUtils utils, boolean value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getFoodTooltip(IServerUtils utils, FoodProperties food) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, food.nutrition()).key("ali.property.value.nutrition"))
                .add(utils.getValueTooltip(utils, food.saturation()).key("ali.property.value.saturation"))
                .add(utils.getValueTooltip(utils, food.canAlwaysEat()).key("ali.property.value.can_always_eat"))
                .add(utils.getValueTooltip(utils, food.eatSeconds()).key("ali.property.value.eat_seconds"))
                .add(utils.getValueTooltip(utils, food.usingConvertsTo()).key("ali.property.branch.using_converts_to"))
                .add(getCollectionTooltip(utils, "ali.property.branch.effect", food.effects()).key("ali.property.branch.effects"));
    }

    @NotNull
    public static ITooltipNode getToolTooltip(IServerUtils utils, Tool tool) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.rule", tool.rules()).key("ali.property.branch.rules"))
                .add(utils.getValueTooltip(utils, tool.defaultMiningSpeed()).key("ali.property.value.default_mining_speed"))
                .add(utils.getValueTooltip(utils, tool.damagePerBlock()).key("ali.property.value.damage_per_block"));
    }

    @NotNull
    public static ITooltipNode getDyedColorTooltip(IServerUtils utils, DyedItemColor value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.rgb()).key("ali.property.value.rgb"))
                .add(utils.getValueTooltip(utils, value.showInTooltip()).key("ali.property.value.show_in_tooltip"));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapColorTooltip(IServerUtils utils, MapItemColor value) {
        return utils.getValueTooltip(utils, value.rgb()).key("ali.property.value.rgb");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapIdTooltip(IServerUtils utils, MapId value) {
        return utils.getValueTooltip(utils, value.id()).key("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getMapDecorationsTooltip(IServerUtils utils, MapDecorations value) {
        return getMapTooltip(utils, value.decorations(), GenericTooltipUtils::getMapDecorationEntryTooltip).key("ali.property.branch.decorations");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapPostProcessingTooltip(IServerUtils utils, MapPostProcessing value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getChargedProjectilesTooltip(IServerUtils utils, ChargedProjectiles value) {
        return getCollectionTooltip(utils, "ali.property.branch.item", value.getItems()).key("ali.property.branch.items");
    }

    @NotNull
    public static ITooltipNode getBundleContentsTooltip(IServerUtils utils, BundleContents value) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.item", value.items).key("ali.property.branch.items"))
                .add(utils.getValueTooltip(utils, value.weight().toString()).key("ali.property.value.fraction"));
    }

    @NotNull
    public static ITooltipNode getPotionContentsTooltip(IServerUtils utils, PotionContents value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.potion()).key("ali.property.value.potion"))
                .add(utils.getValueTooltip(utils, value.customColor()).key("ali.property.value.custom_color"))
                .add(utils.getValueTooltip(utils, value.customEffects()).key("ali.property.branch.custom_effects"));
    }

    @NotNull
    public static ITooltipNode getSuspiciousStewEffectsTooltip(IServerUtils utils, SuspiciousStewEffects value) {
        return utils.getValueTooltip(utils, value.effects()).key("ali.property.branch.effects");
    }

    @NotNull
    public static ITooltipNode getWritableBookContentTooltip(IServerUtils utils, WritableBookContent value) {
        return getFilterableTooltip(utils, "ali.property.branch.page", value.pages()).key("ali.property.branch.pages");
    }

    @NotNull
    public static ITooltipNode getWrittenBookContentTooltip(IServerUtils utils, WrittenBookContent value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.title()).key("ali.property.branch.title"))
                .add(utils.getValueTooltip(utils, value.author()).key("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, value.generation()).key("ali.property.value.generation"))
                .add(getFilterableTooltip(utils, "ali.property.branch.page", value.pages()).key("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, value.resolved()).key("ali.property.value.resolved"));
    }

    @NotNull
    public static ITooltipNode getTrimTooltip(IServerUtils utils, ArmorTrim value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.material().value()).key("ali.property.value.material"))
                .add(utils.getValueTooltip(utils, value.pattern().value()).key("ali.property.value.pattern"))
                .add(utils.getValueTooltip(utils, value.showInTooltip).key("ali.property.value.show_in_tooltip"));
    }

    @NotNull
    public static ITooltipNode getDebugStickStateTooltip(IServerUtils utils, DebugStickState value) {
        return getMapTooltip(utils, value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip).key("ali.property.branch.properties");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getInstrumentTooltip(IServerUtils utils, Holder<Instrument> value) {
        return utils.getValueTooltip(utils, value.value()).key("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayable value) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getEitherHolderTooltip(utils, "ali.property.value.song", value.song(), RegistriesTooltipUtils::getJukeboxSongTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.show_in_tooltip", value.showInTooltip()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRecipesTooltip(IServerUtils utils, List<ResourceLocation> value) {
        return utils.getValueTooltip(utils, value).key("ali.property.branch.recipes");
    }

    @NotNull
    public static ITooltipNode getLodestoneTrackerTooltip(IServerUtils utils, LodestoneTracker value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.target()).key("ali.property.branch.global_pos"))
                .add(utils.getValueTooltip(utils, value.tracked()).key("ali.property.value.tracked"));
    }

    @NotNull
    public static ITooltipNode getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.shape()).key("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, value.colors().toString()).key("ali.property.value.colors"))
                .add(utils.getValueTooltip(utils, value.fadeColors().toString()).key("ali.property.value.fade_colors"))
                .add(utils.getValueTooltip(utils, value.hasTrail()).key("ali.property.value.has_trail"))
                .add(utils.getValueTooltip(utils, value.hasTwinkle()).key("ali.property.value.has_twinkle"));
    }

    @NotNull
    public static ITooltipNode getFireworksTooltip(IServerUtils utils, Fireworks value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.flightDuration()).key("ali.property.value.flight_duration"))
                .add(getCollectionTooltip(utils, "ali.property.branch.explosion", value.explosions()).key("ali.property.branch.explosions"));
    }

    @NotNull
    public static ITooltipNode getProfileTooltip(IServerUtils utils, ResolvableProfile value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.name()).key("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, value.id()).key("ali.property.value.uuid"))
                .add(getMapTooltip(utils, value.properties().asMap(), GenericTooltipUtils::getPropertiesEntryTooltip).key("ali.property.branch.properties"));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getNoteBlockSoundTooltip(IServerUtils utils, ResourceLocation value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBannerPatternsTooltip(IServerUtils utils, BannerPatternLayers value) {
        return utils.getValueTooltip(utils, value).key("ali.property.branch.banner_patterns");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBaseColorTooltip(IServerUtils utils, DyeColor value) {
        return utils.getValueTooltip(utils, value).key("ali.property.value.color");
    }

    @NotNull
    public static ITooltipNode getPotDecorationsTooltip(IServerUtils utils, PotDecorations value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.back()).key("ali.property.value.back"))
                .add(utils.getValueTooltip(utils, value.left()).key("ali.property.value.left"))
                .add(utils.getValueTooltip(utils, value.right()).key("ali.property.value.right"))
                .add(utils.getValueTooltip(utils, value.front()).key("ali.property.value.front"));
    }

    @NotNull
    public static ITooltipNode getContainerTooltip(IServerUtils utils, ItemContainerContents value) {
        return getCollectionTooltip(utils, "ali.property.branch.item", value.items).key("ali.property.branch.items");
    }

    @NotNull
    public static ITooltipNode getBlockStateTooltip(IServerUtils ignoredUtils, BlockItemStateProperties properties) {
        return getMapTooltip(ignoredUtils, properties.properties(), GenericTooltipUtils::getStringEntryTooltip).key("ali.property.branch.properties");
    }

    @NotNull
    public static ITooltipNode getBeesTooltip(IServerUtils utils, List<BeehiveBlockEntity.Occupant> properties) {
        return getCollectionTooltip(utils, "ali.property.branch.occupant", properties).key("ali.property.branch.bees");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLockTooltip(IServerUtils utils, LockCode lockCode) {
        return utils.getValueTooltip(utils, lockCode.key()).key("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getContainerLootTooltip(IServerUtils utils, SeededContainerLoot value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.lootTable()).key("ali.property.value.loot_table"))
                .add(utils.getValueTooltip(utils, value.seed()).key("ali.property.value.seed"));
    }
}
