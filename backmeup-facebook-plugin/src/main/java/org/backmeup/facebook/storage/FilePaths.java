package org.backmeup.facebook.storage;

public enum FilePaths {
    
    USER_FILE("user.xml"),
    ALBUMS_DIRECTORY("albums"),
    ALBUM_DIRECTORY(ALBUMS_DIRECTORY + "/" + ReplaceID.ALBUM_ID),
    ALBUM_INFO(ALBUM_DIRECTORY + "/albuminfo.xml"),
    PHOTO_INFO(ReplaceID.PHOTO_ID + ".xml");

    private String path;

    private FilePaths(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String getPath() {
        return path;
    }
}
