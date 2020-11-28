package yopoyka.mctool;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class NBT {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Expose {
        public boolean serialize() default true;

        public boolean deserialize() default true;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Name {
        public String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Inherit {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public static @interface CustomWriter {
        public Class<? extends RawWriter<?, ?>> value();

        public String instance() default "_yopoyka_nbt_writer";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public static @interface CustomReader {
        public Class<? extends RawReader<?, ?>> value();

        public String instance() default "_yopoyka_nbt_reader";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public static @interface Custom {
        public Class<? extends RawReadWriter<?, ?>> value();

        public String instance() default "_yopoyka_nbt_readwriter";
    }

    public static <T> Consumer<NBTTagCompound> boundSerializer(Class<T> schema, T bindTo) {
        final BiConsumer<T, NBTTagCompound> serializer = serializer(schema);
        return nbtTagCompound -> serializer.accept(bindTo, nbtTagCompound);
    }

    public static <T> BiConsumer<T, NBTTagCompound> serializer(Class<T> schema) {
        BiConsumer<T, NBTTagCompound> result = null;

        for (Field field : schema.getDeclaredFields()) {
            final Expose expose = field.getAnnotation(Expose.class);
            if (expose == null || !expose.serialize()) continue;

            field.setAccessible(true);

            final Name nameAnn = field.getAnnotation(Name.class);

            final String serialName = nameAnn == null ? field.getName() : nameAnn.value();

            BiConsumer<T, NBTTagCompound> mapper;

            final Class<?> fieldType = field.getType();

            if (fieldType == int.class || fieldType == Integer.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setInteger(serialName, (int) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == float.class || fieldType == Float.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setFloat(serialName, (float) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == long.class || fieldType == Long.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setLong(serialName, (long) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == double.class || fieldType == Double.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setDouble(serialName, (double) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == short.class || fieldType == Short.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setShort(serialName, (short) field.get(field));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == byte.class || fieldType == Byte.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setByte(serialName, (byte) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == boolean.class || fieldType == Boolean.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setBoolean(serialName, (boolean) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == String.class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setString(serialName, (String) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == int[].class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setIntArray(serialName, (int[]) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (fieldType == byte[].class) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setByteArray(serialName, (byte[]) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else if (NBTBase.class.isAssignableFrom(fieldType)) {
                mapper = (t, nbtTagCompound) -> {
                    try {
                        nbtTagCompound.setTag(serialName, (NBTBase) field.get(t));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                };
            }
            else
                throw new RuntimeException("Could not serialize field with " + fieldType + " type.");

            if (result == null)
                result = mapper;
            else
                result = result.andThen(mapper);
        }

        return result;
    }

    public static <T> Consumer<NBTTagCompound> boundDeserializer(Class<T> schema, T bindTo) {
        final BiConsumer<T, NBTTagCompound> deserializer = deserializer(schema);
        return nbtTagCompound -> deserializer.accept(bindTo, nbtTagCompound);
    }

    public static <T> BiConsumer<T, NBTTagCompound> deserializer(Class<T> schema) {
        BiConsumer<T, NBTTagCompound> result = null;

        for (Field field : schema.getDeclaredFields()) {
            final Expose expose = field.getAnnotation(Expose.class);
            if (expose == null || !expose.deserialize()) continue;

            field.setAccessible(true);

            final Name nameAnn = field.getAnnotation(Name.class);

            final String serialName = nameAnn == null ? field.getName() : nameAnn.value();

            final MethodHandle setter;
            try {
                setter = new ConstantCallSite(MethodHandles.lookup().unreflectSetter(field)).getTarget();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            BiConsumer<T, NBTTagCompound> mapper;

            final Class<?> fieldType = field.getType();

            final BiFunction<NBTTagCompound, String, ?> nbtGetter;

            if (fieldType == int.class || fieldType == Integer.class) {
                nbtGetter = NBTTagCompound::getInteger;
            }
            else if (fieldType == float.class || fieldType == Float.class) {
                nbtGetter = NBTTagCompound::getFloat;
            }
            else if (fieldType == long.class || fieldType == Long.class) {
                nbtGetter = NBTTagCompound::getLong;
            }
            else if (fieldType == double.class || fieldType == Double.class) {
                nbtGetter = NBTTagCompound::getDouble;
            }
            else if (fieldType == short.class || fieldType == Short.class) {
                nbtGetter = NBTTagCompound::getShort;
            }
            else if (fieldType == byte.class || fieldType == Byte.class) {
                nbtGetter = NBTTagCompound::getByte;
            }
            else if (fieldType == boolean.class || fieldType == Boolean.class) {
                nbtGetter = NBTTagCompound::getBoolean;
            }
            else if (fieldType == String.class) {
                nbtGetter = NBTTagCompound::getString;
            }
            else if (fieldType == int[].class) {
                nbtGetter = NBTTagCompound::getIntArray;
            }
            else if (fieldType == byte[].class) {
                nbtGetter = NBTTagCompound::getByteArray;
            }
            else if (NBTBase.class.isAssignableFrom(fieldType)) {
                nbtGetter = NBTTagCompound::getBoolean;
            }
            else
                throw new RuntimeException("Could not deserialize field with " + fieldType + " type.");

            mapper = (t, nbtTagCompound) -> {
                try {
                    setter.invoke(t, nbtGetter.apply(nbtTagCompound, serialName));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            };

            if (result == null)
                result = mapper;
            else
                result = result.andThen(mapper);
        }

        return result;
    }

    protected final boolean doCaching = true;
    protected Map<Class<?>, Pair<Function<?, ? extends NBTBase>, Function<? extends NBTBase, ?>>> cache = new HashMap<>();
    protected Map<Class<?>, Pair<RawWriter<? extends NBTBase, ?>, RawReader<? extends NBTBase, ?>>> rawProcessors = new HashMap<>();

    public <T, N extends NBTBase> Function<T, N> writer(Class<T> schema) {
        final Pair<Function<?, ? extends NBTBase>, Function<? extends NBTBase, ?>> pair;

        if (doCaching)
            pair = cache.get(schema);

        if (pair == null) {
        }

        return null;
    }

    public static interface RawReadWriter<N extends NBTBase, T> extends RawReader<N, T>, RawWriter<N, T> {}

    public static interface RawWriter<N extends NBTBase, T> {
        public N write(Field field, T t) throws IllegalAccessException;
    }

    public static interface RawReader<N extends NBTBase, T> {
        public void read(Field field, T t, N n) throws IllegalAccessException;
    }

    protected static class Pair<A, B> {
        protected A right;
        protected B left;

        public A getWriter() {
            return right;
        }

        public void setWriter(A right) {
            this.right = right;
        }

        public B getReader() {
            return left;
        }

        public void setReader(B left) {
            this.left = left;
        }
    }
}
