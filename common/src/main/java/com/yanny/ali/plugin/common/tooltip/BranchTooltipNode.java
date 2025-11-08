package com.yanny.ali.plugin.common.tooltip;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BranchTooltipNode extends ListTooltipNode implements IKeyTooltipNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "branch");

    private String key;
    private boolean advancedTooltip;

    private BranchTooltipNode(List<ITooltipNode> children) {
        super(children);
    }

    private BranchTooltipNode(List<ITooltipNode> children, @Nullable String key, boolean advancedTooltip) {
        super(children);
        this.key = key;
        this.advancedTooltip = advancedTooltip;
    }

    @Override
    public BranchTooltipNode key(String key) {
        if (this.key == null) {
            this.key = key;
            return this;
        }

        throw new IllegalStateException("Double key write!");
    }

    @Override
    public IKeyTooltipNode add(ITooltipNode node) {
        super.addNode(node);
        return this;
    }

    @Override
    void encodeNode(FriendlyByteBuf buf) {
        buf.writeNullable(key, FriendlyByteBuf::writeUtf);
        buf.writeBoolean(advancedTooltip);
    }

    @Override
    public List<Component> getComponents(int pad, boolean showAdvancedTooltip) {
        if (key == null) {
            throw new IllegalStateException("Key was not set!");
        }

        if (advancedTooltip && !showAdvancedTooltip) {
            return Collections.emptyList();
        }


        List<Component> children = super.getComponents(pad + 1, showAdvancedTooltip);
        List<Component> components = new ArrayList<>(children.size() + 1);

        components.add(ITooltipNode.pad(pad, Component.translatable(key).withStyle(TEXT_STYLE)));
        components.addAll(children);
        return components;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static BranchTooltipNode branch() {
        return new BranchTooltipNode(new ArrayList<>());
    }

    public static BranchTooltipNode branch(String key) {
        return branch().key(key);
    }

    @NotNull
    public static BranchTooltipNode branch(String key, boolean isAdvancedTooltip) {
        BranchTooltipNode node = branch(key);

        if (isAdvancedTooltip) {
            node.advancedTooltip = true;
        }

        return node;
    }

    @NotNull
    public static BranchTooltipNode decode(IClientUtils utils, FriendlyByteBuf buf) {
        List<ITooltipNode> children = ListTooltipNode.decodeChildren(utils, buf);
        String key = buf.readNullable(FriendlyByteBuf::readUtf);
        boolean advancedTooltip = buf.readBoolean();
        return new BranchTooltipNode(children, key, advancedTooltip);
    }
}
