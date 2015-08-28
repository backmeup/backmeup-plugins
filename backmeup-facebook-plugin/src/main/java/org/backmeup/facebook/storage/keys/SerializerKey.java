package org.backmeup.facebook.storage.keys;

import org.backmeup.facebook.storage.Datatype;

public interface SerializerKey
{
	public String getLabel();

	public Datatype getType();

	public SerializerKey[] getReduced();
}
