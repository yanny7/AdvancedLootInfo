package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.loot.GemLootModifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class GemLootModifierAccessor extends BaseAccessor<GemLootModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GemLootModifierAccessor(GemLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> Collections.singletonList(new IOperation.AddOperation(
                        (itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, ApotheosisUtils.gemStack(), ApotheosisUtils.chance(AdventureConfig.GEM_LOOT_RULES), new RangeValue(1)))));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table((location) -> ApotheosisUtils.matches(AdventureConfig.GEM_LOOT_RULES, location), true);
    }
}
