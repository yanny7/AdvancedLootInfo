package com.yanny.alicompat.compat.relics;

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
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsConfigs;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.level.RelicLootModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RelicLootModifierAccessor extends BaseAccessor<RelicLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    private final List<Drop> drops = collectDrops();

    public RelicLootModifierAccessor(RelicLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        if (drops.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (page, c) -> getOperations(utils, page, c)));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, (location) -> drops.stream().anyMatch((drop) -> drop.matches(location)), false);
    }

    @NotNull
    private List<IOperation> getOperations(IServerUtils utils, LootPage page, List<LootItemCondition> conditions) {
        List<IOperation> operations = new ArrayList<>();
        List<Drop> matching = drops.stream().filter((drop) -> drop.matches(page.tableId())).toList();
        double totalWeight = matching.stream().mapToInt(Drop::weight).sum();

        if (totalWeight > 0) {
            double genChance = ReflectionUtils.copyClassData(LootConfigDataAccessor.class, RelicsConfigs.LOOT_CONFIG).relicGenChance;

            matching.forEach((drop) -> operations.add(new IOperation.AddOperation((itemStack) -> true,
                    GlmNodeUtils.addedNode(utils, conditions, drop.item().getDefaultInstance(),
                            (float) (genChance * drop.weight() / totalWeight), new RangeValue(1)))));
        }

        return operations;
    }

    @NotNull
    private static List<Drop> collectDrops() {
        List<Drop> drops = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IRelicItem relic) {
                LootTemplate template = relic.getDefaultLootTemplate();

                if (template == null) {
                    continue;
                }

                for (LootEntry entry : template.getEntries()) {
                    List<Pattern> patterns = entry.getTables().stream().map(RelicLootModifierAccessor::compile).toList();

                    if (!patterns.isEmpty() && entry.getWeight() > 0) {
                        drops.add(new Drop(item, patterns, entry.getWeight()));
                    }
                }
            }
        }

        return drops;
    }

    @NotNull
    private static Pattern compile(String pattern) {
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return Pattern.compile(Pattern.quote(pattern));
        }
    }

    private record Drop(Item item, List<Pattern> patterns, int weight) {
        private boolean matches(ResourceLocation location) {
            return patterns.stream().anyMatch((pattern) -> pattern.matcher(location.toString()).matches());
        }
    }
}
