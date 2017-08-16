package com.keessi.socks;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksResponse;

public class SocksClientConnectHandler extends SimpleChannelInboundHandler<SocksResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksResponse msg) throws Exception {

    }
}
