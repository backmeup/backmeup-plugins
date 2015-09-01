package org.backmeup.facebook.storage.keys;

import org.backmeup.facebook.storage.Datatype;

public interface SerializerKey {
    String getLabel();

    Datatype getType();

    SerializerKey[] getReduced();
}
