package com.yanny.aci.tooltip;

import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.yanny.aci.api.ICoreTooltipNode.pad;

public abstract class CoreBranchTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>> extends CoreListTooltipNode<SU, TN> {
    private final String key;
    private final boolean translateKey;
    private final boolean advancedTooltip;

    protected CoreBranchTooltipNode(CacheKey<SU, TN> cacheKey) {
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
        CoreBranchTooltipNode<SU, TN> that = (CoreBranchTooltipNode<SU, TN>) o;
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
            SU extends ICoreServerUtils,
            TN extends ICoreTooltipNode<SU>,
            DN extends ICoreDataNode<SU, TN>,
            CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
            WU extends ICoreWidgetUtils<SU, TN, DN>,
            T extends ICoreTooltipNode<SU>
    > T decode(CU utils, RegistryFriendlyByteBuf buf, Function<CacheKey<SU, TN>, T> factory) {
        List<TN> children = CoreListTooltipNode.decodeChildren(utils, buf);
        String key = buf.readUtf();
        boolean advancedTooltip = buf.readBoolean();
        boolean translateKey = buf.readBoolean();
        return factory.apply(new CacheKey<>(children, key, advancedTooltip, translateKey));
    }

    public record CacheKey<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>>(List<TN> children, String key, boolean advancedTooltip, boolean translateKey) {}

    public static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, T extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> extends CoreListTooltipNode.Builder<SU, TN, KTN> {
        private final boolean advancedTooltip;
        private final Function<CacheKey<SU, TN>, T> factory;

        public Builder(boolean advancedTooltip, Function<CacheKey<SU, TN>, T> factory) {
            this.advancedTooltip = advancedTooltip;
            this.factory = factory;
        }

        @NotNull
        public TN build(String key) {
            //noinspection unchecked
            return (TN) build(key, true);
        }

        public T build(String key, boolean translateKey) {
            String internKey = key.intern();
            return factory.apply(new CacheKey<>(ImmutableList.copyOf(children), internKey, advancedTooltip, translateKey));
        }
    }
}
