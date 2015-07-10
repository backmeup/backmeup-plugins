package facebook.storage;

public enum PhotoInfoKey implements SerializerKey
{
	BACK_DATE("Erstellt", Datatype.DATE),
	COMMENT_DIR("Kommentar Ordner", Datatype.OTHER),
	PUBLISH_DATE("Veröffentlicht", Datatype.DATE),
	LIKES("Likes", Datatype.NUMBER),
	LIKES_FROM_PEOPLE("Likes", Datatype.LIST),
	ORIGINAL_LINK("Originaler Link", Datatype.LINK),
	PLACE("Ort", Datatype.OTHER),
	LAST_UPDATE("Zuletzt bearbeitet", Datatype.DATE),
	FILE("Datei", Datatype.OTHER),
	ID("ID", Datatype.OTHER),
	FROM("gepostet von",Datatype.CFT),
	TAGS("Tags",Datatype.LIST);

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
}
