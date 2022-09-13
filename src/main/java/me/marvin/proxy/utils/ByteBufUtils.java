package me.marvin.proxy.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.*;

/**
 * ByteBuf utils.
 */
@SuppressWarnings("NullableProblems")
public enum ByteBufUtils {;
    public static final int MAX_VAR_INT_LENGTH = 5;
    public static final int MAX_VAR_LONG_LENGTH = 10;
    public static final int DEFAULT_MAX_STRING_LENGTH = 32767;

    private static final RuntimeException VARINT_TOO_BIG = new RuntimeException("VarInt too big");
    private static final RuntimeException VARLONG_TOO_BIG = new RuntimeException("VarLong too big");
    private static final DecoderException WEIRD_STRING = new DecoderException("The received encoded string buffer length is less than zero! Weird string!");

    /**
     * Calculates the given variable integer's size.
     *
     * @param value the variable integer
     * @return the size of the variable integer
     */
    public static int calculateVarIntSize(int value) {
        for (int i = 1; i < MAX_VAR_INT_LENGTH; ++i) {
            if ((value & -1 << i * 7) == 0) {
                return i;
            }
        }

        return MAX_VAR_INT_LENGTH;
    }

    /**
     * Calculates the given variable long's size.
     *
     * @param value the variable long
     * @return the size of the variable long
     */
    public static int getVarLongLength(long value) {
        for (int i = 1; i < MAX_VAR_LONG_LENGTH; ++i) {
            if ((value & -1L << i * 7) == 0L) {
                return i;
            }
        }

        return MAX_VAR_LONG_LENGTH;
    }

    /**
     * Writes an optional object to the buffer.
     *
     * @param buf the buffer
     * @param object the object
     * @param entrySerializer the serializer
     * @param <T> the type of the object
     */
    public static <T> void writeOptional(@NotNull ByteBuf buf, @Nullable T object, @NotNull BiConsumer<ByteBuf, T> entrySerializer) {
        if (object != null) {
            buf.writeBoolean(true);
            entrySerializer.accept(buf, object);
        } else {
            buf.writeBoolean(false);
        }
    }

    /**
     * Writes an optional object to the buffer.
     *
     * @param buf the buffer
     * @param entryParser the parser
     * @param <T> the type of the object
     */
    public static <T> T readOptional(@NotNull ByteBuf buf, @NotNull Function<ByteBuf, T> entryParser) {
        if (buf.readBoolean()) {
            return entryParser.apply(buf);
        } else {
            return null;
        }
    }

    /**
     * Iterates on the buffer.
     *
     * @param consumer the consumer
     */
    public static void forEachInCollection(@NotNull ByteBuf buf, Consumer<ByteBuf> consumer) {
        int count = readVarInt(buf);

        for(int j = 0; j < count; ++j) {
            consumer.accept(buf);
        }
    }

    /**
     * Writes the given collection to the buffer.
     *
     * @param <T>             the list's entry type
     * @param entrySerializer the serializer
     * @param collection      the collection
     */
    public static <T> void writeCollection(@NotNull ByteBuf buf, @NotNull Collection<T> collection, @NotNull BiConsumer<ByteBuf, T> entrySerializer) {
        writeVarInt(buf, collection.size());

        for (T object : collection) {
            entrySerializer.accept(buf, object);
        }
    }

    /**
     * Reads a collection from the buffer.
     *
     * @param <T>               the collection's entry type
     * @param <C>               the collection's type
     * @param collectionFactory a factory that creates a collection with a given size
     * @param entryParser       a parser that parses each entry for the collection given this buf
     * @return the new collection
     */
    @NotNull
    public static <T, C extends Collection<T>> C readCollection(@NotNull ByteBuf buf, @NotNull IntFunction<C> collectionFactory, @NotNull Function<ByteBuf, T> entryParser) {
        int size = readVarInt(buf);
        C collection = collectionFactory.apply(size);

        for (int i = 0; i < size; ++i) {
            collection.add(entryParser.apply(buf));
        }

        return collection;
    }

    /**
     * Writes the given map to the buffer.
     *
     * @param <K>             the key type
     * @param <V>             the value type
     * @param map             the map
     * @param keySerializer   the key serializer
     * @param valueSerializer the value serializer
     */
    public static <K, V> void writeMap(@NotNull ByteBuf buf, @NotNull Map<K, V> map, @NotNull BiConsumer<ByteBuf, K> keySerializer, @NotNull BiConsumer<ByteBuf, V> valueSerializer) {
        writeVarInt(buf, map.size());
        map.forEach((key, value) -> {
            keySerializer.accept(buf, key);
            valueSerializer.accept(buf, value);
        });
    }

