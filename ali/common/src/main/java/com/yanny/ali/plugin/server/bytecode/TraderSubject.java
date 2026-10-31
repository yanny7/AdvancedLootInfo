package com.yanny.ali.plugin.server.bytecode;

final class TraderSubject implements Subject {
    private final Class<?> merchant;
    final boolean ownGetOffers;
    Value.Offers offers = new Value.Offers();

    TraderSubject(Class<?> merchant, boolean ownGetOffers) {
        this.merchant = merchant;
        this.ownGetOffers = ownGetOffers;
    }

    @Override
    public Class<?> type() {
        return merchant;
    }
}
