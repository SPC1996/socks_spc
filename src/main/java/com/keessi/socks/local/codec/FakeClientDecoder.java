package com.keessi.socks.local.codec;

import com.keessi.socks.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ByteProcessor;

import java.util.List;

public class FakeClientDecoder extends ReplayingDecoder<FakeClientDecoder.STATE> {
    enum STATE {
        READ_FAKE_HTTP,
        READ_CONTENT
    }

    private int length;

    public FakeClientDecoder() {
        super(STATE.READ_FAKE_HTTP);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_FAKE_HTTP: {
                int start = in.readerIndex();
                int end = in.forEachByte(new ByteProcessor() {
                    int count = 0;

                    @Override
                    public boolean process(byte value) throws Exception {
                        if (value == '\r' || value == '\n') {
                            count++;
                        } else {
                            count = 0;
                        }
                        return count <= 4;
                    }
                });
                if (end == -1) {
                    break;
                }
                byte[] buf = new byte[end - start + 1];
                in.readBytes(buf, 0, end - start + 1);
                String[] ss = new String(buf, "UTF-8").split("\r\n");
                for (String line : ss) {
                    if (line.startsWith("X-C:")) {
                        String lenStr = line.substring(line.indexOf(':') + 1).trim();
                        try {
                            length = Integer.parseInt(lenStr, 16);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
                checkpoint(STATE.READ_CONTENT);
            }
            case READ_CONTENT: {
                if (length > 0) {
                    byte[] buf = new byte[length];
                    in.readBytes(buf, 0, length);
                    byte[] res = new byte[length];
                    for (int i = 0; i < length; i++) {
                        res[i] = (byte) (buf[i] ^ (Config.ins().encryptKey() & 0xFF));
                    }
                    ByteBuf outBuf = ctx.alloc().buffer();
                    outBuf.writeBytes(res);
                    out.add(outBuf);
                }
                checkpoint(STATE.READ_FAKE_HTTP);
            }
            default:
                throw new RuntimeException("Some error unexpected");
        }
    }
}
