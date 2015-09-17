package org.backmeup.facebook.utils;

public class CustomStringBuilder {
    private StringBuilder sb;
    private String seperator;
    private boolean first = true;

    public CustomStringBuilder(String seperator) {
        this.seperator = seperator;
        this.sb = new StringBuilder();
    }

    public void append(String string) {
    	if (!this.first) {
    		this.sb.append(this.seperator);
    	} else {
    		this.first = false;
    	}
        this.sb.append(string);
    }

    public String getSeparator() {
        return seperator;
    }

    public void empty() {
        this.sb = new StringBuilder();
        this.first = true;
    }

    public String toString() {
        return this.sb.toString();
    }
}
