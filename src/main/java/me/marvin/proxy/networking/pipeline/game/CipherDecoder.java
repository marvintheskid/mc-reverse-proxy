package me.marvin.proxy.networking.pipeline.game;

import me.marvin.proxy.utils.MinecraftEncryption;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.List;

public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final Cipher cipher;
    private byte[] inputArr = new byte[0];

    public CipherDecoder(SecretKey key) {
        this.cipher = MinecraftEncryption.createEncryptionCipher(Cipher.DECRYPT_MODE, key);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int len = in.readableBytes();
        if (inputArr.length < len) {
            inputArr = new byte[len];
        }
        in.readBytes(inputArr, 0, len);

        ByteBuf tempBuf = ctx.alloc().heapBuffer(cipher.getOutputSize(len));
        tempBuf.writerIndex(cipher.update(inputArr, 0, len, tempBuf.array(), tempBuf.arrayOffset()));
        out.add(tempBuf);
    }
}
