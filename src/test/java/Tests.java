import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import yopoyka.mctool.Injector;
import yopoyka.mctool.Pak;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class Tests {
    byte[] privateFieldsClass;
    byte[] privateFinalFieldsClass;
    byte[] noneFieldsPresentClass;
    byte[] methodsClass;
    byte[] redirectsClass;

    public static void main(String[] args) {
    }

    @Before
    public void setup() throws IOException {
        privateFieldsClass = readClass("Tests$PrivateFields");
        privateFinalFieldsClass = readClass("Tests$PrivateFinalFields");
        noneFieldsPresentClass = readClass("Tests$NoneFieldsPresent");
        methodsClass = readClass("Tests$Methods");
        redirectsClass = readClass("Tests$Redirects");
        Pak.instance = new Pak(true);
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

    @Test
    public void test_packing_unpacking() {
        final Pak.PacketProcessor<Message> processor = Pak.instance.get(Message.class);
        final CompositeByteBuf buf = new CompositeByteBuf(new UnpooledByteBufAllocator(true), true, Integer.MAX_VALUE);
        final Message outbound = new Message();
        outbound.string = "Outbound!";
        outbound.nested.hello = "nested string";
        outbound.varLen = new byte[20];
        final Message incoming = new Message();
        processor.write(outbound, buf);
        processor.read(incoming, buf);
        test_nested_and_inherited();
        Assert.assertEquals(outbound, incoming);
    }

    @Test
    public void test_nested_and_inherited() {
        final CompositeByteBuf buf = new CompositeByteBuf(new UnpooledByteBufAllocator(true), true, Integer.MAX_VALUE);
        final Pak.PacketProcessor<MessageExtended> processor = Pak.instance.get(MessageExtended.class);
        final MessageExtended outboundExt = new MessageExtended();
        outboundExt.string = "Outbound!";
        outboundExt.nested.hello = "nested string";
        outboundExt.aString = "Other Value";
        outboundExt.varLen = new byte[20];
        final MessageExtended incomingExt = new MessageExtended();
        processor.write(outboundExt, buf);
        processor.read(incomingExt, buf);
        Assert.assertEquals(outboundExt, incomingExt);
    }

    @Pak.Inherit
    public static class MessageExtended extends Message {
        @Pak.Expose
        public String aString = "Some Value";

        @Override
        public String toString() {
            return "MessageExtended{" +
                    "aString='" + aString + '\'' +
                    ", anInt=" + anInt +
                    ", aByte=" + aByte +
                    ", aChar=" + aChar +
                    ", aShort=" + aShort +
                    ", aFloat=" + aFloat +
                    ", aDouble=" + aDouble +
                    ", aLong=" + aLong +
                    ", aBoolean=" + aBoolean +
                    ", string='" + string + '\'' +
                    ", uuid=" + uuid +
                    ", nested=" + nested +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            MessageExtended that = (MessageExtended) o;
            return Objects.equals(aString, that.aString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), aString);
        }
    }

    public static class Message {
        @Pak.Expose
        protected int anInt;
        @Pak.Expose
        protected byte aByte;
        @Pak.Expose
        protected char aChar;
        @Pak.Expose
        protected short aShort;
        @Pak.Expose
        protected float aFloat;
        @Pak.Expose
        protected double aDouble;
        @Pak.Expose
        protected long aLong;
        @Pak.Expose
        protected boolean aBoolean;
        @Pak.Expose
        protected String string = "a string!";
        @Pak.Expose
        protected UUID uuid = UUID.randomUUID();
        @Pak.Expose
        protected byte[] varLen;
        @Pak.Expose
        @Pak.Custom(Pak.ConstLenByteArr.class)
        protected byte[] constLen = new byte[256];
        @Pak.Expose
        protected MessageNested nested = new MessageNested();

        @Override
        public String toString() {
            return "Message{" +
                    "anInt=" + anInt +
                    ", aByte=" + aByte +
                    ", aChar=" + aChar +
                    ", aShort=" + aShort +
                    ", aFloat=" + aFloat +
                    ", aDouble=" + aDouble +
                    ", aLong=" + aLong +
                    ", aBoolean=" + aBoolean +
                    ", string='" + string + '\'' +
                    ", uuid=" + uuid +
                    ", nested=" + nested +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Message message = (Message) o;
            return anInt == message.anInt &&
                    aByte == message.aByte &&
                    aChar == message.aChar &&
                    aShort == message.aShort &&
                    Float.compare(message.aFloat, aFloat) == 0 &&
                    Double.compare(message.aDouble, aDouble) == 0 &&
                    aLong == message.aLong &&
                    aBoolean == message.aBoolean &&
                    Objects.equals(string, message.string) &&
                    Objects.equals(uuid, message.uuid) &&
                    Arrays.equals(varLen, message.varLen) &&
                    Arrays.equals(constLen, message.constLen) &&
                    Objects.equals(nested, message.nested);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(anInt, aByte, aChar, aShort, aFloat, aDouble, aLong, aBoolean, string, uuid, nested);
            result = 31 * result + Arrays.hashCode(varLen);
            result = 31 * result + Arrays.hashCode(constLen);
            return result;
        }
    }

    @Pak.Expose
    public static class MessageNested {
        @Pak.Expose
        public String hello = "Hello!";

        @Override
        public String toString() {
            return "MessageNested{" +
                    "hello='" + hello + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageNested that = (MessageNested) o;
            return Objects.equals(hello, that.hello);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hello);
        }
    }

    @Test
    public void testRedirects() throws IllegalAccessException, InstantiationException {
        final ClassNode classNode = read(redirectsClass);

        Injector.instance.inject(classNode, IRedirects.class);

        final Class<?> aClass = defineClass(write(classNode));
        final IRedirects o = (IRedirects) aClass.newInstance();
        System.out.println(o.methodRedirect(1, (short) 2, '3', (byte) 4, false, 5F, 6L, 7D, "hi"));
        System.out.println(o.staticMethodRedirect(1, (short) 2, '3', (byte) 4, false, 5F, 6L, 7D, "hi"));
    }

    public static interface IRedirects {
        @Injector.Redirect(to = "method")
        Object methodRedirect(int a, short s, char c, byte b, boolean z, float f, long l, double d, String o);

        @Injector.Redirect(to = "staticMethod", callType = Injector.Redirect.Type.STATIC)
        Object staticMethodRedirect(int a, short s, char c, byte b, boolean z, float f, long l, double d, String o);
    }

    public static class Redirects {
        public Object method(int a, short s, char c, byte b, boolean z, float f, long l, double d, String o) {
            return "method";
        }

        public static Object staticMethod(int a, short s, char c, byte b, boolean z, float f, long l, double d, String o) {
            return "static method";
        }
    }

    @Test
    public void accessors() throws IllegalAccessException, InstantiationException {
        test(read(privateFieldsClass), Accessors.class);
    }

    @Test
    public void customNames() throws IllegalAccessException, InstantiationException {
        final ClassNode classNode = read(privateFieldsClass);
        Injector.instance.inject(classNode, CustomNames.class);
        final Class<?> aClass = defineClass(write(classNode));
        final Object instance = aClass.newInstance();
        final CustomNames a = (CustomNames) instance;
        Assert.assertEquals(0, a.getInt());
        a.setInt(1);
        Assert.assertEquals(1, a.getInt());
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
        Injector.instance.inject(classNode, model);
        final Class<?> aClass = defineClass(write(classNode));
        final Object instance = aClass.newInstance();
        Accessors a = (Accessors) instance;
        Assert.assertEquals(0, a.getAnInt());
        Assert.assertEquals(0, a.getAByte());
        Assert.assertEquals(0, a.getAChar());
        Assert.assertEquals(0, a.getAShort());
        Assert.assertEquals(0, a.getAFloat(), 0);
        Assert.assertEquals(0, a.getADouble(), 0);
        Assert.assertEquals(0, a.getALong());
        Assert.assertFalse(a.getABoolean());
        Assert.assertNull(a.getString());
        a.setAnInt(1);
        Assert.assertEquals(1, a.getAnInt());
        a.setAByte((byte) 2);
        Assert.assertEquals(2, a.getAByte());
        a.setAChar('c');
        Assert.assertEquals('c', a.getAChar());
        a.setAShort((short) 4);
        Assert.assertEquals(4, a.getAShort());
        a.setAFloat(5);
        Assert.assertEquals(5, a.getAFloat(), 0);
        a.setADouble(6);
        Assert.assertEquals(6, a.getADouble(), 0);
        a.setALong(7);
        Assert.assertEquals(7, a.getALong());
        a.setABoolean(true);
        Assert.assertTrue(a.getABoolean());
        a.setString("test");
        Assert.assertEquals("test", a.getString());
    }

    @Test
    public void methods() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        final ClassNode classNode = read(methodsClass);
        Injector.instance.inject(classNode, IMethods.class);
        final Class<?> aClass = defineClass(write(classNode));
        final Object instance = aClass.newInstance();
        final IMethods methods = (IMethods) instance;
        methods.getInt();
        methods.getAnotherInt();
        methods.getPublicInt();
    }

    public static interface Accessors extends Getters, Setters {}

    @Injector.Owner("Tests$PrivateFields")
    public static interface InheritedAccessors extends Accessors {}

    @Injector.Owner(clazz = PrivateFields.class)
    public static interface InheritedAccessorsViaClass extends Accessors {}

    public static interface CustomNames {
        @Injector.AccessField("anInt")
        public int getInt();

        @Injector.AccessField("anInt")
        public void setInt(int value);
    }

    public static interface InvalidNames extends Accessors {
        @Injector.AccessField
        public int get();
    }

    public static interface InvalidSetter extends Accessors {
        @Injector.AccessField
        public int setI(int i);
    }

    public static interface InvalidGetter extends Accessors {
        @Injector.AccessField
        public int getI(int i);
    }

    public static interface InvalidAccessor extends Accessors {
        @Injector.AccessField
        public void setter(int a, boolean b);
    }

    public static interface CreateAccessors extends Accessors {
        @Override
        @Injector.AccessField(create = true)
        int getAnInt();

        @Override
        @Injector.AccessField(create = true)
        byte getAByte();

        @Override
        @Injector.AccessField(create = true)
        char getAChar();

        @Override
        @Injector.AccessField(create = true)
        short getAShort();

        @Override
        @Injector.AccessField(create = true)
        float getAFloat();

        @Override
        @Injector.AccessField(create = true)
        double getADouble();

        @Override
        @Injector.AccessField(create = true)
        long getALong();

        @Override
        @Injector.AccessField(create = true)
        boolean getABoolean();

        @Override
        @Injector.AccessField(create = true)
        String getString();

        @Override
        @Injector.AccessField(create = true)
        void setAnInt(int value);

        @Override
        @Injector.AccessField(create = true)
        void setAByte(byte value);

        @Override
        @Injector.AccessField(create = true)
        void setAChar(char value);

        @Override
        @Injector.AccessField(create = true)
        void setAShort(short value);

        @Override
        @Injector.AccessField(create = true)
        void setAFloat(float value);

        @Override
        @Injector.AccessField(create = true)
        void setADouble(double value);

        @Override
        @Injector.AccessField(create = true)
        void setALong(long value);

        @Override
        @Injector.AccessField(create = true)
        void setABoolean(boolean value);

        @Override
        @Injector.AccessField(create = true)
        void setString(String value);
    }

    public static interface Getters {
        @Injector.AccessField
        public int getAnInt();

        @Injector.AccessField
        public byte getAByte();

        @Injector.AccessField
        public char getAChar();

        @Injector.AccessField
        public short getAShort();

        @Injector.AccessField
        public float getAFloat();

        @Injector.AccessField
        public double getADouble();

        @Injector.AccessField
        public long getALong();

        @Injector.AccessField
        public boolean getABoolean();

        @Injector.AccessField
        public String getString();
    }

    public static interface Setters {
        @Injector.AccessField
        public void setAnInt(int value);

        @Injector.AccessField
        public void setAByte(byte value);

        @Injector.AccessField
        public void setAChar(char value);

        @Injector.AccessField
        public void setAShort(short value);

        @Injector.AccessField
        public void setAFloat(float value);

        @Injector.AccessField
        public void setADouble(double value);

        @Injector.AccessField
        public void setALong(long value);

        @Injector.AccessField
        public void setABoolean(boolean value);

        @Injector.AccessField
        public void setString(String value);
    }

    public static interface IMethods {
        @Injector.Public
        public int getInt();

        @Injector.Public
        public int getAnotherInt();

        @Injector.Public
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
