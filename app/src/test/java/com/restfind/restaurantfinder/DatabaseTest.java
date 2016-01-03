package com.restfind.restaurantfinder;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This is the JUnit test class for the database class.
 * Created by gabriel on 04.12.15.
 */
public class DatabaseTest {

    // the user "tester6" must not exist, before executing this test
    @Test
    public void register() throws IOException {
        boolean actual = Database.register("Max", "Max");
        assertEquals(true, actual);

        actual = Database.register("Pia", "Pia");
        assertEquals(true, actual);

        actual = Database.register("Alex", "Alex");
        assertEquals(true, actual);

        actual = Database.register("Alex", "Alex");
        assertEquals(false, actual);

        actual = Database.register("Alex", "Pia");
        assertEquals(false, actual);

        actual = Database.register("Alex", "irgendwos");
        assertEquals(false, actual);
    }

    @Test
    public void login() throws IOException {
        boolean actual = Database.login("Max", "Max");
        assertEquals(true, actual);

        actual = Database.login("Pia", "Pia");
        assertEquals(true, actual);

        actual = Database.login("Max", "Pia");
        assertEquals(false, actual);

        actual = Database.login("Max", "irgendows");
        assertEquals(false, actual);

        actual = Database.login("User", "Max");
        assertEquals(false, actual);

        actual = Database.login("User", "User");
        assertEquals(false, actual);

        actual = Database.login("Pia", "pia");
        assertEquals(false, actual);

        actual = Database.login("pia", "Pia");
        assertEquals(false, actual);

        actual = Database.login("pia", "pia");
        assertEquals(false, actual);
    }


    // Tests for sending and getting friend requests.
    // The table "Friend_Invite" has to be truncated before executing
    // this test.
    @Test
    public void sendFriendInvite() throws IOException {
        assertEquals(true, Database.sendFriendInvite("Max", "Pia"));
        assertEquals(false, Database.sendFriendInvite("max", "Pia"));
        assertEquals(false, Database.sendFriendInvite("Max", "pia"));
        assertEquals(false, Database.sendFriendInvite("max", "pia"));
        assertEquals(false, Database.sendFriendInvite("Max", "Pia"));
        assertEquals(false, Database.sendFriendInvite("Pia", "Max"));
        assertEquals(false, Database.sendFriendInvite("Max", "User"));
        assertEquals(false, Database.sendFriendInvite("User", "Pia"));
        assertEquals(true, Database.sendFriendInvite("Alex", "Max"));
        assertEquals(true, Database.sendFriendInvite("Pia", "Alex"));
        assertEquals(false, Database.sendFriendInvite("Pia", "Pia"));
    }

    // The test send FriendInvite has to be executed and passed before this test.
    @Test
    public void getFriendInvite() throws IOException {
        List<String> actual = Database.getFriendInvites("Max");
        assertEquals(0, actual.size());

        actual = Database.getFriendInvites("Pia");
        assertEquals(1, actual.size());

        actual = Database.getFriendInvites("Alex");
        assertEquals(2, actual.size());

        actual = Database.getFriendInvites("User");
        assertEquals(0, actual.size());
    }

    // The test send FriendInvite has to be executed and passed before this test.
    @Test
    public void acceptFriendInvite() throws IOException {
        assertEquals(false, Database.acceptFriendInvite("pia", "Max"));
        assertEquals(false, Database.acceptFriendInvite("Pia", "max"));
        assertEquals(false, Database.acceptFriendInvite("pia", "max"));
        assertEquals(false, Database.acceptFriendInvite("Max", "Pia"));
        assertEquals(true, Database.acceptFriendInvite("Pia", "Max"));
        assertEquals(false, Database.acceptFriendInvite("Pia", "Max"));
        assertEquals(false, Database.acceptFriendInvite("User1", "User2"));
        assertEquals(true, Database.acceptFriendInvite("Max", "Alex"));
        assertEquals(true, Database.acceptFriendInvite("Alex", "Pia"));
        assertEquals(false, Database.acceptFriendInvite("Pia", "Pia"));
    }

