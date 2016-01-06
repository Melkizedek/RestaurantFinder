package com.restfind.restaurantfinder.database;

import android.util.Log;

import com.restfind.restaurantfinder.assistant.Invitation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        updateUserLocation, getUserLocations, deleteUserLocation,
        createInvitation, sendInvitationInvite,
        acceptInvitation, declineInvitation,
        getInvitations, receivedInvitation
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
//        if(username.equals(invitedUser))
//            return false;
//        return checkResult(Operation.sendFriendInvite, username, invitedUser);
        return !username.equals(invitedUser) && checkResult(Operation.sendFriendInvite, username, invitedUser);
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

    public static boolean deleteUserLocation(String username) throws IOException {
        return checkResult(Operation.deleteUserLocation, username);
    }

    public static boolean createInvitation(String host, String placeID, Timestamp dateTime, List<String> invitees) throws IOException {
        List<String> result = Database.execute(Operation.createInvitation, host, placeID, dateTime.toString());

//        if (result == null || result.isEmpty() || result.get(0).equals(falseString))
//            return false;
//
//        return sendInvitationInvite(result.get(0), invitees);
        return !(result == null || result.isEmpty() || result.get(0).equals(falseString)) && sendInvitationInvite(result.get(0), invitees);
    }

    public static boolean sendInvitationInvite(String invitationID, List<String> invitees) throws IOException {
        if(invitees == null || invitees.isEmpty())
            return false;

        String[] arguments = new String[invitees.size() + 1];
        arguments[0] = invitationID;
        int i = 1;

        for(String s : invitees) {
            arguments[i] = s;
            i++;
        }

        Database.execute(Operation.sendInvitationInvite, arguments);
        return true;
        // TODO: Rückgabewert bei sendInvitationInvite.php Programm
        // return checkResult(Operation.sendInvitationInvite, arguments);
    }

    public static boolean acceptInvitation(String id, String username) throws IOException {
        return checkResult(Operation.acceptInvitation, id, username);
    }

    public static boolean declineInvitation(String id, String username) throws IOException {
        return checkResult(Operation.declineInvitation, id, username);
    }

    public static List<Invitation> getInvitations(String username) throws IOException {
        List<String> result = execute(Operation.getInvitations, username);
        List<Invitation> invitations = new ArrayList<>();
        Map<String, Integer> curInvitees = null;
        Invitation curInvitation = null;
        String curId = "";

        for(String s : result){
            //0: id, 1: host, 2:placeID, 3: time, 4: user, 5: accepted, 6: received
            String[] split = s.split(";");
            if(!split[0].equals(curId)){
                if(curInvitation != null){
                    curInvitation.setInvitees(curInvitees);
                    invitations.add(curInvitation);
                }
                curInvitation = new Invitation(Integer.parseInt(split[0]), split[1], split[2], Timestamp.valueOf(split[3]).getTime());

                if(split[4].equals(username)){
                    curInvitation.setReceived(Boolean.getBoolean(split[6]));
                }
                curInvitees = new HashMap<>();
            }
            curInvitees.put(split[4], Integer.parseInt(split[5]));
            curId = split[0];
        }
        curInvitation.setInvitees(curInvitees);
        invitations.add(curInvitation);

        return invitations;
    }

    public static boolean receivedInvitation(int id, String username) throws IOException {
        return checkResult(Operation.receivedInvitation, String.valueOf(id), username);
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
