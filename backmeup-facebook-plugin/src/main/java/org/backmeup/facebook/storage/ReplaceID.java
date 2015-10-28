package org.backmeup.facebook.storage;

public enum ReplaceID {

    ALBUM_ID("%album_id"), PHOTO_ID("%photo_id"), USER_ID("%user_id");

    private String id;

    private ReplaceID(String id) {
        this.id = id;
    }

    public String getStringToReplace() {
        return id;
    }

    @Override
    public String toString() {
        return getStringToReplace();
    }
}