    // The test "sendFriendInvite" has to be executed and passed before this test.
    @Test
    public void declineFriendInvite() throws IOException {
        assertEquals(true, Database.declineFriendInvite("Alex", "Pia"));
        assertEquals(true, Database.declineFriendInvite("Alex", "Max"));
        assertEquals(true, Database.declineFriendInvite("Pia", "Max"));
        assertEquals(false, Database.declineFriendInvite("Pia", "Pia"));
    }

    // The test send acceptFriendInvite has to be executed and passed before this test.
    @Test
    public void deleteFriend() throws IOException {
        assertEquals(true, Database.deleteFriend("Pia", "Max"));
        assertEquals(true, Database.deleteFriend("Pia", "Max"));
        assertEquals(true, Database.deleteFriend("max", "alex"));
        assertEquals(true, Database.deleteFriend("Max", "alex"));
        assertEquals(true, Database.deleteFriend("max", "Alex"));
        assertEquals(true, Database.deleteFriend("Max", "Alex"));
        assertEquals(true, Database.deleteFriend("Pia", "Alex"));
    }

    @Test
    public void getFriends() throws IOException {
        List<String> actual = Database.getFriends("tester");
        assertEquals(0, actual.size());

        actual = Database.getFriends("Max");
        assertEquals(2, actual.size());
        assertEquals(true, actual.contains("Pia"));
        assertEquals(true, actual.contains("Alex"));

        actual = Database.getFriends("Pia");
        assertEquals(2, actual.size());
        assertEquals(true, actual.contains("Max"));
        assertEquals(true, actual.contains("Alex"));

        actual = Database.getFriends("Alex");
        assertEquals(2, actual.size());
        assertEquals(true, actual.contains("Pia"));
        assertEquals(true, actual.contains("Max"));
    }

    @Test
    public void favorite() throws IOException {
        // favorite
        boolean actual = Database.favorite("Max", "loc1");
        assertEquals(true, actual);

        actual = Database.favorite("Max", "loc1");
        assertEquals(false, actual);

        actual = Database.favorite("max", "loc2");
        assertEquals(false, actual);

        actual = Database.favorite("Max", "loc2");
        assertEquals(true, actual);

        actual = Database.favorite("Max", "loc3");
        assertEquals(true, actual);


        // cleanup
        actual = Database.deleteFavorite("Max", "loc1");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("Max", "loc2");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("Max", "loc3");
        assertEquals(true, actual);
    }

    // this test include adding, getting and deleting favorite restaurants
    @Test
    public void getFavorites() throws IOException {
        List<String> result = Database.getFavorites("Pia");
        assertEquals(0, result.size());

        List<String> expected =  new ArrayList<>();
        expected.add("place01");
        expected.add("place02");
        expected.add("place03");

        assertEquals(true, Database.favorite("Pia", expected.get(0)));
        assertEquals(true, Database.favorite("Pia", expected.get(1)));
        assertEquals(true, Database.favorite("Pia", expected.get(2)));

        List<String> actual = Database.getFavorites("Pia");
        assertEquals(expected.size(), actual.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }

        assertEquals(true, Database.deleteFavorite("Pia", expected.get(0)));
        assertEquals(true, Database.deleteFavorite("Pia", expected.get(1)));
        assertEquals(true, Database.deleteFavorite("Pia", expected.get(2)));
    }

    @Test
    public void updateUserLocation() throws IOException {
        double latitude = 48.287596d;
        double longitude = 14.294394d;

        assertEquals(true, Database.updateUserLocation("Max", "10.0", "12.0"));
        assertEquals(true, Database.updateUserLocation("Max", String.valueOf(latitude), String.valueOf(longitude)));
    }

    @Test
    public void getUserLocations() throws IOException {
        List<String> actual = Database.getUserLocations("1");

        List<String> expected = new ArrayList<>();
        expected.add("Max;48.287596;14.294394");
        expected.add("Pia;20;30");

        for(String s : expected) {
            assertEquals(true, actual.contains(s));
        }
    }
}
