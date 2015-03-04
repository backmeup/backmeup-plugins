package org.backmeup.sftp.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.backmeup.model.api.RequiredInputField;
import org.backmeup.sftp.SftpAuthenticator;

public class SftpAuthenticate {
	public static String readLine() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));
			return reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) throws IOException {
		// first authenticate a user to ftp; store his profile as auth.props
		// file
		SftpAuthenticator auth = new SftpAuthenticator();
		Map<String, String> props = new HashMap<String, String>();
		do {
			props.clear();
			for (RequiredInputField input : auth.getRequiredInputFields()) {
				System.out.print("Enter " + input.getName() + ": ");
				String entered = readLine();
				switch (input.getType()) {
				case Bool:
					Boolean.parseBoolean(entered);
					break;
				case Number:
					Integer.parseInt(entered);
					break;
				default:
					break;
				}
				props.put(input.getName(), entered);
			}
		} while (!auth.isValid(props));
		auth.authorize(props);

		for (Map.Entry<String, String> entry : props.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		Properties tmp = new Properties();
		tmp.putAll(props);
		tmp.store(new FileWriter(new File("auth.props")), null);
	}
}
