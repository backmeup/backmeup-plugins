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
	PHOTOS("Fotos", false, false);

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
