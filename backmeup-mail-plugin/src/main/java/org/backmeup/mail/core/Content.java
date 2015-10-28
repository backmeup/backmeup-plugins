package org.backmeup.mail.core;

import java.io.InputStream;

public class Content {
    private String contentId;
    private String filename;
    private InputStream content;

    public Content() {

    }

    public Content(String contentId, String filename, InputStream content) {
        this.contentId = contentId;
        this.filename = filename;
        this.content = content;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }
}
