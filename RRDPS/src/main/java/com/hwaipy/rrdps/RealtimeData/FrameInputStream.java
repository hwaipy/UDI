package com.hwaipy.rrdps.RealtimeData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author Hwaipy
 */
public class FrameInputStream extends InputStream {

    private final ByteBuffer frameBuffer;
    private final InputStream inputStream;

    public FrameInputStream(InputStream inputStream, int frameSize) {
        frameBuffer = ByteBuffer.allocate(frameSize);
        frameBuffer.position(frameBuffer.limit());
        this.inputStream = new BufferedInputStream(inputStream, 1024 * 1024);
//        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        if (!frameBuffer.hasRemaining()) {
            frameBuffer.position(0);
            frameBuffer.limit(frameBuffer.capacity());
            while (frameBuffer.hasRemaining()) {
                int read = inputStream.read();
                if (read == -1) {
                    break;
                }
                frameBuffer.put((byte) read);
            }
            if (frameBuffer.hasRemaining()) {
                if (frameBuffer.position() == 0) {
                    return -1;
                } else {
                    throw new IOException("Frame invalid.");
                }
            }
            frameBuffer.position(8);
            frameBuffer.limit(frameBuffer.capacity() - 8);
        }
        int r = frameBuffer.get();
        if (r < 0) {
            r += 256;
        }
        return r;
    }
}
