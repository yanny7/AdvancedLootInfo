package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.compatibility.common.TriConsumer;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class EnchantedRanges {
    private final Map<Enchantment, Map<Integer, RangeValue>> map;

    public EnchantedRanges(float unenchantedValue) {
        this(new RangeValue(unenchantedValue));
    }

    public EnchantedRanges(float unenchantedMin, float unenchantedMax) {
        this(new RangeValue(unenchantedMin, unenchantedMax));
    }

    public EnchantedRanges(RangeValue unenchantedValue) {
        map = getBaseMap(unenchantedValue);
    }

    @NotNull
    public RangeValue getUnenchantedValue() {
        return map.get(null).get(0);
    }

    public void setUnenchantedValue(RangeValue unenchantedValue) {
        Map<Integer, RangeValue> defaultMap = map.computeIfAbsent(null, (k) -> new LinkedHashMap<>());

        defaultMap.put(0, unenchantedValue);
    }

    public void modifyUnenchantedValue(UnaryOperator<RangeValue> modifier) {
        setUnenchantedValue(modifier.apply(getUnenchantedValue()));
    }

    public void modifyAllEntries(UnaryOperator<RangeValue> modifier) {
        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> entry : map.entrySet()) {
            Map<Integer, RangeValue> levelMap = entry.getValue();

            for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                levelEntry.setValue(modifier.apply(levelEntry.getValue()));
            }
        }
    }

    public void computeLevels(Enchantment enchantment, BiFunction<Integer, RangeValue, RangeValue> modifier) {
        Map<Integer, RangeValue> levelMap = map.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();

                entry.setValue(modifier.apply(level, entry.getValue()));
            }
        } else {
            RangeValue fallbackBase = getUnenchantedValue();
            int maxLevel = enchantment.getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                levelMap.put(level, modifier.apply(level, fallbackBase));
            }
        }
    }

    public void computeAllLevels(Enchantment enchantment, BiFunction<Integer, RangeValue, RangeValue> modifier) {
        Map<Integer, RangeValue> levelMap = map.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();

                entry.setValue(modifier.apply(level, entry.getValue()));
            }
        } else {
            RangeValue fallbackBase = getUnenchantedValue();
            int maxLevel = enchantment.getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                levelMap.put(level, modifier.apply(level, fallbackBase));
            }
        }

        modifyUnenchantedValue((value) -> modifier.apply(0, value));
    }

    public void forEachEnchantment(TriConsumer<Enchantment, Integer, RangeValue> action) {
        map.forEach((enchantment, levelMap) -> {
            if (enchantment == null) {
                return;
            }

            levelMap.forEach((level, value) -> action.accept(enchantment, level, value));
        });
    }

    @NotNull
    private static Map<Enchantment, Map<Integer, RangeValue>> getBaseMap(RangeValue value) {
        Map<Enchantment, Map<Integer, RangeValue>> map = new LinkedHashMap<>();
        Map<Integer, RangeValue> defaultMap = new LinkedHashMap<>();

        defaultMap.put(0, value);
        map.put(null, defaultMap);
        return map;
    }
}
