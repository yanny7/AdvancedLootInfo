package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import dev.shadowsoffire.apotheosis.ench.objects.WardenLootModifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class WardenLootModifierAccessor extends BaseAccessor<WardenLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public WardenLootModifierAccessor(WardenLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (page, c) -> Collections.singletonList(new IOperation.AddOperation(
                        (itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, c, ApotheosisUtils.wardenTendrilStack(), 1, new RangeValue(1, 2))))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, WardenLootModifier.WARDEN_TABLE_ID::equals, true);
    }
}
