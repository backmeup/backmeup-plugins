package org.backmeup.facebook.storage.keys;

import org.backmeup.facebook.storage.Datatype;

public enum CommentKey implements SerializerKey {
    
	ATTACHMENT("Bild", Datatype.PHOTO),
	CAN_REMOVE("Kann entfernt werden", Datatype.OTHER),
	REPLIES_COUNT("Antwortenanzahl", Datatype.NUMBER),
	CREATED("Erstellt", Datatype.DATE),
	FROM("Von", Datatype.CFT),
	HIDDEN("Versteckt", Datatype.OTHER),
	LIKE_COUNT("Likes", Datatype.NUMBER),
	MESSAGE("Nachricht", Datatype.OTHER),
	ID("ID", Datatype.OTHER),
	METADATA("Metadaten", Datatype.OTHER);

    private String label;
    private Datatype type;

    private CommentKey(String label, Datatype type) {
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
    public CommentKey[] getReduced() {
        CommentKey[] ret = { FROM, MESSAGE, CommentKey.LIKE_COUNT, CommentKey.REPLIES_COUNT, CREATED };
        return ret;
    }
}
