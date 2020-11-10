import net.minecraft.nbt.NBTTagCompound;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        final BiConsumer<NbtTest, NBTTagCompound> serializer = NBT.serializer(NbtTest.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            final NBTTagCompound compound = new NBTTagCompound();
            serializer.accept(new NbtTest(), compound);
        }
        long finish = System.currentTimeMillis();
        System.out.println(finish - start);

        start = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            final NBTTagCompound compound = new NBTTagCompound();
            final NbtTest nbtTest = new NbtTest();
            compound.setInteger("anInt", nbtTest.anInt);
            compound.setLong("aLong", nbtTest.aLong);
            compound.setIntArray("ints", nbtTest.ints);
            compound.setString("string", nbtTest.string);
        }
        finish = System.currentTimeMillis();
        System.out.println(finish - start);

        final Field anInt = NbtTest.class.getDeclaredField("anInt");
        anInt.setAccessible(true);
        final Field aLong = NbtTest.class.getDeclaredField("aLong");
        aLong.setAccessible(true);
        final Field ints = NbtTest.class.getDeclaredField("ints");
        ints.setAccessible(true);
        final Field string = NbtTest.class.getDeclaredField("string");
        string.setAccessible(true);

        start = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            final NBTTagCompound compound = new NBTTagCompound();
            final NbtTest nbtTest = new NbtTest();
            compound.setInteger("anInt", (int) anInt.get(nbtTest));
            compound.setLong("aLong", (long) aLong.get(nbtTest));
            compound.setIntArray("ints", (int[]) ints.get(nbtTest));
            compound.setString("string", (String) string.get(nbtTest));
        }
        finish = System.currentTimeMillis();
        System.out.println(finish - start);

        final NBTTagCompound nbt = new NBTTagCompound();
        serializer.accept(new NbtTest(), nbt);
        System.out.println(nbt);

        final NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("anInt", 10);
        compound.setLong("aLong", 145);
        compound.setIntArray("ints", new int[] { 10, 20, 30, 100});
        compound.setString("string", "different string");

        final BiConsumer<NbtTest, NBTTagCompound> deserializer = NBT.deserializer(NbtTest.class);
        final NbtTest nbtTest = new NbtTest();
        deserializer.accept(nbtTest, compound);
        System.out.println(nbtTest);
    }

    public static class NbtTest {
        @NBT.Expose
        private Integer anInt = 1;
        @NBT.Expose
        private long aLong = 2;
        @NBT.Expose
        private int[] ints = new int[] { 1, 2, 3 };
        @NBT.Expose
        private String string = "a string";

        @Override
        public String toString() {
            return "NbtTest{" +
                    "anInt=" + anInt +
                    ", aLong=" + aLong +
                    ", ints=" + Arrays.toString(ints) +
                    ", string='" + string + '\'' +
                    '}';
        }
    }

    public static void main__(String[] args) {
        String[] a = new String[] { "one", "two", null, "four" };
        String[] a2 = new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
        final List<String> list = Arrays.stream(a)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Collections.shuffle(list);
        System.out.println(list);
        Collections.shuffle(list);
        System.out.println(list);
        Collections.shuffle(list);
        System.out.println(list);
    }

    public static void main_(String[] args) throws Throwable {
//        final Field field = Tests.PrivateFields.class.getDeclaredField("aBoolean");
//        final Field fieldToo = Tests.PrivateFields.class.getDeclaredField("aBoolean");
//        field.setAccessible(true);
//        fieldToo.setAccessible(true);
//        MethodHandle handle = MethodHandles.lookup().unreflectGetter(fieldToo);
//
//        final int iterations = 1_000_000_000;
//
//        final Tests.PrivateFields o1 = new Tests.PrivateFields();
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < iterations; i++) {
//            boolean b = (boolean) handle.invokeExact(o1);
//        }
//        long finish = System.currentTimeMillis();
//        System.out.println(finish - start);
//
//        final Tests.PrivateFields o2 = new Tests.PrivateFields();
//        start = System.currentTimeMillis();
//        for (int i = 0; i < iterations; i++) {
//            boolean b = (boolean) field.get(o2);
//        }
//        finish = System.currentTimeMillis();
//        System.out.println(finish - start);
//
//        final A a = new A();
//        start = System.currentTimeMillis();
//        for (int i = 0; i < iterations; i++) {
//            boolean b = Boolean.valueOf(a.aBoolean);
//        }
//        finish = System.currentTimeMillis();
//        System.out.println(finish - start);

        final Field stringField = Tests.PrivateFields.class.getDeclaredField("aBoolean");
        stringField.setAccessible(true);

        final ConstantCallSite cs = new ConstantCallSite(MethodHandles.lookup().unreflectGetter(stringField).bindTo(new Tests.PrivateFields()));
//        System.out.println();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            boolean v = (boolean) cs.getTarget().invokeExact();
        }
        long finish = System.currentTimeMillis();
        System.out.println(finish - start);

        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString("hi", "hey");
        System.out.println(compound.getString("hi"));

        //        final CallSite site = LambdaMetafactory.metafactory(
//                MethodHandles.lookup(),
//                "apply",
//                MethodType.methodType(Supplier.class),
//                MethodType.methodType(Private.class),
//                MethodHandles.lookup().findGetter(Private.class, "string", String.class),
//                MethodType.methodType(Private.class, String.class)
//        );
//        Function<Private, String> function = (Function<Private, String>) site.getTarget().invokeExact();
//        System.out.println(function.apply(new Private()));
    }

    static class A {
        public boolean aBoolean;
    }

    static class Private {
        public String string = "private string";
    }
}
