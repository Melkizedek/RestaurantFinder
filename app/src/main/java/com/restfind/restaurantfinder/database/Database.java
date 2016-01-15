package com.restfind.restaurantfinder.database;

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
 */

public class Database {

    /**
     * constant string for the boolean return value "true" of the php program
     */
    private static final String trueString = "TRUE";
    /**
     * constant string for the boolean return value "false" of the php program
     */
    private static final String falseString = "FALSE";


    /**
     * The available database operations.
     */
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

    /**
     * url to server
     */
    private final static String serverUrl = "http://restfind.heliohost.org/";

    // *********** methods for the database operations **************
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

    public static List<String> getUserLocations(String invitation_id) throws IOException {
        return execute(Operation.getUserLocations, invitation_id);
    }

    public static boolean deleteUserLocation(String username) throws IOException {
        return checkResult(Operation.deleteUserLocation, username);
    }

    public static boolean createInvitation(String host, String placeID, Timestamp dateTime, List<String> invitees) throws IOException {
        List<String> result = Database.execute(Operation.createInvitation, host, placeID, dateTime.toString());

        return !(result == null || result.isEmpty() || result.get(0).equals(falseString)) && sendInvitationInvite(result.get(0), invitees);
    }

    public static boolean sendInvitationInvite(String invitationID, List<String> invitees) throws IOException {
        if(invitees == null || invitees.isEmpty())
            return false;

        String[] arguments = new String[invitees.size() + 1];
        arguments[0] = invitationID;
        int i = 1;

        // filling the array with the arguments (friends to invite)
        for(String s : invitees) {
            arguments[i] = s;
            i++;
        }

        Database.execute(Operation.sendInvitationInvite, arguments);
        return true;
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

                curInvitees = new HashMap<>();
            }
            if(split[4].equals(username)){
                if(split[6].equals("1")) {
                    curInvitation.setReceived(true);
                }else{
                    curInvitation.setReceived(false);
                }
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


    /**
     * This method checks the success of an insert, update or delete operation.
     * @param operation the database operation
     * @param arguments the parameters for the php program
     * @return true, if the execution of the database operation was successful, otherwise false
     * @throws IOException
     */
    private static boolean checkResult(Operation operation, String... arguments) throws IOException {
        List<String> result = Database.execute(operation, arguments);
        return !(result == null || result.isEmpty()) && result.get(0).equals(trueString);
    }

    // The execute method starts the php program, which is defined by the operation parameter,
    // on the server. The php program does the actual access to the database.

    /**
     * Executes the database operation by starting the corresponding php program on the server.
     * @param operation the database operation
     * @param arguments the parameter for the php program (respectively the values for the
     *                  sql statements)
     * @return list of strings, which represents lines of the result of the sql query (columns
     * are seperated by semicolon)
     * @throws IOException is thrown, if there is no connection to the server
     */
    private static List<String> execute(Operation operation, String... arguments) throws IOException {
        // concatenate the server url and the .php filename together
        String urlString = serverUrl + operation.toString() + ".php";

        // preparing the arguments as parameters for the php program
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

        // connect to url
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);

        // writing the parameter string to the stream
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.close();

        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader reader = new BufferedReader(isr);

        List<String> result = new ArrayList<>();
        String line;

        // reading result from the php program (result from sql-statement)
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }

        reader.close();
        isr.close();

        return result;
    }
}
