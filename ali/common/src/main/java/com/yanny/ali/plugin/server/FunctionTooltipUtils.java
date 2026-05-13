package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static TooltipBuilder getApplyBonusTooltip(IServerUtils utils, ApplyBonusCount fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.enchantment).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, fun.formula).build(Lang.Value.FORMULA));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).isAdvancedTooltip().key(Lang.Functions.APPLY_BONUS);
    }

    @NotNull
    public static TooltipBuilder getCopyNameTooltip(IServerUtils utils, CopyNameFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.source).build(Lang.Value.SOURCE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_NAME);
    }

    @NotNull
    public static TooltipBuilder getCopyCustomDataTooltip(IServerUtils utils, CopyCustomDataFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.source.getType()).build(Lang.Value.NBT_PROVIDER));
            b.add(utils.getValueTooltip(utils, fun.operations).build(Lang.Branch.COPY_OPERATIONS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_CUSTOM_DATA);
    }

    @NotNull
    public static TooltipBuilder getCopyStateTooltip(IServerUtils utils, CopyBlockState fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.block).build(Lang.Value.BLOCK));
            b.add(utils.getValueTooltip(utils, fun.properties).build(Lang.Branch.PROPERTIES));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.COPY_STATE);
    }

    @NotNull
    public static TooltipBuilder getEnchantRandomlyTooltip(IServerUtils utils, EnchantRandomlyFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.options).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, fun.onlyCompatible).build(Lang.Value.ONLY_COMPATIBLE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.ENCHANT_RANDOMLY);
    }

    @NotNull
    public static TooltipBuilder getEnchantWithLevelsTooltip(IServerUtils utils, EnchantWithLevelsFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.levels).build(Lang.Value.LEVELS));
            b.add(utils.getValueTooltip(utils, fun.options).build(Lang.Branch.OPTIONS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.ENCHANT_WITH_LEVELS);
    }

    @NotNull
    public static TooltipBuilder getExplorationMapTooltip(IServerUtils utils, ExplorationMapFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.destination).build(Lang.Value.DESTINATION));
            b.add(utils.getValueTooltip(utils, fun.mapDecoration).build(Lang.Value.MAP_DECORATION));
            b.add(utils.getValueTooltip(utils, fun.zoom).build(Lang.Value.ZOOM));
            b.add(utils.getValueTooltip(utils, fun.searchRadius).build(Lang.Value.SEARCH_RADIUS));
            b.add(utils.getValueTooltip(utils, fun.skipKnownStructures).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.EXPLORATION_MAP);
    }

    @NotNull
    public static TooltipBuilder getExplosionDecayTooltip(IServerUtils utils, ApplyExplosionDecay fun) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS)))
                .showEmpty().key(Lang.Functions.EXPLOSION_DECAY);
    }

    @NotNull
    public static TooltipBuilder getFillPlayerHeadTooltip(IServerUtils utils, FillPlayerHead fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.entityTarget).build(Lang.Value.TARGET));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.FILL_PLAYER_HEAD);
    }

    @NotNull
    public static TooltipBuilder getFurnaceSmeltTooltip(IServerUtils utils, SmeltItemFunction fun) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS)))
                .showEmpty().key(Lang.Functions.FURNACE_SMELT);
    }

    @NotNull
    public static TooltipBuilder getLimitCountTooltip(IServerUtils utils, LimitCount fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.limiter).build(Lang.Value.LIMIT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).isAdvancedTooltip().key(Lang.Functions.LIMIT_COUNT);
    }

    @NotNull
    public static TooltipBuilder getEnchantedCountIncreaseTooltip(IServerUtils utils, EnchantedCountIncreaseFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.enchantment).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, fun.value).build(Lang.Value.VALUE));

            if (fun.limit > 0) {
                b.add(utils.getValueTooltip(utils, fun.limit).build(Lang.Value.LIMIT));
            }

            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.ENCHANTED_COUNT_INCREASE);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(IServerUtils utils, FunctionReference fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.REFERENCE);
    }

    @NotNull
    public static TooltipNode getSequenceTooltip(IServerUtils utils, SequenceFunction fun) {
        return TooltipBuilder.array((b) -> b.add(GenericTooltipUtils.getFunctionListTooltip(utils, fun.functions)))
                .key(Lang.Functions.SEQUENCE);
    }

    @NotNull
    public static TooltipBuilder getSetAttributesTooltip(IServerUtils utils, SetAttributesFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.modifiers).build(Lang.Branch.MODIFIERS));
            b.add(utils.getValueTooltip(utils, fun.replace).build(Lang.Value.REPLACE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_ATTRIBUTES);
    }

    @NotNull
    public static TooltipBuilder getSetBannerPatternTooltip(IServerUtils utils, SetBannerPatternFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.append).build(Lang.Value.APPEND));
            b.add(utils.getValueTooltip(utils, fun.patterns).build(Lang.Branch.BANNER_PATTERNS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_BANNER_PATTERN);
    }

    @NotNull
    public static TooltipBuilder getSetContentsTooltip(IServerUtils utils, SetContainerContents fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.component).build(Lang.Value.CONTAINER));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
            // TODO entries
        }).key(Lang.Functions.SET_CONTENTS);
    }

    @NotNull
    public static TooltipBuilder getSetCountTooltip(IServerUtils utils, SetItemCountFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.value).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, fun.add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).isAdvancedTooltip().key(Lang.Functions.SET_COUNT);
    }

    @NotNull
    public static TooltipBuilder getSetDamageTooltip(IServerUtils utils, SetItemDamageFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.damage).build(Lang.Value.DAMAGE));
            b.add(utils.getValueTooltip(utils, fun.add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_DAMAGE);
    }

    @NotNull
    public static TooltipBuilder getSetEnchantmentsTooltip(IServerUtils utils, SetEnchantmentsFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(getMapTooltip(utils, fun.enchantments, GenericTooltipUtils::getEnchantmentLevelsEntryTooltip).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, fun.add).build(Lang.Value.ADD));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_ENCHANTMENTS);
    }

    @NotNull
    public static TooltipBuilder getSetInstrumentTooltip(IServerUtils utils, SetInstrumentFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.options).build(Lang.Value.OPTIONS));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_INSTRUMENT);
    }

    @NotNull
    public static TooltipBuilder getSetLootTableTooltip(IServerUtils utils, SetContainerLootTable fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, fun.seed).build(Lang.Value.SEED));
            b.add(utils.getValueTooltip(utils, fun.type).build(Lang.Value.BLOCK_ENTITY_TYPE));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_LOOT_TABLE);
    }

    @NotNull
    public static TooltipBuilder getSetLoreTooltip(IServerUtils utils, SetLoreFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.mode).build(Lang.Value.LIST_OPERATION));
            b.add(utils.getValueTooltip(utils, fun.lore).build(Lang.Branch.LORE));
            b.add(utils.getValueTooltip(utils, fun.resolutionContext).build(Lang.Value.RESOLUTION_CONTEXT));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_LORE);
    }

    @NotNull
    public static TooltipBuilder getSetNameTooltip(IServerUtils utils, SetNameFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, fun.resolutionContext).build(Lang.Value.RESOLUTION_CONTEXT));
            b.add(utils.getValueTooltip(utils, fun.target).build(Lang.Value.TARGET));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_NAME);
    }

    @NotNull
    public static TooltipBuilder getSetNbtTooltip(IServerUtils utils, SetNbtFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.tag.getAsString()).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_NBT);
    }

    @NotNull
    public static TooltipBuilder getSetPotionTooltip(IServerUtils utils, SetPotionFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.potion).build(Lang.Value.POTION));
            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).key(Lang.Functions.SET_POTION);
    }

    @NotNull
    public static TooltipBuilder getSetStewEffectTooltip(IServerUtils utils, SetStewEffectFunction fun) {
        return TooltipBuilder.array((b) -> {
            if (!fun.effects.isEmpty()) {
                b.add(utils.getValueTooltip(utils, fun.effects));
            }

            b.add(utils.getValueTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.SET_STEW_EFFECT);
    }

    @NotNull
    public static TooltipNode getSetItemTooltip(IServerUtils utils, SetItemFunction fun) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fun.item).build(Lang.Value.ITEM));
            b.add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS));
        }).showEmpty().key(Lang.Functions.SET_ITEM);
    }

    @NotNull
    public static TooltipNode getSetComponentsTooltip(IServerUtils utils, SetComponentsFunction fun) {
        return utils.getValueTooltip(utils, fun.components)
                .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                .key(Lang.Functions.SET_COMPONENTS);
    }

    @NotNull
    public static TooltipNode getModifyContentsTooltip(IServerUtils utils, ModifyContainerContents fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.component).build("ali.property.value.container"))
                        .add(TooltipBuilder.branch((c) -> c.add(utils.getFunctionTooltip(utils, fun.modifier))).build("ali.property.branch.modifier"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.modify_contents");
    }

    @NotNull
    public static TooltipNode getFilteredTooltip(IServerUtils utils, FilteredFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.filter).build("ali.property.branch.filter"))
                        .add(TooltipBuilder.branch((c) -> c.add(utils.getFunctionTooltip(utils, fun.modifier))).build("ali.property.branch.modifier"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.filtered");
    }

    @NotNull
    public static TooltipNode getCopyComponentsTooltip(IServerUtils utils, CopyComponentsFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.source).build("ali.property.value.source"))
                        .add(utils.getValueTooltip(utils, fun.include).build("ali.property.branch.include"))
                        .add(utils.getValueTooltip(utils, fun.exclude).build("ali.property.branch.exclude"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.copy_components");
    }

    @NotNull
    public static TooltipNode getSetFireworksTooltip(IServerUtils utils, SetFireworksFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getStandaloneTooltip(utils, "ali.property.branch.explosion", fun.explosions).build("ali.property.branch.explosions"))
                        .add(utils.getValueTooltip(utils, fun.flightDuration).build("ali.property.value.flight_duration"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_fireworks");
    }

    @NotNull
    public static TooltipNode getSetFireworkExplosionTooltip(IServerUtils utils, SetFireworkExplosionFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.shape).build("ali.property.value.shape"))
                        .add(utils.getValueTooltip(utils, fun.colors).build("ali.property.value.colors"))
                        .add(utils.getValueTooltip(utils, fun.fadeColors).build("ali.property.value.fade_colors"))
                        .add(utils.getValueTooltip(utils, fun.trail).build("ali.property.value.trail"))
                        .add(utils.getValueTooltip(utils, fun.twinkle).build("ali.property.value.twinkle"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_firework_explosion");
    }

    @NotNull
    public static TooltipNode getSetBookCoverTooltip(IServerUtils utils, SetBookCoverFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.author).build("ali.property.value.author"))
                        .add(utils.getValueTooltip(utils, fun.title).build("ali.property.branch.title"))
                        .add(utils.getValueTooltip(utils, fun.generation).build("ali.property.value.generation"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_book_cover");
    }

    @NotNull
    public static TooltipNode getSetWrittenBookPagesTooltip(IServerUtils utils, SetWrittenBookPagesFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getFilterableTooltip(utils, "ali.property.branch.page", fun.pages).build("ali.property.branch.pages"))
                        .add(utils.getValueTooltip(utils, fun.pageOperation).build("ali.property.value.list_operation"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_written_book_pages");
    }

    @NotNull
    public static TooltipNode getSetWritableBookPagesTooltip(IServerUtils utils, SetWritableBookPagesFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getFilterableTooltip(utils, "ali.property.branch.page", fun.pages).build("ali.property.branch.pages"))
                        .add(utils.getValueTooltip(utils, fun.pageOperation).build("ali.property.value.list_operation"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_writable_book_pages");
    }

    @NotNull
    public static TooltipNode getToggleTooltipsTooltip(IServerUtils utils, ToggleTooltips fun) {
        return TooltipBuilder.array((b) -> b
                        .add(getMapTooltip(utils, fun.values, GenericTooltipUtils::getToggleEntryTooltip).build("ali.property.branch.components"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.toggle_tooltips");
    }

    @NotNull
    public static TooltipNode getSetOminousBottleAmplifierTooltip(IServerUtils utils, SetOminousBottleAmplifierFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.amplifierGenerator).build("ali.property.value.amplifier"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_ominous_bottle_amplifier");
    }

    @NotNull
    public static TooltipNode getSetCustomModelDataTooltip(IServerUtils utils, SetCustomModelDataFunction fun) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, fun.valueProvider).build("ali.property.value.value"))
                        .add(getSubConditionsTooltip(utils, fun.predicates).build(Lang.Branch.CONDITIONS))
                )
                .build("ali.type.function.set_custom_model_data");
    }
}
