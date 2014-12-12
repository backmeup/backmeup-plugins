package org.backmeup.plugin.storage.constants;

public final class Constants {
	private Constants() {
		 // Utility classes should not have a public constructor
	}

	public static final String PROP_CONNECTION_STRING = "connectionString";
	public static final String PROP_CONNECTION_STRING_LABEL = "Connection String. Currently, set user id here (eg. '1')";
	public static final String PROP_CONNECTION_STRING_DEFAULT = "";
	public static final String PROP_CONNECTION_STRING_DESC = "Connection string for a Backmeup-Storage service";
}
