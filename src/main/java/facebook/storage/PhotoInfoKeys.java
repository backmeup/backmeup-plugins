package facebook.storage;

public enum PhotoInfoKeys implements Data
{
	BACK_DATE("Erstellt", false, true),
	COMMENT_DIR("Kommentar Ordner", false, false),
	PUBLISH_DATE("Veröffentlicht", false, true),
	LIKES("Likes", false, false),
	LIKES_FROM_PEOPLE("Likes", false, false),
	ORIGINAL_LINK("Originaler Link", true, false),
	PLACE("Ort", false, false),
	LAST_UPDATE("Zuletzt bearbeitet", false, true),
	FILE("Datei", false, false),
	ID("ID", false, false);

	private String label;
	private boolean link, date;

	private PhotoInfoKeys(String label, boolean link, boolean date)
	{
		this.label = label;
		this.date = date;
		this.link = link;
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
