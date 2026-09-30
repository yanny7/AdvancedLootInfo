package com.yanny.aci.tooltip;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TooltipNumber(NumberExpr expr, boolean percent, @Nullable NumberInterval limit) {
    public void encode(FriendlyByteBuf buf) {
        expr.encode(buf);
        buf.writeBoolean(percent);
        buf.writeNullable(limit, TooltipNumber::writeInterval);
    }

    @NotNull
    public static TooltipNumber decode(FriendlyByteBuf buf) {
        return new TooltipNumber(NumberExpr.decode(buf), buf.readBoolean(), buf.readNullable(TooltipNumber::readInterval));
    }

    private static void writeInterval(FriendlyByteBuf buf, NumberInterval interval) {
        buf.writeDouble(interval.lo());
        buf.writeDouble(interval.hi());
        buf.writeBoolean(interval.loClosed());
        buf.writeBoolean(interval.hiClosed());
    }

    @NotNull
    private static NumberInterval readInterval(FriendlyByteBuf buf) {
        return new NumberInterval(buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readBoolean());
    }
}
