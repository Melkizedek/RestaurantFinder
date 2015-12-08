package com.restfind.restaurantfinder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This is the JUnit test class for the database class.
 * Created by gabriel on 04.12.15.
 */
public class DatabaseTest {
    @Test
    public void getFriendInvites() throws Exception {
        List<String> actual = new ArrayList<>();
        for(int i = 2; i < 6; i++) {
            actual.add("tester" + i);
        }

        List<String> result = Database.getFriendInvite("tester1");

        for(int i = 0; i < result.size(); i++) {
            assertEquals(actual.get(i), result.get(i));
        }
    }

    @Test
    public void login() {
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

    // für diesen Test darf es den user "tester6" noch nicht geben
    @Test
    public void register() {
        boolean actual = Database.register("tester", "irgendows");
        assertEquals(false, actual);

        actual = Database.register("tester6", "tests");
        assertEquals(true, actual);

        actual = Database.register("tester6", "tests1");
        assertEquals(false, actual);
    }

    @Test
    public void favorite() {
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
        assertEquals(false, actual);

        actual = Database.deleteFavorite("testerLol", "loc1");
        assertEquals(false, actual);

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

}
