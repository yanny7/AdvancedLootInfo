package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.ICoreClientUtils;
import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreBranchTooltipNode<
        TServerUtils extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode extends ICoreTooltipNode<TServerUtils>
        > extends CoreListTooltipNode<TServerUtils, TTooltipNode> {
    private final String key;
    private final boolean translateKey;
    private final boolean advancedTooltip;

    protected CoreBranchTooltipNode(CacheKey<TTooltipNode> cacheKey) {
        super(cacheKey.children);
        key = cacheKey.key;
        advancedTooltip = cacheKey.advancedTooltip;
        translateKey = cacheKey.translateKey;
    }

    @Override
    protected final void encodeNode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(key);
        buf.writeBoolean(advancedTooltip);
        buf.writeBoolean(translateKey);
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (advancedTooltip && !showAdvancedTooltip) {
            return Collections.emptyList();
        }

        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        components.add(pad(pad, Component.translatable(key).withStyle(TEXT_STYLE)));
        components.addAll(children);
        return components;
    }

    @Override
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        //noinspection unchecked
        CoreBranchTooltipNode<TServerUtils, TTooltipNode> that = (CoreBranchTooltipNode<TServerUtils, TTooltipNode>) o;
        return advancedTooltip == that.advancedTooltip
                && translateKey == that.translateKey
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, advancedTooltip, translateKey);
    }

    @Override
    public String toString() {
        return "BranchTooltipNode{" +
                "key='" + key + '\'' +
                ", advancedTooltip=" + advancedTooltip +
                ", translateKey=" + translateKey +
                ", children=" + getChildren() +
                '}';
    }

    @NotNull
    protected static <
            TServerUtils extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode extends ICoreTooltipNode<TServerUtils>,
            TClientUtils extends ICoreClientUtils<TTooltipNode, ?, ?, TClientUtils>,
            SELF         extends ICoreTooltipNode<?>
            > SELF decode(TClientUtils utils, RegistryFriendlyByteBuf buf, Function<CacheKey<TTooltipNode>, SELF> factory) {
        List<TTooltipNode> children = CoreListTooltipNode.decodeChildren(utils, buf);
        String key = buf.readUtf();
        boolean advancedTooltip = buf.readBoolean();
        boolean translateKey = buf.readBoolean();
        return factory.apply(new CacheKey<>(children, key, advancedTooltip, translateKey));
    }

    public record CacheKey<TTooltipNode extends ICoreTooltipNode<?>>(List<TTooltipNode> children, String key, boolean advancedTooltip, boolean translateKey) {}

    public static class Builder<
            TTooltipNOde    extends ICoreTooltipNode<?>,
            TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>,
            SELF            extends CoreBranchTooltipNode<?, ?>
            > extends CoreListTooltipNode.Builder<TTooltipNOde, TKeyTooltipNode> {
        private final boolean advancedTooltip;
        private final Function<CacheKey<TTooltipNOde>, SELF> factory;

        public Builder(boolean advancedTooltip, Function<CacheKey<TTooltipNOde>, SELF> factory) {
            this.advancedTooltip = advancedTooltip;
            this.factory = factory;
        }

        @NotNull
        public TTooltipNOde build(String key) {
            //noinspection unchecked
            return (TTooltipNOde) build(key, true);
        }

        public SELF build(String key, boolean translateKey) {
            String internKey = key.intern();
            return factory.apply(new CacheKey<>(ImmutableList.copyOf(children), internKey, advancedTooltip, translateKey));
        }
    }
}
