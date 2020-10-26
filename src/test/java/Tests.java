import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class Tests {
    byte[] privateFieldsClass;
    byte[] privateFinalFieldsClass;
    byte[] noneFieldsPresentClass;
    byte[] methodsClass;

    @Before
    public void setup() throws IOException {
        privateFieldsClass = write(read("Tests$PrivateFields"));
        privateFinalFieldsClass = write(read("Tests$PrivateFinalFields"));
        noneFieldsPresentClass = write(read("Tests$NoneFieldsPresent"));
        methodsClass = write(read("Tests$Methods"));
    }

    @Test
    public void accessors() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), Accessors.class);
    }

    @Test
    public void customNames() throws IllegalAccessException, InstantiationException {
        final ClassNode classNode = read(privateFieldsClass);
        Inject.inject(classNode, CustomNames.class);
        final Class<?> aClass = defineClass(write(classNode));
        final Object instance = aClass.newInstance();
        final CustomNames a = (CustomNames) instance;
        Assert.assertEquals(a.getInt(), 0);
        a.setInt(1);
        Assert.assertEquals(a.getInt(), 1);
    }

    @Test
    public void accessorsAnnotated() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), Accessors.class);
    }

    // there is a possibility for circularity error in MinecraftForge env
    @Test
    public void accessorsAnnotatedWithClass() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), InheritedAccessorsViaClass.class);
    }

    @Test
    public void finalAccessors() throws IllegalAccessException, InstantiationException {
        test(read(privateFinalFieldsClass), Accessors.class);
    }

    @Test(expected = NoSuchFieldError.class)
    public void nonPresentFieldsNoSuchField() throws IllegalAccessException, InstantiationException {
        test(read(noneFieldsPresentClass), Accessors.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAccessorName() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), InvalidNames.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSetter() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), InvalidSetter.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidGetter() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), InvalidGetter.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAccessor() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), InvalidAccessor.class);
    }

    @Test
    public void nonPresentFields() throws IllegalAccessException, InstantiationException {
        test(read(noneFieldsPresentClass), CreateAccessors.class);
    }

    public static void test(ClassNode classNode, Class<?> model) throws IllegalAccessException, InstantiationException {
        Inject.inject(classNode, model);
        final Class<?> aClass = defineClass(write(classNode));
        final Object instance = aClass.newInstance();
        Accessors a = (Accessors) instance;
        Assert.assertEquals(a.getAnInt(), 0);
        Assert.assertEquals(a.getAByte(), 0);
        Assert.assertEquals(a.getAChar(), 0);
        Assert.assertEquals(a.getAShort(), 0);
        Assert.assertEquals(a.getAFloat(), 0, 0);
        Assert.assertEquals(a.getADouble(), 0, 0);
        Assert.assertEquals(a.getALong(), 0);
        Assert.assertFalse(a.getABoolean());
        Assert.assertNull(a.getString());
        a.setAnInt(1);
        Assert.assertEquals(a.getAnInt(), 1);
        a.setAByte((byte) 2);
        Assert.assertEquals(a.getAByte(), 2);
        a.setAChar('c');
        Assert.assertEquals(a.getAChar(), 'c');
        a.setAShort((short) 4);
        Assert.assertEquals(a.getAShort(), 4);
        a.setAFloat(5);
        Assert.assertEquals(a.getAFloat(), 5, 0);
        a.setADouble(6);
        Assert.assertEquals(a.getADouble(), 6, 0);
        a.setALong(7);
        Assert.assertEquals(a.getALong(), 7);
        a.setABoolean(true);
        Assert.assertTrue(a.getABoolean());
        a.setString("test");
        Assert.assertEquals(a.getString(), "test");
    }

    @Test
    public void methods() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        final ClassNode classNode = read(methodsClass);
        Inject.inject(classNode, IMethods.class);
        final Class<?> aClass = defineClass(write(classNode));
        final Object instance = aClass.newInstance();
        final IMethods methods = (IMethods) instance;
        methods.getInt();
        methods.getAnotherInt();
        methods.getPublicInt();
    }

    public static interface Accessors extends Getters, Setters {}

    @Inject.Owner("Tests$PrivateFields")
    public static interface InheritedAccessors extends Accessors {}

    @Inject.Owner(clazz = PrivateFields.class)
    public static interface InheritedAccessorsViaClass extends Accessors {}

    public static interface CustomNames {
        @Inject.Access("anInt")
        public int getInt();

        @Inject.Access("anInt")
        public void setInt(int value);
    }

    public static interface InvalidNames extends Accessors {
        @Inject.Access
        public int get();
    }

    public static interface InvalidSetter extends Accessors {
        @Inject.Access
        public int setI(int i);
    }

    public static interface InvalidGetter extends Accessors {
        @Inject.Access
        public int getI(int i);
    }

    public static interface InvalidAccessor extends Accessors {
        @Inject.Access
        public void setter(int a, boolean b);
    }

    public static interface CreateAccessors extends Accessors {
        @Override
        @Inject.Access(create = true)
        int getAnInt();

        @Override
        @Inject.Access(create = true)
        byte getAByte();

        @Override
        @Inject.Access(create = true)
        char getAChar();

        @Override
        @Inject.Access(create = true)
        short getAShort();

        @Override
        @Inject.Access(create = true)
        float getAFloat();

        @Override
        @Inject.Access(create = true)
        double getADouble();

        @Override
        @Inject.Access(create = true)
        long getALong();

        @Override
        @Inject.Access(create = true)
        boolean getABoolean();

        @Override
        @Inject.Access(create = true)
        String getString();

        @Override
        @Inject.Access(create = true)
        void setAnInt(int value);

        @Override
        @Inject.Access(create = true)
        void setAByte(byte value);

        @Override
        @Inject.Access(create = true)
        void setAChar(char value);

        @Override
        @Inject.Access(create = true)
        void setAShort(short value);

        @Override
        @Inject.Access(create = true)
        void setAFloat(float value);

        @Override
        @Inject.Access(create = true)
        void setADouble(double value);

        @Override
        @Inject.Access(create = true)
        void setALong(long value);

        @Override
        @Inject.Access(create = true)
        void setABoolean(boolean value);

        @Override
        @Inject.Access(create = true)
        void setString(String value);
    }

    public static interface Getters {
        @Inject.Access
        public int getAnInt();

        @Inject.Access
        public byte getAByte();

        @Inject.Access
        public char getAChar();

        @Inject.Access
        public short getAShort();

        @Inject.Access
        public float getAFloat();

        @Inject.Access
        public double getADouble();

        @Inject.Access
        public long getALong();

        @Inject.Access
        public boolean getABoolean();

        @Inject.Access
        public String getString();
    }

    public static interface Setters {
        @Inject.Access
        public void setAnInt(int value);

        @Inject.Access
        public void setAByte(byte value);

        @Inject.Access
        public void setAChar(char value);

        @Inject.Access
        public void setAShort(short value);

        @Inject.Access
        public void setAFloat(float value);

        @Inject.Access
        public void setADouble(double value);

        @Inject.Access
        public void setALong(long value);

        @Inject.Access
        public void setABoolean(boolean value);

        @Inject.Access
        public void setString(String value);
    }

    public static interface IMethods {
        @Inject.Public
        public int getInt();

        @Inject.Public
        public int getAnotherInt();

        @Inject.Public
        public int getPublicInt();
    }

    public static class Inheritor extends PrivateFields {}

    public static class PrivateFields {
        private int anInt;
        private byte aByte;
        private char aChar;
        private short aShort;
        private float aFloat;
        private double aDouble;
        private long aLong;
        private boolean aBoolean;
        private String string;
    }

    public static class PrivateFinalFields {
        private final int anInt = 0;
        private final byte aByte = 0;
        private final char aChar = 0;
        private final short aShort = 0;
        private final float aFloat = 0;
        private final double aDouble = 0;
        private final long aLong = 0;
        private final boolean aBoolean = false;
        private final String string = null;
    }

    public static class NoneFieldsPresent {}

    public static class Methods {
        private int getInt() {
            return 0;
        }

        protected int getAnotherInt() {
            return 1;
        }

        public int getPublicInt() {
            return 2;
        }

        private static int getStaticInt() {
            return 3;
        }
    }

    public static Class<?> defineClass(byte[] bytes) {
        return new ClassLoader() {
            Class<?> defineClass(byte[] bytes) {
                return defineClass(bytes, 0, bytes.length);
            }
        }.defineClass(bytes);
    };

    public static ClassNode read(String name) throws IOException {
        ClassReader cr = new ClassReader(name);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        return classNode;
    }

    public static ClassNode read(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        return classNode;
    }

    public static byte[] write(ClassNode classNode) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
