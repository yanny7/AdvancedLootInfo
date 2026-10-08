package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.INumberProvider;
import me.drex.villagerconfig.util.loot.number.ReferenceLootNumberProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ReferenceLootNumberProviderAccessor extends BaseAccessor<ReferenceLootNumberProvider> implements INumberProvider {
    @Nullable
    private static Map<String, NumberProvider> references = null;

    @FieldAccessor
    private String id;

    public ReferenceLootNumberProviderAccessor(ReferenceLootNumberProvider parent) {
        super(parent);
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
                    BuiltInRegistries.ENCHANTMENT.stream().mapToInt(Enchantment::getMinLevel).min().orElse(1),
                    BuiltInRegistries.ENCHANTMENT.stream().mapToInt(Enchantment::getMaxLevel).max().orElse(1)
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
