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
        return utils.getValueTooltip(utils, value.copyTag().getAsString()).build("ali.property.value.tag");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntTooltip(IServerUtils utils, int value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getUnbreakableTooltip(IServerUtils utils, Unbreakable value) {
        return utils.getValueTooltip(utils, value.showInTooltip()).build("ali.property.value.show_in_tooltip");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.custom_name");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getItemNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.item_name");
    }

    @NotNull
    public static ITooltipNode getItemLoreTooltip(IServerUtils utils, ItemLore value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.lines()).build("ali.property.branch.lines"))
                .add(utils.getValueTooltip(utils, value.styledLines()).build("ali.property.branch.styled_lines"))
                .build();
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRarityTooltip(IServerUtils utils, Rarity value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.rarity");
    }

    @NotNull
    public static ITooltipNode getItemEnchantmentsTooltip(IServerUtils utils, ItemEnchantments value) {
        return getMapTooltip(utils, value.enchantments, GenericTooltipUtils::getEnchantmentLevelEntryTooltip).build("ali.property.branch.enchantments");
    }

    @NotNull
    public static ITooltipNode getAdventureModePredicateTooltip(IServerUtils utils, AdventureModePredicate value) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.predicate", value.predicates).build("ali.property.branch.blocks"))
                .add(getCollectionTooltip(utils, "ali.property.value.null", value.tooltip).build("ali.property.branch.tooltip"))
                .add(utils.getValueTooltip(utils, value.showInTooltip).build("ali.property.value.show_in_tooltip"))
                .build();
    }

    @NotNull
    public static ITooltipNode getAttributeModifiersTooltip(IServerUtils utils, ItemAttributeModifiers value) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.modifier", value.modifiers()).build("ali.property.branch.modifiers"))
                .add(utils.getValueTooltip(utils, value.showInTooltip()).build("ali.property.value.show_in_tooltip"))
                .build();
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCustomModelDataTooltip(IServerUtils utils, CustomModelData value) {
        return utils.getValueTooltip(utils, value.value()).build("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEmptyTooltip(IServerUtils ignoredUtils, Unit ignoredValue) {
        return EmptyTooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBoolTooltip(IServerUtils utils, boolean value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getFoodTooltip(IServerUtils utils, FoodProperties food) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, food.nutrition()).build("ali.property.value.nutrition"))
                .add(utils.getValueTooltip(utils, food.saturation()).build("ali.property.value.saturation"))
                .add(utils.getValueTooltip(utils, food.canAlwaysEat()).build("ali.property.value.can_always_eat"))
                .add(utils.getValueTooltip(utils, food.eatSeconds()).build("ali.property.value.eat_seconds"))
                .add(utils.getValueTooltip(utils, food.usingConvertsTo()).build("ali.property.branch.using_converts_to"))
                .add(getCollectionTooltip(utils, "ali.property.branch.effect", food.effects()).build("ali.property.branch.effects"))
                .build();
    }

    @NotNull
    public static ITooltipNode getToolTooltip(IServerUtils utils, Tool tool) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.rule", tool.rules()).build("ali.property.branch.rules"))
                .add(utils.getValueTooltip(utils, tool.defaultMiningSpeed()).build("ali.property.value.default_mining_speed"))
                .add(utils.getValueTooltip(utils, tool.damagePerBlock()).build("ali.property.value.damage_per_block"))
                .build();
    }

    @NotNull
    public static ITooltipNode getDyedColorTooltip(IServerUtils utils, DyedItemColor value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.rgb()).build("ali.property.value.rgb"))
                .add(utils.getValueTooltip(utils, value.showInTooltip()).build("ali.property.value.show_in_tooltip"))
                .build();
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapColorTooltip(IServerUtils utils, MapItemColor value) {
        return utils.getValueTooltip(utils, value.rgb()).build("ali.property.value.rgb");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapIdTooltip(IServerUtils utils, MapId value) {
        return utils.getValueTooltip(utils, value.id()).build("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getMapDecorationsTooltip(IServerUtils utils, MapDecorations value) {
        return getMapTooltip(utils, value.decorations(), GenericTooltipUtils::getMapDecorationEntryTooltip).build("ali.property.branch.decorations");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapPostProcessingTooltip(IServerUtils utils, MapPostProcessing value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getChargedProjectilesTooltip(IServerUtils utils, ChargedProjectiles value) {
        return getCollectionTooltip(utils, "ali.property.branch.item", value.getItems()).build("ali.property.branch.items");
    }

    @NotNull
    public static ITooltipNode getBundleContentsTooltip(IServerUtils utils, BundleContents value) {
        return ArrayTooltipNode.array()
                .add(getCollectionTooltip(utils, "ali.property.branch.item", value.items).build("ali.property.branch.items"))
                .add(utils.getValueTooltip(utils, value.weight().toString()).build("ali.property.value.fraction"))
                .build();
    }

    @NotNull
    public static ITooltipNode getPotionContentsTooltip(IServerUtils utils, PotionContents value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.potion()).build("ali.property.value.potion"))
                .add(utils.getValueTooltip(utils, value.customColor()).build("ali.property.value.custom_color"))
                .add(utils.getValueTooltip(utils, value.customEffects()).build("ali.property.branch.custom_effects"))
                .build();
    }

    @NotNull
    public static ITooltipNode getSuspiciousStewEffectsTooltip(IServerUtils utils, SuspiciousStewEffects value) {
        return utils.getValueTooltip(utils, value.effects()).build("ali.property.branch.effects");
    }

    @NotNull
    public static ITooltipNode getWritableBookContentTooltip(IServerUtils utils, WritableBookContent value) {
        return getFilterableTooltip(utils, "ali.property.branch.page", value.pages()).build("ali.property.branch.pages");
    }

    @NotNull
    public static ITooltipNode getWrittenBookContentTooltip(IServerUtils utils, WrittenBookContent value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.title()).build("ali.property.branch.title"))
                .add(utils.getValueTooltip(utils, value.author()).build("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, value.generation()).build("ali.property.value.generation"))
                .add(getFilterableTooltip(utils, "ali.property.branch.page", value.pages()).build("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, value.resolved()).build("ali.property.value.resolved"))
                .build();
    }

    @NotNull
    public static ITooltipNode getTrimTooltip(IServerUtils utils, ArmorTrim value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.material().value()).build("ali.property.value.material"))
                .add(utils.getValueTooltip(utils, value.pattern().value()).build("ali.property.value.pattern"))
                .add(utils.getValueTooltip(utils, value.showInTooltip).build("ali.property.value.show_in_tooltip"))
                .build();
    }

    @NotNull
    public static ITooltipNode getDebugStickStateTooltip(IServerUtils utils, DebugStickState value) {
        return getMapTooltip(utils, value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip).build("ali.property.branch.properties");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getInstrumentTooltip(IServerUtils utils, Holder<Instrument> value) {
        return utils.getValueTooltip(utils, value.value()).build("ali.property.value.value");
    }

    @NotNull
    public static ITooltipNode getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayable value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.song()).build("ali.property.value.song"))
                .add(utils.getValueTooltip(utils, value.showInTooltip()).build("ali.property.value.show_in_tooltip"))
                .build();
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRecipesTooltip(IServerUtils utils, List<ResourceLocation> value) {
        return utils.getValueTooltip(utils, value).build("ali.property.branch.recipes");
    }

    @NotNull
    public static ITooltipNode getLodestoneTrackerTooltip(IServerUtils utils, LodestoneTracker value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.target()).build("ali.property.branch.global_pos"))
                .add(utils.getValueTooltip(utils, value.tracked()).build("ali.property.value.tracked"))
                .build();
    }

    @NotNull
    public static ITooltipNode getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.shape()).build("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, value.colors().toString()).build("ali.property.value.colors"))
                .add(utils.getValueTooltip(utils, value.fadeColors().toString()).build("ali.property.value.fade_colors"))
                .add(utils.getValueTooltip(utils, value.hasTrail()).build("ali.property.value.has_trail"))
                .add(utils.getValueTooltip(utils, value.hasTwinkle()).build("ali.property.value.has_twinkle"))
                .build();
    }

    @NotNull
    public static ITooltipNode getFireworksTooltip(IServerUtils utils, Fireworks value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.flightDuration()).build("ali.property.value.flight_duration"))
                .add(getCollectionTooltip(utils, "ali.property.branch.explosion", value.explosions()).build("ali.property.branch.explosions"))
                .build();
    }

    @NotNull
    public static ITooltipNode getProfileTooltip(IServerUtils utils, ResolvableProfile value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.name()).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, value.id()).build("ali.property.value.uuid"))
                .add(getMapTooltip(utils, value.properties().asMap(), GenericTooltipUtils::getPropertiesEntryTooltip).build("ali.property.branch.properties"))
                .build();
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getNoteBlockSoundTooltip(IServerUtils utils, ResourceLocation value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBannerPatternsTooltip(IServerUtils utils, BannerPatternLayers value) {
        return utils.getValueTooltip(utils, value).build("ali.property.branch.banner_patterns");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBaseColorTooltip(IServerUtils utils, DyeColor value) {
        return utils.getValueTooltip(utils, value).build("ali.property.value.color");
    }

    @NotNull
    public static ITooltipNode getPotDecorationsTooltip(IServerUtils utils, PotDecorations value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.back()).build("ali.property.value.back"))
                .add(utils.getValueTooltip(utils, value.left()).build("ali.property.value.left"))
                .add(utils.getValueTooltip(utils, value.right()).build("ali.property.value.right"))
                .add(utils.getValueTooltip(utils, value.front()).build("ali.property.value.front"))
                .build();
    }

    @NotNull
    public static ITooltipNode getContainerTooltip(IServerUtils utils, ItemContainerContents value) {
        return getCollectionTooltip(utils, "ali.property.branch.item", value.items).build("ali.property.branch.items");
    }

    @NotNull
    public static ITooltipNode getBlockStateTooltip(IServerUtils ignoredUtils, BlockItemStateProperties properties) {
        return getMapTooltip(ignoredUtils, properties.properties(), GenericTooltipUtils::getStringEntryTooltip).build("ali.property.branch.properties");
    }

    @NotNull
    public static ITooltipNode getBeesTooltip(IServerUtils utils, List<BeehiveBlockEntity.Occupant> properties) {
        return getCollectionTooltip(utils, "ali.property.branch.occupant", properties).build("ali.property.branch.bees");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLockTooltip(IServerUtils utils, LockCode lockCode) {
        return utils.getValueTooltip(utils, lockCode.key()).build("ali.property.value.value");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getContainerLootTooltip(IServerUtils utils, SeededContainerLoot value) {
        return ArrayTooltipNode.array()
                .add(utils.getValueTooltip(utils, value.lootTable()).build("ali.property.value.loot_table"))
                .add(utils.getValueTooltip(utils, value.seed()).build("ali.property.value.seed"))
                .build();
    }
}
