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
        login, register
    }

    private final static String serverUrl = "http://restfind.heliohost.org/";
    private final static String logTag = "Database";

    public static boolean register(String username, String password) {
        List<String> result = execute(Operation.register, username, password);
        return false;
    }

    public static boolean login(String username, String password) {
        List<String> result = execute(Operation.login, username, password);

        if(result == null)
            return false;
        if (result.size() == 1) {
            Log.v(logTag, "Login successful");
            return true;
        } else {
            Log.v(logTag, "Login failed");
            return false;
        }
    }

    private static List<String> execute(Operation operation, String... arguments) {
        try {
            StringBuilder sbUrl = new StringBuilder(serverUrl);
            sbUrl.append(operation.toString());
            sbUrl.append(".php");
            Log.v(logTag, "Connecting to: " + sbUrl.toString());

            StringBuilder sbData = new StringBuilder();
            if (arguments.length > 0) {
                sbData.append(URLEncoder.encode("arg0", "UTF-8") + "=" + URLEncoder.encode(arguments[0], "UTF-8"));
            }
            for (int i = 1; i < arguments.length; i++) {
                sbData.append("&" + URLEncoder.encode("arg" + i, "UTF-8") + "=" + URLEncoder.encode(arguments[i], "UTF-8"));
            }

            URL url = new URL(sbUrl.toString());
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(sbData.toString());
            wr.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            List<String> result = new ArrayList<>();
            String line;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                Log.v(logTag, line);
                result.add(line);
            }
            return result;
        } catch (Exception e) {
            Log.e(logTag, Log.getStackTraceString(e));
            return null;
        }
    }

}
