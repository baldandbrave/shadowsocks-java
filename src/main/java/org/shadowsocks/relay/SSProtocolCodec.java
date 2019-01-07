package org.shadowsocks.relay;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

/**
 * SSProtocolCodec
 */
public class SSProtocolCodec extends ByteToMessageCodec<ByteBuf> {

    private InetSocketAddress targetSocketAddress;

    private Bootstrap relay = new Bootstrap();

    public SSProtocolCodec() {
        // super(relayStateEnum.READ_TARGET);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte hostType = in.readByte();
        switch (hostType) {
            case 0x01:
                targetSocketAddress = new InetSocketAddress(InetAddress.getByAddress(in.readBytes(4).array()), in.readInt());
                break;
            case 0x02:
                short hostLength = in.readUnsignedByte();
                targetSocketAddress = new InetSocketAddress(in.readBytes(hostLength).toString(), in.readInt());
                break;
            case 0x03:
                targetSocketAddress = new InetSocketAddress(InetAddress.getByAddress(in.readBytes(16).array()), in.readInt());
                break;
            default:
                // TODO: throw error and close channel
                break;
        }
        relay.group(ctx.channel().eventLoop())
             .channel(ctx.channel().getClass());
             // TODO: add options for proxy bootstrap
        relay.connect(targetSocketAddress);
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        
    }
    
}