package facebook.storage.keys;

import facebook.storage.Datatype;

public interface SerializerKey
{
	public String getLabel();

	public Datatype getType();

	public SerializerKey[] getReduced();
}
