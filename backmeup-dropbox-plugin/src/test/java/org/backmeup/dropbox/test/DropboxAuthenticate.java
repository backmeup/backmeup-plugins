package org.backmeup.dropbox.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.backmeup.dropbox.DropboxAuthenticator;

public class DropboxAuthenticate {
	public static void main(String[] args) throws IOException {
		// first authenticate a user to dropbox; store his profile as auth.props file 
		DropboxAuthenticator auth = new DropboxAuthenticator();
		Map<String, String> props = new HashMap<>();
		props.put("callback", "http://www.localhost.at:9998");
		String url = auth.createRedirectURL(props, "http://www.localhost.at:9998");
		System.out.println(url);
		System.out.print("key: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String code = reader.readLine();
		props.put("code", code);
		auth.authorize(props);
		for (Entry<String, String> entry: props.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
}
