package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.INumberProvider;
import me.drex.villagerconfig.common.util.loot.number.ReferenceLootNumberProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ReferenceLootNumberProviderAccessor extends BaseAccessor<ReferenceLootNumberProvider> implements INumberProvider {
    @Nullable
    private static Map<String, NumberProvider> references = null;

    private final String id;

    public ReferenceLootNumberProviderAccessor(ReferenceLootNumberProvider parent) {
        super(parent);
        id = parent.id();
    }

    @NotNull
    @Override
    public NumberExpr convertNumber(IServerUtils utils, List<TooltipNode> conditions) {
        if (references == null) {
            return NumberExpr.constant(0);
        }

        NumberProvider provider = references.get(id);

        if (provider != null) {
            return withReferences(null, () -> utils.convertNumber(utils, provider, conditions));
        }

        return switch (id) {
            case "enchantmentLevel" -> new NumberExpr.Var(
                    VillagerConfigLang.ENCHANTMENT_LEVEL,
                    List.of(),
                    utils.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT).listElements().mapToInt((e) -> e.value().getMinLevel()).min().orElse(1),
                    utils.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT).listElements().mapToInt((e) -> e.value().getMaxLevel()).max().orElse(1)
            );
            case "treasureMultiplier" -> new NumberExpr.Var(VillagerConfigLang.TREASURE_MULTIPLIER, List.of(), 1, 2);
            default -> NumberExpr.constant(0);
        };
    }

    static <T> T withReferences(@Nullable Map<String, NumberProvider> scope, Supplier<T> supplier) {
        Map<String, NumberProvider> previous = references;

        references = scope;

        try {
            return supplier.get();
        } finally {
            references = previous;
        }
    }
}
