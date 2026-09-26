package com.yanny.alicompat.compat.hybridaquatic;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.LootPoolNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import dev.hybridlabs.aquatic.loot.HAGlobalLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class HAGlobalLootModifierAccessor extends BaseAccessor<HAGlobalLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public HAGlobalLootModifierAccessor(HAGlobalLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(conditions),
                (page, c) -> Collections.singletonList(new IOperation.ReplaceOperation(
                        (itemStack) -> itemStack.is(parent.getItem_tag()),
                        (src) -> List.of(new ModifiedNode(utils, src, getTablesNode(utils, c, src)))))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, parent.getTarget()::equals, true);
    }

    @NotNull
    private IDataNode getTablesNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), ((IItemNode) src).getConditions().stream()).toList();
        List<WeightedEntry.Wrapper<ResourceLocation>> tables = parent.getTables().unwrap();
        int sumWeight = tables.stream().mapToInt((table) -> table.getWeight().asInt()).sum();
        float chance = src.getChance() * (1 - parent.getChance());
        List<IDataNode> children = tables.stream()
                .map((table) -> getTableNode(utils, allConditions, table.getData(), chance * table.getWeight().asInt() / sumWeight))
                .toList();

        return new LootPoolNode(children, TooltipUtils.getLootPoolTooltip(new RangeValue(1), new RangeValue(0)).build());
    }

    @NotNull
    private static IDataNode getTableNode(IServerUtils utils, List<LootItemCondition> conditions, ResourceLocation table, float chance) {
        LootTable lootTable = utils.getLootTable(table);
        TooltipBuilder tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(TooltipUtils.getChanceTooltip(NodeUtils.getEnchantedChance(utils, conditions, chance)));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
        });
        IDataNode child;

        if (lootTable != null) {
            child = NodeUtils.getLootTableNode(Collections.emptyList(), utils, lootTable, chance, Collections.emptyList(), conditions);
        } else {
            child = new MissingNode(utils.getValueTooltip(utils, table).build(Lang.Value.LOOT_TABLE));
        }

        return new ReferenceNode(Collections.singletonList(child), chance, tooltip.build());
    }
}
