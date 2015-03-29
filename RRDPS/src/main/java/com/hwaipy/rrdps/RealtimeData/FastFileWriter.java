package com.hwaipy.rrdps.RealtimeData;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author Hwaipy
 */
public class FastFileWriter extends OutputStream {

    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 16);

    public FastFileWriter(File file) throws IOException {
        randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.setLength(0);
        fileChannel = randomAccessFile.getChannel();
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put((byte) b);
        if (!buffer.hasRemaining()) {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        buffer.flip();
        fileChannel.write(buffer);
        buffer.flip();
    }

    @Override
    public void close() throws IOException {
        flush();
        randomAccessFile.close();
    }
}
