package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.List;

public final class TestTrades {
    private TestTrades() {}

    @Nullable
    public static IDataNode interpret(IServerUtils utils, Method getOffer, Object listing, TooltipNode condition) {
        BoundOffer offer = new BoundOffer(listing, Type.getInternalName(getOffer.getDeclaringClass()), getOffer.getName(), Type.getMethodDescriptor(getOffer),
                getOffer.getDeclaringClass().isInterface(), List.of(BoundOffer.Bound.WORLD, BoundOffer.Bound.TRADER, BoundOffer.Bound.RANDOM));

        return BytecodeInterpreter.getItemListing(utils, offer, condition);
    }

    public static VillagerTrade of(Item result) {
        return VillagerTrade.builder(new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(result), 1, 1, 0).build();
    }
}
