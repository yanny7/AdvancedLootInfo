package com.yanny.aci.tooltip;

import com.yanny.aci.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreLiteralTooltipNode<SU extends ICoreServerUtils> implements ICoreTooltipNode<SU> {
    private final String text;

    protected CoreLiteralTooltipNode(String text) {
        this.text = text;
    }

    @Override
    public final void encode(SU utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(text);
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.singletonList(pad(pad, Component.translatable(text).withStyle(TEXT_STYLE)));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        //noinspection unchecked
        CoreLiteralTooltipNode<SU> that = (CoreLiteralTooltipNode<SU>) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(text);
    }

    @Override
    public String toString() {
        return "LiteralTooltipNode{" +
                "text='" + text + '\'' +
                '}';
    }

    @NotNull
    protected static <
            SU extends ICoreServerUtils,
            TN extends ICoreTooltipNode<SU>,
            DN extends ICoreDataNode<SU, TN>,
            CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
            WU extends ICoreWidgetUtils<SU, TN, DN>,
            T extends ICoreTooltipNode<SU>
    > T decode(CU ignoredUtils, RegistryFriendlyByteBuf buf, Function<String, T> factory) {
        String text = buf.readUtf();
        return factory.apply(text);
    }
}
