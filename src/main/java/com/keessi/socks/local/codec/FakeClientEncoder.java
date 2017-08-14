package com.keessi.socks.local.codec;

import com.keessi.socks.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

public class FakeClientEncoder extends MessageToByteEncoder<ByteBuf> {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int length = msg.readableBytes();

        String header;
        header = "POST /form.action HTTP/1.1\r\n"
                + "HOST:www.baidu.com\r\n"
                + "X-C: " + String.format("%1$08x", length) + "\r\n"
                + "X-U: " + Config.ins().user() + "\r\n"
                + "Content-Length: " + "1024" + "\r\n"
                + "Connection: Keep-Alive\r\n\r\n";

        out.writeBytes(header.getBytes(UTF_8));

        if (length > 0) {
            byte[] buf = new byte[length];
            msg.readBytes(buf, 0, length);
            byte[] res = new byte[length];
            for (int i = 0; i < buf.length; i++) {
                res[i] = (byte) (buf[i] ^ (Config.ins().encryptKey() & 0xFF));
            }
            out.writeBytes(res);
        }
    }
}
