package org.backmeup.mail.core;

public class TextContent {
    private String text;
    private boolean isHtml;
    private String charset;

    public TextContent() {

    }

    public TextContent(String text, boolean isHtml, String charset) {
        super();
        this.text = text;
        this.isHtml = isHtml;
        this.charset = charset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
