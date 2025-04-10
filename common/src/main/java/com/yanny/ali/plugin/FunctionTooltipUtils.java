package com.yanny.ali.plugin;

import com.yanny.ali.api.IUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static List<Component> getApplyBonusTooltip(IUtils utils, int pad, ApplyBonusCount fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.enchantment, GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getFormulaTooltip(utils, pad + 1, fun.formula));

        return components;
    }

    @NotNull
    public static List<Component> getCopyNameTooltip(IUtils utils, int pad, CopyNameFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_name")));
        components.addAll(getNameSourceTooltip(utils, pad + 1, fun.source));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCopyCustomDataTooltip(IUtils utils, int pad, CopyCustomDataFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_custom_data")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.source",
                Objects.requireNonNull(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.getKey(fun.source.getType()))));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.copy_operations", fun.operations, GenericTooltipUtils::getCopyOperationTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getCopyStateTooltip(IUtils utils, int pad, CopyBlockState fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_state")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.block, GenericTooltipUtils::getBlockTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.properties", fun.properties, GenericTooltipUtils::getPropertyTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantRandomlyTooltip(IUtils utils, int pad, EnchantRandomlyFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.enchantments", fun.enchantments, GenericTooltipUtils::getEnchantmentTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantWithLevelsTooltip(IUtils utils, int pad, EnchantWithLevelsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.levels", utils.convertNumber(utils, fun.levels)));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.treasure", fun.treasure));

        return components;
    }

    @NotNull
    public static List<Component> getExplorationMapTooltip(IUtils utils, int pad, ExplorationMapFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.exploration_map")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.destination", fun.destination));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.map_decoration", fun.mapDecoration,
                (u, p, c) -> getHolderTooltip(u, p, c, GenericTooltipUtils::getMapDecorationTypeTooltip)));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.zoom", (int) fun.zoom));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.search_radius", fun.searchRadius));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.skip_known_structures", fun.skipKnownStructures));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getExplosionDecayTooltip(IUtils utils, int pad, ApplyExplosionDecay fun) {
        return List.of(pad(pad, translatable("ali.type.function.explosion_decay")));
    }

    @NotNull
    public static List<Component> getFillPlayerHeadTooltip(IUtils utils, int pad, FillPlayerHead fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", "target", Optional.of(fun.entityTarget)));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFurnaceSmeltTooltip(IUtils utils, int pad, SmeltItemFunction fun) {
        return List.of(pad(pad, translatable("ali.type.function.furnace_smelt")));
    }

    @NotNull
    public static List<Component> getLimitCountTooltip(IUtils utils, int pad, LimitCount fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.limit", fun.limiter));

        return components;
    }

    @NotNull
    public static List<Component> getLootingEnchantTooltip(IUtils utils, int pad, LootingEnchantFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.looting_enchant")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.value", utils.convertNumber(utils, fun.value)));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.limit", fun.limit));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IUtils utils, int pad, FunctionReference fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.addAll(getResourceKeyTooltip(utils, pad + 1, "ali.property.value.name", fun.name));

        return components;
    }

    @NotNull
    public static List<Component> getSequenceTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SequenceFunction fun = (SequenceFunction) function;

        components.add(pad(pad, translatable("ali.type.function.sequence")));
        components.addAll(GenericTooltipUtils.getFunctionsTooltip(utils, pad + 1, fun.functions));

        return components;
    }

    @NotNull
    public static List<Component> getSetAttributesTooltip(IUtils utils, int pad, SetAttributesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.modifiers", fun.modifiers, GenericTooltipUtils::getModifierTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetBannerPatternTooltip(IUtils utils, int pad, SetBannerPatternFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.append", fun.append));
        components.addAll(getBannerPatternLayersTooltip(utils, pad + 1, fun.patterns));

        return components;
    }

    @NotNull
    public static List<Component> getSetContentsTooltip(IUtils utils, int pad, SetContainerContents fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.addAll(getContainerComponentManipulatorTooltip(utils, pad + 1, fun.component));
        //FIXME .getEntries

        return components;
    }

    @NotNull
    public static List<Component> getSetCountTooltip(IUtils utils, int pad, SetItemCountFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.count", utils.convertNumber(utils, fun.value)));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetDamageTooltip(IUtils utils, int pad, SetItemDamageFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.damage", utils.convertNumber(utils, fun.damage)));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetEnchantmentsTooltip(IUtils utils, int pad, SetEnchantmentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_enchantments")));

        if (!fun.enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.enchantments")));
            fun.enchantments.forEach((enchantment, value) -> {
                components.addAll(getHolderTooltip(utils, pad + 2, enchantment, GenericTooltipUtils::getEnchantmentTooltip));
                components.addAll(getRangeValueTooltip(utils, pad + 3, "ali.property.value.levels", utils.convertNumber(utils, value)));
            });
        }

        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetInstrumentTooltip(IUtils utils, int pad, SetInstrumentFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.options", fun.options));

        return components;
    }

    @NotNull
    public static List<Component> getSetLootTableTooltip(IUtils utils, int pad, SetContainerLootTable fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.addAll(getResourceKeyTooltip(utils, pad + 1, "ali.property.value.name", fun.name));
        components.addAll(getLongTooltip(utils, pad + 1, "ali.property.value.seed", fun.seed));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.type, GenericTooltipUtils::getBlockEntityTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetLoreTooltip(IUtils utils, int pad, SetLoreFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.addAll(getListOperationTooltip(utils, pad + 1, fun.mode));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.lore", fun.lore, (u, p, c) -> List.of(pad(pad + 2, c))));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.resolution_context", "target", fun.resolutionContext));

        return components;
    }

    @NotNull
    public static List<Component> getSetNameTooltip(IUtils utils, int pad, SetNameFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.name", fun.name, GenericTooltipUtils::getComponentTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.resolution_context", "target", fun.resolutionContext));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", fun.target));

        return components;
    }

    @NotNull
    public static List<Component> getSetCustomDataTooltip(IUtils utils, int pad, SetCustomDataFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_data")));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.tag", fun.tag.getAsString()));

        return components;
    }

    @NotNull
    public static List<Component> getSetPotionTooltip(IUtils utils, int pad, SetPotionFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.potion, GenericTooltipUtils::getPotionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetStewEffectTooltip(IUtils utils, int pad, SetStewEffectFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));

        if (!fun.effects.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.mob_effects")));
            fun.effects.forEach((effect) -> {
                components.addAll(getHolderTooltip(utils, pad + 2, effect.effect(), GenericTooltipUtils::getMobEffectTooltip));
                components.addAll(getRangeValueTooltip(utils, pad + 3, "ali.property.value.duration", utils.convertNumber(utils, effect.duration())));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetItemTooltip(IUtils utils, int pad, SetItemFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_item")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.item, GenericTooltipUtils::getItemTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetComponentsTooltip(IUtils utils, int pad, SetComponentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_components")));
        components.addAll(getDataComponentPatchTooltip(utils, pad + 1, fun.components));

        return components;
    }

    @NotNull
    public static List<Component> getModifyContentsTooltip(IUtils utils, int pad, ModifyContainerContents fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.modify_contents")));
        components.addAll(getContainerComponentManipulatorTooltip(utils, pad + 1, fun.component));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.modifier", fun.modifier, utils::getFunctionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getFilteredTooltip(IUtils utils, int pad, FilteredFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.filtered")));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.filter", fun.filter, GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.modifier", fun.modifier, utils::getFunctionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getCopyComponentsTooltip(IUtils utils, int pad, CopyComponentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_components")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.source", fun.source));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.include", fun.include, GenericTooltipUtils::getDataComponentTypeTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.exclude", fun.exclude, GenericTooltipUtils::getDataComponentTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetFireworksTooltip(IUtils utils, int pad, SetFireworksFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_fireworks")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.explosions", fun.explosions,
                (u, i, s, l) -> getCollectionTooltip(u, i, s, l.value(), GenericTooltipUtils::getFireworkExplosionTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.flight_duration", fun.flightDuration, GenericTooltipUtils::getIntegerTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetFireworkExplosionTooltip(IUtils utils, int pad, SetFireworkExplosionFunction fun) {
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
    public static List<Component> getSetBookCoverTooltip(IUtils utils, int pad, SetBookCoverFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_book_cover")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.author", fun.author, GenericTooltipUtils::getStringTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.title", fun.title,
                (u, i, s, v) -> getFilterableTooltip(u, i, s, v, GenericTooltipUtils::getStringTooltip)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.generation", fun.generation, GenericTooltipUtils::getIntegerTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetWrittenBookPagesTooltip(IUtils utils, int pad, SetWrittenBookPagesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_written_book_pages")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.pages", fun.pages,
                (u, i, v) -> getFilterableTooltip(u, i, "ali.property.branch.page", v, GenericTooltipUtils::getComponentTooltip)));
        components.addAll(getListOperationTooltip(utils, pad + 1, fun.pageOperation));

        return components;
    }

    @NotNull
    public static List<Component> getSetWritableBookPagesTooltip(IUtils utils, int pad, SetWritableBookPagesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_writable_book_pages")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.pages", fun.pages,
                (u, i, v) -> getFilterableTooltip(u, i, "ali.property.branch.page", v, GenericTooltipUtils::getStringTooltip)));
        components.addAll(getListOperationTooltip(utils, pad + 1, fun.pageOperation));

        return components;
    }

    @NotNull
    public static List<Component> getToggleTooltipsTooltip(IUtils utils, int pad, ToggleTooltips fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.toggle_tooltips")));

        if (!fun.values.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.values")));
            fun.values.forEach((toggle, value) -> {
                components.addAll(getDataComponentTypeTooltip(utils, pad + 2, toggle.type()));
                components.addAll(getBooleanTooltip(utils, pad + 3, "ali.property.value.value", value));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetOminousBottleAmplifierTooltip(IUtils utils, int pad, SetOminousBottleAmplifierFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_ominous_bottle_amplifier")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.amplifier", utils.convertNumber(utils, fun.amplifierGenerator)));

        return components;
    }

    @NotNull
    public static List<Component> getSetCustomModelDataTooltip(IUtils utils, int pad, SetCustomModelDataFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_model_data")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.value", utils.convertNumber(utils, fun.valueProvider)));

        return components;
    }
}
