package com.yanny.awi.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IBlockNode;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A leaf standing for what a feature places: one block, or a whole block tag the feature picks from (coral, and
 * anything a mod drives from a tag). The tag is kept as a tag all the way to the client and only resolved into members
 * there, which is both what lets the slot be named after it and what makes it survive a datapack reload - the decode
 * that resolves it re-runs whenever tags are synced.
 */
public class BlockNode implements IDataNode, IBlockNode {
    public static final Identifier ID = Utils.modLoc("block");

    private final Either<Block, TagKey<Block>> block;
    private final TooltipNode tooltip;
    private final float chance;
    /** Resolved members, in registry order so a cycling slot does not jitter between reloads. Mutated by {@link #retainBlocks}. */
    private final List<Block> blocks;

    public BlockNode(IServerUtils utils, Block block) {
        this(utils, Either.left(block), TooltipNode.empty());
    }

    public BlockNode(IServerUtils utils, TagKey<Block> tag, TooltipNode tooltip) {
        this(utils, Either.right(tag), tooltip);
    }

    public BlockNode(IServerUtils utils, Block block, TooltipNode tooltip) {
        this(utils, Either.left(block), tooltip);
    }

    public BlockNode(IServerUtils ignoredUtils, Either<Block, TagKey<Block>> block, TooltipNode tooltip) {
        this.block = block;
        this.tooltip = tooltip;
        blocks = resolve(block);
        chance = 1f;
    }

    public BlockNode(IClientUtils utils, FriendlyByteBuf buf) {
        block = buf.readBoolean()
                ? Either.left(BuiltInRegistries.BLOCK.getValue(buf.readIdentifier()))
                : Either.right(TagKey.create(Registries.BLOCK, buf.readIdentifier()));
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        chance = buf.readFloat();
        blocks = resolve(block);
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        block.ifLeft((value) -> {
            buf.writeBoolean(true);
            buf.writeIdentifier(BuiltInRegistries.BLOCK.getKey(value));
        }).ifRight((tag) -> {
            buf.writeBoolean(false);
            buf.writeIdentifier(tag.location());
        });
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeFloat(chance);
    }

    @NotNull
    @Override
    public Either<Block, TagKey<Block>> getBlock() {
        return block;
    }

    @NotNull
    @Override
    public List<Block> getBlocks() {
        return blocks;
    }

    @Override
    public float getChance() {
        return chance;
    }

    /**
     * Drops every member the recipe viewer hides. A tag whose members are all hidden ends up empty, which is how
     * {@code GenericUtils.pruneHiddenBlocks} knows to drop the whole node.
     */
    @Override
    public void retainBlocks(Predicate<Block> isVisible) {
        blocks.removeIf((b) -> !isVisible.test(b));
    }

    @NotNull
    private static List<Block> resolve(Either<Block, TagKey<Block>> block) {
        List<Block> members = new ArrayList<>();

        block.ifLeft(members::add).ifRight((tag) -> BuiltInRegistries.BLOCK.getTagOrEmpty(tag)
                .forEach((holder) -> members.add(holder.value())));
        return members;
    }
}
