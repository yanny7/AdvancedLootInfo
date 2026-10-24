package com.yanny.ali.plugin.server.bytecode;

import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

record Call(MethodInsnNode insn, List<Value> args, ClassLoader loader, int depth) {
}
