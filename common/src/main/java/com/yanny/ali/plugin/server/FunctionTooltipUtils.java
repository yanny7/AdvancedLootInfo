package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static ITooltipNode getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        return BranchTooltipNode.branch("ali.type.function.apply_bonus", true)
                .add(utils.getValueTooltip(utils, fun.enchantment).key("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, fun.formula).key("ali.property.value.formula"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_name")
                .add(utils.getValueTooltip(utils, fun.source).key("ali.property.value.source"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyNbtTooltip(IServerUtils utils, CopyNbtFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_nbt")
                .add(utils.getValueTooltip(utils, fun.source.getType()).key("ali.property.value.nbt_provider"))
                .add(getCollectionTooltip(utils, "ali.property.branch.operation", fun.operations).key("ali.property.branch.operations"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return BranchTooltipNode.branch("ali.type.function.copy_state")
                .add(utils.getValueTooltip(utils, fun.block).key("ali.property.value.block"))
                .add(utils.getValueTooltip(utils, fun.properties).key("ali.property.branch.properties"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.enchantments.isEmpty()) {
            tooltip = utils.getValueTooltip(utils, fun.enchantments).key("ali.type.function.enchant_randomly");
        } else {
            tooltip = BranchTooltipNode.branch("ali.type.function.enchant_randomly");
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.enchant_with_levels")
                .add(utils.getValueTooltip(utils, fun.levels).key("ali.property.value.levels"))
                .add(utils.getValueTooltip(utils, fun.treasure).key("ali.property.value.treasure"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.exploration_map")
                .add(utils.getValueTooltip(utils, fun.destination).key("ali.property.value.destination"))
                .add(utils.getValueTooltip(utils, fun.mapDecoration).key("ali.property.value.map_decoration"))
                .add(utils.getValueTooltip(utils, fun.zoom).key("ali.property.value.zoom"))
                .add(utils.getValueTooltip(utils, fun.searchRadius).key("ali.property.value.search_radius"))
                .add(utils.getValueTooltip(utils, fun.skipKnownStructures).key("ali.property.value.skip_known_structures"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return BranchTooltipNode.branch("ali.type.function.explosion_decay")
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return BranchTooltipNode.branch("ali.type.function.fill_player_head")
                .add(utils.getValueTooltip(utils, fun.entityTarget).key("ali.property.value.target"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.furnace_smelt")
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return BranchTooltipNode.branch("ali.type.function.limit_count", true)
                .add(utils.getValueTooltip(utils, fun.limiter).key("ali.property.value.limit"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getLootingEnchantTooltip(IServerUtils utils, LootingEnchantFunction fun) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch("ali.type.function.looting_enchant", true)
                .add(utils.getValueTooltip(utils, fun.value).key("ali.property.value.value"));

        if (fun.limit > 0) {
            tooltip.add(utils.getValueTooltip(utils, fun.limit).key("ali.property.value.limit"));
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
        return tooltip;
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return BranchTooltipNode.branch("ali.type.function.reference")
                .add(utils.getValueTooltip(utils, fun.name).key("ali.property.value.name"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.modifiers.isEmpty()) {
            tooltip = getCollectionTooltip(utils, "ali.property.branch.modifier", fun.modifiers).key("ali.type.function.set_attributes");
        } else {
            tooltip = BranchTooltipNode.branch("ali.type.function.set_attributes");
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_banner_pattern")
                .add(utils.getValueTooltip(utils, fun.append).key("ali.property.value.append"))
                .add(utils.getValueTooltip(utils, fun.patterns).key("ali.property.branch.banner_patterns"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return BranchTooltipNode.branch("ali.type.function.set_contents")
                .add(utils.getValueTooltip(utils, fun.type).key("ali.property.value.block_entity_type"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
                // TODO entries
    }

    @NotNull
    public static ITooltipNode getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_count", true)
                .add(utils.getValueTooltip(utils, fun.value).key("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, fun.add).key("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_damage")
                .add(utils.getValueTooltip(utils, fun.damage).key("ali.property.value.damage"))
                .add(utils.getValueTooltip(utils, fun.add).key("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_enchantments")
                .add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).key("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, fun.add).key("ali.property.value.add"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_instrument")
                .add(utils.getValueTooltip(utils, fun.options).key("ali.property.value.options"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return BranchTooltipNode.branch("ali.type.function.set_loot_table")
                .add(utils.getValueTooltip(utils, fun.name).key("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.seed).key("ali.property.value.seed"))
                .add(utils.getValueTooltip(utils, fun.type).key("ali.property.value.block_entity_type"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_lore")
                .add(utils.getValueTooltip(utils, fun.replace).key("ali.property.value.replace"))
                .add(utils.getValueTooltip(utils, fun.lore).key("ali.property.branch.lore"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).key("ali.property.value.resolution_context"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_name")
                .add(utils.getValueTooltip(utils, fun.name).key("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, fun.resolutionContext).key("ali.property.value.resolution_context"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetNbtTooltip(IServerUtils utils, SetNbtFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_nbt")
                .add(utils.getValueTooltip(utils, fun.tag.getAsString()).key("ali.property.value.tag"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return BranchTooltipNode.branch("ali.type.function.set_potion")
                .add(utils.getValueTooltip(utils, fun.potion).key("ali.property.value.potion"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
    }

    @NotNull
    public static ITooltipNode getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        IKeyTooltipNode tooltip;

        if (!fun.effectDurationMap.isEmpty()) {
            tooltip = getMapTooltip(utils, fun.effectDurationMap, GenericTooltipUtils::getMobEffectDurationEntryTooltip).key("ali.type.function.set_stew_effect");
        } else {
            tooltip = BranchTooltipNode.branch("ali.type.function.set_stew_effect");
        }

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)).key("ali.property.branch.conditions"));
        return tooltip;
    }
}