    /**
     * Reads a map from the buffer.
     *
     * @param <K>         the key type
     * @param <V>         the value type
     * @param <M>         the map type
     *
     * @param mapFactory  the map factory
     * @param keyParser   the key deserializer
     * @param valueParser the value deserializer
     * @return the new map
     */
    public static <K, V, M extends Map<K, V>> M readMap(@NotNull ByteBuf buf, @NotNull IntFunction<M> mapFactory, @NotNull Function<ByteBuf, K> keyParser, @NotNull Function<ByteBuf, V> valueParser) {
        int size = readVarInt(buf);
        M map = mapFactory.apply(size);

        for (int j = 0; j < size; ++j) {
            K k = keyParser.apply(buf);
            V v = valueParser.apply(buf);
            map.put(k, v);
        }

        return map;
    }

    /**
     * Writes the given byte array to the buffer.
     *
     * @param array the array
     */
    public static void writeByteArray(@NotNull ByteBuf buf, @NotNull byte[] array) {
        writeVarInt(buf, array.length);
        buf.writeBytes(array);
    }

    /**
     * Reads a byte array from the buffer, with an expected max size of {@link Short#MAX_VALUE}.
     *
     * @return the array
     */
    @NotNull
    public static byte[] readByteArray(@NotNull ByteBuf buf) {
        return readByteArray(buf, Short.MAX_VALUE);
    }

