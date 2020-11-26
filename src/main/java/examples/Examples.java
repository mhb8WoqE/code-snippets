package examples;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static yopoyka.mctool.Asm.*;

public class Examples {
    public static void main(String[] args) throws Throwable {
        ClassNode classNode = read(readClass("examples/Ex"));

        classNode.methods
                .stream()
                .filter(forMethod("getInt", "()I"))
                .findFirst()
                .ifPresent(methodNode -> {
                    InsnList list = new InsnList();
                    compose(
                            getThis(),
                            callStatic("examples/Examples$Hooks", "getIntHook", "(Lexamples/Ex;)V")
                    ).accept(list);
                    insertFirst(methodNode.instructions, list);
                });

        classNode.methods
                .stream()
                .filter(forMethod("numbers").and(forMethodDesc("(I)Ljava/lang/String;")))
                .findFirst()
                .ifPresent(methodNode -> {
                    forEach(methodNode.instructions,
                            opcode(Opcodes.ARETURN),
                            insertBefore(supplyCode(compose(
                                    getThis(),
                                    callStatic("examples/Examples$Hooks", "numbersHook", "(Ljava/lang/String;Lexamples/Ex;)Ljava/lang/String;")
                            )))
                    );
                });

        classNode.methods
                .stream()
                .filter(forMethod("days"))
                .filter(forMethodDesc("(I)Ljava/lang/String;"))
                .findFirst()
                .ifPresent(methodNode -> {
                    forEach(methodNode.instructions,
                            opcode(Opcodes.ARETURN),
                            insertBefore(supplyIf(
                                    compose(
                                            addInst(Opcodes.DUP),
                                            callStatic("examples/Examples$Hooks", "daysHook", "(Ljava/lang/String;)Z")
                                    ),
                                    jumpIfTrue(),
                                    compose(
                                            addInst(() -> new LdcInsnNode("Garfield doesn't like this day")),
                                            addInst(Opcodes.ARETURN)
                                    ),
                                    nothing()
                            ))
                    );
                });

        Class<?> aClass = defineClass(write(classNode));
        Object o = aClass.newInstance();
        System.out.println(aClass.getDeclaredMethod("getInt").invoke(o));
        System.out.println();

        for (int i = 0; i < 10; i++) {
            System.out.println(aClass.getDeclaredMethod("numbers", int.class).invoke(o, i));
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(aClass.getDeclaredMethod("days", int.class).invoke(o, i));
        }
    }

    public static class Hooks {
        public static void getIntHook(Ex ex) {
            System.out.println("getIntHook " + ex);
        }

        public static String numbersHook(String s, Ex ex) {
            System.out.println("numbesHook " + s + ' ' + ex);
            return "hey";
        }

        public static boolean daysHook(String day) {
            if (day.equals("Monday"))
                return false;

            return true;
        }
    }


    public static byte[] readClass(String name) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(name.replace('.', '/').concat(".class"))) {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int read;
            final byte[] buff = new byte[Short.MAX_VALUE];
            while ((read = is.read(buff)) > 0) {
                buffer.write(buff, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> defineClass(byte[] bytes) {
        return new ClassLoader() {
            Class<?> defineClass(byte[] bytes) {
                return defineClass(bytes, 0, bytes.length);
            }
        }.defineClass(bytes);
    };
}
