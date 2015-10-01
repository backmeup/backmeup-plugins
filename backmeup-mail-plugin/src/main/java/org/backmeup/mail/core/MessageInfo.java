package org.backmeup.mail.core;

import java.util.Date;

public class MessageInfo {
    private String fileName;
    private String subject;
    private String from;
    private String to;
    private String sentAt;
    private Date receivedAt;

    public MessageInfo(String fileName, String subject, String from, String to, String sentAt, Date receivedAt) {
        this.fileName = fileName;
        this.subject = subject;
        this.from = from;
        this.to = to;
        this.sentAt = sentAt;
        this.receivedAt = receivedAt;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSentAt() {
        return this.sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public Date getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }
}
