package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
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
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AffixConvertLootModifierAccessor extends BaseAccessor<AffixConvertLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AffixConvertLootModifierAccessor(AffixConvertLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (page, c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        (itemStack) -> !LootCategory.forItem(itemStack).isNone(),
                        (src) -> getNodes(utils, page, c, src)))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, (location) -> ApotheosisUtils.matches(AdventureConfig.AFFIX_CONVERT_LOOT_RULES, location), true);
    }

    @NotNull
    private static List<IDataNode> getNodes(IServerUtils utils, LootPage page, List<LootItemCondition> conditions, IDataNode src) {
        IItemNode node = (IItemNode) src;
        LootItemCondition chance = LootItemRandomChanceCondition.randomChance(ApotheosisUtils.chance(AdventureConfig.AFFIX_CONVERT_LOOT_RULES, page.tableId())).build();
        List<LootItemCondition> allConditions = Stream.concat(Stream.concat(conditions.stream(), node.getConditions().stream()), Stream.of(chance)).toList();
        EnchantedRanges enchantedChance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        RangeValue count = new RangeValue(node.getCount());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, enchantedChance, new EnchantedRanges(count), node.getFunctions(), allConditions);
        RarityClamp rarities = AdventureConfig.AFFIX_CONVERT_RARITIES.get(utils.getServerLevel().dimension().location());

        if (rarities != null) {
            tooltip.add(utils.getValueTooltip(utils, rarities.getMinRarity()).build(ApotheosisLang.Value.MIN_RARITY));
            tooltip.add(utils.getValueTooltip(utils, rarities.getMaxRarity()).build(ApotheosisLang.Value.MAX_RARITY));
        }

        ItemNode replacement = new ItemNode(node.getChance(), count, node.getItem(), tooltip.build(), node.getFunctions(), allConditions);

        return List.of(new ModifiedNode(utils, src, replacement));
    }
}
