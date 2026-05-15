package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.JukeboxPlayable;
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

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getFilterableTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class DataComponentTooltipUtils {
    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCustomDataTooltip(IServerUtils utils, CustomData value) {
        return utils.getValueTooltip(utils, value.copyTag().getAsString()).key(Lang.Value.TAG);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getIntTooltip(IServerUtils utils, int value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getUnbreakableTooltip(IServerUtils utils, Unbreakable value) {
        return utils.getValueTooltip(utils, value.showInTooltip()).key(Lang.Value.SHOW_IN_TOOLTIP);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCustomNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.CUSTOM_NAME);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getItemNameTooltip(IServerUtils utils, Component value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.ITEM_NAME);
    }

    @NotNull
    public static TooltipBuilder getItemLoreTooltip(IServerUtils utils, ItemLore value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.lines()).build(Lang.Branch.LINES));
            b.add(utils.getValueTooltip(utils, value.styledLines()).build(Lang.Branch.STYLED_LINES));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getRarityTooltip(IServerUtils utils, Rarity value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.RARITY);
    }

    @NotNull
    public static TooltipBuilder getItemEnchantmentsTooltip(IServerUtils utils, ItemEnchantments value) {
        return getMapTooltip(utils, value.enchantments, GenericTooltipUtils::getEnchantmentLevelEntryTooltip).key(Lang.Branch.ENCHANTMENTS);
    }

    @NotNull
    public static TooltipBuilder getAdventureModePredicateTooltip(IServerUtils utils, AdventureModePredicate value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.predicates).build(Lang.Branch.BLOCKS));
            b.add(utils.getValueTooltip(utils, value.tooltip).build(Lang.Branch.TOOLTIP));
            b.add(utils.getValueTooltip(utils, value.showInTooltip).build(Lang.Value.SHOW_IN_TOOLTIP));
        });
    }

    @NotNull
    public static TooltipBuilder getAttributeModifiersTooltip(IServerUtils utils, ItemAttributeModifiers value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.modifiers()).build(Lang.Branch.MODIFIERS));
            b.add(utils.getValueTooltip(utils, value.showInTooltip()).build(Lang.Value.SHOW_IN_TOOLTIP));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCustomModelDataTooltip(IServerUtils utils, CustomModelData value) {
        return utils.getValueTooltip(utils, value.value()).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getEmptyTooltip(IServerUtils ignoredUtils, Unit ignoredValue) {
        return TooltipBuilder.empty();
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getBoolTooltip(IServerUtils utils, boolean value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getFoodTooltip(IServerUtils utils, FoodProperties food) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, food.nutrition()).build(Lang.Value.NUTRITION));
            b.add(utils.getValueTooltip(utils, food.saturation()).build(Lang.Value.SATURATION));
            b.add(utils.getValueTooltip(utils, food.canAlwaysEat()).build(Lang.Value.CAN_ALWAYS_EAT));
            b.add(utils.getValueTooltip(utils, food.eatSeconds()).build(Lang.Value.EAT_SECONDS));
            b.add(utils.getValueTooltip(utils, food.usingConvertsTo()).build(Lang.Branch.USING_CONVERTS_TO));
            b.add(utils.getValueTooltip(utils, food.effects()).build(Lang.Branch.EFFECTS));
        });
    }

    @NotNull
    public static TooltipBuilder getToolTooltip(IServerUtils utils, Tool tool) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, tool.rules()).build(Lang.Branch.RULES));
            b.add(utils.getValueTooltip(utils, tool.defaultMiningSpeed()).build(Lang.Value.DEFAULT_MINING_SPEED));
            b.add(utils.getValueTooltip(utils, tool.damagePerBlock()).build(Lang.Value.DAMAGE_PER_BLOCK));
        });
    }

    @NotNull
    public static TooltipBuilder getDyedColorTooltip(IServerUtils utils, DyedItemColor value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.rgb()).build(Lang.Value.RGB));
            b.add(utils.getValueTooltip(utils, value.showInTooltip()).build(Lang.Value.SHOW_IN_TOOLTIP));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getMapColorTooltip(IServerUtils utils, MapItemColor value) {
        return utils.getValueTooltip(utils, value.rgb()).key(Lang.Value.RGB);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getMapIdTooltip(IServerUtils utils, MapId value) {
        return utils.getValueTooltip(utils, value.id()).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getMapDecorationsTooltip(IServerUtils utils, MapDecorations value) {
        return getMapTooltip(utils, value.decorations(), GenericTooltipUtils::getMapDecorationEntryTooltip).key(Lang.Branch.DECORATIONS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getMapPostProcessingTooltip(IServerUtils utils, MapPostProcessing value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getChargedProjectilesTooltip(IServerUtils utils, ChargedProjectiles value) {
        return utils.getValueTooltip(utils, value.getItems()).key(Lang.Branch.ITEMS);
    }

    @NotNull
    public static TooltipBuilder getBundleContentsTooltip(IServerUtils utils, BundleContents value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.items).build(Lang.Branch.ITEMS));
            b.add(utils.getValueTooltip(utils, value.weight().toString()).build(Lang.Value.FRACTION));
        });
    }

    @NotNull
    public static TooltipBuilder getPotionContentsTooltip(IServerUtils utils, PotionContents value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.potion()).build(Lang.Value.POTION));
            b.add(utils.getValueTooltip(utils, value.customColor()).build(Lang.Value.CUSTOM_COLOR));
            b.add(utils.getValueTooltip(utils, value.customEffects()).build(Lang.Branch.CUSTOM_EFFECTS));
        });
    }

    @NotNull
    public static TooltipBuilder getSuspiciousStewEffectsTooltip(IServerUtils utils, SuspiciousStewEffects value) {
        return utils.getValueTooltip(utils, value.effects()).key(Lang.Branch.EFFECTS);
    }

    @NotNull
    public static TooltipBuilder getWritableBookContentTooltip(IServerUtils utils, WritableBookContent value) {
        return getFilterableTooltip(utils, Lang.Branch.PAGE, value.pages()).key(Lang.Branch.PAGES);
    }

    @NotNull
    public static TooltipBuilder getWrittenBookContentTooltip(IServerUtils utils, WrittenBookContent value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.title()).build(Lang.Branch.TITLE));
            b.add(utils.getValueTooltip(utils, value.author()).build(Lang.Value.AUTHOR));
            b.add(utils.getValueTooltip(utils, value.generation()).build(Lang.Value.GENERATION));
            b.add(getFilterableTooltip(utils, Lang.Branch.PAGE, value.pages()).build(Lang.Branch.PAGES));
            b.add(utils.getValueTooltip(utils, value.resolved()).build(Lang.Value.RESOLVED));
        });
    }

    @NotNull
    public static TooltipBuilder getTrimTooltip(IServerUtils utils, ArmorTrim value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.material().value()).build(Lang.Value.MATERIAL));
            b.add(utils.getValueTooltip(utils, value.pattern().value()).build(Lang.Value.PATTERN));
            b.add(utils.getValueTooltip(utils, value.showInTooltip).build(Lang.Value.SHOW_IN_TOOLTIP));
        });
    }

    @NotNull
    public static TooltipBuilder getDebugStickStateTooltip(IServerUtils utils, DebugStickState value) {
        return getMapTooltip(utils, value.properties(), GenericTooltipUtils::getBlockPropertyEntryTooltip).key(Lang.Branch.PROPERTIES);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getHolderTooltip(IServerUtils utils, Holder<?> value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @NotNull
    public static TooltipBuilder getJukeboxPlayableTooltip(IServerUtils utils, JukeboxPlayable value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.song()).build(Lang.Value.SONG));
            b.add(utils.getValueTooltip(utils, value.showInTooltip()).build(Lang.Value.SHOW_IN_TOOLTIP));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getRecipesTooltip(IServerUtils utils, List<ResourceLocation> value) {
        return utils.getValueTooltip(utils, value).key(Lang.Branch.RECIPES);
    }

    @NotNull
    public static TooltipBuilder getLodestoneTrackerTooltip(IServerUtils utils, LodestoneTracker value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.target()).build(Lang.Branch.GLOBAL_POS));
            b.add(utils.getValueTooltip(utils, value.tracked()).build(Lang.Value.TRACKED));
        });
    }

    @NotNull
    public static TooltipBuilder getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.shape()).build(Lang.Value.SHAPE));
            b.add(utils.getValueTooltip(utils, value.colors().toString()).build(Lang.Value.COLORS));
            b.add(utils.getValueTooltip(utils, value.fadeColors().toString()).build(Lang.Value.FADE_COLORS));
            b.add(utils.getValueTooltip(utils, value.hasTrail()).build(Lang.Value.HAS_TRAIL));
            b.add(utils.getValueTooltip(utils, value.hasTwinkle()).build(Lang.Value.HAS_TWINKLE));
        });
    }

    @NotNull
    public static TooltipBuilder getFireworksTooltip(IServerUtils utils, Fireworks value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.flightDuration()).build(Lang.Value.FLIGHT_DURATION));
            b.add(utils.getValueTooltip(utils, value.explosions()).build(Lang.Branch.EXPLOSIONS));
        });
    }

    @NotNull
    public static TooltipBuilder getProfileTooltip(IServerUtils utils, ResolvableProfile value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.name()).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, value.id()).build(Lang.Value.UUID));
            b.add(getMapTooltip(utils, value.properties().asMap(), GenericTooltipUtils::getPropertiesEntryTooltip).build(Lang.Branch.PROPERTIES));
        });
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getNoteBlockSoundTooltip(IServerUtils utils, ResourceLocation value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getBannerPatternsTooltip(IServerUtils utils, BannerPatternLayers value) {
        return utils.getValueTooltip(utils, value).key(Lang.Branch.BANNER_PATTERNS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getBaseColorTooltip(IServerUtils utils, DyeColor value) {
        return utils.getValueTooltip(utils, value).key(Lang.Value.COLOR);
    }

    @NotNull
    public static TooltipBuilder getPotDecorationsTooltip(IServerUtils utils, PotDecorations value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.back()).build(Lang.Value.BACK));
            b.add(utils.getValueTooltip(utils, value.left()).build(Lang.Value.LEFT));
            b.add(utils.getValueTooltip(utils, value.right()).build(Lang.Value.RIGHT));
            b.add(utils.getValueTooltip(utils, value.front()).build(Lang.Value.FRONT));
        });
    }

    @NotNull
    public static TooltipBuilder getContainerTooltip(IServerUtils utils, ItemContainerContents value) {
        return utils.getValueTooltip(utils, value.items).key(Lang.Branch.ITEMS);
    }

    @NotNull
    public static TooltipBuilder getBlockStateTooltip(IServerUtils utils, BlockItemStateProperties properties) {
        return getMapTooltip(utils, properties.properties(), GenericTooltipUtils::getStringEntryTooltip).key(Lang.Branch.PROPERTIES);
    }

    @NotNull
    public static TooltipBuilder getBeesTooltip(IServerUtils utils, List<BeehiveBlockEntity.Occupant> properties) {
        return utils.getValueTooltip(utils, properties).key(Lang.Branch.BEES);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getLockTooltip(IServerUtils utils, LockCode lockCode) {
        return utils.getValueTooltip(utils, lockCode.key()).key(Lang.Value.VALUE);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getContainerLootTooltip(IServerUtils utils, SeededContainerLoot value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.lootTable()).build(Lang.Value.LOOT_TABLE));
            b.add(utils.getValueTooltip(utils, value.seed()).build(Lang.Value.SEED));
        });
    }
}
