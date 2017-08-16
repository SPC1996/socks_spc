package com.keessi.socks.remote.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {
    private final Bootstrap bootstrap = new Bootstrap();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksCmdRequest msg) throws Exception {
        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener(
                future -> {
                    final Channel outboundChannel = (Channel) future.getNow();
                    if (future.isSuccess()) {
                        ctx.channel()
                                .writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, msg.addressType()))
                                .addListener((ChannelFutureListener) future1 -> {
                                    ctx.pipeline().remove(SocksServerConnectHandler.this);
                                    outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                                    ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                                });
                    } else {
                        ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, msg.addressType()));
                        if (ctx.channel().isActive()) {
                            ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                }
        );
        final Channel inboundChannel = ctx.channel();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new DirectClientHandler(promise));
        bootstrap.connect(msg.host(), msg.port()).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, msg.addressType()));
                if (ctx.channel().isActive()) {
                    ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx.channel().isActive()) {
            ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
