package org.shadowsocks;

import io.netty.handler.codec.ByteToMessageCodec;

/**
 * AbstractCryptoHandler
 */
public class AbstractCryptoHandler extends ByteToMessageCodec{

    private String method;
    private byte[] pre_shared_key;

    public AbstractCryptoHandler(String method, byte[] pre_shared_key) {
        // super();
        this.method = method;
        this.pre_shared_key = pre_shared_key;
    }

    @Override
    
}