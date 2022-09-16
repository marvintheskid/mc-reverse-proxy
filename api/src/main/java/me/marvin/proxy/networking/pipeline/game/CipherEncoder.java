package me.marvin.proxy.networking.pipeline.game;

import me.marvin.proxy.utils.MinecraftEncryption;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Cipher cipher;
    private byte[] inputArr = new byte[0];
    private byte[] outputArr = new byte[0];

    public CipherEncoder(SecretKey key) {
        this.cipher = MinecraftEncryption.createEncryptionCipher(Cipher.ENCRYPT_MODE, key);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws ShortBufferException {
        int inLen = in.readableBytes();
        if (inputArr.length < inLen) {
            inputArr = new byte[inLen];
        }
        in.readBytes(inputArr, 0, inLen);

        int outLen = cipher.getOutputSize(inLen);
        if (outputArr.length < outLen) {
            outputArr = new byte[outLen];
        }
        out.writeBytes(outputArr, 0, cipher.update(inputArr, 0, inLen, outputArr));
    }
}
