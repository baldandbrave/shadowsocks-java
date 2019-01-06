package org.shadowsocks.relay;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import org.shadowsocks.crypto.AeadStateEnum;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.proxy.Socks5ProxyHandler;

/**
 * tcpRelay
 */
public class tcpRelay extends ReplayingDecoder<relayStateEnum>{

    private InetSocketAddress targetSocketAddress;

    private Bootstrap relay = new Bootstrap();

    public tcpRelay() {
        super(relayStateEnum.READ_TARGET);
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
        relay.handler();
        relay.connect(targetSocketAddress);
    }
}