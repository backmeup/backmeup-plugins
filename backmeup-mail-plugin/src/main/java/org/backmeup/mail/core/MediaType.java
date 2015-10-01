package org.backmeup.mail.core;

public final class MediaType {
    private MediaType() {
        // Utility classes should not have public constructor
    }
    
    public final static String TEXT = "text/*";
    public final static String TEXT_PLAIN = "text/plain";
    public final static String TEXT_HTML = "text/html";
    
    public final static String MULTIPART = "multipart/*";
    public final static String MULTIPART_ALTERNATIVE = "multipart/alternative";
    
    public final static String MESSAGE_RFC822 = "message/rfc822";
}
