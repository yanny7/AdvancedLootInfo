package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class TraderModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        TraderRun run = context.traderRun();
        Value receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null : args.get(0);

        if (run != null && run.isSelf(receiver)) {
            if (!run.ownGetOffers && RuntimeNames.is(RuntimeNames.GET_OFFERS, insn)) {
                context.traceCase("receiver.trader.getOffers", insn);
                return run.offers;
            }
            if (RuntimeNames.is(RuntimeNames.ADD_OFFERS_FROM_ITEM_LISTINGS, insn)) {
                if (!(args.get(1) instanceof Value.Offers offers) || !(args.get(2) instanceof Value.Known known)
                        || !(known.value() instanceof VillagerTrades.ItemListing[] listings)) {
                    context.traceCase("receiver.trader.addOffers.fail", insn);
                    throw new Fail("offers from item listings");
                }

                offers.entries.add(new Value.Offers.Listings(listings, args.get(3), context.levelKey(listings)));
                context.traceCase("receiver.trader.addOffers", insn);
                return null;
            }

            boolean special = insn.getOpcode() == Opcodes.INVOKESPECIAL;
            Class<?> start = special ? load(insn.owner, call.loader()) : run.merchant;
            Declaration own = ownDeclaration(start, insn.name, insn.desc, special);

            if (own != null) {
                context.traceCase("receiver.trader.own", insn);
                return context.execute(own.owner(), own.method(), args, call.depth());
            }
        }

        return NO_OPINION;
    }

    @Nullable
    static Declaration ownDeclaration(Class<?> start, String name, String desc, boolean exact) {
        if (!(declaration(start, name, desc, exact) instanceof Declaration declaration) || LambdaResolver.isPlatform(Type.getInternalName(declaration.owner()))) {
            return null;
        }

        return declaration;
    }

    static final class TraderRun {
        final Class<?> merchant;
        final boolean ownGetOffers;
        Value.Offers offers = new Value.Offers();

        TraderRun(Class<?> merchant, boolean ownGetOffers) {
            this.merchant = merchant;
            this.ownGetOffers = ownGetOffers;
        }

        boolean isSelf(@Nullable Value value) {
            return value instanceof Value.Unknown unknown && Origin.TRADER.equals(unknown.origin());
        }
    }
}
