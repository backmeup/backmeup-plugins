package facebook.storage.keys;

import facebook.storage.Datatype;

public enum GroupInfoKey implements SerializerKey
{
	ID("ID", Datatype.OTHER),
	DESCRIPTION("Beschreibung", Datatype.OTHER),
	ICON("Gruppenbild", Datatype.OTHER),
	LINK("Originaler Link", Datatype.LINK),
	OWNER("Ersteller", Datatype.NFT),
	PRIVACY("Zugänglichkeit", Datatype.OTHER),
	LAST_UPDATE("Zuletzt bearbeitet", Datatype.DATE),
	VENUE("Venue", Datatype.OTHER),
	METATDATA("Metadaten", Datatype.OTHER),
	NAME("Name", Datatype.OTHER);

	private String label;
	private Datatype type;

	private GroupInfoKey(String label, Datatype type)
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
