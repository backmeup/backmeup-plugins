package org.backmeup.plugin.api.actions.encryption;

import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.connectors.BaseActionDescribable;

public class EncryptionDescribable extends BaseActionDescribable {
	public EncryptionDescribable() {
		super("encryption.properties");
	}

	@Override
	public List<String> getAvailableOptions() {
		List<String> options = new LinkedList<>();
		options.add("org.backmeup.encryption.password");
		return options;
	}
}
