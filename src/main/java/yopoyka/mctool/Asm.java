package yopoyka.mctool;

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

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void fromTop(org.objectweb.asm.tree.InsnList list, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        forEach(list.getFirst(), list, filter, action);
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void fromTop(org.objectweb.asm.tree.InsnList list, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        forEach(list.getFirst(), list, alwaysTrue(), action);
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void fromBottom(org.objectweb.asm.tree.InsnList list, java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> filter, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        forEachBack(list.getLast(), list, filter, action);
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> void fromBottom(org.objectweb.asm.tree.InsnList list, java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> action) {
        forEachBack(list.getLast(), list, alwaysTrue(), action);
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

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> opcodes(int opcodes) {
        return n -> (n.getOpcode() & opcodes) != 0;
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> methodCall() {
        return opcodes(
                org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                | org.objectweb.asm.Opcodes.INVOKEINTERFACE
                | org.objectweb.asm.Opcodes.INVOKESPECIAL
                | org.objectweb.asm.Opcodes.INVOKESTATIC
        );
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> type(String type) {
        return n -> n instanceof org.objectweb.asm.tree.TypeInsnNode && type.equals(((org.objectweb.asm.tree.TypeInsnNode) n).desc);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.MethodNode> forMethod(String name) {
        return m -> name.equals(m.name);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.MethodNode> forMethod(String name, String desc) {
        return m -> name.equals(m.name) && desc.equals(m.desc);
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

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> fieldName(String name) {
        return n -> n instanceof org.objectweb.asm.tree.FieldInsnNode && name.equals(((org.objectweb.asm.tree.FieldInsnNode) n).name);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> fieldOwner(String owner) {
        return n -> n instanceof org.objectweb.asm.tree.FieldInsnNode && owner.equals(((org.objectweb.asm.tree.FieldInsnNode) n).owner);
    }

    public static java.util.function.Predicate<org.objectweb.asm.tree.AbstractInsnNode> fieldDesc(String desc) {
        return n -> n instanceof org.objectweb.asm.tree.FieldInsnNode && desc.equals(((org.objectweb.asm.tree.FieldInsnNode) n).desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> callMethod(int opcode, String owner, String name, String desc) {
        return list -> list.add(new org.objectweb.asm.tree.MethodInsnNode(
                opcode,
                owner,
                name,
                desc,
                opcode == org.objectweb.asm.Opcodes.INVOKEINTERFACE
        ));
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> callStatic(String owner, String name, String desc) {
        return callMethod(org.objectweb.asm.Opcodes.INVOKESTATIC, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> callVirtual(String owner, String name, String desc) {
        return callMethod(org.objectweb.asm.Opcodes.INVOKEVIRTUAL, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> callInterface(String owner, String name, String desc) {
        return callMethod(org.objectweb.asm.Opcodes.INVOKEINTERFACE, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> callSpecial(String owner, String name, String desc) {
        return callMethod(org.objectweb.asm.Opcodes.INVOKESPECIAL, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> accessField(int opcode, String owner, String name, String desc) {
        return list -> list.add(new org.objectweb.asm.tree.FieldInsnNode(
                opcode,
                owner,
                name,
                desc
        ));
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> getStatic(String owner, String name, String desc) {
        return accessField(org.objectweb.asm.Opcodes.GETSTATIC, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> putStatic(String owner, String name, String desc) {
        return accessField(org.objectweb.asm.Opcodes.PUTSTATIC, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> getField(String owner, String name, String desc) {
        return accessField(org.objectweb.asm.Opcodes.GETFIELD, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> setField(String owner, String name, String desc) {
        return accessField(org.objectweb.asm.Opcodes.PUTFIELD, owner, name, desc);
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> accessVar(int opcode, int index) {
        return list -> list.add(new org.objectweb.asm.tree.VarInsnNode(opcode, index));
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> getThis() {
        return list -> list.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> addInst(int opcode) {
        return list -> list.add(new org.objectweb.asm.tree.InsnNode(opcode));
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> addInst(java.util.function.Supplier<org.objectweb.asm.tree.AbstractInsnNode> inst) {
        return list -> list.add(inst.get());
    }

    private static final java.util.function.Function<org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnList> TO_INSTRUCTIONS = methodNode -> methodNode.instructions;
    public static java.util.function.Function<org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnList> toInstructions() {
        return TO_INSTRUCTIONS;
    }

    private static final java.util.function.Predicate ALWAYS_TRUE = o -> true;
    private static final java.util.function.Predicate ALWAYS_FALSE = o -> false;
    public static <T> java.util.function.Predicate<T> alwaysTrue() {
        return ALWAYS_TRUE;
    }

    public static <T> java.util.function.Predicate<T> alwaysFalse() {
        return ALWAYS_FALSE;
    }

    private static final java.util.function.Consumer NOTHING = o -> {};
    public static <T> java.util.function.Consumer<T> nothing() {
        return NOTHING;
    }

    public static <T> java.util.function.Predicate<T> and(java.util.function.Predicate<T>... predicates) {
        return t -> {
            for (java.util.function.Predicate<T> predicate : predicates) {
                if (!predicate.test(t))
                    return false;
            }
            return true;
        };
    }

    public static <T> java.util.function.Predicate<T> or(java.util.function.Predicate<T>... predicates) {
        return t -> {
            for (java.util.function.Predicate<T> predicate : predicates) {
                if (predicate.test(t))
                    return true;
            }
            return false;
        };
    }

    public static <T> java.util.function.Consumer<T> compose(java.util.function.Consumer<T>... consumers) {
        return t -> {
            for (java.util.function.Consumer<T> consumer : consumers)
                consumer.accept(t);
        };
    }

    public static int addModifier(int original, int modifier) {
        return original | modifier;
    }

    public static int removeModifier(int original, int modifier) {
        return original & ~modifier;
    }

    public static int makePublic(int original) {
        return removeModifier(original, org.objectweb.asm.Opcodes.ACC_PRIVATE & org.objectweb.asm.Opcodes.ACC_PROTECTED)
                | org.objectweb.asm.Opcodes.ACC_PUBLIC;
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> insertBefore(java.util.function.Supplier<org.objectweb.asm.tree.InsnList> list) {
        return (nodes, node) -> nodes.insertBefore(node, list.get());
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> insertAfter(java.util.function.Supplier<org.objectweb.asm.tree.InsnList> list) {
        return (nodes, node) -> nodes.insert(node, list.get());
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> oneInsertBefore(java.util.function.Supplier<? extends org.objectweb.asm.tree.AbstractInsnNode> list) {
        return (nodes, node) -> nodes.insertBefore(node, list.get());
    }

    public static <T extends org.objectweb.asm.tree.AbstractInsnNode> java.util.function.BiConsumer<org.objectweb.asm.tree.InsnList, T> oneInsertAfter(java.util.function.Supplier<? extends org.objectweb.asm.tree.AbstractInsnNode> list) {
        return (nodes, node) -> nodes.insert(node, list.get());
    }

    public static java.util.function.Function<org.objectweb.asm.Label, org.objectweb.asm.tree.JumpInsnNode> makeJump(int opcode) {
        return label -> new org.objectweb.asm.tree.JumpInsnNode(opcode, new org.objectweb.asm.tree.LabelNode(label));
    }

    public static java.util.function.Function<org.objectweb.asm.Label, org.objectweb.asm.tree.JumpInsnNode> jumpIfTrue() {
        return label -> new org.objectweb.asm.tree.JumpInsnNode(org.objectweb.asm.Opcodes.IFNE, new org.objectweb.asm.tree.LabelNode(label));
    }

    public static java.util.function.Function<org.objectweb.asm.Label, org.objectweb.asm.tree.JumpInsnNode> jumpIfFalse() {
        return label -> new org.objectweb.asm.tree.JumpInsnNode(org.objectweb.asm.Opcodes.IFEQ, new org.objectweb.asm.tree.LabelNode(label));
    }

    public static void makeIf(
            org.objectweb.asm.tree.InsnList list,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> setup,
            java.util.function.Function<org.objectweb.asm.Label, org.objectweb.asm.tree.JumpInsnNode> jump,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> ifDidntJump,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> ifDidJump
    ) {
        setup.accept(list);
        org.objectweb.asm.Label jumpTo = new org.objectweb.asm.Label();
        org.objectweb.asm.tree.JumpInsnNode jumpNode = jump.apply(jumpTo);
        list.add(jumpNode);
        ifDidntJump.accept(list);
        list.add(new org.objectweb.asm.tree.LabelNode(jumpTo));
        ifDidJump.accept(list);
    }

    public static java.util.function.Supplier<org.objectweb.asm.tree.InsnList> supplyIf(
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> setup,
            java.util.function.Function<org.objectweb.asm.Label, org.objectweb.asm.tree.JumpInsnNode> jump,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> ifDidntJump,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> ifDidJump
    ) {
        return () -> {
            org.objectweb.asm.tree.InsnList list = new org.objectweb.asm.tree.InsnList();
            makeIf(list, setup, jump, ifDidntJump, ifDidJump);
            return list;
        };
    }

    public static java.util.function.Consumer<org.objectweb.asm.tree.InsnList> makeIf(
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> setup,
            java.util.function.Function<org.objectweb.asm.Label, org.objectweb.asm.tree.JumpInsnNode> jump,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> ifDidntJump,
            java.util.function.Consumer<org.objectweb.asm.tree.InsnList> ifDidJump
    ) {
        return list -> makeIf(list, setup, jump, ifDidntJump, ifDidJump);
    }

    public static void addInterface(org.objectweb.asm.tree.ClassNode classNode, String inter) {
        if (!classNode.interfaces.contains(inter))
            classNode.interfaces.add(inter);
    }

    public static void addInterfaces(org.objectweb.asm.tree.ClassNode classNode, java.lang.Iterable<String> interfaces) {
        interfaces.forEach(s -> addInterface(classNode, s));
    }

    public static void insertFirst(org.objectweb.asm.tree.InsnList list, org.objectweb.asm.tree.InsnList insert) {
        list.insertBefore(list.getFirst(), insert);
    }

    public static void insertFirst(org.objectweb.asm.tree.InsnList list, org.objectweb.asm.tree.AbstractInsnNode node) {
        list.insertBefore(list.getFirst(), node);
    }

    public static org.objectweb.asm.tree.InsnList createCode(java.util.function.Consumer<org.objectweb.asm.tree.InsnList> init) {
        org.objectweb.asm.tree.InsnList list = new org.objectweb.asm.tree.InsnList();
        init.accept(list);
        return list;
    }

    public static java.util.function.Supplier<org.objectweb.asm.tree.InsnList> supplyCode(java.util.function.Consumer<org.objectweb.asm.tree.InsnList> init) {
        return () -> createCode(init);
    }

    public static org.objectweb.asm.tree.MethodNode createMethod(int access, String name, String desc) {
        return new org.objectweb.asm.tree.MethodNode(access, name, desc, null, null);
    }

    public static org.objectweb.asm.tree.MethodNode createMethod(String name, String desc) {
        return new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC, name, desc, null, null);
    }

    public static org.objectweb.asm.tree.MethodNode createMethod(String name) {
        int i = name.indexOf('(');
        return new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC, name.substring(0, i), name.substring(i), null, null);
    }

    public static org.objectweb.asm.tree.MethodNode initMethodCode(org.objectweb.asm.tree.MethodNode method, java.util.function.Consumer<org.objectweb.asm.tree.InsnList> initializer) {
        initializer.accept(method.instructions);
        return method;
    }

    public static void ensureField(String name, String desc, org.objectweb.asm.tree.ClassNode classNode) {
        if (classNode.fields
                .stream()
                .noneMatch(f -> name.equals(f.name) && desc.equals(f.desc))) {
            createField(name, desc, classNode);
        }
    }

    public static void createField(String name, String desc, org.objectweb.asm.tree.ClassNode classNode) {
        classNode.fields.add(new org.objectweb.asm.tree.FieldNode(
                org.objectweb.asm.Opcodes.ACC_PUBLIC,
                name,
                desc,
                null,
                null
        ));
    }

    public static void createAccessors(String fieldName, String fieldDesc, String baseName, boolean createField, boolean isStatic, org.objectweb.asm.tree.ClassNode classNode) {
        createAccessors(fieldName, fieldDesc, classNode.name, createField, isStatic, "get" + baseName, "set" + baseName, classNode);
    }

    public static void createAccessors(String fieldName, String fieldDesc, String fieldOwner, boolean createField, boolean isStatic, String getterName, String setterName, org.objectweb.asm.tree.ClassNode classNode) {
        if (createField)
            ensureField(fieldName, fieldDesc, classNode);

        createGetter(fieldName, fieldDesc, fieldOwner, getterName, isStatic, classNode);
        createSetter(fieldName, fieldDesc, fieldOwner, getterName, isStatic, classNode);
    }

    public static void createGetter(String fieldName, String fieldDesc, String fieldOwner, String name, boolean isStatic, org.objectweb.asm.tree.ClassNode classNode) {
        initMethodCode(createMethod(name, "()" + fieldDesc), compose(
                list -> {
                    if (!isStatic)
                        getThis().accept(list);
                },
                getField(fieldOwner, fieldName, fieldDesc),
                list -> {
                    switch (fieldDesc.charAt(0)) {
                        case '[':
                        case 'L':
                            addInst(org.objectweb.asm.Opcodes.ARETURN).accept(list);
                            break;
                        case 'F':
                            addInst(org.objectweb.asm.Opcodes.FRETURN).accept(list);
                            break;
                        case 'D':
                            addInst(org.objectweb.asm.Opcodes.DRETURN).accept(list);
                            break;
                        case 'J':
                            addInst(org.objectweb.asm.Opcodes.LRETURN).accept(list);
                            break;
                        default:
                            addInst(org.objectweb.asm.Opcodes.IRETURN).accept(list);
                            break;
                    }
                }
        ));
    }

    public static void createSetter(String fieldName, String fieldDesc, String fieldOwner, String name, boolean isStatic, org.objectweb.asm.tree.ClassNode classNode) {
        final int varIndex = isStatic ? 0 : 1;
        initMethodCode(createMethod(name, '(' + fieldDesc + ")V"), compose(
                list -> {
                    if (!isStatic)
                        getThis().accept(list);
                },
                list -> {
                    switch (fieldDesc.charAt(0)) {
                        case '[':
                        case 'L':
                            accessVar(org.objectweb.asm.Opcodes.ALOAD, varIndex).accept(list);
                            break;
                        case 'F':
                            accessVar(org.objectweb.asm.Opcodes.FLOAD, varIndex).accept(list);
                            break;
                        case 'D':
                            accessVar(org.objectweb.asm.Opcodes.DLOAD, varIndex).accept(list);
                            break;
                        case 'J':
                            accessVar(org.objectweb.asm.Opcodes.LLOAD, varIndex).accept(list);
                            break;
                        default:
                            accessVar(org.objectweb.asm.Opcodes.ILOAD, varIndex).accept(list);
                            break;
                    }
                },
                setField(fieldOwner, fieldName, fieldDesc),
                addInst(org.objectweb.asm.Opcodes.RETURN)
        ));
    }

    public static void renameMethod(java.util.function.Predicate<org.objectweb.asm.tree.MethodNode> filter, String newName, boolean replace, org.objectweb.asm.tree.ClassNode classNode) {
        classNode.methods
                .stream()
                .filter(filter)
                .findFirst()
                .ifPresent(methodNode -> {
                    final java.util.Optional<org.objectweb.asm.tree.MethodNode> dup = classNode.methods
                            .stream()
                            .filter(forMethod(newName, methodNode.desc))
                            .findFirst();
                    if (dup.isPresent()) {
                        if (replace)
                            classNode.methods.remove(dup.get());
                        else
                            throw new RuntimeException("Found duplicate while renaming " + methodNode.name + methodNode.desc + " to " + newName);
                    }
                    methodNode.name = newName;
                });
    }
}
