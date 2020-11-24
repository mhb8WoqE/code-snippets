import org.objectweb.asm.tree.ClassNode;
import yopoyka.mctool.Injector;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Throwable {
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
