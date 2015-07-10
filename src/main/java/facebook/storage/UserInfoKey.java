package facebook.storage;

public enum UserInfoKey implements SerializerKey
{
	ABOUT("Allgemein", Datatype.OTHER),
	GENDER("Geschlecht", Datatype.OTHER),
	DATE_OF_BIRTH("Geburtstag", Datatype.DATE),
	FIRST_NAME("Vorname", Datatype.OTHER),
	LAST_NAME("Nachname", Datatype.OTHER),
	METADATA("Metadaten", Datatype.OTHER),
	PROFILE_PICTURE("Profilbild", Datatype.OTHER),
	WEBSITE("Website", Datatype.LINK),
	ORIGINAL_LINK("Originaler Link", Datatype.LINK),
	LOCALE("Land", Datatype.OTHER),
	LANGUAGES("Sprachen", Datatype.LIST),
	LIFE_HISTORY("Lebenslauf", Datatype.OTHER),
	HOME_TOWN("Heimatstadt", Datatype.OTHER),
	ID("ID", Datatype.OTHER),
	ALBUMS("Alben", Datatype.LIST),
	PICTURES("Bilder", Datatype.LIST),
	PHOTOS("Fotos", Datatype.LIST),
	AGE_RAND("Ansprechendes Alter", Datatype.OTHER),
	BIO("Bio", Datatype.OTHER),
	CURRENCY("Währung", Datatype.OTHER),
	EDUCATION("Ausbildung", Datatype.LIST),
	EMAIL("EMail", Datatype.OTHER),
	INTERESTED_IN("Hobbies", Datatype.LIST),
	MEETING_FOR("Treffen", Datatype.LIST),
	POLITICAL("Politik", Datatype.OTHER),
	QUOTES("Anführungszeichen", Datatype.OTHER),
	RELATIONSHIP_STATUS("Beziehungsstatus", Datatype.OTHER),
	RELIGION("Religion", Datatype.OTHER),
	SIGNIFICANT_OTHER("Andere Merkmale", Datatype.NFT),
	SPORTS("Sportarten", Datatype.LIST),
	TIMEZONE("Zeitzone", Datatype.NUMBER),
	LAST_UPDATED("Zuletzt aktualisiert", Datatype.DATE),
	THIRD_PARTY_ID("Drittanbieter ID", Datatype.OTHER),
	TOKEN_FOR_BUISSNESS("ID für andere Unternehmen", Datatype.OTHER),
	VERIFIED("Bestätigt", Datatype.OTHER),
	WORK("Arbeit", Datatype.LIST),
	FAVOURITE_ATHLETS("Lieblings Sportler", Datatype.LIST),
	FAVOURITE_TEAMS("Lieblingsteams", Datatype.LIST),
	NAME("Name", Datatype.OTHER),
	MIDDLE_NAME("Mittelname", Datatype.OTHER);

	private String label;
	private Datatype type;

	private UserInfoKey(String label, Datatype type)
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
