package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreLiteralTooltipNode<TServerUtils extends ICoreServerUtils<?, ?, ?>> implements ICoreTooltipNode<TServerUtils> {
    private final String text;

    protected CoreLiteralTooltipNode(String text) {
        this.text = text;
    }

    @Override
    public final void encode(TServerUtils utils, RegistryFriendlyByteBuf buf) {
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
        CoreLiteralTooltipNode<TServerUtils> that = (CoreLiteralTooltipNode<TServerUtils>) o;
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
            TClientUtils extends ICoreClientUtils<?, ?, ?, ?>,
            SELF         extends CoreLiteralTooltipNode<?>
            > SELF decode(TClientUtils ignoredUtils, RegistryFriendlyByteBuf buf, Function<String, SELF> factory) {
        String text = buf.readUtf();
        return factory.apply(text);
    }
}
