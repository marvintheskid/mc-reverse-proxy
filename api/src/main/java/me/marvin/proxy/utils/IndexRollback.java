package me.marvin.proxy.utils;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * A helper closeable for {@link ByteBuf buffers}.
 */
public interface IndexRollback extends AutoCloseable {
    /**
     * {@inheritDoc}
     */
    @Override
    void close();

    /**
     * Creates a new reader rollback using {@link ByteBuf#markReaderIndex()}.
     *
     * @param buf the buffer
     * @return a new index rollback
     */
    @NotNull
    static IndexRollback reader(@NotNull ByteBuf buf) {
        buf.markReaderIndex();
        return buf::resetReaderIndex;
    }

    /**
     * Creates a new reader rollback using {@link ByteBuf#readerIndex()}.
     *
     * @param buf the buffer
     * @return a new index rollback
     */
    @NotNull
    static IndexRollback readerManual(@NotNull ByteBuf buf) {
        int index = buf.readerIndex();
        return () -> buf.readerIndex(index);
    }

    /**
     * Creates a new writer rollback using {@link ByteBuf#markWriterIndex()}.
     *
     * @param buf the buffer
     * @return a new index rollback
     */
    @NotNull
    static IndexRollback writer(@NotNull ByteBuf buf) {
        buf.markWriterIndex();
        return buf::resetWriterIndex;
    }

    /**
     * Creates a new writer rollback using {@link ByteBuf#writerIndex()}.
     *
     * @param buf the buffer
     * @return a new index rollback
     */
    @NotNull
    static IndexRollback writerManual(@NotNull ByteBuf buf) {
        int index = buf.writerIndex();
        return () -> buf.writerIndex(index);
    }
}
