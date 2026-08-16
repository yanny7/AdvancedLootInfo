package com.yanny.aci.test;

import com.yanny.aci.compatibility.DataReceiver;
import com.yanny.aci.network.NetworkUtils;
import com.yanny.aci.test.utils.FailingNode;
import com.yanny.aci.test.utils.TestClientUtils;
import com.yanny.aci.test.utils.TestDataNode;
import com.yanny.aci.test.utils.TestLeafNode;
import com.yanny.aci.test.utils.TestListNode;
import com.yanny.aci.test.utils.TestServerUtils;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.aci.tooltip.TooltipNodePalette;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeCodecTest {
    private static final String MOD_ID = "aci_test";
    private static final int MAX_CHUNK_SIZE = 32 * 1024;

    // the indexed-key path is only taken for a key the dictionary knows; anything else travels as a raw string
    private static final List<String> DICTIONARY = List.of("tooltip.aci_test.known", "tooltip.aci_test.other");

    @Test
    public void testSingleLevelTreeSurvivesTheRoundTrip() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        TestListNode root = new TestListNode("root", leafTooltip(palette, "tooltip.aci_test.known", "1"));

        root.addChildren(new TestLeafNode("first", 3, 1.0f, leafTooltip(palette, "tooltip.aci_test.other", "a")));
        root.addChildren(new TestLeafNode("second", 7, 0.5f, leafTooltip(palette, "tooltip.aci_test.unknown", "b")));

        byte[] payload = encodePayload(server, Map.of(entryId("root"), root));
        Map<ResourceLocation, TestListNode> decoded = roundTrip(payload, new TestClientUtils(DICTIONARY));
        TestListNode decodedRoot = decoded.get(entryId("root"));

        assertEquals(1, decoded.size());
        assertEquals("root", decodedRoot.name);
        assertEquals(List.of("first", "second"), values(decodedRoot));
        assertEquals(List.of(3, 7), counts(decodedRoot));
    }

    @Test
    public void testDecodedTreeReEncodesToTheSameBytes() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        Map<ResourceLocation, TestDataNode> nodes = new LinkedHashMap<>();

        nodes.put(entryId("first"), tree(palette, "first", 4, 0));
        nodes.put(entryId("second"), tree(palette, "second", 2, 100));

        byte[] payload = encodePayload(server, nodes);
        TestClientUtils client = new TestClientUtils(DICTIONARY);
        Map<ResourceLocation, TestListNode> decoded = roundTrip(payload, client);
        byte[] reEncoded = encodePayload(new TestServerUtils(client.getTooltipCache(), DICTIONARY), new LinkedHashMap<>(decoded));

        assertArrayEquals(payload, reEncoded);
    }

    @Test
    public void testNestedTreeSurvivesTheRoundTrip() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        TestListNode root = new TestListNode("root", leafTooltip(palette, "tooltip.aci_test.known", "1"));
        TestListNode branch = new TestListNode("branch", leafTooltip(palette, "tooltip.aci_test.known", "2"));
        TestListNode leafless = new TestListNode("leafless", leafTooltip(palette, "tooltip.aci_test.known", "3"));

        branch.addChildren(new TestLeafNode("deep", 1, 1.0f, leafTooltip(palette, "tooltip.aci_test.other", "d")));
        root.addChildren(branch);
        root.addChildren(leafless);

        byte[] payload = encodePayload(server, Map.of(entryId("root"), root));
        TestListNode decodedRoot = roundTrip(payload, new TestClientUtils(DICTIONARY)).get(entryId("root"));
        TestListNode decodedBranch = (TestListNode) decodedRoot.nodes().get(0);

        assertEquals(2, decodedRoot.nodes().size());
        assertEquals("branch", decodedBranch.name);
        assertEquals(List.of("deep"), values(decodedBranch));
        assertEquals("leafless", ((TestListNode) decodedRoot.nodes().get(1)).name);
        assertTrue(((TestListNode) decodedRoot.nodes().get(1)).nodes().isEmpty());
    }

    @Test
    public void testChildrenAreSortedByChanceOnDecode() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        TestListNode root = new TestListNode("root", leafTooltip(palette, "tooltip.aci_test.known", "1"));
        TooltipNode shared = leafTooltip(palette, "tooltip.aci_test.other", "x");

        root.addChildren(new TestLeafNode("rare", 1, 0.1f, shared));
        root.addChildren(new TestLeafNode("common", 1, 1.0f, shared));
        root.addChildren(new TestLeafNode("uncommon", 1, 0.5f, shared));

        byte[] payload = encodePayload(server, Map.of(entryId("root"), root));
        TestListNode decodedRoot = roundTrip(payload, new TestClientUtils(DICTIONARY)).get(entryId("root"));

        assertEquals(List.of("common", "uncommon", "rare"), values(decodedRoot));
    }

    @Test
    public void testLargePayloadIsSplitIntoChunksAndReassembled() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        TestListNode root = new TestListNode("root", leafTooltip(palette, "tooltip.aci_test.known", "1"));
        Random random = new Random(42);

        for (int i = 0; i < 4000; i++) {
            root.addChildren(new TestLeafNode(randomText(random), i, 1.0f, leafTooltip(palette, "tooltip.aci_test.other", randomText(random))));
        }

        byte[] payload = encodePayload(server, Map.of(entryId("root"), root));
        List<byte[]> chunks = compress(payload);
        TestListNode decodedRoot = decode(reassemble(chunks, chunks.size()), new TestClientUtils(DICTIONARY)).get(entryId("root"));

        assertTrue(chunks.size() > 1, "payload should not fit into a single chunk, got " + chunks.size());
        assertEquals(4000, decodedRoot.nodes().size());
        assertEquals(values(root), values(decodedRoot));
    }

    @Test
    public void testChunksMayArriveOutOfOrder() {
        List<byte[]> chunks = compress(simplePayload());
        DataReceiver receiver = new DataReceiver(chunks.size());

        for (int i = chunks.size() - 1; i >= 0; i--) {
            receiver.messageReceived(i, chunks.get(i));
        }

        assertEquals("root", decode(await(receiver.getFuture()), new TestClientUtils(DICTIONARY)).get(entryId("root")).name);
    }

    @Test
    public void testDuplicateChunkIsIgnored() {
        List<byte[]> chunks = compress(simplePayload());
        DataReceiver receiver = new DataReceiver(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            receiver.messageReceived(i, chunks.get(i));
        }

        receiver.messageReceived(0, chunks.get(0));

        assertTrue(receiver.getFuture().isDone());
        assertEquals("root", decode(await(receiver.getFuture()), new TestClientUtils(DICTIONARY)).get(entryId("root")).name);
    }

    @Test
    public void testMissingChunkFailsTheFuture() {
        List<byte[]> chunks = compress(largePayload());
        DataReceiver receiver = new DataReceiver(chunks.size());

        for (int i = 1; i < chunks.size(); i++) {
            receiver.messageReceived(i, chunks.get(i));
        }

        assertFalse(receiver.getFuture().isDone());
        receiver.forceDone();
        assertTrue(receiver.getFuture().isCompletedExceptionally());
        assertThrows(ExecutionException.class, () -> receiver.getFuture().get());
    }

    @Test
    public void testChildThatCannotBeEncodedIsDroppedFromTheTree() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        TestListNode root = new TestListNode("root", leafTooltip(palette, "tooltip.aci_test.known", "1"));
        TooltipNode shared = leafTooltip(palette, "tooltip.aci_test.other", "x");

        root.addChildren(new TestLeafNode("before", 1, 1.0f, shared));
        root.addChildren(new FailingNode(0.9f));
        root.addChildren(new TestLeafNode("after", 2, 0.8f, shared));

        byte[] payload = encodePayload(server, Map.of(entryId("root"), root));
        TestListNode decodedRoot = roundTrip(payload, new TestClientUtils(DICTIONARY)).get(entryId("root"));

        assertEquals(List.of("before", "after"), values(decodedRoot));
        assertEquals(List.of(1, 2), counts(decodedRoot));
    }

    @Test
    public void testTopLevelEntryThatCannotBeEncodedIsDroppedFromTheMap() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestServerUtils server = new TestServerUtils(palette, DICTIONARY);
        Map<ResourceLocation, TestDataNode> nodes = new LinkedHashMap<>();

        nodes.put(entryId("broken"), new FailingNode(1.0f));
        nodes.put(entryId("healthy"), tree(palette, "healthy", 2, 0));

        byte[] payload = encodePayload(server, nodes);
        Map<ResourceLocation, TestListNode> decoded = roundTrip(payload, new TestClientUtils(DICTIONARY));

        assertEquals(1, decoded.size());
        assertEquals("healthy", decoded.get(entryId("healthy")).name);
    }

    private static byte[] simplePayload() {
        TooltipNodePalette palette = new TooltipNodePalette();

        return encodePayload(new TestServerUtils(palette, DICTIONARY), Map.of(entryId("root"), tree(palette, "root", 3, 0)));
    }

    private static byte[] largePayload() {
        TooltipNodePalette palette = new TooltipNodePalette();
        TestListNode root = new TestListNode("root", leafTooltip(palette, "tooltip.aci_test.known", "1"));
        Random random = new Random(7);

        for (int i = 0; i < 4000; i++) {
            root.addChildren(new TestLeafNode(randomText(random), i, 1.0f, leafTooltip(palette, "tooltip.aci_test.other", randomText(random))));
        }

        return encodePayload(new TestServerUtils(palette, DICTIONARY), Map.of(entryId("root"), root));
    }

    private static TestListNode tree(TooltipNodePalette palette, String name, int children, int seed) {
        TestListNode node = new TestListNode(name, leafTooltip(palette, "tooltip.aci_test.known", name));

        for (int i = 0; i < children; i++) {
            node.addChildren(new TestLeafNode(name + "_" + i, seed + i, 1.0f - i * 0.1f,
                    leafTooltip(palette, i % 2 == 0 ? "tooltip.aci_test.other" : "tooltip.aci_test.unregistered", Integer.toString(seed + i))));
        }

        return node;
    }

    private static TooltipNode leafTooltip(TooltipNodePalette palette, String key, String value) {
        TooltipNode child = TooltipNode.getOrCreate(palette, key, new String[]{value}, null,
                (short) (TooltipNode.FLAG_HAS_KEY | TooltipNode.FLAG_HAS_VALUE), List.of());

        return TooltipNode.getOrCreate(palette, key, null, Component.literal(value), TooltipNode.FLAG_COMPONENT, List.of(child));
    }

    private static byte[] encodePayload(TestServerUtils utils, Map<ResourceLocation, ? extends TestDataNode> nodes) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        utils.getTooltipCache().encode(utils, buf);
        NetworkUtils.writeMapData(MOD_ID, utils, buf, new LinkedHashMap<>(nodes));

        byte[] payload = new byte[buf.readableBytes()];

        buf.readBytes(payload);
        buf.release();
        return payload;
    }

    private static List<byte[]> compress(byte[] payload) {
        List<byte[]> chunks = new ArrayList<>();

        NetworkUtils.compressAndStoreData(MOD_ID, Unpooled.wrappedBuffer(payload), (index, data) -> {
            assertEquals(chunks.size(), index, "chunks are produced in order");
            assertTrue(data.length <= MAX_CHUNK_SIZE, "chunk exceeds the packet limit: " + data.length);
            chunks.add(data);
        });
        return chunks;
    }

    private static byte[] reassemble(List<byte[]> chunks, int expectedCount) {
        DataReceiver receiver = new DataReceiver(expectedCount);

        for (int i = 0; i < chunks.size(); i++) {
            receiver.messageReceived(i, chunks.get(i));
        }

        return await(receiver.getFuture());
    }

    private static Map<ResourceLocation, TestListNode> roundTrip(byte[] payload, TestClientUtils client) {
        List<byte[]> chunks = compress(payload);

        return decode(reassemble(chunks, chunks.size()), client);
    }

    private static Map<ResourceLocation, TestListNode> decode(byte[] compressed, TestClientUtils utils) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(decompress(compressed)));
        Map<ResourceLocation, TestListNode> nodes = new LinkedHashMap<>();

        try {
            utils.getTooltipCache().decode(utils, buf);

            int count = buf.readInt();

            for (int i = 0; i < count; i++) {
                nodes.put(buf.readResourceLocation(), new TestListNode(utils, buf));
            }

            assertFalse(buf.isReadable(), "payload has trailing bytes after decoding");
        } finally {
            buf.release();
        }

        return nodes;
    }

    private static byte[] decompress(byte[] compressed) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return gzip.readAllBytes();
        } catch (IOException e) {
            throw new AssertionError("Failed to decompress payload", e);
        }
    }

    private static byte[] await(CompletableFuture<byte[]> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("Chunk reassembly did not complete", e);
        }
    }

    private static List<String> values(TestListNode node) {
        return node.nodes().stream().map((child) -> ((TestLeafNode) child).value).toList();
    }

    private static List<Integer> counts(TestListNode node) {
        return node.nodes().stream().map((child) -> ((TestLeafNode) child).count).toList();
    }

    private static String randomText(Random random) {
        StringBuilder builder = new StringBuilder(32);

        for (int i = 0; i < 32; i++) {
            builder.append((char) ('a' + random.nextInt(26)));
        }

        return builder.toString();
    }

    private static ResourceLocation entryId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
