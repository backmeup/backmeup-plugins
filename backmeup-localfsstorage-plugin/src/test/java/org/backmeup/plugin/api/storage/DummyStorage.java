package org.backmeup.plugin.api.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.filesystem.FileDataObject;

public class DummyStorage implements Storage {

    private List<DataObject> dataObjects = new ArrayList<>();

    public DummyStorage() {
        dataObjects.add(new FileDataObject("src/test/resources/creative-commons.jpg"));
        dataObjects.add(new FileDataObject("src/test/resources/creative-commons.png"));
        dataObjects.add(new FileDataObject("src/test/resources/creative-commons.pdf"));
    }

    @Override
    public void open(String path) {
        // Do nothing - this is just a dummy
    }

    @Override
    public Iterator<DataObject> getDataObjects() {
        return dataObjects.iterator();
    }

    @Override
    public void close() {
        // Do nothing - this is just a dummy
    }

    @Override
    public int getDataObjectCount() {
        return 3;
    }

    @Override
    public boolean existsPath(String path) {
        // Just a dummy
        return false;
    }

    @Override
    public void delete() {
    }

    @Override
    public void addFile(InputStream is, String path, MetainfoContainer metadata) {
    }

    @Override
    public void removeFile(String path) {
    }

    @Override
    public void removeDir(String path) {
    }

    @Override
    public void move(String fromPath, String toPath) {
    }

    @Override
    public long getDataObjectSize() {
        return 500l;
    }
}
