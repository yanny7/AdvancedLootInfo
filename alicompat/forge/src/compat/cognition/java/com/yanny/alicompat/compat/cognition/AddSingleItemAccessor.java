package com.yanny.alicompat.compat.cognition;

import com.cyanogen.experienceobelisk.loot_modifiers.AddSingleItem;
import com.yanny.aci.api.NumberExpr;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddSingleItemAccessor extends BaseAccessor<AddSingleItem> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    private static final int MAX_BIASED_RANGE = 100;
    private static final float MAX_BIAS = 10.0F;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AddSingleItemAccessor(AddSingleItem parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        Item item = parent.item;
        float appearChance = parent.appearChance;
        NumberExpr count = getCount(parent.min, parent.max, parent.bias);

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (page, c) -> List.of(new IOperation.AddOperation((itemStack) -> true, GlmNodeUtils.addedNode(utils, c, item.getDefaultInstance(), appearChance, count)))));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        String path = parent.path;

        return GlobalLootModifierUtils.testTable(page, (id) -> id.getPath().contains(path), false);
    }

    @NotNull
    private static NumberExpr getCount(int min, int max, float bias) {
        if (bias == 0 || min >= max || max - min >= MAX_BIASED_RANGE || Math.abs(bias) >= MAX_BIAS) {
            return NumberExpr.uniformInt(min, max);
        }

        int range = max - min;
        double step = (Math.pow(2.0, bias) - 1.0) / range;
        List<NumberExpr.WeightedEntry> entries = new ArrayList<>();

        for (int i = 0; i <= range; i++) {
            entries.add(new NumberExpr.WeightedEntry(1.0 + step * i, NumberExpr.constant(min + i)));
        }

        return NumberExpr.weighted(entries);
    }
}
