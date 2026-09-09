package com.yanny.alicompat.compat.relics;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
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

public class RelicLootModifierAccessor extends BaseAccessor<RelicLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public RelicLootModifierAccessor(RelicLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);
        Map<IRelicItem, Map<Pattern, Float>> relics = collectRelics();

        if (relics.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ILootModifier<ResourceLocation>() {
            @Override
            public boolean predicate(ResourceLocation value) {
                return relics.values().stream().anyMatch((patterns) -> getChance(patterns, value) != null);
            }

            @NotNull
            @Override
            public List<IOperation> getOperations() {
                ResourceLocation location = TooltipContext.get();
                List<IOperation> operations = new ArrayList<>();

                if (location != null) {
                    relics.forEach((relic, patterns) -> {
                        Float chance = getChance(patterns, location);

                        if (chance != null) {
                            operations.add(new IOperation.AddOperation((itemStack) -> true,
                                    GlmNodeUtils.addedNode(utils, conditionList, relic.getItem().getDefaultInstance(), chance, new RangeValue(1))));
                        }
                    });
                }

                return operations;
            }

            @NotNull
            @Override
            public IType<ResourceLocation> getType() {
                return IType.LOOT_TABLE;
            }
        });
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
