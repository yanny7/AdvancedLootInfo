package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getLootNbtProviderTypeTooltip;

public class FunctionTooltipUtils {
    @NotNull
    public static ITooltipNode getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.apply_bonus"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.enchantment", fun.enchantment, RegistriesTooltipUtils::getEnchantmentTooltip));
        tooltip.add(getFormulaTooltip(utils, "ali.property.value.formula", fun.formula));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.copy_name"));

        tooltip.add(getEnumTooltip(utils, "ali.property.value.source", fun.source));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCopyCustomDataTooltip(IServerUtils utils, CopyCustomDataFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.copy_custom_data"));

        tooltip.add(getLootNbtProviderTypeTooltip(utils, "ali.property.value.source", fun.source.getType()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.copy_operations", "ali.property.branch.operation", fun.operations, GenericTooltipUtils::getCopyOperationTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.copy_state"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.block", fun.block, RegistriesTooltipUtils::getBlockTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.properties", "ali.property.value.null", fun.properties, GenericTooltipUtils::getPropertyTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.enchant_randomly"));

        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.enchantments", "ali.property.value.null", fun.options, RegistriesTooltipUtils::getEnchantmentTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.only_compatible", fun.onlyCompatible));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.enchant_with_levels"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.levels", fun.levels));
        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.options", "ali.property.value.null", fun.options, RegistriesTooltipUtils::getEnchantmentTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.exploration_map"));

        tooltip.add(getTagKeyTooltip(utils, "ali.property.value.destination", fun.destination));
        tooltip.add(getHolderTooltip(utils, "ali.property.value.map_decoration", fun.mapDecoration, RegistriesTooltipUtils::getMapDecorationTypeTooltip));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.zoom", fun.zoom));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.search_radius", fun.searchRadius));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.skip_known_structures", fun.skipKnownStructures));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.explosion_decay"));

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.fill_player_head"));

        tooltip.add(getEnumTooltip(utils, "ali.property.value.target", fun.entityTarget));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.furnace_smelt"));

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.limit_count"));

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.limit", fun.limiter));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantedCountIncreaseTooltip(IServerUtils utils, EnchantedCountIncreaseFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.enchanted_count_increase"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.enchantment", fun.enchantment, RegistriesTooltipUtils::getEnchantmentTooltip));
        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.value", fun.value));

        if (fun.limit > 0) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.limit", fun.limit));
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.reference"));

        tooltip.add(getResourceKeyTooltip(utils, "ali.property.value.name", fun.name));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSequenceTooltip(IServerUtils utils, SequenceFunction fun) {
        return getCollectionTooltip(utils, "ali.type.function.sequence", fun.functions, utils::getFunctionTooltip);
    }

    @NotNull
    public static ITooltipNode getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_attributes"));

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.modifiers", "ali.property.branch.modifier", fun.modifiers, GenericTooltipUtils::getModifierTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.replace", fun.replace));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_banner_pattern"));

        tooltip.add(getBooleanTooltip(utils, "ali.property.value.append", fun.append));
        tooltip.add(getBannerPatternLayersTooltip(utils, "ali.property.branch.banner_patterns", fun.patterns));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_contents"));

        tooltip.add(getContainerComponentManipulatorTooltip(utils, "ali.property.value.container", fun.component));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));
        //TODO entries

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_count"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.count", fun.value));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.add", fun.add));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_damage"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.damage", fun.damage));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.add", fun.add));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_enchantments"));

        tooltip.add(getMapTooltip(utils, "ali.property.branch.enchantments", fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.add", fun.add));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_instrument"));

        tooltip.add(getTagKeyTooltip(utils, "ali.property.value.options", fun.options));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_loot_table"));

        tooltip.add(getResourceKeyTooltip(utils, "ali.property.value.name", fun.name));
        tooltip.add(getLongTooltip(utils, "ali.property.value.seed", fun.seed));
        tooltip.add(getHolderTooltip(utils, "ali.property.value.block_entity_type", fun.type, RegistriesTooltipUtils::getBlockEntityTypeTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_lore"));

        tooltip.add(getListOperationTooltip(utils, "ali.property.value.list_operation", fun.mode));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.lore", "ali.property.value.null", fun.lore, GenericTooltipUtils::getComponentTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.resolution_context", fun.resolutionContext, GenericTooltipUtils::getEnumTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_name"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.name", fun.name, GenericTooltipUtils::getComponentTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.resolution_context", fun.resolutionContext, GenericTooltipUtils::getEnumTooltip));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.target", fun.target));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetCustomDataTooltip(IServerUtils utils, SetCustomDataFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_custom_data"));

        tooltip.add(getStringTooltip(utils, "ali.property.value.tag", fun.tag.getAsString()));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_potion"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.potion", fun.potion, RegistriesTooltipUtils::getPotionTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        ITooltipNode tooltip = getCollectionTooltip(utils, "ali.type.function.set_stew_effect", "ali.property.value.null", fun.effects, GenericTooltipUtils::getEffectEntryTooltip);

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetItemTooltip(IServerUtils utils, SetItemFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_item"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.item", fun.item, RegistriesTooltipUtils::getItemTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetComponentsTooltip(IServerUtils utils, SetComponentsFunction fun) {
        ITooltipNode tooltip = getDataComponentPatchTooltip(utils, "ali.type.function.set_components", fun.components);

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getModifyContentsTooltip(IServerUtils utils, ModifyContainerContents fun) {
        ITooltipNode tooltips = new TooltipNode(translatable("ali.type.function.modify_contents"));
        ITooltipNode tooltip = new TooltipNode(translatable("ali.property.branch.modifier"));

        tooltips.add(getContainerComponentManipulatorTooltip(utils, "ali.property.value.container", fun.component));
        tooltips.add(tooltip);
        tooltips.add(getSubConditionsTooltip(utils, fun.predicates));
        tooltip.add(utils.getFunctionTooltip(utils, fun.modifier));

        return tooltips;
    }

    @NotNull
    public static ITooltipNode getFilteredTooltip(IServerUtils utils, FilteredFunction fun) {
        ITooltipNode tooltips = new TooltipNode(translatable("ali.type.function.filtered"));
        ITooltipNode tooltip = new TooltipNode(translatable("ali.property.branch.modifier"));

        tooltips.add(getItemPredicateTooltip(utils, "ali.property.branch.filter", fun.filter));
        tooltips.add(tooltip);
        tooltips.add(getSubConditionsTooltip(utils, fun.predicates));
        tooltip.add(utils.getFunctionTooltip(utils, fun.modifier));

        return tooltips;
    }

    @NotNull
    public static ITooltipNode getCopyComponentsTooltip(IServerUtils utils, CopyComponentsFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.copy_components"));

        tooltip.add(getEnumTooltip(utils, "ali.property.value.source", fun.source));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.include", "ali.property.value.null", fun.include, RegistriesTooltipUtils::getDataComponentTypeTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.exclude", "ali.property.value.null", fun.exclude, RegistriesTooltipUtils::getDataComponentTypeTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetFireworksTooltip(IServerUtils utils, SetFireworksFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_fireworks"));

        tooltip.add(getStandaloneTooltip(utils, "ali.property.branch.explosions", "ali.property.branch.explosion", fun.explosions, GenericTooltipUtils::getFireworkExplosionTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.flight_duration", fun.flightDuration, GenericTooltipUtils::getIntegerTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetFireworkExplosionTooltip(IServerUtils utils, SetFireworkExplosionFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_firework_explosion"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.shape", fun.shape, GenericTooltipUtils::getEnumTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.colors", fun.colors, GenericTooltipUtils::getIntListTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.fade_colors", fun.fadeColors, GenericTooltipUtils::getIntListTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.trail", fun.trail, GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.twinkle", fun.twinkle, GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetBookCoverTooltip(IServerUtils utils, SetBookCoverFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_book_cover"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.author", fun.author, GenericTooltipUtils::getStringTooltip));
        tooltip.add(getFilterableTooltip(utils, "ali.property.branch.title", fun.title, GenericTooltipUtils::getStringTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.generation", fun.generation, GenericTooltipUtils::getIntegerTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetWrittenBookPagesTooltip(IServerUtils utils, SetWrittenBookPagesFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_written_book_pages"));

        tooltip.add(getFilterableTooltip(utils, "ali.property.branch.pages", "ali.property.branch.page", fun.pages, GenericTooltipUtils::getComponentTooltip));
        tooltip.add(getListOperationTooltip(utils, "ali.property.value.list_operation", fun.pageOperation));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetWritableBookPagesTooltip(IServerUtils utils, SetWritableBookPagesFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_writable_book_pages"));

        tooltip.add(getFilterableTooltip(utils, "ali.property.branch.pages", "ali.property.branch.page", fun.pages, GenericTooltipUtils::getStringTooltip));
        tooltip.add(getListOperationTooltip(utils, "ali.property.value.list_operation", fun.pageOperation));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getToggleTooltipsTooltip(IServerUtils utils, ToggleTooltips fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.toggle_tooltips"));

        tooltip.add(getMapTooltip(utils, "ali.property.branch.components", fun.values, GenericTooltipUtils::getToggleEntryTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetOminousBottleAmplifierTooltip(IServerUtils utils, SetOminousBottleAmplifierFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_ominous_bottle_amplifier"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.amplifier", fun.amplifierGenerator));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetCustomModelDataTooltip(IServerUtils utils, SetCustomModelDataFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_custom_model_data"));

        tooltip.add(getStandaloneTooltip(utils, "ali.property.branch.floats", "ali.property.value.null", fun.floats, GenericTooltipUtils::getNumberProviderTooltip));
        tooltip.add(getStandaloneTooltip(utils, "ali.property.branch.colors", "ali.property.value.null", fun.colors, GenericTooltipUtils::getNumberProviderTooltip));
        tooltip.add(getStandaloneTooltip(utils, "ali.property.branch.flags", "ali.property.value.null", fun.flags, GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getStandaloneTooltip(utils, "ali.property.branch.strings", "ali.property.value.null", fun.strings, GenericTooltipUtils::getStringTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }
}
