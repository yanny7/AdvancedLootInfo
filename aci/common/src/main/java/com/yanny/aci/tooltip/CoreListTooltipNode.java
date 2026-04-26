package com.yanny.aci.tooltip;

import com.yanny.aci.api.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class CoreListTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>> implements ICoreTooltipNode<SU> {
    @Nullable
    private List<TN> children;

    protected CoreListTooltipNode(List<TN> children) {
        this.children = children;
    }

    protected abstract void encodeNode(FriendlyByteBuf buf);

    public void addNode(TN node) {
        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(node);
    }

    public List<TN> getChildren() {
        return Objects.requireNonNullElse(children, Collections.emptyList());
    }

    @NotNull
    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        List<TN> children = getChildren();
        List<Component> components;
        int count = children.size();

        if (count == 0) {
            components = Collections.emptyList();
        } else if (count == 1) {
            components = children.get(0).getComponents(pad, showAdvancedTooltip);
        } else {
            components = new ArrayList<>(count);

            for (TN child : children) {
                components.addAll(child.getComponents(pad, showAdvancedTooltip));
            }
        }

        return components;
    }

    @Override
    public final void encode(SU utils, FriendlyByteBuf buf) {
        List<TN> children = getChildren();

        buf.writeInt(children.size());

        for (TN child : children) {
            ICoreTooltipNode.encodeNode(utils, child, buf);
        }

        encodeNode(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        //noinspection unchecked
        CoreListTooltipNode<SU, TN> that = (CoreListTooltipNode<SU, TN>) o;
        return Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(children);
    }

    public static <
            SU extends ICoreServerUtils,
            TN extends ICoreTooltipNode<SU>,
            DN extends ICoreDataNode<SU, TN>,
            CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
            WU extends ICoreWidgetUtils<SU, TN, DN>
    > List<TN> decodeChildren(CU utils, FriendlyByteBuf buf) {
        int count = buf.readInt();

        if (count == 0) {
            return Collections.emptyList();
        } else if (count == 1) {
            return Collections.singletonList(ICoreTooltipNode.decodeNode(utils, buf));
        } else {
            List<TN> children = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                children.add(ICoreTooltipNode.decodeNode(utils, buf));
            }

            return children;
        }
    }

    public abstract static class Builder<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> implements ICoreKeyTooltipNode<SU, TN, KTN> {
        protected final List<TN> children = new ArrayList<>();

        @NotNull
        public KTN add(TN node) {
            children.add(node);
            //noinspection unchecked
            return (KTN) this;
        }
    }
}
