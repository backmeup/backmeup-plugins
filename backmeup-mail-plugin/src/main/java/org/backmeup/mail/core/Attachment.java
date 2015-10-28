package org.backmeup.mail.core;

import java.io.InputStream;

public class Attachment {
    private InputStream stream;
    private String filename;

    public Attachment() {

    }

    public Attachment(InputStream stream, String filename) {
        super();
        this.stream = stream;
        this.filename = filename;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
