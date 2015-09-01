package org.backmeup.facebook.storage.keys;

import java.util.ArrayList;
import java.util.Arrays;

import org.backmeup.facebook.storage.Datatype;

public enum PostInfoKey implements SerializerKey {
    
	ID("ID", Datatype.OTHER),
	ACTION("Aktionen", Datatype.LIST),
	ADMIN("Administrator", Datatype.NFT),
	APPLICATION("Benutzte Anwendung", Datatype.NFT),
	ATTRIBUTION("Benutzte Anwendung", Datatype.OTHER),
	CAPTION("Link", Datatype.LINK),
	COMMENTS("Kommentare", Datatype.OTHER),
	COMMENTS_COUNT("Anzahl der Kommentare", Datatype.NUMBER),
	CREATED_TIME("Erstellungsdatum", Datatype.DATE),
	DESCRIPTION("Beschreibung", Datatype.OTHER),
	FROM("Erstellt von", Datatype.CFT),
	ICON("Icon", Datatype.OTHER),
	LIKES("Likes", Datatype.OTHER),
	LIKES_COUNT("Likes", Datatype.NUMBER),
	LINK("Link", Datatype.LINK),
	MESSAGE("Nachricht", Datatype.OTHER),
	MESSAGE_TAGS("Tags", Datatype.OTHER),
	OBJECT_ID("Objekt ID", Datatype.OTHER),
	PICTURE("Bild", Datatype.OTHER),
	PLACE("Ort", Datatype.OTHER),
	PRIVACY("Zugriff", Datatype.OTHER),
	PROPERTIES("Einstellungen", Datatype.OTHER),
	SHARES("Geteilt", Datatype.OTHER),
	SHARES_COUNT("Geteilt", Datatype.NUMBER),
	SOURCE("Quelle", Datatype.OTHER),
	STATUS_TYPE("Status", Datatype.OTHER),
	LAST_UPDATE("Zuletzt aktualisiert", Datatype.DATE),
	WITH_TAGS("Tags", Datatype.LIST);

    private String label;
    private Datatype type;

    private PostInfoKey(String label, Datatype type) {
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
    public PostInfoKey[] getReduced() {
        ArrayList<PostInfoKey> ret = new ArrayList<>(Arrays.asList(values()));
        ret.remove(ID);
        ret.remove(PICTURE);
        ret.remove(ICON);
        return ret.toArray(new PostInfoKey[ret.size()]);
    }
}
