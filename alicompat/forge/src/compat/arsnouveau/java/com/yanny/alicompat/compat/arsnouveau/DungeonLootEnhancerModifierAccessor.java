package com.yanny.alicompat.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.loot.DungeonLootEnhancerModifier;
import com.hollingsworth.arsnouveau.api.loot.DungeonLootTables;
import com.hollingsworth.arsnouveau.setup.config.Config;
import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DungeonLootEnhancerModifierAccessor extends BaseAccessor<DungeonLootEnhancerModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public DungeonLootEnhancerModifierAccessor(DungeonLootEnhancerModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        List<Drop> drops = new ArrayList<>();
        List<Supplier<ItemStack>> unsupported = new ArrayList<>();
        Random random = DungeonLootTables.r;

        DungeonLootTables.r = TrapRandom.INSTANCE;

        try {
            collect(drops, unsupported, DungeonLootTables.BASIC_LOOT, parent.commonChance, parent.commonRolls);
            collect(drops, unsupported, DungeonLootTables.UNCOMMON_LOOT, parent.uncommonChance, parent.uncommonRolls);
            collect(drops, unsupported, DungeonLootTables.RARE_LOOT, parent.rareChance, parent.rareRolls);

            if (Config.SPAWN_TOMES.get()) {
                collect(drops, unsupported, DungeonLootTables.CASTER_TOMES, parent.rareChance, parent.rareRolls);
            }
        } finally {
            DungeonLootTables.r = random;
        }

        if (drops.isEmpty() && unsupported.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (page, c) -> Stream.concat(
                                drops.stream().map((d) -> GlmNodeUtils.addedNode(utils, c, d.stack(), d.chance(), d.count())),
                                unsupported.stream().map((s) -> (IDataNode) new MissingNode(MissingTooltipUtils.getMissingValueTooltip(utils, s).build())))
                        .map((node) -> (IOperation) new IOperation.AddOperation((itemStack) -> true, node))
                        .toList()));
    }

    private static void collect(List<Drop> drops, List<Supplier<ItemStack>> unsupported, List<Supplier<ItemStack>> pool, double chance, int rolls) {
        if (pool.isEmpty() || rolls <= 0 || chance <= 0) {
            return;
        }

        float rolledChance = (float) (1 - Math.pow(1 - chance / pool.size(), rolls));

        for (Supplier<ItemStack> supplier : pool) {
            ItemStack stack;

            try {
                stack = supplier.get();
            } catch (Throwable e) {
                unsupported.add(supplier);
                continue;
            }

            if (!stack.isEmpty()) {
                drops.add(new Drop(stack, NumberExpr.constant(stack.getCount()), rolledChance));
            }
        }
    }

    private record Drop(ItemStack stack, NumberExpr count, float chance) {}

    private static final class TrapRandom extends Random {
        private static final TrapRandom INSTANCE = new TrapRandom();

        @Override
        protected int next(int bits) {
            throw new UnsupportedOperationException();
        }
    }
}
