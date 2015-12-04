package com.restfind.restaurantfinder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabriel on 04.12.15.
 */

public class Database {

    private enum Operation {
        login, register, getFriendInvite
    }

    private final static String serverUrl = "http://restfind.heliohost.org/";

    public static boolean register(String username, String password) {
        List<String> result = execute(Operation.register, username, password);
        return false;
    }

    public static boolean login(String username, String password) {
        List<String> result = execute(Operation.login, username, password);

        if(result == null)
            return false;
        if (result.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> getFriendInvite(String username) {
        return execute(Operation.getFriendInvite, username);
    }

    private static List<String> execute(Operation operation, String... arguments) {
        try {
            // erzeuge URL zur *.php Datei auf Server
            StringBuilder sbUrl = new StringBuilder(serverUrl);
            sbUrl.append(operation.toString());
            sbUrl.append(".php");

            // Parameter (arguments) werden zu einem String zusammen gefügt, der später
            // an PHP gesendet wird.
            StringBuilder sbData = new StringBuilder();
            if (arguments.length > 0) {
                sbData.append(URLEncoder.encode("arg0", "UTF-8") + "=" + URLEncoder.encode(arguments[0], "UTF-8"));
            }
            for (int i = 1; i < arguments.length; i++) {
                sbData.append("&" + URLEncoder.encode("arg" + i, "UTF-8") + "=" + URLEncoder.encode(arguments[i], "UTF-8"));
            }

            // Verbindung zu Server wird aufgebaut
            URL url = new URL(sbUrl.toString());
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            // String mit Parametern wird an die PHP Datei als Stream gesendet
            wr.write(sbData.toString());
            wr.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            List<String> result = new ArrayList<>();
            String line;

            // Ergebnis der SQL-Abfrage wird vom Input Stream gelesen
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
