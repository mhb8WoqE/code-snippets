package examples;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static yopoyka.mctool.Asm.*;

public class Examples {
    public static void main(String[] args) throws Throwable {
        ClassNode classNode = read(readClass("examples/Ex"));

        classNode.methods
                .stream()
                .filter(forMethod("getInt", "()I")) // find method getInt that returns an int
                .findFirst()
                .ifPresent(methodNode -> {
                    InsnList list = new InsnList(); // manual instantiation
                    compose(
                            getThis(), // equivalent to addInst(() -> new VarInsnNode(Opcodes.ALOAD, 0))
                            // calls static method
                            callStatic("examples/Examples$Hooks", "getIntHook", "(Lexamples/Ex;)V")
                    ).accept(list); // modify instructions list
                    insertFirst(methodNode.instructions, list); // insert before first instruction in supplied list
                });

        classNode.methods
                .stream()
                .filter(forMethod("numbers").and(forMethodDesc("(I)Ljava/lang/String;"))) // combine filters
                .findFirst()
                .ifPresent(methodNode -> {
                    forEach(methodNode.instructions, // perform operation for each instruction that matched filter below
                            opcode(Opcodes.ARETURN), // matches instruction with opcode equals to ARETURN
                            insertBefore(supplyCode(compose( // inserts instructions before every matched node
                                    getThis(), // loads local var 0 (zero)
                                    // calls static method
                                    callStatic("examples/Examples$Hooks", "numbersHook", "(Ljava/lang/String;Lexamples/Ex;)Ljava/lang/String;")
                            )))
                    );
                });

        classNode.methods
                .stream()
                .filter(forMethod("days"))                  // another way
                .filter(forMethodDesc("(I)Ljava/lang/String;"))  // to combine filters
                .findFirst()
                .ifPresent(methodNode -> {
                    forEach(methodNode.instructions, // perform operation for each instruction that matched filter below
                            opcode(Opcodes.ARETURN), // matches instruction with opcode equals to ARETURN
                            // inserts instructions before every matched node
                            insertBefore(supplyIf( // creates if statement
                                    compose( // sets up if statement
                                            addInst(Opcodes.DUP), // dup String that currently on stack
                                            // calls static method that returns boolean (integer)
                                            callStatic("examples/Examples$Hooks", "daysHook", "(Ljava/lang/String;)Z")
                                    ),
                                    jumpIfTrue(), // performs jump if value on stack is not equal to 0 (zero)
                                    compose( // these instructions will be executed if jump wasn't made
                                            addInst(() -> new LdcInsnNode("Garfield doesn't like this day")),
                                            addInst(Opcodes.ARETURN)
                                    ),
                                    nothing() // these instructions will be executed if jump was made
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
