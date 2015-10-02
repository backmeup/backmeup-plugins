package org.backmeup.mail.core;

public final class MediaType {
    public static final String TEXT = "text/*";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";
    
    public static final String MULTIPART = "multipart/*";
    public static final String MULTIPART_ALTERNATIVE = "multipart/alternative";
    
    public static final String MESSAGE_RFC822 = "message/rfc822";
    
    private MediaType() {
        // Utility classes should not have public constructor
    }
}
