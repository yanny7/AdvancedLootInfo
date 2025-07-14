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
    public static ITooltipNode getCopyNbtTooltip(IServerUtils utils, CopyNbtFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.copy_nbt"));

        tooltip.add(getLootNbtProviderTypeTooltip(utils, "ali.property.value.nbt_provider", fun.source.getType()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.operations", "ali.property.branch.operation", fun.operations, GenericTooltipUtils::getCopyOperationTooltip));
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
        ITooltipNode tooltip;

        if (fun.enchantments.isPresent() && fun.enchantments.get().size() > 0) {
            tooltip = getOptionalHolderSetTooltip(utils, "ali.type.function.enchant_randomly", "ali.property.value.null", fun.enchantments, RegistriesTooltipUtils::getEnchantmentTooltip);
        } else {
            tooltip = new TooltipNode(translatable("ali.type.function.enchant_randomly"));
        }

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.enchant_with_levels"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.levels", fun.levels));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.treasure", fun.treasure));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.exploration_map"));

        tooltip.add(getTagKeyTooltip(utils, "ali.property.value.destination", fun.destination));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.map_decoration", fun.mapDecoration));
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
    public static ITooltipNode getLootingEnchantTooltip(IServerUtils utils, LootingEnchantFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.looting_enchant"));

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

        tooltip.add(getResourceLocationTooltip(utils, "ali.property.value.name", fun.name));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSequenceTooltip(IServerUtils utils, SequenceFunction fun) {
        return getCollectionTooltip(utils, "ali.type.function.sequence", fun.functions, utils::getFunctionTooltip);
    }

    @NotNull
    public static ITooltipNode getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        ITooltipNode tooltip = getCollectionTooltip(utils, "ali.type.function.set_attributes", "ali.property.branch.modifier", fun.modifiers, GenericTooltipUtils::getModifierTooltip);

        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_banner_pattern"));

        tooltip.add(getBooleanTooltip(utils, "ali.property.value.append", fun.append));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.banner_patterns", "ali.property.value.null", fun.patterns, GenericTooltipUtils::getBannerPatternsTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_contents"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.block_entity_type", fun.type, RegistriesTooltipUtils::getBlockEntityTypeTooltip));
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

        tooltip.add(getResourceLocationTooltip(utils, "ali.property.value.name", fun.name));
        tooltip.add(getLongTooltip(utils, "ali.property.value.seed", fun.seed));
        tooltip.add(getHolderTooltip(utils, "ali.property.value.block_entity_type", fun.type, RegistriesTooltipUtils::getBlockEntityTypeTooltip));
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_lore"));

        tooltip.add(getBooleanTooltip(utils, "ali.property.value.replace", fun.replace));
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
        tooltip.add(getSubConditionsTooltip(utils, fun.predicates));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSetNbtTooltip(IServerUtils utils, SetNbtFunction fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_nbt"));

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
}
