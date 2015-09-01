package org.backmeup.facebook.storage.keys;

import org.backmeup.facebook.storage.Datatype;

public enum AlbumInfoKey implements SerializerKey {
    
	COUNT("Anzahl der Fotos", Datatype.NUMBER),
	COVER_PHOTO_ID("ID des Covers", Datatype.OTHER),
	CREATED("Erstellungsdatum", Datatype.DATE),
	DESCRIPTION("Beschreibung", Datatype.OTHER),
	ORIGINAL_LINK("Originaler Link", Datatype.LINK),
	PHOTO_DIR("Foto Verzeichnis", Datatype.OTHER),
	PHOTO_INFO("Foto XML", Datatype.OTHER),
	PRIVACY("öffentliche Zugänglichkeit", Datatype.OTHER),
	LAST_UPDATE("Zuletzt bearbeitet", Datatype.DATE),
	DIRECTORY("Verzeichnis", Datatype.OTHER),
	NAME("Name", Datatype.OTHER),
	LOCAL_COUNT("Anzahl der lokalen Fotos", Datatype.NUMBER),
	COMES_FROM("Erstellt von", Datatype.CFT),
	LOCATION("Ort", Datatype.OTHER),
	ID("ID", Datatype.OTHER);

	private String label;
	private Datatype type;

    private AlbumInfoKey(String label, Datatype type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public Datatype getType() {
        return type;
    }

    @Override
    public AlbumInfoKey[] getReduced() {
        AlbumInfoKey[] ret = { NAME, DESCRIPTION, CREATED, LAST_UPDATE, COUNT, COMES_FROM, AlbumInfoKey.LOCATION, ORIGINAL_LINK, LOCAL_COUNT };
        return ret;
    }
}
