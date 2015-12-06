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
        List<String> expected = new ArrayList<>();
        for(int i = 2; i < 6; i++) {
            expected.add("tester"+i);
        }

        List<String> result = Database.getFriendInvite("tester1");

        for(int i = 0; i < result.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test
    public void login() {
        boolean expected = Database.login("tester", "tests");
        assertEquals(true, expected);

        expected = Database.login("tester", "tests0");
        assertEquals(false, expected);

        expected = Database.login("testerEI", "tests");
        assertEquals(false, expected);

        expected = Database.login("Tester", "tests");
        assertEquals(false, expected);

        expected = Database.login("tester", "Tests");
        assertEquals(false, expected);

        expected = Database.login("tester1", "tests");
        assertEquals(true, expected);
    }

    // für diesen Test darf es den user "tester6" noch nicht geben
    @Test
    public void register() {
        boolean expected = Database.register("tester", "irgendows");
        assertEquals(false, expected);

        expected = Database.register("tester6", "tests");
        assertEquals(true, expected);

        expected = Database.register("tester6", "tests");
        assertEquals(false, expected);
    }

}
