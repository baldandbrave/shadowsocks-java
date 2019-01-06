package org.shadowsocks;

import java.net.InetAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

/**
 * SSServer
 */
public class SSServer {

    private EpollEventLoopGroup tcpMasterGroup;
    private EpollEventLoopGroup tcpWorkerGroup;
    private EpollEventLoopGroup udpMasteGroup;
    private EpollEventLoopGroup udpWorkerGroup;
    private int timeout;
    private InetAddress host;
    private int port;
    private byte[] pre_shared_master_key;
    // TODO: map port and psk
    private String method;
    private boolean obfs;
    private String obfs_param;

    public SSServer() {
        // TODO: read config and assign private value
        super();
    }

    public void tcpServer() {
        
        ServerBootstrap tcpServerBootstrap = new ServerBootstrap();
        try {
            tcpServerBootstrap.group(tcpMasterGroup,tcpWorkerGroup)
                          .channel(EpollServerSocketChannel.class)
                          .option(ChannelOption.SO_BACKLOG, 5120)
                          .option(ChannelOption.SO_RCVBUF, 32*1024)
                          .option(ChannelOption.SO_KEEPALIVE, true)
                          .option(ChannelOption.SO_TIMEOUT, timeout)
                          .option(ChannelOption.TCP_NODELAY, true)
                          // TODO: fine tuning TCP params
                          .childOption(EpollChannelOption.TCP_FASTOPEN, 5)
                          .childOption(EpollChannelOption.TCP_NOTSENT_LOWAT, Long.valueOf(1301291))
                          // TODO: add more options
                          .childHandler(new ChannelInitializer<EpollServerSocketChannel>() {
                              @Override
                              public void initChannel (EpollServerSocketChannel ctx) {
                                  ctx.pipeline()
                                     .addLast("decrypt", null);
                              }
                          })
                          .bind(host, port)
                          .syncUninterruptibly()
                          .channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            //TODO: handle exception
        } finally {
            tcpWorkerGroup.shutdownGracefully();
            tcpMasterGroup.shutdownGracefully();
        }

    }
    
    public void udpServer() {
        Bootstrap udpBootstrap = new Bootstrap();
        try {
            udpBootstrap.group(udpMasteGroup)
                        .channel(EpollDatagramChannel.class) // TODO: set UDP channel option
                        .bind(host, port)
                        .syncUninterruptibly()
                        .channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            //TODO: handle exception
            udpMasteGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }
}