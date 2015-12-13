package com.restfind.restaurantfinder;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This is the JUnit test class for the database class.
 * Created by gabriel on 04.12.15.
 */
public class DatabaseTest {

    @Test
    public void login() throws IOException {
        boolean actual = Database.login("tester", "tests");
        assertEquals(true, actual);

        actual = Database.login("tester", "tests0");
        assertEquals(false, actual);

        actual = Database.login("testerEI", "tests");
        assertEquals(false, actual);

        actual = Database.login("Tester", "tests");
        assertEquals(false, actual);

        actual = Database.login("tester", "Tests");
        assertEquals(false, actual);

        actual = Database.login("tester1", "tests");
        assertEquals(true, actual);
    }

    // the user "tester6" must not exist, before executing this test
    @Test
    public void register() throws IOException {
        boolean actual = Database.register("tester", "irgendows");
        assertEquals(false, actual);

        actual = Database.register("tester6", "tests");
        assertEquals(true, actual);

        actual = Database.register("tester6", "tests1");
        assertEquals(false, actual);
    }

    // Tests for sending and getting friend requests.
    // The table "Friend_Invite" has to be truncated before executing
    // this test.
    @Test
    public void sendFriendInvite() throws IOException {
        List<String> actual = Database.getFriendInvites("tester1");
        assertEquals(0, actual.size());
        assertEquals(true, Database.sendFriendInvite("tester2", "tester1"));
        assertEquals(true, Database.sendFriendInvite("tester3", "tester1"));
        assertEquals(true, Database.sendFriendInvite("tester4", "tester1"));

        actual = Database.getFriendInvites("tester1");
        assertEquals(3, actual.size());

        actual = Database.getFriendInvites("tester2");
        assertEquals(0, actual.size());
    }


    // for this test, tester3 has to have two friends, of which one of them
    // is tester2
    @Test
    public void deleteFriend() throws IOException {
        assertEquals(2, Database.getFriends("tester3").size());
        assertEquals(true, Database.deleteFriend("tester3", "tester2"));
        assertEquals(1, Database.getFriends("tester3").size());
    }

    @Test
    public void getFriends() throws IOException {
        List<String> expected = new ArrayList<>();
        expected.add("tester1");
        expected.add("tester2");
        expected.add("tester3");
        expected.add("tester5");

        List<String> actual = Database.getFriends("tester");

        assertEquals(expected.size(), actual.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEquals(true, actual.contains(expected.get(i)));
        }
    }

    @Test
    public void favorite() throws IOException {
        // favorite
        boolean actual = Database.favorite("tester", "loc1");
        assertEquals(true, actual);

        actual = Database.favorite("tester", "loc1");
        assertEquals(false, actual);

        actual = Database.favorite("testerLol", "loc1");
        assertEquals(false, actual);

        actual = Database.favorite("tester", "loc2");
        assertEquals(true, actual);

        actual = Database.favorite("tester1", "loc1");
        assertEquals(true, actual);

        actual = Database.favorite("tester1", "loc2");
        assertEquals(true, actual);

        actual = Database.favorite("tester2", "loc2");
        assertEquals(true, actual);

        actual = Database.favorite("tester2", "loc1");
        assertEquals(true, actual);

        // deleteFavorite
        actual = Database.deleteFavorite("tester", "loc1");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("tester", "loc1");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("testerLol", "loc1");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("tester", "loc2");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("tester1", "loc1");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("tester1", "loc2");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("tester2", "loc2");
        assertEquals(true, actual);

        actual = Database.deleteFavorite("tester2", "loc1");
        assertEquals(true, actual);
    }

    // this test include adding, getting and deleting favorite restaurants
    @Test
    public void getFavorites() throws IOException {
        List<String> result = Database.getFavorites("tester");
        assertEquals(0, result.size());

        List<String> expected =  new ArrayList<>();
        expected.add("place01");
        expected.add("place02");
        expected.add("place03");

        assertEquals(true, Database.favorite("tester", expected.get(0)));
        assertEquals(true, Database.favorite("tester", expected.get(1)));
        assertEquals(true, Database.favorite("tester", expected.get(2)));

        List<String> actual = Database.getFavorites("tester");
        assertEquals(expected.size(), actual.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }

        assertEquals(true, Database.deleteFavorite("tester", expected.get(0)));
        assertEquals(true, Database.deleteFavorite("tester", expected.get(1)));
        assertEquals(true, Database.deleteFavorite("tester", expected.get(2)));
    }
}
