import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import yopoyka.mctool.Injector;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static yopoyka.mctool.Asm.*;

public class Test {
    public static void main(String[] args) {
        ClassNode classNode = null;

        classNode.methods.add(initMethodCode(createMethod("getA", "()V"), compose(
                getThis(),
                getField("", "", ""),
                addInst(Opcodes.ARETURN),
        )));

        classNode.methods
            .stream()
            .filter(forMethod(""))
            .findFirst()
            .ifPresent(methodNode -> {
                fromTop(
                    methodNode.instructions,
                    and(
                        methodCall(),
                        methodOwner(""),
                        methodDesc("")
                    ),
                    insertBefore(supplyIf(
                        list -> {

                        },
                        makeJump(1),
                            compose(
                                    getThis(),
                                    getField(null, null, null)
                            ),
                        nothing()
                    ))
                );
            });
    }

    public static InsnList fullIf(Supplier<InsnList> setup,
                           Function<Label, JumpInsnNode> jump,
                           Supplier<InsnList> ifDidJump,
                           Supplier<InsnList> ifDidntJump
    ) {
        InsnList setupList = setup.get();
        Label jumpTo = new Label();
        JumpInsnNode jumpNode = jump.apply(jumpTo);
        setupList.add(ifDidntJump.get());
        setupList.add(jumpNode);
        setupList.add(ifDidJump.get());
        return setupList;
    }


























    public static void main_(String[] args) throws Throwable {
        final byte[] bytes = Tests.readClass("Test$AClass");
        final ClassNode classNode = Tests.read(bytes);
        Injector.instance.inject(classNode, Schema.class);
        final byte[] write = Tests.write(classNode);
        final Class<Schema> aClass = (Class<Schema>) Tests.defineClass(write);
        final Schema schema = aClass.newInstance();
        System.out.println(Arrays.toString(schema.getClass().getDeclaredMethods()));
        schema.getClass().getDeclaredMethod("target", long.class).invoke(schema, 42);
    }

    @Injector.RenameMethods(@Injector.Rename(to = "_target", from = "target"))
    public static interface Schema {
        @Injector.Rename(to = "_target", from = "target")
        public int _dummy_0(long l);

        @Injector.Redirect(to = "_target")
        public int _redirect_to_target(long l);

        @Injector.Callme(from = "target", callFromStatic = true)
        public static int proxy(long l) {
            System.out.println("proxy called");
            System.out.println(l);
//            System.out.println(_redirect_to_target(l));
            System.out.println("proxy done");
            return 10;
        }
    }

    public static class AClass {
        public static int target(long l) {
            System.out.println("target");
            return (int) l;
        }

        public static String staticTarget(String v) {
            return v;
        }
    }
}
