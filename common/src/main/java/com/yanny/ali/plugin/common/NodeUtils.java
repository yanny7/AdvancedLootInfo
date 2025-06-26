package com.yanny.ali.plugin.common;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeUtils {
    public static void encodeTooltipNodes(IServerUtils ignoredUtils, FriendlyByteBuf buf, List<ITooltipNode> nodes) {
        buf.writeInt(nodes.size());

        for (ITooltipNode node : nodes) {
            node.encode(buf);
        }
    }

    @NotNull
    public static List<ITooltipNode> decodeTooltipNodes(IClientUtils ignoredUtils, FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ITooltipNode> nodes = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            nodes.add(new TooltipNode(buf));
        }

        return nodes;
    }

    public static int getTotalWeight(List<LootPoolEntryContainer> entries) {
        int sum = 0;

        for (LootPoolEntryContainer entry : entries) {
            if (entry instanceof LootPoolSingletonContainer singletonContainer) {
                sum += singletonContainer.weight;
            } else if (entry instanceof CompositeEntryBase compositeEntryBase) {
                if (entry instanceof AlternativesEntry) {
                    sum += LootPoolSingletonContainer.DEFAULT_WEIGHT;
                } else {
                    sum += getTotalWeight(Arrays.asList(compositeEntryBase.children));
                }
            }
        }

        return sum;
    }

    @NotNull
    public static List<Component> toComponents(List<ITooltipNode> tooltip, int pad) {
        List<Component> components = new ArrayList<>();

        for (ITooltipNode node : tooltip) {
            components.addAll(toComponents(node, pad));
        }

        return components;
    }

    @NotNull
    public static List<Component> toComponents(ITooltipNode tooltip, int pad) {
        List<Component> components = new ArrayList<>();

        if (tooltip.getContent().getContents() != ComponentContents.EMPTY) {
            components.add(GenericTooltipUtils.pad(pad, tooltip.getContent()));
            components.addAll(toComponents(tooltip.getChildren(), pad + 1));
        } else {
            components.addAll(toComponents(tooltip.getChildren(), pad));
        }

        return components;
    }
}
