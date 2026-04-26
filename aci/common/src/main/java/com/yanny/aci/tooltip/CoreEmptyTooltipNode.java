package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreKeyTooltipNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class CoreEmptyTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> implements ICoreTooltipNode<SU>, ICoreKeyTooltipNode<SU, TN, KTN> {
    protected CoreEmptyTooltipNode() {}

    @Override
    public final void encode(SU utils, FriendlyByteBuf buf) {
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "EmptyTooltipNode{}";
    }

    @NotNull
    @Override
    public KTN add(TN node) {
        //noinspection unchecked
        return (KTN) this;
    }

    @NotNull
    @Override
    public TN build(String key) {
        //noinspection unchecked
        return (TN) this;
    }

    public static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> extends CoreListTooltipNode.Builder<SU, TN, KTN> {
        private final CoreEmptyTooltipNode<SU, TN, KTN> node;

        public Builder(CoreEmptyTooltipNode<SU, TN, KTN> node) {
            this.node = node;
        }

        @NotNull
        @Override
        public TN build(String key) {
            //noinspection unchecked
            return (TN) node;
        }

        public TN build() {
            //noinspection unchecked
            return (TN) node;
        }
    }
}
