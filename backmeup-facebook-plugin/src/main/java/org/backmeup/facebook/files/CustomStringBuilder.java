package org.backmeup.facebook.files;

public class CustomStringBuilder {
    private StringBuilder sb;
    private String regex;

    public CustomStringBuilder(String regex) {
        this.regex = regex;
        this.sb = new StringBuilder();
    }

    public void append(String string) {
        this.sb.append(string);
        this.sb.append(getRegex());
    }

    public String getRegex() {
        return regex;
    }

    public void empty() {
        this.sb = new StringBuilder();
    }

    @Override
    public String toString() {
        return this.sb.toString();
    }
}
