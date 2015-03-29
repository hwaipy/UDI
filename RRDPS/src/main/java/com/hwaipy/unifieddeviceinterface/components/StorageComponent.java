package com.hwaipy.unifieddeviceinterface.components;

import com.hwaipy.unifieddeviceinterface.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Hwaipy
 */
public abstract class StorageComponent implements Component {

    private File file;
    private FileOutputStream outputStream;
    private boolean running = false;

    public void start() throws IOException {
        if (file != null) {
            openFile();
            running = true;
        } else {
            throw new IllegalStateException();
        }
    }

    public void stop() throws IOException {
        if (outputStream != null) {
            closeFile();
            running = false;
        } else {
            throw new IllegalStateException();
        }
    }

    public void setFile(File file) throws IOException {
        if (running) {
            throw new IllegalStateException();
        }
        this.file = file;

    }

    private void openFile() throws IOException {
        outputStream = new FileOutputStream(file);
    }

    private void closeFile() throws IOException {
        outputStream.close();
    }
}
