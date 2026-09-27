package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.compatibility.common.TriConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class EnchantedRanges {
    private RangeValue unenchanted;
    @Nullable
    private Map<Holder<Enchantment>, Map<Integer, RangeValue>> enchanted;

    public EnchantedRanges(float unenchantedValue) {
        this(new RangeValue(unenchantedValue));
    }

    public EnchantedRanges(float unenchantedMin, float unenchantedMax) {
        this(new RangeValue(unenchantedMin, unenchantedMax));
    }

    public EnchantedRanges(RangeValue unenchantedValue) {
        unenchanted = unenchantedValue;
    }

    @NotNull
    public RangeValue getUnenchantedValue() {
        return unenchanted;
    }

    public void setUnenchantedValue(RangeValue unenchantedValue) {
        unenchanted = unenchantedValue;
    }

    public void modifyUnenchantedValue(UnaryOperator<RangeValue> modifier) {
        setUnenchantedValue(modifier.apply(getUnenchantedValue()));
    }

    public void modifyAllEntries(UnaryOperator<RangeValue> modifier) {
        unenchanted = modifier.apply(unenchanted);

        if (enchanted == null) {
            return;
        }

        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> entry : enchanted.entrySet()) {
            Map<Integer, RangeValue> levelMap = entry.getValue();

            for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                levelEntry.setValue(modifier.apply(levelEntry.getValue()));
            }
        }
    }

    public void computeLevels(Holder<Enchantment> enchantment, BiFunction<Integer, RangeValue, RangeValue> modifier) {
        Map<Integer, RangeValue> levelMap = getEnchanted().computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();

                entry.setValue(modifier.apply(level, entry.getValue()));
            }
        } else {
            RangeValue fallbackBase = getUnenchantedValue();
            int maxLevel = enchantment.value().getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                levelMap.put(level, modifier.apply(level, fallbackBase));
            }
        }
    }

    public void computeAllLevels(Holder<Enchantment> enchantment, BiFunction<Integer, RangeValue, RangeValue> modifier) {
        Map<Integer, RangeValue> levelMap = getEnchanted().computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();

                entry.setValue(modifier.apply(level, entry.getValue()));
            }
        } else {
            RangeValue fallbackBase = getUnenchantedValue();
            int maxLevel = enchantment.value().getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                levelMap.put(level, modifier.apply(level, fallbackBase));
            }
        }

        modifyUnenchantedValue((value) -> modifier.apply(0, value));
    }

    public void forEachEnchantment(TriConsumer<Enchantment, Integer, RangeValue> action) {
        if (enchanted != null) {
            enchanted.forEach((enchantment, levelMap) -> levelMap.forEach((level, value) -> action.accept(enchantment.value(), level, value)));
        }
    }

    @NotNull
    private Map<Holder<Enchantment>, Map<Integer, RangeValue>> getEnchanted() {
        if (enchanted == null) {
            enchanted = new LinkedHashMap<>();
        }

        return enchanted;
    }
}
