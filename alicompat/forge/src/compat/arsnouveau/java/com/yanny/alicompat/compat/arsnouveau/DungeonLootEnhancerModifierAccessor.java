package com.yanny.alicompat.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.loot.DungeonLootEnhancerModifier;
import com.hollingsworth.arsnouveau.api.loot.DungeonLootTables;
import com.hollingsworth.arsnouveau.setup.config.Config;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DungeonLootEnhancerModifierAccessor extends BaseAccessor<DungeonLootEnhancerModifier> implements IGlobalLootModifierAccessor {
    private static final int SAMPLES = 64;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public DungeonLootEnhancerModifierAccessor(DungeonLootEnhancerModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<Drop> drops = new ArrayList<>();

        collect(drops, DungeonLootTables.BASIC_LOOT, parent.commonChance, parent.commonRolls);
        collect(drops, DungeonLootTables.UNCOMMON_LOOT, parent.uncommonChance, parent.uncommonRolls);
        collect(drops, DungeonLootTables.RARE_LOOT, parent.rareChance, parent.rareRolls);

        if (Config.SPAWN_TOMES.get()) {
            collect(drops, DungeonLootTables.CASTER_TOMES, parent.rareChance, parent.rareRolls);
        }

        if (drops.isEmpty()) {
            return Optional.empty();
        }

        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> drops.stream()
                        .map((d) -> (IOperation) new IOperation.AddOperation((itemStack) -> true,
                                GlmNodeUtils.addedNode(utils, c, d.stack(), d.chance(), d.count())))
                        .toList());
    }

    private static void collect(List<Drop> drops, List<Supplier<ItemStack>> pool, double chance, int rolls) {
        if (pool.isEmpty() || rolls <= 0 || chance <= 0) {
            return;
        }

        for (Supplier<ItemStack> supplier : pool) {
            for (Sample sample : sample(supplier)) {
                double perRoll = chance * sample.hits / (SAMPLES * (double) pool.size());

                drops.add(new Drop(sample.stack, new RangeValue(sample.min, sample.max), (float) (1 - Math.pow(1 - perRoll, rolls))));
            }
        }
    }

    @NotNull
    private static List<Sample> sample(Supplier<ItemStack> supplier) {
        List<Sample> samples = new ArrayList<>();

        for (int i = 0; i < SAMPLES; i++) {
            ItemStack stack = supplier.get();

            if (stack.isEmpty()) {
                continue;
            }

            samples.stream().filter((s) -> ItemStack.isSameItemSameTags(s.stack, stack)).findFirst().orElseGet(() -> {
                Sample sample = new Sample(stack);

                samples.add(sample);
                return sample;
            }).add(stack.getCount());
        }

        return samples;
    }

    private record Drop(ItemStack stack, RangeValue count, float chance) {}

    private static class Sample {
        private final ItemStack stack;
        private int min;
        private int max;
        private int hits;

        Sample(ItemStack stack) {
            this.stack = stack;
            this.min = Integer.MAX_VALUE;
            this.max = 0;
        }

        void add(int count) {
            min = Math.min(min, count);
            max = Math.max(max, count);
            hits++;
        }
    }
}
