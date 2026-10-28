package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class ComponentModelTest {
    private static final String ITEM_STACK = "net/minecraft/world/item/ItemStack";

    private final Value.Stack stack = new Value.Stack(List.of(new ItemStack(Items.EMERALD)), NumberExpr.constant(1), false);

    @Test
    public void testItemCostResultIsRandomCopy() {
        Value.Stack copy = assertInstanceOf(Value.Stack.class, ComponentModel.unknownChange(insn(INVOKESTATIC, "mod/Costs", "of",
                "(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/trading/ItemCost;"), stack));

        assertNotSame(stack, copy);
        assertTrue(copy.randomTag);
        assertFalse(stack.randomTag);
    }

    @Test
    public void testSetMarksRandomProperties() {
        assertComponentWrite(insn(INVOKEVIRTUAL, ITEM_STACK, RuntimeNames.STACK_SET));
    }

    @Test
    public void testUpdateWithAnyDescriptorMarksRandomProperties() {
        assertComponentWrite(new MethodInsnNode(INVOKEVIRTUAL, ITEM_STACK, RuntimeNames.STACK_UPDATE.name(), "()Ljava/lang/Object;", false));
    }

    @Test
    public void testUpdateEnchantmentsMarksRandomProperties() {
        assertComponentWrite(insn(INVOKESTATIC, "net/minecraft/world/item/enchantment/EnchantmentHelper", RuntimeNames.UPDATE_ENCHANTMENTS));
    }

    @Test
    public void testOtherCallHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, ComponentModel.unknownChange(insn(INVOKEVIRTUAL, ITEM_STACK, "getRarity", "()Lnet/minecraft/world/item/Rarity;"), stack));
        assertFalse(stack.randomTag);
    }

    private void assertComponentWrite(MethodInsnNode insn) {
        assertInstanceOf(Value.Unknown.class, ComponentModel.unknownChange(insn, stack));
        assertTrue(stack.randomTag);
    }

    private static MethodInsnNode insn(int opcode, String owner, RuntimeNames.Ref ref) {
        return insn(opcode, owner, ref.name(), ref.desc());
    }

    private static MethodInsnNode insn(int opcode, String owner, String name, String desc) {
        return new MethodInsnNode(opcode, owner, name, desc, false);
    }
}
