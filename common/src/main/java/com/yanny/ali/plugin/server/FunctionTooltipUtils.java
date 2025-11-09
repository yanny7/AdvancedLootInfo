package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
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
    public static ITooltipNode getCopyCustomDataTooltip(IServerUtils utils, CopyCustomDataFunction fun) {
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
        return BranchTooltipNode.branch("ali.type.function.enchant_randomly")
                .add(utils.getValueTooltip(utils, fun.options).key("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, fun.onlyCompatible).key("ali.property.value.only_compatible"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.enchant_with_levels")
                .add(utils.getValueTooltip(utils, fun.levels).key("ali.property.value.levels"))
                .add(utils.getValueTooltip(utils, fun.options).key("ali.property.branch.options"))
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
    public static ITooltipNode getEnchantedCountIncreaseTooltip(IServerUtils utils, EnchantedCountIncreaseFunction fun) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch("ali.type.function.enchanted_count_increase", true)
                .add(utils.getValueTooltip(utils, fun.enchantment).key("ali.property.value.enchantment"))
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
                .add(GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.modifier", fun.modifiers).key("ali.property.branch.modifiers"))
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
        return BranchTooltipNode.branch("ali.type.function.set_item")
                .add(utils.getValueTooltip(utils, fun.item).key("ali.property.value.item"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetComponentsTooltip(IServerUtils utils, SetComponentsFunction fun) {
        return utils.getValueTooltip(utils, fun.components).key("ali.type.function.set_components")
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getModifyContentsTooltip(IServerUtils utils, ModifyContainerContents fun) {
        return BranchTooltipNode.branch("ali.type.function.modify_contents")
                .add(utils.getValueTooltip(utils, fun.component).key("ali.property.value.container"))
                .add(BranchTooltipNode.branch("ali.property.branch.modifier").add(utils.getFunctionTooltip(utils, fun.modifier)))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getFilteredTooltip(IServerUtils utils, FilteredFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.filtered")
                .add(utils.getValueTooltip(utils, fun.filter).key("ali.property.branch.filter"))
                .add(BranchTooltipNode.branch("ali.property.branch.modifier").add(utils.getFunctionTooltip(utils, fun.modifier)))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyComponentsTooltip(IServerUtils utils, CopyComponentsFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_components")
                .add(utils.getValueTooltip(utils, fun.source).key("ali.property.value.source"))
                .add(utils.getValueTooltip(utils, fun.include).key("ali.property.branch.include"))
                .add(utils.getValueTooltip(utils, fun.exclude).key("ali.property.branch.exclude"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetFireworksTooltip(IServerUtils utils, SetFireworksFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_fireworks")
                .add(getStandaloneTooltip(utils, "ali.property.branch.explosion", fun.explosions).key("ali.property.branch.explosions"))
                .add(utils.getValueTooltip(utils, fun.flightDuration).key("ali.property.value.flight_duration"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetFireworkExplosionTooltip(IServerUtils utils, SetFireworkExplosionFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_firework_explosion")
                .add(utils.getValueTooltip(utils, fun.shape).key("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, fun.colors).key("ali.property.value.colors"))
                .add(utils.getValueTooltip(utils, fun.fadeColors).key("ali.property.value.fade_colors"))
                .add(utils.getValueTooltip(utils, fun.trail).key("ali.property.value.trail"))
                .add(utils.getValueTooltip(utils, fun.twinkle).key("ali.property.value.twinkle"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetBookCoverTooltip(IServerUtils utils, SetBookCoverFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_book_cover")
                .add(utils.getValueTooltip(utils, fun.author).key("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, fun.title).key("ali.property.branch.title"))
                .add(utils.getValueTooltip(utils, fun.generation).key("ali.property.value.generation"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetWrittenBookPagesTooltip(IServerUtils utils, SetWrittenBookPagesFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_written_book_pages")
                .add(getFilterableTooltip(utils, "ali.property.branch.page", fun.pages).key("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, fun.pageOperation).key("ali.property.value.list_operation"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetWritableBookPagesTooltip(IServerUtils utils, SetWritableBookPagesFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_writable_book_pages")
                .add(getFilterableTooltip(utils, "ali.property.branch.page", fun.pages).key("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, fun.pageOperation).key("ali.property.value.list_operation"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getToggleTooltipsTooltip(IServerUtils utils, ToggleTooltips fun) {
        return BranchTooltipNode.branch("ali.type.function.toggle_tooltips")
                .add(getMapTooltip(utils, fun.values, GenericTooltipUtils::getToggleEntryTooltip).key("ali.property.branch.components"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetOminousBottleAmplifierTooltip(IServerUtils utils, SetOminousBottleAmplifierFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_ominous_bottle_amplifier")
                .add(utils.getValueTooltip(utils, fun.amplifierGenerator).key("ali.property.value.amplifier"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetCustomModelDataTooltip(IServerUtils utils, SetCustomModelDataFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_custom_model_data")
                .add(utils.getValueTooltip(utils, fun.valueProvider).key("ali.property.value.value"))
                .add(getSubConditionsTooltip(utils, fun.predicates).key("ali.property.branch.conditions"));
    }
}
