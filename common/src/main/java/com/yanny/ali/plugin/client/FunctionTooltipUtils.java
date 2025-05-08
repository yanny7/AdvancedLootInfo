package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.getDataComponentTypeTooltip;
import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.getLootNbtProviderTypeTooltip;

public class FunctionTooltipUtils {
    @NotNull
    public static List<Component> getApplyBonusTooltip(IClientUtils utils, int pad, ApplyBonusCount fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.enchantment", fun.enchantment, RegistriesTooltipUtils::getEnchantmentTooltip));
        components.addAll(getFormulaTooltip(utils, pad + 1, "ali.property.value.formula", fun.formula));

        return components;
    }

    @NotNull
    public static List<Component> getCopyNameTooltip(IClientUtils utils, int pad, CopyNameFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_name")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.source", fun.source));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCopyCustomDataTooltip(IClientUtils utils, int pad, CopyCustomDataFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_custom_data")));
        components.addAll(getLootNbtProviderTypeTooltip(utils, pad + 1, "ali.property.value.source", fun.source.getType()));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.copy_operations", "ali.property.branch.operation", fun.operations, GenericTooltipUtils::getCopyOperationTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getCopyStateTooltip(IClientUtils utils, int pad, CopyBlockState fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_state")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.block", fun.block, RegistriesTooltipUtils::getBlockTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.properties", "ali.property.value.null", fun.properties, GenericTooltipUtils::getPropertyTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantRandomlyTooltip(IClientUtils utils, int pad, EnchantRandomlyFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.enchantments", "ali.property.value.null", fun.options, RegistriesTooltipUtils::getEnchantmentTooltip));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.only_compatible", fun.onlyCompatible));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantWithLevelsTooltip(IClientUtils utils, int pad, EnchantWithLevelsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.levels", fun.levels));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.options", "ali.property.value.null", fun.options, RegistriesTooltipUtils::getEnchantmentTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getExplorationMapTooltip(IClientUtils utils, int pad, ExplorationMapFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.exploration_map")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.destination", fun.destination));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.map_decoration", fun.mapDecoration, GenericTooltipUtils::getMapDecorationTypeTooltip));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.zoom", (int) fun.zoom));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.search_radius", fun.searchRadius));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.skip_known_structures", fun.skipKnownStructures));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getExplosionDecayTooltip(IClientUtils ignoredUtils, int pad, ApplyExplosionDecay ignoredFun) {
        return List.of(pad(pad, translatable("ali.type.function.explosion_decay")));
    }

    @NotNull
    public static List<Component> getFillPlayerHeadTooltip(IClientUtils utils, int pad, FillPlayerHead fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", fun.entityTarget));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFurnaceSmeltTooltip(IClientUtils ignoredUtils, int pad, SmeltItemFunction ignoredFun) {
        return List.of(pad(pad, translatable("ali.type.function.furnace_smelt")));
    }

    @NotNull
    public static List<Component> getLimitCountTooltip(IClientUtils utils, int pad, LimitCount fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.limit", fun.limiter));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantedCountIncreaseTooltip(IClientUtils utils, int pad, EnchantedCountIncreaseFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchanted_count_increase")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.enchantment", fun.enchantment, RegistriesTooltipUtils::getEnchantmentTooltip));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.value", fun.value));

        if (fun.limit > 0) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.limit", fun.limit));
        }

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IClientUtils utils, int pad, FunctionReference fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.addAll(getResourceKeyTooltip(utils, pad + 1, "ali.property.value.name", fun.name));

        return components;
    }

    @NotNull
    public static List<Component> getSequenceTooltip(IClientUtils utils, int pad, SequenceFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.sequence")));
        components.addAll(getFunctionsTooltip(utils, pad + 1, fun.functions));

        return components;
    }

    @NotNull
    public static List<Component> getSetAttributesTooltip(IClientUtils utils, int pad, SetAttributesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.modifiers", "ali.property.branch.modifier", fun.modifiers, GenericTooltipUtils::getModifierTooltip));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.replace", fun.replace));

        return components;
    }

    @NotNull
    public static List<Component> getSetBannerPatternTooltip(IClientUtils utils, int pad, SetBannerPatternFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.append", fun.append));
        components.addAll(getBannerPatternLayersTooltip(utils, pad + 1, "ali.property.branch.banner_patterns", fun.patterns));

        return components;
    }

    @NotNull
    public static List<Component> getSetContentsTooltip(IClientUtils utils, int pad, SetContainerContents fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.addAll(getContainerComponentManipulatorTooltip(utils, pad + 1, "ali.property.value.container", fun.component));
        //TODO entries

        return components;
    }

    @NotNull
    public static List<Component> getSetCountTooltip(IClientUtils utils, int pad, SetItemCountFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.count", fun.value));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetDamageTooltip(IClientUtils utils, int pad, SetItemDamageFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.damage", fun.damage));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetEnchantmentsTooltip(IClientUtils utils, int pad, SetEnchantmentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_enchantments")));

        if (!fun.enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.enchantments")));
            fun.enchantments.forEach((enchantment, value) -> {
                components.addAll(getHolderTooltip(utils, pad + 2, "ali.property.value.null", enchantment, RegistriesTooltipUtils::getEnchantmentTooltip));
                components.addAll(getNumberProviderTooltip(utils, pad + 3, "ali.property.value.levels", value));
            });
        }

        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetInstrumentTooltip(IClientUtils utils, int pad, SetInstrumentFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.options", fun.options));

        return components;
    }

    @NotNull
    public static List<Component> getSetLootTableTooltip(IClientUtils utils, int pad, SetContainerLootTable fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.addAll(getResourceKeyTooltip(utils, pad + 1, "ali.property.value.name", fun.name));
        components.addAll(getLongTooltip(utils, pad + 1, "ali.property.value.seed", fun.seed));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.block_entity_type", fun.type, RegistriesTooltipUtils::getBlockEntityTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetLoreTooltip(IClientUtils utils, int pad, SetLoreFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.addAll(getListOperationTooltip(utils, pad + 1, "ali.property.value.list_operation", fun.mode));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.lore", fun.lore, (u, p, c) -> List.of(pad(pad + 2, c))));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.resolution_context", fun.resolutionContext, GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetNameTooltip(IClientUtils utils, int pad, SetNameFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.name", fun.name, GenericTooltipUtils::getComponentTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.resolution_context", fun.resolutionContext, GenericTooltipUtils::getEnumTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", fun.target));

        return components;
    }

    @NotNull
    public static List<Component> getSetCustomDataTooltip(IClientUtils utils, int pad, SetCustomDataFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_data")));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.tag", fun.tag.toString()));

        return components;
    }

    @NotNull
    public static List<Component> getSetPotionTooltip(IClientUtils utils, int pad, SetPotionFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.potion", fun.potion, RegistriesTooltipUtils::getPotionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetStewEffectTooltip(IClientUtils utils, int pad, SetStewEffectFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.mob_effects", "ali.property.value.mob_effect", fun.effects, GenericTooltipUtils::getEffectEntryTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetItemTooltip(IClientUtils utils, int pad, SetItemFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_item")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.item", fun.item, RegistriesTooltipUtils::getItemTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetComponentsTooltip(IClientUtils utils, int pad, SetComponentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_components")));
        components.addAll(getDataComponentPatchTooltip(utils, pad + 1, "ali.property.branch.components", fun.components));

        return components;
    }

    @NotNull
    public static List<Component> getModifyContentsTooltip(IClientUtils utils, int pad, ModifyContainerContents fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.modify_contents")));
        components.addAll(getContainerComponentManipulatorTooltip(utils, pad + 1, "ali.property.value.container", fun.component));
        components.add(pad(pad + 1, translatable("ali.property.branch.modifier")));
        components.addAll(utils.getFunctionTooltip(utils, pad + 2, fun.modifier));

        return components;
    }

    @NotNull
    public static List<Component> getFilteredTooltip(IClientUtils utils, int pad, FilteredFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.filtered")));
        components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.filter", fun.filter));
        components.add(pad(pad + 1, translatable("ali.property.branch.modifier")));
        components.addAll(utils.getFunctionTooltip(utils, pad + 2, fun.modifier));

        return components;
    }

    @NotNull
    public static List<Component> getCopyComponentsTooltip(IClientUtils utils, int pad, CopyComponentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_components")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.source", fun.source));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.include", "ali.property.value.null", fun.include, RegistriesTooltipUtils::getDataComponentTypeTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.exclude", "ali.property.value.null", fun.exclude, RegistriesTooltipUtils::getDataComponentTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetFireworksTooltip(IClientUtils utils, int pad, SetFireworksFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_fireworks")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.explosions", fun.explosions,
                (u, i, s, l) -> getCollectionTooltip(u, i, s, "ali.property.branch.explosion", l.value(), GenericTooltipUtils::getFireworkExplosionTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.flight_duration", fun.flightDuration, GenericTooltipUtils::getIntegerTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetFireworkExplosionTooltip(IClientUtils utils, int pad, SetFireworkExplosionFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_firework_explosion")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.shape", fun.shape, GenericTooltipUtils::getEnumTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.colors", fun.colors, GenericTooltipUtils::getIntListTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.fade_colors", fun.fadeColors, GenericTooltipUtils::getIntListTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.trail", fun.trail, GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.twinkle", fun.twinkle, GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetBookCoverTooltip(IClientUtils utils, int pad, SetBookCoverFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_book_cover")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.author", fun.author, GenericTooltipUtils::getStringTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.title", fun.title,
                (u, i, s, v) -> getFilterableTooltip(u, i, s, v, GenericTooltipUtils::getStringTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.generation", fun.generation, GenericTooltipUtils::getIntegerTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetWrittenBookPagesTooltip(IClientUtils utils, int pad, SetWrittenBookPagesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_written_book_pages")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.pages", fun.pages,
                (u, i, v) -> getFilterableTooltip(u, i, "ali.property.branch.page", v, GenericTooltipUtils::getComponentTooltip)));
        components.addAll(getListOperationTooltip(utils, pad + 1, "ali.property.value.list_operation", fun.pageOperation));

        return components;
    }

    @NotNull
    public static List<Component> getSetWritableBookPagesTooltip(IClientUtils utils, int pad, SetWritableBookPagesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_writable_book_pages")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.pages", fun.pages,
                (u, i, v) -> getFilterableTooltip(u, i, "ali.property.branch.page", v, GenericTooltipUtils::getStringTooltip)));
        components.addAll(getListOperationTooltip(utils, pad + 1, "ali.property.value.list_operation", fun.pageOperation));

        return components;
    }

    @NotNull
    public static List<Component> getToggleTooltipsTooltip(IClientUtils utils, int pad, ToggleTooltips fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.toggle_tooltips")));

        if (!fun.values.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.values")));
            fun.values.forEach((toggle, value) -> {
                components.addAll(getDataComponentTypeTooltip(utils, pad + 2, "ali.property.value.type", toggle));
                components.addAll(getBooleanTooltip(utils, pad + 3, "ali.property.value.value", value));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetOminousBottleAmplifierTooltip(IClientUtils utils, int pad, SetOminousBottleAmplifierFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_ominous_bottle_amplifier")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.amplifier", fun.amplifierGenerator));

        return components;
    }

    @NotNull
    public static List<Component> getSetCustomModelDataTooltip(IClientUtils utils, int pad, SetCustomModelDataFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_model_data")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.floats", fun.floats,
                (u, p, k, s) -> GenericTooltipUtils.getStandaloneListOperationTooltip(u, p, k, s, GenericTooltipUtils::getNumberProviderTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.colors", fun.colors,
                (u, p, k, s) -> GenericTooltipUtils.getStandaloneListOperationTooltip(u, p, k, s, GenericTooltipUtils::getNumberProviderTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.flags", fun.flags,
                (u, p, k, s) -> GenericTooltipUtils.getStandaloneListOperationTooltip(u, p, k, s, GenericTooltipUtils::getBooleanTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.strings", fun.strings,
                (u, p, k, s) -> GenericTooltipUtils.getStandaloneListOperationTooltip(u, p, k, s, GenericTooltipUtils::getStringTooltip)));

        return components;
    }
}
