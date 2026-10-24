package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

final class Heap {
    private final Map<Object, ObjectInfo> objects = new IdentityHashMap<>();
    private final ValueStore store;
    private int ownedCount;
    private int frozenBelow;

    Heap(ValueStore store) {
        this.store = store;
    }

    void own(Object object) {
        ObjectInfo info = info(object);

        if (info.ownedOrder == 0) {
            info.ownedOrder = ++ownedCount;
        }
    }

    boolean isOwned(@Nullable Object object) {
        ObjectInfo info = objects.get(object);

        return info != null && info.ownedOrder != 0;
    }

    Freeze freeze() {
        int outer = frozenBelow;

        frozenBelow = ownedCount + 1;
        return () -> frozenBelow = outer;
    }

    boolean isFrozen(Object object) {
        ObjectInfo info = objects.get(object);

        return info != null && info.ownedOrder != 0 && info.ownedOrder < frozenBelow;
    }

    void taint(@Nullable Object object) {
        if (object == null) {
            return;
        }

        ObjectInfo info = info(object);

        if (!info.tainted) {
            info.tainted = true;

            if (info.stackPart != null) {
                info.stackPart.randomTag = true;
            }
        }
    }

    boolean isTainted(@Nullable Object object) {
        ObjectInfo info = objects.get(object);

        return info != null && info.tainted;
    }

    void stackPart(Object part, Value.Stack stack) {
        info(part).stackPart = stack;
    }

    Value.Stack shadow(ItemStack real) {
        ObjectInfo info = info(real);

        if (info.shadow == null) {
            Value.Stack shadow = Interpreter.stackOf(real);

            info.shadow = shadow;
            store.replaceInFrames((v) -> v instanceof Value.Known known && known.value() == real, (v) -> shadow);
        }

        return info.shadow;
    }

    @Nullable
    Value.Stack shadowOf(@Nullable Object object) {
        ObjectInfo info = objects.get(object);

        return info != null ? info.shadow : null;
    }

    CollState coll(Object object) {
        ObjectInfo info = info(object);

        if (info.coll == null) {
            info.coll = new CollState();
        }

        return info.coll;
    }

    @Nullable
    private CollState findColl(@Nullable Object object) {
        ObjectInfo info = objects.get(object);

        return info != null ? info.coll : null;
    }

    int removals(@Nullable Object object) {
        CollState coll = findColl(object);

        return coll != null ? coll.removals : 0;
    }

    boolean isPool(@Nullable Object object) {
        CollState coll = findColl(object);

        return coll != null && coll.pool;
    }

    @Nullable
    NumberExpr drawCount(@Nullable Object object) {
        CollState coll = findColl(object);

        return coll != null ? coll.drawCount : null;
    }

    @Nullable
    Value.Draws shuffled(@Nullable Object object) {
        CollState coll = findColl(object);

        return coll != null ? coll.shuffled : null;
    }

    @Nullable
    int[] randomIndexSet(@Nullable Object object) {
        CollState coll = findColl(object);

        return coll != null ? coll.randomIndexSet : null;
    }

    @Nullable
    Value.Offers offers(@Nullable Object object) {
        CollState coll = findColl(object);

        return coll != null ? coll.offers : null;
    }

    private ObjectInfo info(Object object) {
        return objects.computeIfAbsent(object, (o) -> new ObjectInfo());
    }

    interface Freeze extends AutoCloseable {
        @Override
        void close();
    }

    private static final class ObjectInfo {
        int ownedOrder;
        boolean tainted;
        @Nullable
        Value.Stack shadow;
        @Nullable
        Value.Stack stackPart;
        @Nullable
        CollState coll;
    }
}
