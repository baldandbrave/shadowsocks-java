package org.shadowsocks.crypto;

import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * AeadDecoder
 */
public class AeadDecoder extends ReplayingDecoder<AeadStateEnum>{

    private Cipher mambo;
    private AlgorithmParameterSpec mamboSpec;
    private byte[] masterKey;
    private byte[] salt;
    private int saltLength = 16;
    private byte[] subkey;
    private int subkeyLength;
    private byte[] info = "ss-subkey".getBytes();

    private int nonce=0;
    private int nonce_length=12;
    private int tagLength;

    private int headerLength = 2;
    private SecretKey mamboKey;

    private short payloadLength;

    public AeadDecoder () throws Exception {
        super(AeadStateEnum.READ_SALT);
        // TODO: constructor needs definition
        mambo = Cipher.getInstance("ChaCha20-Poly1305");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_SALT:
                // read salt from in
                in.readBytes(salt, 0, saltLength);
                // gen subkey for this session
                HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA1Digest());
                hkdf.init(new HKDFParameters(masterKey, salt, info));
                hkdf.generateBytes(subkey, 0, subkeyLength);
                mamboKey = new SecretKeySpec(subkey, "HKDF_SHA1"); // this constructer doesn't check the match betwen key and algorithm. Cipher.unwrap checks it.
                checkpoint(AeadStateEnum.READ_LENGTH);
                break;
            case READ_LENGTH:
                // read header, 2 bytes length, 16 bytes length tag
                ByteBuffer header = ByteBuffer.allocate(headerLength + tagLength);
                in.readBytes(header);
                // in.nioBuffer(0, 2+16); expose the raw input ByteBuf as ByteBuffer, shouldn't use it cos it doesn't change read index.
                ByteBuffer headerOutput = decrypt(header);
                // TODO: if >0x3FFF throw error
                payloadLength = headerOutput.getShort(); // payloadLength is a big-endian unsigned int capped at 0x3FFF
                // https://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html#doFinal(java.nio.ByteBuffer,%20java.nio.ByteBuffer)
                checkpoint(AeadStateEnum.READ_PAYLOAD);
                break;
            case READ_PAYLOAD:
                // read and decrypt payload
                ByteBuffer payload = ByteBuffer.allocate(payloadLength + tagLength);
                in.readBytes(payload);
                ByteBuffer payloadOutput = decrypt(payload);
                checkpoint(AeadStateEnum.READ_LENGTH);
                out.add(Unpooled.wrappedBuffer(payloadOutput));
            default:
                break;
        }
        
    }

    public ByteBuffer decrypt(ByteBuffer in) throws Exception{
        // TODO: ByteBuffer is big endian, nonce might need to be little endian unsigned int
        mamboSpec = new IvParameterSpec(ByteBuffer.allocate(nonce_length).putInt(nonce).array());
        mambo.init(Cipher.DECRYPT_MODE, mamboKey, mamboSpec);
        ByteBuffer out = ByteBuffer.allocate(mambo.getOutputSize(in.capacity()));
        mambo.doFinal(in, out);
        nonce++;
        return out;
    }
}