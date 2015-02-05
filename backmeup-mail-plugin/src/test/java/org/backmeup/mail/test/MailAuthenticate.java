package org.backmeup.mail.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.backmeup.mail.MailAuthenticator;

public class MailAuthenticate {
  public static String readLine() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      return reader.readLine();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }
  
	public static void main(String[] args) throws IOException {
		// first authenticate a user to mail; store his profile as auth.props file 
		MailAuthenticator auth = new MailAuthenticator();
		Map<String, String> props = new HashMap<>();
		do {
  		props.clear();
//  		for (String input : auth.getRequiredInputFields()) {
//  		  System.out.print("Enter " + input + ": ");
//  		  String entered = readLine();
//  		  switch (auth.getTypeMapping().get(input)) {
//  		    case Bool:
//            Boolean.parseBoolean(entered);
//            break;
//    		  case Number:
//    		    Integer.parseInt(entered);
//    		    break;  		  
//    		  default:
//            break;
//  		  }
//  		  props.setProperty(input, entered);
//  		}	
		}
		while (!auth.isValid(props));
		auth.authorize(props);
		for (Entry<String, String> entry: props.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
//		props.store(new FileWriter(new File("auth.props")), null);
	}
}
