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
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.enchantment).build("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, fun.formula).build("ali.property.value.formula"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.apply_bonus");
    }

    @NotNull
    public static ITooltipNode getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.source.contextParam().name()).build("ali.property.value.source"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_name");
    }

    @NotNull
    public static ITooltipNode getCopyCustomDataTooltip(IServerUtils utils, CopyCustomDataFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.source.getType()).build("ali.property.value.source"))
                .add(getCollectionTooltip(utils, "ali.property.branch.operation", fun.operations).build("ali.property.branch.copy_operations"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_custom_data");
    }

    @NotNull
    public static ITooltipNode getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.block).build("ali.property.value.block"))
                .add(utils.getValueTooltip(utils, fun.properties).build("ali.property.branch.properties"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_state");
    }

    @NotNull
    public static ITooltipNode getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.options).build("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, fun.onlyCompatible).build("ali.property.value.only_compatible"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.enchant_randomly");
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.levels).build("ali.property.value.levels"))
                .add(utils.getValueTooltip(utils, fun.options).build("ali.property.branch.options"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.enchant_with_levels");
    }

    @NotNull
    public static ITooltipNode getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.destination).build("ali.property.value.destination"))
                .add(utils.getValueTooltip(utils, fun.mapDecoration).build("ali.property.value.map_decoration"))
                .add(utils.getValueTooltip(utils, fun.zoom).build("ali.property.value.zoom"))
                .add(utils.getValueTooltip(utils, fun.searchRadius).build("ali.property.value.search_radius"))
                .add(utils.getValueTooltip(utils, fun.skipKnownStructures).build("ali.property.value.skip_known_structures"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.exploration_map");
    }

    @NotNull
    public static ITooltipNode getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return BranchTooltipNode.branch()
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.explosion_decay");
    }

    @NotNull
    public static ITooltipNode getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.entityTarget).build("ali.property.value.target"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.fill_player_head");
    }

    @NotNull
    public static ITooltipNode getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return BranchTooltipNode.branch()
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.furnace_smelt");
    }

    @NotNull
    public static ITooltipNode getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.limiter).build("ali.property.value.limit"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.limit_count");
    }

    @NotNull
    public static ITooltipNode getEnchantedCountIncreaseTooltip(IServerUtils utils, EnchantedCountIncreaseFunction fun) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.enchantment).build("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, fun.value).build("ali.property.value.value"));

        if (fun.limit > 0) {
            tooltip.add(utils.getValueTooltip(utils, fun.limit).build("ali.property.value.limit"));
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"));
        return tooltip.build("ali.type.function.enchanted_count_increase");
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.reference");
    }

    @NotNull
    public static ITooltipNode getSequenceTooltip(IServerUtils utils, SequenceFunction fun) {
        return BranchTooltipNode.branch()
                .add(GenericTooltipUtils.getFunctionListTooltip(utils, fun.functions))
                .build("ali.type.function.sequence");
    }

    @NotNull
    public static ITooltipNode getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        return BranchTooltipNode.branch()
                .add(GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.modifier", fun.modifiers).build("ali.property.branch.modifiers"))
                .add(utils.getValueTooltip(utils, fun.replace).build("ali.property.value.replace"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_attributes");
    }

    @NotNull
    public static ITooltipNode getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.append).build("ali.property.value.append"))
                .add(utils.getValueTooltip(utils, fun.patterns).build("ali.property.branch.banner_patterns"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_banner_pattern");
    }

    @NotNull
    public static ITooltipNode getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.component).build("ali.property.value.container"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_contents");
                // TODO entries
    }

    @NotNull
    public static ITooltipNode getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return BranchTooltipNode.branch(true)
                .add(utils.getValueTooltip(utils, fun.value).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_count");
    }

    @NotNull
    public static ITooltipNode getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.damage).build("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_damage");
    }

    @NotNull
    public static ITooltipNode getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return BranchTooltipNode.branch()
                .add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).build("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, fun.add).build("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_enchantments");
    }

    @NotNull
    public static ITooltipNode getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.options).build("ali.property.value.options"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_instrument");
    }

    @NotNull
    public static ITooltipNode getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.seed).build("ali.property.value.seed"))
                .add(utils.getValueTooltip(utils, fun.type).build("ali.property.value.block_entity_type"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_loot_table");
    }

    @NotNull
    public static ITooltipNode getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.mode).build("ali.property.value.list_operation"))
                .add(utils.getValueTooltip(utils, fun.lore).build("ali.property.branch.lore"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).build("ali.property.value.resolution_context"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_lore");
    }

    @NotNull
    public static ITooltipNode getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.name).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).build("ali.property.value.resolution_context"))
                .add(utils.getValueTooltip(utils, fun.target).build("ali.property.value.target"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_name");
    }

    @NotNull
    public static ITooltipNode getSetCustomDataTooltip(IServerUtils utils, SetCustomDataFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.tag.toString()).build("ali.property.value.tag"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_custom_data");
    }

    @NotNull
    public static ITooltipNode getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.potion).build("ali.property.value.potion"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_potion");
    }

    @NotNull
    public static ITooltipNode getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.effects.isEmpty()) {
            tooltip = utils.getValueTooltip(utils, fun.effects);
        } else {
            tooltip = BranchTooltipNode.branch();
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"));
        return tooltip.build("ali.type.function.set_stew_effect");
    }

    @NotNull
    public static ITooltipNode getSetItemTooltip(IServerUtils utils, SetItemFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.item).build("ali.property.value.item"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_item");
    }

    @NotNull
    public static ITooltipNode getSetComponentsTooltip(IServerUtils utils, SetComponentsFunction fun) {
        return utils.getValueTooltip(utils, fun.components)
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_components");
    }

    @NotNull
    public static ITooltipNode getModifyContentsTooltip(IServerUtils utils, ModifyContainerContents fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.component).build("ali.property.value.container"))
                .add(BranchTooltipNode.branch().add(utils.getFunctionTooltip(utils, fun.modifier)).build("ali.property.branch.modifier"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.modify_contents");
    }

    @NotNull
    public static ITooltipNode getFilteredTooltip(IServerUtils utils, FilteredFunction fun) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.filter).build("ali.property.branch.filter"));

        fun.onPass.ifPresent(f -> tooltip.add(BranchTooltipNode.branch().add(utils.getFunctionTooltip(utils, f)).build("ali.property.branch.on_pass")));
        fun.onFail.ifPresent(f -> tooltip.add(BranchTooltipNode.branch().add(utils.getFunctionTooltip(utils, f)).build("ali.property.branch.on_fail")));

        return tooltip.add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.filtered");
    }

    @NotNull
    public static ITooltipNode getCopyComponentsTooltip(IServerUtils utils, CopyComponentsFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.source.contextParam().name()).build("ali.property.value.source"))
                .add(utils.getValueTooltip(utils, fun.include).build("ali.property.branch.include"))
                .add(utils.getValueTooltip(utils, fun.exclude).build("ali.property.branch.exclude"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.copy_components");
    }

    @NotNull
    public static ITooltipNode getSetFireworksTooltip(IServerUtils utils, SetFireworksFunction fun) {
        return BranchTooltipNode.branch()
                .add(GenericTooltipUtils.getStandaloneTooltip(utils, "ali.property.branch.explosion", fun.explosions).build("ali.property.branch.explosions"))
                .add(utils.getValueTooltip(utils, fun.flightDuration).build("ali.property.value.flight_duration"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_fireworks");
    }

    @NotNull
    public static ITooltipNode getSetFireworkExplosionTooltip(IServerUtils utils, SetFireworkExplosionFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.shape).build("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, fun.colors).build("ali.property.value.colors"))
                .add(utils.getValueTooltip(utils, fun.fadeColors).build("ali.property.value.fade_colors"))
                .add(utils.getValueTooltip(utils, fun.trail).build("ali.property.value.trail"))
                .add(utils.getValueTooltip(utils, fun.twinkle).build("ali.property.value.twinkle"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_firework_explosion");
    }

    @NotNull
    public static ITooltipNode getSetBookCoverTooltip(IServerUtils utils, SetBookCoverFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.author).build("ali.property.value.author"))
                .add(utils.getValueTooltip(utils, fun.title).build("ali.property.branch.title"))
                .add(utils.getValueTooltip(utils, fun.generation).build("ali.property.value.generation"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_book_cover");
    }

    @NotNull
    public static ITooltipNode getSetWrittenBookPagesTooltip(IServerUtils utils, SetWrittenBookPagesFunction fun) {
        return BranchTooltipNode.branch()
                .add(getFilterableTooltip(utils, "ali.property.branch.page", fun.pages).build("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, fun.pageOperation).build("ali.property.value.list_operation"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_written_book_pages");
    }

    @NotNull
    public static ITooltipNode getSetWritableBookPagesTooltip(IServerUtils utils, SetWritableBookPagesFunction fun) {
        return BranchTooltipNode.branch()
                .add(getFilterableTooltip(utils, "ali.property.branch.page", fun.pages).build("ali.property.branch.pages"))
                .add(utils.getValueTooltip(utils, fun.pageOperation).build("ali.property.value.list_operation"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_writable_book_pages");
    }

    @NotNull
    public static ITooltipNode getToggleTooltipsTooltip(IServerUtils utils, ToggleTooltips fun) {
        return BranchTooltipNode.branch()
                .add(getMapTooltip(utils, fun.values, GenericTooltipUtils::getDataComponentEntryTooltip).build("ali.property.branch.components"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.toggle_tooltips");
    }

    @NotNull
    public static ITooltipNode getSetOminousBottleAmplifierTooltip(IServerUtils utils, SetOminousBottleAmplifierFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.amplifierGenerator).build("ali.property.value.amplifier"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_ominous_bottle_amplifier");
    }

    @NotNull
    public static ITooltipNode getSetCustomModelDataTooltip(IServerUtils utils, SetCustomModelDataFunction fun) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fun.floats).build("ali.property.branch.floats"))
                .add(utils.getValueTooltip(utils, fun.colors).build("ali.property.branch.colors"))
                .add(utils.getValueTooltip(utils, fun.flags).build("ali.property.branch.flags"))
                .add(utils.getValueTooltip(utils, fun.strings).build("ali.property.branch.strings"))
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_custom_model_data");
    }

    @NotNull
    public static ITooltipNode getDiscardItemTooltip(IServerUtils utils, DiscardItem fun) {
        return BranchTooltipNode.branch()
                .add(getSubConditionsTooltip(utils, fun.predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.discard_item");
    }
}
