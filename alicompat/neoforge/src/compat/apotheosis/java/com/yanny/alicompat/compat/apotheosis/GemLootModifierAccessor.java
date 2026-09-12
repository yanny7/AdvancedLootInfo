package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import dev.shadowsoffire.apotheosis.loot.modifiers.GemLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GemLootModifierAccessor extends BaseAccessor<GemLootModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    private List<GemLootModifier.GemTableEntry> entries;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GemLootModifierAccessor(GemLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> getOperations(utils, c));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table((location) -> matching(location) != null, true);
    }

    @NotNull
    private List<IOperation> getOperations(IServerUtils utils, List<LootItemCondition> conditions) {
        GemLootModifier.GemTableEntry entry = matching(TooltipContext.get());

        if (entry == null || entry.chance() <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getNode(utils, conditions, entry)));
    }

    @NotNull
    private static IDataNode getNode(IServerUtils utils, List<LootItemCondition> conditions, GemLootModifier.GemTableEntry entry) {
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, entry.chance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(1)), Collections.emptyList(), conditions);

        tooltip.add(TooltipBuilder.keyOnly(ApotheosisLang.Conditions.REQUIRES_PLAYER));
        tooltip.add(utils.getValueTooltip(utils, entry.purities()).build(ApotheosisLang.Branch.PURITY));
        tooltip.add(utils.getValueTooltip(utils, entry.gems()).build(Lang.Branch.ENTRIES));

        return new ItemNode(entry.chance(), new RangeValue(1), ApotheosisUtils.gemStack(), tooltip.build(), Collections.emptyList(), conditions);
    }

    @Nullable
    private GemLootModifier.GemTableEntry matching(@Nullable ResourceLocation location) {
        if (location == null) {
            return null;
        }

        return entries.stream().filter((entry) -> entry.pattern().matches(location)).findFirst().orElse(null);
    }
}
