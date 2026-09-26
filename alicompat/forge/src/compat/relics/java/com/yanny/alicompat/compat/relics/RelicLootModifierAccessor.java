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
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import it.hurts.sskirillss.relics.level.RelicLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RelicLootModifierAccessor extends BaseAccessor<RelicLootModifier> implements IGlobalLootModifierAccessor, IPageResolverAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    private final Map<IRelicItem, Map<Pattern, Float>> relics = collectRelics();

    public RelicLootModifierAccessor(RelicLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        if (relics.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (page, c) -> getOperations(utils, page, c)));
    }

    @NotNull
    @Override
    public Verdict test(IServerUtils ignoredUtils, LootPage page) {
        return GlobalLootModifierUtils.testTable(page, (location) -> relics.values().stream().anyMatch((patterns) -> getChance(patterns, location) != null), false);
    }

    @NotNull
    private List<IOperation> getOperations(IServerUtils utils, LootPage page, List<LootItemCondition> conditions) {
        List<IOperation> operations = new ArrayList<>();

        relics.forEach((relic, patterns) -> {
            Float chance = getChance(patterns, page.tableId());

            if (chance != null) {
                operations.add(new IOperation.AddOperation((itemStack) -> true,
                        GlmNodeUtils.addedNode(utils, conditions, relic.getItem().getDefaultInstance(), chance, new RangeValue(1))));
            }
        });

        return operations;
    }

    @NotNull
    private static Map<IRelicItem, Map<Pattern, Float>> collectRelics() {
        Map<IRelicItem, Map<Pattern, Float>> relics = new LinkedHashMap<>();

        RelicStorage.RELICS.keySet().forEach((relic) -> {
            Map<Pattern, Float> patterns = new LinkedHashMap<>();

            relic.getLootData().getCollection().getEntries().forEach((pattern, chance) -> patterns.put(compile(pattern), chance));

            if (!patterns.isEmpty()) {
                relics.put(relic, patterns);
            }
        });

        return relics;
    }

    @NotNull
    private static Pattern compile(String pattern) {
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return Pattern.compile(Pattern.quote(pattern));
        }
    }

    @Nullable
    private static Float getChance(Map<Pattern, Float> patterns, ResourceLocation location) {
        String lootId = location.toString();

        for (Map.Entry<Pattern, Float> entry : patterns.entrySet()) {
            if (entry.getKey().matcher(lootId).matches()) {
                return entry.getValue();
            }
        }

        return null;
    }
}
