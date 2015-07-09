package facebook.storage;

public enum AlbumInfoKeys implements Data
{
	COUNT("Anzahl der Fotos", false, false),
	COVER_PHOTO_ID("ID des Covers", false, false),
	CREATED("Erstellungsdatum", false, true),
	DESCRIPTION("Beschreibung", false, false),
	ORIGINAL_LINK("Originaler Link", true, false),
	PRIVACY("öffentliche Zugänglichkeit", false, false),
	LAST_UPDATE("Zuletzt bearbeitet", false, true),
	DIRECTORY("Verzeichnis", false, false),
	NAME("Name", false, false),
	LOCAL_COUNT("Anzahl der lokalen Fotos", false,false),
	COMES_FROM("Herkunft",false,false);

	private String label;
	private boolean link, date;

	private AlbumInfoKeys(String label, boolean link, boolean date)
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
