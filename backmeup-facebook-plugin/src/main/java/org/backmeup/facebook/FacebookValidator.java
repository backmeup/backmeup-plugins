package org.backmeup.facebook;

import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.spi.Validationable;

public class FacebookValidator implements Validationable {

    @Override
    public boolean hasRequiredProperties() {
        return false;
    }

    @Override
    public List<RequiredInputField> getRequiredProperties() {
        return null;
    }

    @Override
    public ValidationNotes validateProperties(Map<String, String> properties) {
        return null;
    }

    @Override
    public boolean hasAvailableOptions() {
        return false;
    }

    @Override
    public List<String> getAvailableOptions(Map<String, String> authData) {
        return null;
    }

    @Override
    public ValidationNotes validateOptions(List<String> options) {
        return null;
    }
}
