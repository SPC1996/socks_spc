package com.keessi.socks;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;

public class SocksClientHandler extends SimpleChannelInboundHandler<SocksResponse> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(new SocksInitRequest(new ArrayList<>(0)));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksResponse msg) throws Exception {
        switch (msg.responseType()) {
            case INIT:
                ctx.pipeline().addAfter("log", "cmdResponseDecoder", new SocksCmdResponseDecoder());
                ctx.write(new SocksCmdRequest(SocksCmdType.CONNECT, SocksAddressType.DOMAIN, "www.baidu.com", 80));
                break;
            case AUTH:
                ctx.pipeline().addAfter("log", "cmdResponseDecoder", new SocksCmdResponseDecoder());
                ctx.write(new SocksCmdRequest(SocksCmdType.CONNECT, SocksAddressType.DOMAIN, "www.baidu.com", 80));
                break;
            case CMD:
                SocksCmdResponse response = (SocksCmdResponse) msg;
                if (response.cmdStatus() == SocksCmdStatus.SUCCESS) {
                    ctx.pipeline().addLast(new SocksClientConnectHandler());
                    ctx.pipeline().remove(this);
                    String s = "GET / HTTP/1.1\r\nHOST:www.baidu.com\r\n\r\n";
                    ctx.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
                } else {
                    System.out.println("fail to connect");
                    ctx.close();
                }
                break;
            case UNKNOWN:
                ctx.close();
                break;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }
}
