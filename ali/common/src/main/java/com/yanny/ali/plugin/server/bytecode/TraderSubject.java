package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.npc.villager.VillagerTrades;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;

final class TraderSubject extends TradeSubject {
    private final Class<?> merchant;
    final boolean ownGetOffers;
    Value.Offers offers = new Value.Offers();
    private final Map<Object, Integer> levelKeys = new IdentityHashMap<>();
    private final Map<Object, Object> elementSources = new IdentityHashMap<>();
    private final Map<Object, List<BoundOffer>> boundOffers = new IdentityHashMap<>();

    TraderSubject(Class<?> merchant, boolean ownGetOffers) {
        this.merchant = merchant;
        this.ownGetOffers = ownGetOffers;
    }

    @Override
    public Class<?> type() {
        return merchant;
    }

    @Override
    public void keyedRead(@Nullable Object key, @Nullable Object value) {
        if (!(value instanceof Object[] array && array.length > 0 || value instanceof List<?> list && !list.isEmpty())) {
            return;
        }
        if (key instanceof Integer level) {
            putLevel(value, level);
        } else if (key instanceof Enum<?> constant) {
            putLevel(value, constant.ordinal() + 1);
        }
    }

    @Override
    public void elementRead(@Nullable Object element, Object array) {
        if (element instanceof VillagerTrades.ItemListing) {
            putSource(element, array);
        }
    }

    void putLevel(Object listings, int level) {
        levelKeys.put(listings, level);
    }

    @Nullable
    Integer levelKey(Object listings) {
        return levelKeys.get(listings);
    }

    void putSource(Object element, Object array) {
        elementSources.put(element, array);
    }

    @Nullable
    Object listingSource(Object listing) {
        return elementSources.containsKey(listing) ? elementSources.get(listing) : keyedSource(listing);
    }

    @Nullable
    Integer poolKey(List<Object> listings) {
        for (Map.Entry<Object, Integer> entry : levelKeys.entrySet()) {
            List<?> keyed = entry.getKey() instanceof Object[] array ? Arrays.asList(array) : (List<?>) entry.getKey();

            if (keyed.equals(listings)) {
                return entry.getValue();
            }
        }

        return null;
    }

    BoundOffer boundOffer(Object element, MethodInsnNode call, List<Object> args) {
        BoundOffer offer = new BoundOffer(element, call.owner, call.name, call.desc, call.itf, args);

        return boundOffers.computeIfAbsent(element, (e) -> new ArrayList<>()).stream().filter(offer::equals).findFirst().orElseGet(() -> {
            boundOffers.get(element).add(offer);
            return offer;
        });
    }

    @Nullable
    private Object keyedSource(Object listing) {
        for (Object keyed : levelKeys.keySet()) {
            List<?> elements = keyed instanceof Object[] array ? Arrays.asList(array) : (List<?>) keyed;

            if (elements.stream().anyMatch((e) -> e == listing)) {
                return keyed;
            }
        }

        return null;
    }
}
