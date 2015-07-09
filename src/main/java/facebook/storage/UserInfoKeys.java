package facebook.storage;

public enum UserInfoKeys implements Data
{
	ABOUT("Allgemein", false, false),
	GENDER("Geschlecht", false, false),
	DATE_OF_BIRTH("Geburtstag", false, true),
	FIRST_NAME("Vorname", false, false),
	LAST_NAME("Nachname", false, false),
	METADATA("Metadaten", false, false),
	PROFILE_PICTURE("Profilbild", false, false),
	WEBSITE("Website", true, false),
	ORIGINAL_LINK("Originaler Link", true, false),
	LOCALE("Land", false, false),
	LANGUAGES("Sprachen", false, false),
	LIFE_HISTORY("Lebenslauf", false, false),
	HOME_TOWN("Heimatstadt", false, false),
	ID("ID", false, false),
	ALBUMS("Alben", false, false),
	PICTURES("Bilder", false, false),
	PHOTOS("Fotos", false, false),
	AGE_RAND("Ansprechendes Alter",false,false),
	BIO("Bio",false,false),
	CURRENCY("Währung",false,false),
	EDUCATION("Ausbildung",false,false),
	EMAIL("EMail",true,false),
	INTERESTED_IN("Hobbies",false,false),
	MEETING_FOR("Treffen",false,false),
	POLITICAL("Politik",false,false),
	QUOTES("Anführungszeichen",false,false),
	RELATIONSHIP_STATUS("Beziehungsstatus",false,false),
	RELIGION("Religion",false,false),
	SIGNIFICANT_OTHER("Andere Merkmale",false,false),
	SPORTS("Sportarten",false,false),
	TIMEZONE("Zeitzone",false,false),
	LAST_UPDATED("Zuletzt aktualisiert",false,false),
	THIRD_PARTY_ID("Drittanbieter ID",false,false),
	TOKEN_FOR_BUISSNESS("ID für andere Unternehmen",false,false),
	VERIFIED("Bestätigt",false,false),
	WORK("Arbeit",false,false),
	FAVOURITE_ATHLETS("Lieblings Sportler",false,false),
	FAVOURITE_TEAMS("Lieblingsteams",false,false);

	private String label;
	private boolean link, date;

	private UserInfoKeys(String label, boolean link, boolean date)
	{
		this.label = label;
		this.link = link;
		this.date = date;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isLink()
	{
		return link;
	}

	public boolean isDate()
	{
		return date;
	}

}
