package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static ITooltipNode getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        return BranchTooltipNode.branch("ali.type.function.apply_bonus", true)
                .add(utils.getValueTooltip(utils, fun.enchantment).key("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, fun.formula).key("ali.property.value.formula"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_name")
                .add(utils.getValueTooltip(utils, fun.source).key("ali.property.value.source"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyCustomDataTooltip(IServerUtils utils, CopyNbtFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_custom_data")
                .add(utils.getValueTooltip(utils, fun.source.getType()).key("ali.property.value.source"))
                .add(getCollectionTooltip(utils, "ali.property.branch.operation", fun.operations).key("ali.property.branch.copy_operations"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_state")
                .add(utils.getValueTooltip(utils, fun.block).key("ali.property.value.block"))
                .add(utils.getValueTooltip(utils, fun.properties).key("ali.property.branch.properties"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        IKeyTooltipNode tooltip;

        if (fun.enchantments.isPresent() && fun.enchantments.get().size() > 0) {
            tooltip = utils.getValueTooltip(utils, fun.enchantments).key("ali.type.function.enchant_randomly");
        } else {
            tooltip = BranchTooltipNode.branch("ali.type.function.enchant_randomly");
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.enchant_with_levels")
                .add(utils.getValueTooltip(utils, fun.levels).key("ali.property.value.levels"))
                .add(utils.getValueTooltip(utils, fun.treasure).key("ali.property.value.treasure"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.exploration_map")
                .add(utils.getValueTooltip(utils, fun.destination).key("ali.property.value.destination"))
                .add(utils.getValueTooltip(utils, fun.mapDecoration).key("ali.property.value.map_decoration"))
                .add(utils.getValueTooltip(utils, fun.zoom).key("ali.property.value.zoom"))
                .add(utils.getValueTooltip(utils, fun.searchRadius).key("ali.property.value.search_radius"))
                .add(utils.getValueTooltip(utils, fun.skipKnownStructures).key("ali.property.value.skip_known_structures"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return BranchTooltipNode.branch("ali.type.function.explosion_decay")
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return BranchTooltipNode.branch("ali.type.function.fill_player_head")
                .add(utils.getValueTooltip(utils, fun.entityTarget).key("ali.property.value.target"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.furnace_smelt")
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return BranchTooltipNode.branch("ali.type.function.limit_count", true)
                .add(utils.getValueTooltip(utils, fun.limiter).key("ali.property.value.limit"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getLootingEnchantTooltip(IServerUtils utils, LootingEnchantFunction fun) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch("ali.type.function.looting_enchant", true)
                .add(utils.getValueTooltip(utils, fun.value).key("ali.property.value.value"));

        if (fun.limit > 0) {
            tooltip.add(utils.getValueTooltip(utils, fun.limit).key("ali.property.value.limit"));
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
        return tooltip;
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return BranchTooltipNode.branch("ali.type.function.reference")
                .add(utils.getValueTooltip(utils, fun.name).key("ali.property.value.name"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSequenceTooltip(IServerUtils utils, SequenceFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.sequence")
                .add(GenericTooltipUtils.getFunctionListTooltip(utils, fun.functions));
    }

    @NotNull
    public static ITooltipNode getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_attributes")
                .add(utils.getValueTooltip(utils, fun.modifiers).key("ali.property.branch.modifiers"))
                .add(utils.getValueTooltip(utils, fun.replace).key("ali.property.value.replace"))
                .add(getSubConditionsTooltip(utils, fun.predicates));
    }

    @NotNull
    public static ITooltipNode getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_banner_pattern")
                .add(utils.getValueTooltip(utils, fun.append).key("ali.property.value.append"))
                .add(utils.getValueTooltip(utils, fun.patterns).key("ali.property.branch.banner_patterns"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return BranchTooltipNode.branch("ali.type.function.set_contents")
                .add(utils.getValueTooltip(utils, fun.component).key("ali.property.value.container"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
                // TODO entries
    }

    @NotNull
    public static ITooltipNode getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_count", true)
                .add(utils.getValueTooltip(utils, fun.value).key("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, fun.add).key("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_damage")
                .add(utils.getValueTooltip(utils, fun.damage).key("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, fun.add).key("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_enchantments")
                .add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).key("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, fun.add).key("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_instrument")
                .add(utils.getValueTooltip(utils, fun.options).key("ali.property.value.options"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return BranchTooltipNode.branch("ali.type.function.set_loot_table")
                .add(utils.getValueTooltip(utils, fun.name).key("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.seed).key("ali.property.value.seed"))
                .add(utils.getValueTooltip(utils, fun.type).key("ali.property.value.block_entity_type"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_lore")
                .add(utils.getValueTooltip(utils, fun.mode).key("ali.property.value.list_operation"))
                .add(utils.getValueTooltip(utils, fun.lore).key("ali.property.branch.lore"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).key("ali.property.value.resolution_context"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_name")
                .add(utils.getValueTooltip(utils, fun.name).key("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).key("ali.property.value.resolution_context"))
                .add(utils.getValueTooltip(utils, fun.target).key("ali.property.value.target"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetCustomDataTooltip(IServerUtils utils, SetCustomDataFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_custom_data")
                .add(utils.getValueTooltip(utils, fun.tag.getAsString()).key("ali.property.value.tag"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_potion")
                .add(utils.getValueTooltip(utils, fun.potion).key("ali.property.value.potion"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.effects.isEmpty()) {
            tooltip = utils.getValueTooltip(utils, fun.effects).key("ali.type.function.set_stew_effect");
        } else {
            tooltip = BranchTooltipNode.branch("ali.type.function.set_stew_effect");
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
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

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.value", fun.valueProvider));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }
}
