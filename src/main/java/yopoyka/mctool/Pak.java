package yopoyka.mctool;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Pak {
    public static Pak instance = new Pak();
    protected final Map<Class<?>, RawProcessor<?>> processors = new HashMap<>();
    protected final boolean doCaching;
    protected final Map<Class<?>, PacketProcessor<?>> cache;
    protected final Map<Class<?>, RawProcessor<?>> processorsCache;

    {
        processors.put(int.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeInt(field.getInt(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setInt(o, buffer.readInt());
            }
        });
        processors.put(float.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeFloat(field.getFloat(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setFloat(o, buffer.readFloat());
            }
        });
        processors.put(long.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeLong(field.getLong(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setLong(o, buffer.readLong());
            }
        });
        processors.put(double.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeDouble(field.getDouble(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setDouble(o, buffer.readDouble());
            }
        });
        processors.put(byte.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeByte(field.getByte(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setByte(o, buffer.readByte());
            }
        });
        processors.put(char.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeChar(field.getChar(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setChar(o, buffer.readChar());
            }
        });
        processors.put(short.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeShort(field.getShort(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setShort(o, buffer.readShort());
            }
        });
        processors.put(boolean.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                buffer.writeBoolean(field.getBoolean(o));
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.setBoolean(o, buffer.readBoolean());
            }
        });
        processors.put(byte[].class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                final byte[] bytes = (byte[]) field.get(o);
                buffer.writeInt(bytes.length);
                buffer.writeBytes(bytes);
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                final byte[] bytes = new byte[buffer.readInt()];
                buffer.readBytes(bytes);
                field.set(o, bytes);
            }
        });
        processors.put(String.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                final String string = (String) field.get(o);
                final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
                buffer.writeInt(bytes.length);
                buffer.writeBytes(bytes);
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                final byte[] bytes = new byte[buffer.readInt()];
                buffer.readBytes(bytes);
                field.set(o, new String(bytes, StandardCharsets.UTF_8));
            }
        });
        processors.put(UUID.class, new RawProcessor<Object>() {
            @Override public void serialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                final UUID uuid = (UUID) field.get(o);
                buffer.writeLong(uuid.getMostSignificantBits());
                buffer.writeLong(uuid.getLeastSignificantBits());
            }
            @Override public void deserialize(Field field, Object o, ByteBuf buffer) throws IllegalAccessException {
                field.set(o, new UUID(buffer.readLong(), buffer.readLong()));
            }
        });
    }

    public Pak() {
        this(false);
    }

    public Pak(boolean doCaching) {
        this.doCaching = doCaching;
        cache = doCaching ? new HashMap<>() : null;
        processorsCache = doCaching ? new HashMap<>() : null;
    }

    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Expose {
    }

    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Inherit {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Custom {
        public Class<? extends RawProcessor<?>> value();

        public String instance() default "_yopoyka_instance";
    }

    @SuppressWarnings("unchecked")
    public <T> PacketProcessor<T> get(Class<T> type) {
        PacketProcessor<T> cached = null;
        if (doCaching) {
            cached = (PacketProcessor<T>) cache.get(type);
        }

        if (cached == null) {
            if (type.getAnnotation(Inherit.class) != null) {
                if (type.getSuperclass() != null)
                    cached = (PacketProcessor<T>) get(type.getSuperclass());
            }

            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);

                RawProcessor<T> rawProcessor = fromCustom(field.getAnnotation(Custom.class));

                if (rawProcessor == null)
                    rawProcessor = (RawProcessor<T>) processors.get(field.getType());

                if (rawProcessor == null && field.getType().getAnnotation(Expose.class) != null) {
                    final PacketProcessor<Object> nested = (PacketProcessor<Object>) get(field.getType());
                    rawProcessor = new RawProcessor<T>() {
                        @Override public void serialize(Field f, T t, ByteBuf buffer) throws IllegalAccessException {
                            nested.write(f.get(t), buffer);
                        }
                        @Override public void deserialize(Field f, T t, ByteBuf buffer) throws IllegalAccessException {
                            nested.read(f.get(t), buffer);
                        }
                    };
                }

                if (rawProcessor == null)
                    throw new RuntimeException("Unsupported type: " + field.getType());

                if (cached == null)
                    cached = bindRawProcessor(rawProcessor, field);
                else
                    cached = cached.and(bindRawProcessor(rawProcessor, field));
            }

            if (doCaching)
                cache.put(type, cached);
        }

        return cached;
    }

    protected <T> PacketProcessor<T> bindRawProcessor(RawProcessor<T> raw, Field bindTo) {
        return new PacketProcessor<T>() {
            @Override
            public void write(T t, ByteBuf buffer) {
                try {
                    raw.serialize(bindTo, t, buffer);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void read(T t, ByteBuf buffer) {
                try {
                    raw.deserialize(bindTo, t, buffer);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <T> RawProcessor<T> fromCustom(@Nullable Custom ann) {
        if (ann == null) return null;

        final Class<? extends RawProcessor<?>> processorClass = ann.value();

        RawProcessor<?> processor = null;

        if (doCaching)
            processor = processorsCache.get(processorClass);

        if (processor == null) {
            try {
                final Field instanceField = processorClass.getDeclaredField(ann.instance());
                instanceField.setAccessible(true);
                processor = (RawProcessor<?>) instanceField.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                try {
                    processor = processorClass.newInstance();
                } catch (InstantiationException | IllegalAccessException instantiationException) {
                    throw new RuntimeException(instantiationException);
                }
            }

            if (doCaching)
                processorsCache.put(processorClass, processor);
        }

        return (RawProcessor<T>) processor;
    }

    public static class ConstLenByteArr implements WrappedProcessor<Object, byte[]> {
        public static final ConstLenByteArr _yopoyka_instance = new ConstLenByteArr();

        @Override
        public void write(byte[] bytes, Object o, ByteBuf buf) {
            buf.writeBytes(bytes);
        }

        @Override
        public byte[] read(byte[] bytes, Object o, ByteBuf buf) {
            buf.readBytes(bytes);
            return bytes;
        }

        @Override
        public boolean doGet() {
            return true;
        }

        @Override
        public boolean doSet() {
            return false;
        }
    }

    public static interface PacketProcessor<T> {
        public void write(T t, ByteBuf buffer);

        public void read(T t, ByteBuf buffer);

        default public PacketProcessor<T> and(PacketProcessor<T> other) {
            PacketProcessor<T> original = this;
            return new PacketProcessor<T>() {
                @Override public void write(T t, ByteBuf buffer) {
                    original.write(t, buffer);
                    other.write(t, buffer);
                }
                @Override public void read(T t, ByteBuf buffer) {
                    original.read(t, buffer);
                    other.read(t, buffer);
                }
            };
        }
    }

    public static interface WrappedProcessor<T, V> extends RawProcessor<T> {
        public void write(V v, T t, ByteBuf buf);

        public V read(V v, T t, ByteBuf buf);

        @SuppressWarnings("unchecked")
        @Override
        default void serialize(Field field, T t, ByteBuf buffer) throws IllegalAccessException {
            write((V) field.get(t), t, buffer);
        }

        @SuppressWarnings("unchecked")
        @Override
        default void deserialize(Field field, T t, ByteBuf buffer) throws IllegalAccessException {
            final V value = read(doGet() ? (V) field.get(t) : null, t, buffer);
            if (doSet())
                field.set(t, value);
        }

        default public boolean doGet() {
            return false;
        }

        default public boolean doSet() {
            return true;
        }
    }

    public static interface RawProcessor<T> {
        public void serialize(Field field, T t, ByteBuf buffer) throws IllegalAccessException;

        public void deserialize(Field field, T t, ByteBuf buffer) throws IllegalAccessException;
    }
}
