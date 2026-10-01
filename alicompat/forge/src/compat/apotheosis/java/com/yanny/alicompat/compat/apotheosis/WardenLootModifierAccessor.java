package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import com.yanny.alicompat.accessor.IPageResolverAccessor;
import dev.shadowsoffire.apotheosis.ench.objects.WardenLootModifier;
import net.minecraft.world.item.enchantment.Enchantments;
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
                        GlmNodeUtils.addedNode(utils, c, ApotheosisUtils.wardenTendrilStack(), 1, getCount())))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, WardenLootModifier.WARDEN_TABLE_ID::equals, true);
    }

    @NotNull
    private static NumberExpr getCount() {
        NumberExpr chance = NumberExpr.add(NumberExpr.constant(0.1), NumberExpr.mul(NumberExpr.constant(0.1), TooltipUtils.level(Enchantments.MOB_LOOTING)));

        return NumberExpr.add(NumberExpr.constant(1), NumberExpr.binomial(NumberExpr.constant(1), chance));
    }
}
