package facebook.storage.keys;

import facebook.storage.Datatype;

public enum PhotoInfoKey implements SerializerKey
{
	BACK_DATE("Erstellt", Datatype.DATE),
	COMMENT_DIR("Kommentar Ordner", Datatype.OTHER),
	COMMENT_INFO_FILENAME("Kommentar xml Position in Kommentarordner", Datatype.OTHER),
	PUBLISH_DATE("Veröffentlicht", Datatype.DATE),
	LIKES("Likes", Datatype.NUMBER),
	LIKES_FROM_PEOPLE("Likes", Datatype.LIST),
	LOCATION("Ort", Datatype.OTHER),
	ORIGINAL_LINK("Originaler Link", Datatype.LINK),
	PLACE("Ort", Datatype.OTHER),
	LAST_UPDATE("Zuletzt bearbeitet", Datatype.DATE),
	FILE("Datei", Datatype.OTHER),
	ID("ID", Datatype.OTHER),
	FROM("gepostet von", Datatype.CFT),
	TAGS("Tags", Datatype.LIST),
	HEIGHT("Höhe", Datatype.NUMBER),
	ICON("Icon", Datatype.OTHER),
	IMAGES("Größen", Datatype.LIST),
	NAME("Name", Datatype.OTHER),
	PICTURE("Verkleinertes Bild", Datatype.OTHER),
	WIDTH("Breite", Datatype.NUMBER);

	private String label;
	private Datatype type;

	private PhotoInfoKey(String label, Datatype type)
	{
		this.label = label;
		this.type = type;
	}

	public String getLabel()
	{
		return label;
	}

	public Datatype getType()
	{
		return type;
	}

	@Override
	public PhotoInfoKey[] getReduced()
	{
		PhotoInfoKey[] ret = { PhotoInfoKey.NAME, LIKES, LIKES_FROM_PEOPLE, PLACE, TAGS, PhotoInfoKey.LOCATION, PhotoInfoKey.PUBLISH_DATE, PhotoInfoKey.FROM, PhotoInfoKey.IMAGES, ORIGINAL_LINK };
		return ret;
	}
}