    /**
     * Reads a byte array from the buffer.
     *
     * @param limit the limit
     * @return the array
     * @throws DecoderException if the length of the array is larger than expected
     */
    @NotNull
    public static byte[] readByteArray(@NotNull ByteBuf buf, int limit) {
        int len = readVarInt(buf);

        if (len > limit)
            throw new DecoderException("The received byte array longer than allowed " + len + " > " + limit);

        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    /**
     * Writes the given variable integer array to the buffer.
     *
     * @param array the array
     */
    public static void writeVarIntArray(@NotNull ByteBuf buf, @NotNull int[] array) {
        writeVarInt(buf, array.length);
        for (int i : array) {
            writeVarInt(buf, i);
        }
    }

    /**
     * Reads a variable integer array from the buffer, with an expected max size of {@link Short#MAX_VALUE}.
     *
     * @return the array
     */
    @NotNull
    public static int[] readVarIntArray(@NotNull ByteBuf buf) {
        return readVarIntArray(buf, Short.MAX_VALUE);
    }

    /**
     * Reads a variable integer array from the buffer.
     *
     * @param limit the limit
     * @return the array
     * @throws DecoderException if the length of the array is larger than expected
     */
    @NotNull
    public static int[] readVarIntArray(@NotNull ByteBuf buf, int limit) {
        int len = readVarInt(buf);

        if (len > limit)
            throw new DecoderException("The received varint array longer than allowed " + len + " > " + limit);

        int[] array = new int[len];
        for (int i = 0; i < array.length; i++) {
            array[i] = readVarInt(buf);
        }
        return array;
    }

    /**
     * Writes the given integer array to the buffer.
     *
     * @param array the array
     */
    public static void writeIntArray(@NotNull ByteBuf buf, @NotNull int[] array) {
        writeVarInt(buf, array.length);
        for (int i : array) {
            buf.writeInt(i);
        }
    }

    /**
     * Reads an integer array from the buffer, with an expected max size of {@link Short#MAX_VALUE}.
     *
     * @return the array
     */
    @NotNull
    public static int[] readIntArray(@NotNull ByteBuf buf) {
        return readIntArray(buf, Short.MAX_VALUE);
    }

    /**
     * Reads an integer array from the buffer.
     *
     * @param limit the limit
     * @return the array
     * @throws DecoderException if the length of the array is larger than expected
     */
    @NotNull
    public static int[] readIntArray(@NotNull ByteBuf buf, int limit) {
        int len = readVarInt(buf);

        if (len > limit)
            throw new DecoderException("The received integer array longer than allowed " + len + " > " + limit);

        int[] array = new int[len];
        for (int i = 0; i < array.length; i++) {
            array[i] = buf.readInt();
        }
        return array;
    }

    /**
     * Writes the given {@link Enum} to the buffer.
     *
     * @param e the enum
     */
    public static void writeEnum(@NotNull ByteBuf buf, @NotNull Enum<?> e) {
        writeVarInt(buf, e.ordinal());
    }

    /**
     * Reads an {@link Enum} from the buffer.
     *
     * @return the tag
     */
    @NotNull
    public static <T extends Enum<T>> T readEnum(@NotNull ByteBuf buf, @NotNull Class<T> clazz) {
        return clazz.getEnumConstants()[readVarInt(buf)];
    }

    /**
     * Writes the given variable integer to the buffer.
     * This method is generally faster than other variable integer
     * writers.
     * <p>
     * Based on: <a href="https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/">How fast can you write a VarInt?</a>
     *
     * @param value the variable integer
     */
    public static void writeVarInt(@NotNull ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = ((value & 0x7F) | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else {
            writeVarIntFull(buf, value);
        }
    }

    /**
     * Writes the given variable integer to the buffer.
     * This method is generally faster than other variable integer
     * writers.
     * <p>
     * Based on: <a href="https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/">How fast can you write a VarInt?</a>
     *
     * @param value the variable integer
     */
    public static void writeVarIntFull(@NotNull ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (((value & 0x7F) | 0x80) << 8) | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w =
                (((value & 0x7F) | 0x80) << 16) | ((((value >>> 7) & 0x7F) | 0x80) << 8) | (value >>> 14);
            buf.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (((value & 0x7F) | 0x80) << 24) | ((((value >>> 7) & 0x7F) | 0x80) << 16)
                | ((((value >>> 14) & 0x7F) | 0x80) << 8) | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (((value & 0x7F) | 0x80) << 24) | ((((value >>> 7) & 0x7F) | 0x80) << 16)
                | ((((value >>> 14) & 0x7F) | 0x80) << 8) | (((value >>> 21) & 0x7F) | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }

    /**
     * Reads a variable integer from the buffer.
     *
     * @return the variable integer
     */
    public static int readVarInt(@NotNull ByteBuf buf) {
        int number = 0;
        int chunk = 0;

        byte currentByte;

        do {
            currentByte = buf.readByte();
            number |= (currentByte & 127) << chunk++ * 7;
            if (chunk > 5) {
                throw VARINT_TOO_BIG;
            }
        } while ((currentByte & 128) == 128);

        return number;
    }

    /**
     * Writes the given variable long to the buffer.
     *
     * @param value the variable long
     */
    public static void writeVarLong(@NotNull ByteBuf buf, long value) {
        while ((value & -128L) != 0L) {
            buf.writeByte((int) (value & 127L) | 128);
            value >>>= 7;
        }

        buf.writeByte((int) value);
    }

    /**
     * Reads a variable long from the buffer.
     *
     * @return the variable integer
     */
    public static long readVarLong(@NotNull ByteBuf buf) {
        long number = 0L;
        int chunk = 0;

        byte currentByte;

        do {
            currentByte = buf.readByte();
            number |= (long) (currentByte & 127) << chunk++ * 7;
            if (chunk > 10) {
                throw VARLONG_TOO_BIG;
            }
        } while ((currentByte & 128) == 128);

        return number;
    }

    /**
     * Writes the given {@link UUID} to the buffer.
     *
     * @param uuid the uuid
     */
    public static void writeUuid(@NotNull ByteBuf buf, @NotNull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads an {@link UUID} from the buffer.
     *
     * @return the tag
     */
    @NotNull
    public static UUID readUuid(@NotNull ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    /**
     * Writes the given {@link String} to the buffer with a max size of 32767.
     *
     * @param s the string
     * @throws DecoderException if the length of the string is longer than {@link Short#MAX_VALUE}
     */
    public static void writeString(@NotNull ByteBuf buf, @NotNull String s) {
        writeString(buf, s, DEFAULT_MAX_STRING_LENGTH);
    }

    /**
     * Writes the given {@link String} to the buffer.
     *
     * @param s the string
     * @param allowedLength the allowed length
     * @throws DecoderException if the length of the string is longer than {@code allowedLength}
     */
    public static void writeString(@NotNull ByteBuf buf, @NotNull String s, int allowedLength) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        if (bytes.length > allowedLength) {
            throw new EncoderException("String too big ('" + s + "' was " + bytes.length + " bytes encoded, max " + allowedLength + ")");
        } else {
            writeVarInt(buf, bytes.length);
            buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }
    }

    /**
     * Reads a {@link String} from the buffer, with a max size of 32767.
     *
     * @return the result
     * @throws DecoderException if the buffer is bigger than the expected,
     *                          or the length of the string is bigger than the expected
     */
    @NotNull
    public static String readString(@NotNull ByteBuf buf) {
        return readString(buf, DEFAULT_MAX_STRING_LENGTH);
    }

    /**
     * Reads a {@link String} from the buffer.
     *
     * @param expectedLength the expected length of the result
     * @return the result
     * @throws DecoderException if the buffer is bigger than the expected,
     *                          or the length of the string is bigger than the expected
     */
    @NotNull
    public static String readString(@NotNull ByteBuf buf, int expectedLength) {
        int length = readVarInt(buf);

        if (length > expectedLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + length + " > " + expectedLength * 4 + ")");
        } else if (length < 0) {
            throw WEIRD_STRING;
        } else {
            String s = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();

            if (s.length() > expectedLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + length + " > " + expectedLength + ")");
            } else {
                return s;
            }
        }
    }
}
