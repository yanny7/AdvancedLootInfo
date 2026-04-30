package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public abstract class CoreErrorTooltipNode<
        TServerUtils    extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode    extends ICoreTooltipNode<?>,
        TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
        > implements ICoreTooltipNode<TServerUtils>, ICoreKeyTooltipNode<TTooltipNode, TKeyTooltipNode> {
    private final String value;

    protected CoreErrorTooltipNode(String value) {
        this.value = value;
    }

    protected String getValue() {
        return value;
    }

    @Override
    public final void encode(TServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        //noinspection unchecked
        CoreErrorTooltipNode<TServerUtils, TTooltipNode, TKeyTooltipNode> that = (CoreErrorTooltipNode<TServerUtils, TTooltipNode, TKeyTooltipNode>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "ErrorTooltipNode{" +
                "value='" + value + '\'' +
                '}';
    }

    @NotNull
    @Override
    public TKeyTooltipNode add(TTooltipNode node) {
        //noinspection unchecked
        return (TKeyTooltipNode) this;
    }

    @NotNull
    @Override
    public TTooltipNode build(String key) {
        //noinspection unchecked
        return (TTooltipNode) this;
    }

    @NotNull
    protected static <
            TTooltipNode extends ICoreTooltipNode<?>,
            TClientUtils extends ICoreClientUtils<TTooltipNode, ?, ?, TClientUtils>,
            SELF         extends ICoreTooltipNode<?>
            > SELF decode(TClientUtils ignoredUtils, FriendlyByteBuf buf, Function<String, SELF> factory) {
        String value = buf.readUtf();
        return factory.apply(value);
    }

    public static class Builder<
            TServerUtils    extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode    extends ICoreTooltipNode<?>,
            TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>
            > extends CoreListTooltipNode.Builder<TTooltipNode, TKeyTooltipNode> {
        private final String value;
        private final Function<String, CoreErrorTooltipNode<TServerUtils, TTooltipNode, TKeyTooltipNode>> factory;

        public Builder(String value, Function<String, CoreErrorTooltipNode<TServerUtils, TTooltipNode, TKeyTooltipNode>> factory) {
            this.value = value;
            this.factory = factory;
        }

        @NotNull
        public TKeyTooltipNode add(TTooltipNode node) {
            //noinspection unchecked
            return (TKeyTooltipNode) this;
        }

        @NotNull
        public TTooltipNode build(String key) {
            //noinspection unchecked
            return (TTooltipNode) factory.apply(value);
        }

        public TTooltipNode build() {
            //noinspection unchecked
            return (TTooltipNode) factory.apply(value);
        }
    }
}
