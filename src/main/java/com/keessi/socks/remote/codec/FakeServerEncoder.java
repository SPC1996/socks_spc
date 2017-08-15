package com.keessi.socks.remote.codec;

import com.keessi.socks.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

public class FakeServerEncoder extends MessageToByteEncoder<ByteBuf>{
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int length = msg.readableBytes();

        String header;
        header = "HTTP/1.1 200 OK \r\n"
                + "Content-Type: image/png\r\n"
                + "X-C: " + String.format("%1$08x", length) + "\r\n"
                + "Content-Length: " + "1024" + "\r\n"
                + "Connection: Keep-Alive\r\n\r\n"
                + "Server: nginx\r\n\r\n";

        out.writeBytes(header.getBytes(UTF_8));

        if (length > 0) {
            byte[] buf = new byte[length];
            msg.readBytes(buf);
            byte[] res = new byte[length];
            for (int i = 0; i < buf.length; i++) {
                res[i] = (byte) (buf[i] ^ (Config.ins().encryptKey() & 0xFF));
            }
            out.writeBytes(res);
        }
    }
}
