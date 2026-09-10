package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.GlobalLootModifierNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootModifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class AffixLootModifierAccessor extends BaseAccessor<AffixLootModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AffixLootModifierAccessor(AffixLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getNode(utils, c))));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table((location) -> ApotheosisUtils.matches(AdventureConfig.AFFIX_ITEM_LOOT_RULES, location), true);
    }

    @NotNull
    private static GlobalLootModifierNode getNode(IServerUtils utils, java.util.List<LootItemCondition> conditions) {
        return new GlobalLootModifierNode(TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, ApotheosisUtils.chance(AdventureConfig.AFFIX_ITEM_LOOT_RULES)).build(Lang.Value.PROBABILITY));
            b.add(utils.getValueTooltip(utils, conditions).build(Lang.Branch.PREDICATES));
        }, ApotheosisLang.GlobalLootModifier.RANDOM_AFFIX_ITEM).build());
    }
}
