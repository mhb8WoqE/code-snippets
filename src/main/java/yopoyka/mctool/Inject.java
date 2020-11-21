package yopoyka.mctool;

public class Inject {
    public static final Inject instance = new Inject();
    public IMcp mcp = new LegacyMcp();


    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface SrgName {
        public String value();
    }

    /**
     * Denotes which class is owner of whatever target
     * is annotated by this annotation.
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface Owner {
        public static class C {}
        /**
         * Should be the same as calling {@code Class#getName}.
         * @see Class#getName
         * @return the name of target class.
         */
        public String value() default "";

        /**
         * @return target class.
         */
        public Class<?> clazz() default C.class;
    }

    /**
     * Mark interface's method as accessor for field of target class.
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface Access {
        /**
         * @return name of target field.
         */
        public String value() default "";

        /**
         * Denotes that field should be created if it doesn't exist.
         * @return true if field should be created.
         */
        public boolean create() default false;

        /**
         * Explicitly specifies field descriptor.
         * @return field descriptor
         */
        public String type() default "";
    }

    /**
     * Forces method of the target class to be public.
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface Public {}

    /**
     * Redirect method execution to another method
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface Redirect {
        enum Type {
            VIRTUAL(org.objectweb.asm.Opcodes.INVOKEVIRTUAL),
            SPECIAL(org.objectweb.asm.Opcodes.INVOKESPECIAL),
            INTERFACE(org.objectweb.asm.Opcodes.INVOKEINTERFACE),
            STATIC(org.objectweb.asm.Opcodes.INVOKESTATIC);

            final int opcode;

            Type(int opcode) {
                this.opcode = opcode;
            }

            boolean isInterface() {
                return opcode == org.objectweb.asm.Opcodes.INVOKEINTERFACE;
            }
        }
        /**
         * @return name of method to redirect to
         */
        public String value();

        /**
         * Explicitly specify return type of the method
         * @return descriptor
         */
        public String returnType() default "";

        /**
         * Explicitly specifies descriptor of the method without return type and braces
         * @return descriptor without return type and braces
         */
        public String desc() default "";

        /**
         * Specifies instruction to use to make a call
         * @return call type
         */
        public Type callType() default Type.VIRTUAL;
    }

    /**
     * Accepts a ClassNode and Class of interface which will be used as a model
     * to make modifications to ClassNode.
     *
     * The {@code inter} should be annotated with {@code Owner} annotation
     * to tell which class is owner of members targeted by the following modifications.
     * If {@code Owner} annotation is missing or if the {@code Owner#value()} is empty
     * or {@code Owner#clazz()} equals to {@code Owner.C.class} the name of ClassNode will be used.
     *
     * Examples:
     * <blockquote><pre>
     * &#064;Access
     * public int getAnInt();
     * </pre></blockquote>
     * Creates a method to access field {@code anInt} of target class.
     * <blockquote><pre>
     * &#064;Access("integer")
     * public int getAnInt();
     * </pre></blockquote>
     * Creates a method to access field {@code integer} of target class.
     * <blockquote><pre>
     * &#064;Access(create = true)
     * public int getAnInt();
     * </pre></blockquote>
     * Creates a method to access field {@code anInt} of target class.
     * Also creates field {@code public int anInt} on a target class
     * if it doesn't exist.
     *
     * <blockquote><pre>
     * &#064;Access
     * public void setAnInt(int value);
     * </pre></blockquote>
     * Creates a method to set field {@code anInt} of target class.
     * <blockquote><pre>
     * &#064;Access("integer")
     * public void setAnInt(int value);
     * </pre></blockquote>
     * Creates a method to set field {@code integer} of target class.
     * <blockquote><pre>
     * &#064;Access(create = true)
     * public void setAnInt(int value);
     * </pre></blockquote>
     * Creates a method to set field {@code anInt} of target class.
     * If targeted field is final it will be made non-final.
     * Also creates field {@code public int anInt} on a target class
     * if it doesn't exist.
     *
     * @see org.objectweb.asm.tree.ClassNode
     * @param classNode the class to be modified.
     * @param inter the model according to which the class will be modified.
     *              Target class will be implementing the interface.
     */
    public void inject(org.objectweb.asm.tree.ClassNode classNode, Class<?> inter) {
        final Owner classAnnotation = inter.getAnnotation(Owner.class);
        final String owner = getOwnerOrDefault(classAnnotation, classNode.name);

        final String interfaceName = inter.getName().replace('.', '/');

        if (!classNode.interfaces.contains(interfaceName))
            classNode.interfaces.add(interfaceName);

        for (java.lang.reflect.Method method : inter.getMethods()) {
            final Owner methodOwnerAnn = method.getAnnotation(Owner.class);
            final String methodOwner = getOwnerOrDefault(methodOwnerAnn, null);
            if (doPublic(method, classNode, owner, methodOwner, inter))
                continue;
            if (doAccess(method, classNode, owner, methodOwner, inter))
                continue;
            if (doRedirect(method, classNode, owner, methodOwner, inter))
                continue;
        }
    }

    private boolean doPublic(java.lang.reflect.Method method, org.objectweb.asm.tree.ClassNode classNode, String classOwner, String methodOwner, Class<?> inter) {
        final Public publicAnn = method.getAnnotation(Public.class);
        if (publicAnn != null) {
            final String desc = getMethodDescriptor(method);
            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals(method.getName()) && m.desc.equals(desc))
                    .forEach(m -> m.access = m.access & ~(org.objectweb.asm.Opcodes.ACC_PRIVATE | org.objectweb.asm.Opcodes.ACC_PROTECTED)
                            | org.objectweb.asm.Opcodes.ACC_PUBLIC);
            return true;
        }
        return false;
    }

    private String getMappedName(SrgName ann) {
        if (ann == null || mcp == null) return null;

        return mcp.fromSrg(ann.value());
    }

    private boolean doAccess(java.lang.reflect.Method method, org.objectweb.asm.tree.ClassNode classNode, String classOwner, String methodOwner, Class<?> inter) {
        final Access accessAnn = method.getAnnotation(Access.class);
        if (accessAnn != null) {
            final String fieldOwner = methodOwner == null ? classOwner : methodOwner;

            String fname = getMappedName(method.getAnnotation(SrgName.class));

            if (fname == null) {
                if (accessAnn.value().isEmpty()) {
                    if (method.getName().length() < 4)
                        throw new IllegalArgumentException("Accessor name must start with `get` or `set` got " + method.getName() + " in " + inter);
                    fname = (Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4));
                }
                else
                    fname = accessAnn.value();
            }

            final String fieldName = fname;

            final String fieldDesc;

            if (method.getParameterCount() == 0) { // getter
                if (method.getReturnType() == void.class)
                    throw new IllegalArgumentException("Getter returns void " + method + " in " + inter);

                fieldDesc = accessAnn.type().isEmpty() ? getDescriptorForClass(method.getReturnType()) : accessAnn.type();
                final org.objectweb.asm.MethodVisitor mv = classNode.visitMethod(org.objectweb.asm.Opcodes.ACC_PUBLIC, method.getName(), "()" + fieldDesc, null, null);
                mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0);
                mv.visitFieldInsn(org.objectweb.asm.Opcodes.GETFIELD, fieldOwner, fieldName, fieldDesc);
                switch (fieldDesc.charAt(0)) {
                    case '[':
                    case 'L': mv.visitInsn(org.objectweb.asm.Opcodes.ARETURN); break;
                    case 'F': mv.visitInsn(org.objectweb.asm.Opcodes.FRETURN); break;
                    case 'J': mv.visitInsn(org.objectweb.asm.Opcodes.LRETURN); break;
                    case 'D': mv.visitInsn(org.objectweb.asm.Opcodes.DRETURN); break;
                    default: mv.visitInsn(org.objectweb.asm.Opcodes.IRETURN); break;
                }
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            else if (method.getParameterCount() == 1) { // setter
                if (method.getReturnType() != void.class)
                    throw new IllegalArgumentException("Setter must return void! Cause: " + method + " at " + inter);
                fieldDesc = accessAnn.type().isEmpty() ? getDescriptorForClass(method.getParameterTypes()[0]) : accessAnn.type();
                final org.objectweb.asm.MethodVisitor mv = classNode.visitMethod(org.objectweb.asm.Opcodes.ACC_PUBLIC, method.getName(), "(" + fieldDesc + ")V", null, null);
                mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0);
                switch (fieldDesc.charAt(0)) {
                    case '[':
                    case 'L': mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 1); break;
                    case 'F': mv.visitVarInsn(org.objectweb.asm.Opcodes.FLOAD, 1); break;
                    case 'J': mv.visitVarInsn(org.objectweb.asm.Opcodes.LLOAD, 1); break;
                    case 'D': mv.visitVarInsn(org.objectweb.asm.Opcodes.DLOAD, 1); break;
                    default: mv.visitVarInsn(org.objectweb.asm.Opcodes.ILOAD, 1); break;
                }
                mv.visitFieldInsn(org.objectweb.asm.Opcodes.PUTFIELD, fieldOwner, fieldName, fieldDesc);
                mv.visitInsn(org.objectweb.asm.Opcodes.RETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
                classNode.fields
                        .stream()
                        .filter(f -> f.name.equals(fieldName))
                        .findFirst()
                        .ifPresent(f -> f.access &= ~org.objectweb.asm.Opcodes.ACC_FINAL);
            }
            else
                throw new IllegalArgumentException("Parameters count must be 1 (one) ore (zero) but got: " + method.getParameterCount() + " for " + method + " in " + inter);

            if (accessAnn.create() && classNode.fields.stream().noneMatch(f -> f.name.equals(fieldName))) {
                classNode.visitField(org.objectweb.asm.Opcodes.ACC_PUBLIC, fieldName, fieldDesc, null, null)
                        .visitEnd();
            }
            return true;
        }
        return false;
    }

    private boolean doRedirect(java.lang.reflect.Method method, org.objectweb.asm.tree.ClassNode classNode, String classOwner, String methodOwner, Class<?> inter) {
        final Redirect redirect = method.getAnnotation(Redirect.class);
        if (redirect != null) {
            final String methodDescriptor = getMethodDescriptor(method);
            final org.objectweb.asm.MethodVisitor mv = classNode.visitMethod(org.objectweb.asm.Opcodes.ACC_PUBLIC, method.getName(), methodDescriptor, null, null);
            if (redirect.callType() != Redirect.Type.STATIC) {
                mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0);
            }
            final Class<?>[] parameterTypes = method.getParameterTypes();
            int i = 1;
            for (Class<?> pType : parameterTypes) {
                if (pType == float.class) {
                    mv.visitVarInsn(org.objectweb.asm.Opcodes.FLOAD, i);
                }
                else if (pType == long.class) {
                    mv.visitVarInsn(org.objectweb.asm.Opcodes.LLOAD, i);
                    i++;
                }
                else if (pType == double.class) {
                    mv.visitVarInsn(org.objectweb.asm.Opcodes.DLOAD, i);
                    i++;
                }
                else if (pType.isPrimitive()) {
                    mv.visitVarInsn(org.objectweb.asm.Opcodes.ILOAD, i);
                }
                else {
                    mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, i);
                }
                i++;
            }
            final String returnType = redirect.returnType().isEmpty() ? getDescriptorForClass(method.getReturnType()) : redirect.returnType();
            final String desc = (redirect.desc().isEmpty() ? methodDescriptor.substring(0, methodDescriptor.indexOf(')') + 1) : "(" + redirect.desc() + ")") + returnType;
            String methodName = getMappedName(method.getAnnotation(SrgName.class));
            if (methodName == null)
                methodName = redirect.value();
            mv.visitMethodInsn(
                    redirect.callType().opcode,
                    methodOwner == null ? classOwner : methodOwner,
                    methodName,
                    desc,
                    redirect.callType().isInterface()
            );
            if (method.getReturnType() == void.class) {
                mv.visitInsn(org.objectweb.asm.Opcodes.RETURN);
            }
            else {
                switch (returnType.charAt(0)) {
                    case '[':
                    case 'L': mv.visitInsn(org.objectweb.asm.Opcodes.ARETURN); break;
                    case 'F': mv.visitInsn(org.objectweb.asm.Opcodes.FRETURN); break;
                    case 'J': mv.visitInsn(org.objectweb.asm.Opcodes.LRETURN); break;
                    case 'D': mv.visitInsn(org.objectweb.asm.Opcodes.DRETURN); break;
                    default: mv.visitInsn(org.objectweb.asm.Opcodes.IRETURN); break;
                }
            }
            mv.visitMaxs(i + 1, i + 1);
            mv.visitEnd();
            return true;
        }
        return false;
    }

    private static String getOwnerOrDefault(Owner ann, String def) {
        return ann == null ? def : ann.value().isEmpty()
                ? ann.clazz() == Owner.C.class
                    ? def
                    : ann.clazz().getName().replace('.', '/')
                : ann.value().replace('.', '/');
    }

    private static String getDescriptorForClass(final Class<?> c) {
        if(c.isPrimitive()) {
            if(c == byte.class)
                return "B";
            if(c == char.class)
                return "C";
            if(c == double.class)
                return "D";
            if(c == float.class)
                return "F";
            if(c == int.class)
                return "I";
            if(c == long.class)
                return "J";
            if(c == short.class)
                return "S";
            if(c == boolean.class)
                return "Z";
            if(c == void.class)
                return "V";
            throw new RuntimeException("Unrecognized primitive " + c);
        }
        if(c.isArray())
            return c.getName().replace('.', '/');
        return
                ('L' + c.getName() + ';').replace('.', '/');
    }

    private static String getMethodDescriptor(java.lang.reflect.Method m) {
        StringBuilder s = new StringBuilder("(");
        for(final Class<?> c : (m.getParameterTypes()))
            s.append(getDescriptorForClass(c));
        s.append(')').append(getDescriptorForClass(m.getReturnType()));
        return s.toString();
    }

    public static interface IMcp {
        public String fromSrg(String srgName);

        public String fromSrgMethod(String srgName);

        public String fromSrgField(String srgName);
    }

    public static abstract class GradleMcp implements IMcp {
        protected java.io.File fieldsFile;
        protected java.io.File methodsFile;

        {
            try {
                final Class<?> gradle = Class.forName("net.minecraftforge.gradle.GradleStartCommon", false, null);
                final java.lang.reflect.Field csv_dir = gradle.getDeclaredField("CSV_DIR");
                csv_dir.setAccessible(true);
                final java.io.File csvDir = (java.io.File) csv_dir.get(null);
                fieldsFile = csvDir.toPath().resolve("fields.csv").toFile();
                methodsFile = csvDir.toPath().resolve("methods.csv").toFile();
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                System.err.println("GradleStartCommon is not loaded!");
                e.printStackTrace();
            }
        }
    }

    public static class LegacyMcp extends GradleMcp {
        protected java.util.Map<String, String> fieldMap;
        protected java.util.Map<String, String> methodMap;

        @Override
        public String fromSrg(String srgName) {
            if (srgName.startsWith("field"))
                return fromSrgField(srgName);
            if (srgName.startsWith("func"))
                return fromSrgMethod(srgName);
            return srgName;
        }

        @Override
        public String fromSrgMethod(String srgName) {
            if (methodMap == null)
                methodMap = readLines(methodsFile);

            return methodMap.getOrDefault(srgName, srgName);
        }

        @Override
        public String fromSrgField(String srgName) {
            if (fieldMap == null)
                fieldMap = readLines(fieldsFile);

            return fieldMap.getOrDefault(srgName, srgName);
        }

        private static java.util.Map<String, String> readLines(java.io.File file) {
            try {
                final java.util.Map<String, String> map = new java.util.HashMap<>();
                final java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                lines.forEach(line -> {
                    final String[] split = line.split(",", 3);
                    map.put(split[0], split[1]);
                });
                return map;
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return java.util.Collections.emptyMap();
            }
        }
    }
}
