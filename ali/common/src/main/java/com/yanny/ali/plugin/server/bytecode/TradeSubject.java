package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

abstract sealed class TradeSubject implements Subject permits TraderSubject, ListingSubject {
    private final Map<Object, Value.Offers> collectionOffers = new IdentityHashMap<>();

    @Nullable
    Value.Offers offersOf(Object collection) {
        return collectionOffers.get(collection);
    }

    void putOffers(Object collection, Value.Offers offers) {
        collectionOffers.put(collection, offers);
    }
}
