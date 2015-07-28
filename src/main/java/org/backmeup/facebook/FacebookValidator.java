package org.backmeup.facebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.backmeup.facebook.files.PropertyOption;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.spi.Validationable;

public class FacebookValidator implements Validationable
{

	@Override
	public boolean hasRequiredProperties()
	{
		return true;
	}

	@Override
	public List<RequiredInputField> getRequiredProperties()
	{
		ArrayList<RequiredInputField> ret = new ArrayList<>();
		ret.add(new RequiredInputField(PropertyOption.ACCESS_TOKEN.toString(), "Accesstoken", "Is required to acces to a facebookprofile", true, 0, Type.String, "Enter your Accestoken here"));
		ret.add(new RequiredInputField(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString(), "Albums maximum", "Is to limit the photos per album to download; -1 for unlimited", true, 0, Type.Number, "-1"));
		ret.add(new RequiredInputField(PropertyOption.REDUCED_INFOS.toString(), "Reduced infos", "Generate view with reduced infos; extended infos only recommend for debugging", true, 0, Type.Bool, Boolean.TRUE.toString()));
		return ret;
	}

	@Override
	public ValidationNotes validateProperties(Map<String, String> properties)
	{
		return null;
	}

	@Override
	public boolean hasAvailableOptions()
	{
		return false;
	}

	@Override
	public List<String> getAvailableOptions(Map<String, String> authData)
	{
		return null;
	}

	@Override
	public ValidationNotes validateOptions(List<String> options)
	{
		return null;
	}

}
