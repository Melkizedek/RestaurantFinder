package com.restfind.restaurantfinder;

import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This static class handles the database access.
 * Created by gabriel on 04.12.15.
 */

public class Database {

    private static final String trueString = "TRUE";
    private static final String falseString = "FALSE";

    private enum Operation {
        login, register,
        getFriendInvites, sendFriendInvite, acceptFriendInvite, declineFriendInvite,
        getFriends, deleteFriend,
        favorite, deleteFavorite, getFavorites,
        updateUserLocation, getUserLocations
    }

    private final static String serverUrl = "http://restfind.heliohost.org/";

    // *********** Methoden für die einzelnen SQL-Statements **************
    public static boolean register(String username, String password) throws IOException {
        return checkResult(Operation.register, username, password);
    }

    public static boolean login(String username, String password) throws IOException {
        return checkResult(Operation.login, username, password);
    }

    public static List<String> getFriendInvites(String username) throws IOException {
        return execute(Operation.getFriendInvites, username);
    }

    public static boolean sendFriendInvite(String username, String invitedUser) throws IOException {
        if(username.equals(invitedUser))
            return false;
        return checkResult(Operation.sendFriendInvite, username, invitedUser);
    }

    public static boolean acceptFriendInvite(String username, String invitor) throws IOException {
        return checkResult(Operation.acceptFriendInvite, username, invitor);
    }

    public static boolean declineFriendInvite(String username, String invitor) throws IOException {
        return checkResult(Operation.declineFriendInvite, username, invitor);
    }

    public static boolean deleteFriend(String username, String friend) throws IOException {
        return checkResult(Operation.deleteFriend, username, friend);
    }

    public static List<String> getFriends(String username) throws IOException {
        return execute(Operation.getFriends, username);
    }

    public static boolean favorite(String username, String locationID) throws IOException {
        return checkResult(Operation.favorite, username, locationID);
    }

    public static List<String> getFavorites(String username) throws IOException {
        return execute(Operation.getFavorites, username);
    }

    public static boolean deleteFavorite(String username, String locationID) throws IOException {
        return checkResult(Operation.deleteFavorite, username, locationID);
    }

    public static boolean updateUserLocation(String username, String latitude, String longitude) throws IOException{
        return checkResult(Operation.updateUserLocation, username, latitude, longitude);
    }

    // Rückgabewert ist eine Liste mit Strings im folgenden Format: "Username;Latitude;Longitude"
    public static List<String> getUserLocations(String invitation_id) throws IOException {
        return execute(Operation.getUserLocations, invitation_id);
    }

    // Diese Methode überprüft, ob das Einfügen von Daten in die Tabelle oder das Löschen
    // von Daten aus der Tabelle erfolgreich ist.
    private static boolean checkResult(Operation operation, String... arguments) throws IOException {
        List<String> result = Database.execute(operation, arguments);
        return !(result == null || result.isEmpty()) && result.get(0).equals(trueString);
    }

    // Diese Methode führt die jeweilige .php Datei aus, die den Zugriff auf die Datenbank
    // ausführt. Dazu müssen die Parameter für das SQL Statement mit übergeben werden.
    // Das Ergebnis wird als eine Liste aus Strings zurückgegeben, wobei ein String für
    // einen Datensatz in der Tabelle steht. Die Spalten einer Tabelle sind mittels ";"
    // im String geteilt. Bei Insert und Delete SQL Statements beinhaltet die Liste nur
    // einen String, der die beiden Werte "TRUE" oder "FALSE" haben kann.
    private static List<String> execute(Operation operation, String... arguments) throws IOException {
        // erzeuge URL zur *.php Datei auf Server
        String urlString = serverUrl + operation.toString() + ".php";

        // Parameter (arguments) werden zu einem String zusammen gefügt, der später
        // an PHP gesendet wird.
        String data = "";
        if (arguments.length > 0) {
            data = data + URLEncoder.encode("arg0", "UTF-8") +
                    "=" +
                    URLEncoder.encode(arguments[0], "UTF-8");
        }
        for (int i = 1; i < arguments.length; i++) {
              data = data + "&" + URLEncoder.encode("arg"+i, "UTF-8")+
                    "="+
                    URLEncoder.encode(arguments[i], "UTF-8");
        }

        // Verbindung zu Server wird aufgebaut
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

        // String mit Parametern wird an die PHP Datei als Stream gesendet
        wr.write(data);
        wr.close();

        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader reader = new BufferedReader(isr);

        List<String> result = new ArrayList<>();
        String line;

        // Ergebnis der SQL-Abfrage wird vom Input Stream gelesen
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }

        // BufferedReader und InputStreamWriter werden geschlossen
        reader.close();
        isr.close();

        return result;
    }
}
