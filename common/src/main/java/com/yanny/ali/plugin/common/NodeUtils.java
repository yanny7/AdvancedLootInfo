package com.yanny.ali.plugin.common;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
                    sum += getTotalWeight(compositeEntryBase.children);
                }
            }
        }

        return sum;
    }

    @NotNull
    public static List<Component> toComponents(List<ITooltipNode> tooltip, int pad, boolean showAdvancedTooltip) {
        List<Component> components = new ArrayList<>();

        for (ITooltipNode node : tooltip) {
            if (!node.isAdvancedTooltip() || showAdvancedTooltip) {
                components.addAll(toComponents(node, pad, showAdvancedTooltip));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> toComponents(ITooltipNode tooltip, int pad, boolean showAdvancedTooltip) {
        List<Component> components = new ArrayList<>();

        if (tooltip.getContent().getContents() != PlainTextContents.EMPTY) {
            components.add(GenericTooltipUtils.pad(pad, tooltip.getContent()));
            components.addAll(toComponents(tooltip.getChildren(), pad + 1, showAdvancedTooltip));
        } else {
            components.addAll(toComponents(tooltip.getChildren(), pad, showAdvancedTooltip));
        }

        return components;
    }

    public static void processLootModifier(IServerUtils utils, ILootModifier<?> modifier, LootTableNode node) {
        List<IOperation> operations = modifier.getOperations();

        for (IOperation operation : operations) {
            if (operation instanceof IOperation.AddOperation addOperation) {
                node.addChildren(addOperation.node());
            } else if (operation instanceof IOperation.RemoveOperation removeOperation) {
                removeItem(node, removeOperation.predicate());
            } else if (operation instanceof IOperation.ReplaceOperation replaceOperation) {
                replaceItem(utils, node, replaceOperation.factory(), replaceOperation.predicate());
            }
        }
    }

    private static void removeItem(IDataNode node, Predicate<ItemStack> predicate) {
        if (node instanceof ListNode listNode) {
            listNode.nodes().removeIf((n) -> {
                if (n instanceof IItemNode itemNode) {
                    return predicateEither(itemNode, predicate);
                } else {
                    removeItem(n, predicate);
                }

                return false;
            });
            removeEmptyNodes(node);
        }
    }

    private static void replaceItem(IServerUtils utils, IDataNode node, Function<IDataNode, List<IDataNode>> factory, Predicate<ItemStack> predicate) {
        if (node instanceof ListNode listNode) {
            List<IDataNode> nodes = new ArrayList<>();

            listNode.nodes().replaceAll((n) -> {
                if (n instanceof IItemNode itemNode && predicateEither(itemNode, predicate)) {
                    List<IDataNode> result = factory.apply(n);

                    if (result.size() > 1) {
                        nodes.addAll(result.subList(1, result.size()));
                    }

                    return result.get(0);
                } else if (n instanceof ListNode l) {
                    replaceItem(utils, l, factory, predicate);
                }

                return n;
            });

            nodes.forEach(listNode::addChildren);
        }
    }

    private static boolean hasItems(IDataNode node) {
        if (node instanceof ListNode listNode) {
            return listNode.nodes().stream().anyMatch(NodeUtils::hasItems);
        } else {
            return node instanceof IItemNode;
        }
    }

    private static void removeEmptyNodes(IDataNode node) {
        if (node instanceof ListNode listNode) {
            listNode.nodes().removeIf((n) -> !hasItems(n));
        }
    }

    private static <T extends ItemLike> boolean predicateEither(IItemNode itemNode, Predicate<ItemStack> predicate) {
        return itemNode.getModifiedItem().map(
                predicate::test,
                (tagKey) -> {
                    //noinspection unchecked
                    Registry<T> registry = (Registry<T>)BuiltInRegistries.REGISTRY.get(tagKey.registry().location());

                    if (registry != null) {
                        //noinspection unchecked
                        return registry
                                .getTag((TagKey<T>) tagKey)
                                .map((holders) -> holders.stream().map(Holder::value))
                                .orElse(Stream.of())
                                .map((i) -> i.asItem().getDefaultInstance())
                                .allMatch(predicate);
                    } else {
                        return false;
                    }
                });
    }
}
