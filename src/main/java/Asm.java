public class Asm {
    public static org.objectweb.asm.tree.ClassNode read(byte[] bytes) {
        org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(bytes);
        org.objectweb.asm.tree.ClassNode classNode = new org.objectweb.asm.tree.ClassNode();
        cr.accept(classNode, 0);
        return classNode;
    }

    public static byte[] write(org.objectweb.asm.tree.ClassNode classNode, org.objectweb.asm.ClassWriter cw) {
        classNode.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] write(org.objectweb.asm.tree.ClassNode classNode) {
        return write(classNode, new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_FRAMES | org.objectweb.asm.ClassWriter.COMPUTE_MAXS));
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> T find(org.objectweb.asm.tree.InsnList list, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter) {
        org.objectweb.asm.tree.AbstractInsnNode node = list.getFirst();
        while (node != null) {
            if (filter.test(node))
                return (T) node;
            node = node.getNext();
        }
        return null;
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> T find(org.objectweb.asm.tree.AbstractInsnNode node, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter) {
        while (node != null) {
            if (filter.test(node))
                return (T) node;
            node = node.getNext();
        }
        return null;
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> T findBack(org.objectweb.asm.tree.AbstractInsnNode node, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter) {
        while (node != null) {
            if (filter.test(node))
                return (T) node;
            node = node.getPrevious();
        }
        return null;
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void forEach(org.objectweb.asm.tree.InsnList list, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        org.objectweb.asm.tree.AbstractInsnNode node = list.getFirst();
        while (node != null) {
            if (filter.test(node))
                action.accept(list, (T) node);
            node = node.getNext();
        }
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void forEach(org.objectweb.asm.tree.AbstractInsnNode from, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.Consumer<T> action) {
        org.objectweb.asm.tree.AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept((T) node);
            node = node.getNext();
        }
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void forEachBack(org.objectweb.asm.tree.AbstractInsnNode from, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.Consumer<T> action) {
        org.objectweb.asm.tree.AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept((T) node);
            node = node.getPrevious();
        }
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void forEach(org.objectweb.asm.tree.AbstractInsnNode from, org.objectweb.asm.tree.InsnList list, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        org.objectweb.asm.tree.AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept(list, (T) node);
            node = node.getNext();
        }
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void forEachBack(org.objectweb.asm.tree.AbstractInsnNode from, org.objectweb.asm.tree.InsnList list, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        org.objectweb.asm.tree.AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept(list, (T) node);
            node = node.getPrevious();
        }
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> opcode(int opcode) {
        return n -> n.getOpcode() == opcode;
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> type(String type) {
        return n -> n instanceof org.objectweb.asm.tree.TypeInsnNode && type.equals(((org.objectweb.asm.tree.TypeInsnNode) n).desc);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.MethodNode> forMethod(String name) {
        return m -> name.equals(m.name);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.MethodNode> forMethodDesc(String desc) {
        return m -> desc.equals(m.desc);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> method(String name) {
        return m -> m instanceof org.objectweb.asm.tree.MethodInsnNode && name.equals(((org.objectweb.asm.tree.MethodInsnNode) m).name);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> methodDesc(String desc) {
        return m -> m instanceof org.objectweb.asm.tree.MethodInsnNode && desc.equals(((org.objectweb.asm.tree.MethodInsnNode) m).desc);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> methodOwner(String owner) {
        return m -> m instanceof org.objectweb.asm.tree.MethodInsnNode && owner.equals(((org.objectweb.asm.tree.MethodInsnNode) m).owner);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> label(org.objectweb.asm.Label label) {
        return m -> m instanceof org.objectweb.asm.tree.LabelNode && label.equals(((org.objectweb.asm.tree.LabelNode) m).getLabel());
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> jump(org.objectweb.asm.Label label) {
        return m -> m instanceof org.objectweb.asm.tree.JumpInsnNode && label.equals(((org.objectweb.asm.tree.JumpInsnNode) m).label.getLabel());
    }
}
