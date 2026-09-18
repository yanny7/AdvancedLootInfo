package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AffixLootModifierAccessor extends BaseAccessor<AffixLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    private List<AffixLootModifier.AffixTableEntry> entries;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AffixLootModifierAccessor(AffixLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> getOperations(utils, c)));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, (location) -> matching(location) != null, true);
    }

    @NotNull
    private List<IOperation> getOperations(IServerUtils utils, List<LootItemCondition> conditions) {
        AffixLootModifier.AffixTableEntry entry = matching(TooltipContext.get());

        if (entry == null || entry.chance() <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getNode(utils, conditions, entry)));
    }

    @NotNull
    private static IDataNode getNode(IServerUtils utils, List<LootItemCondition> conditions, AffixLootModifier.AffixTableEntry entry) {
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, entry.chance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(1)), Collections.emptyList(), conditions);

        tooltip.add(TooltipBuilder.keyOnly(ApotheosisLang.Conditions.REQUIRES_PLAYER));
        tooltip.add(utils.getValueTooltip(utils, entry.rarities()).build(ApotheosisLang.Branch.RARITY));
        tooltip.add(utils.getValueTooltip(utils, entry.entries()).build(Lang.Branch.ENTRIES));

        return new ItemNode(entry.chance(), new RangeValue(1), ApotheosisUtils.firstEntryStack(entry.entries()), tooltip.build(), Collections.emptyList(), conditions);
    }

    @Nullable
    private AffixLootModifier.AffixTableEntry matching(@Nullable ResourceLocation location) {
        if (location == null) {
            return null;
        }

        return entries.stream().filter((entry) -> entry.pattern().matches(location)).findFirst().orElse(null);
    }
}
