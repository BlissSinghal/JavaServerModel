package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class Task2Test {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     * 
     * For task 2, you haven't yet implemented any way
     * to modify the state, so the test cases in this class makes sure
     * that the basic queries return appropriate default values.
     */
    @BeforeEach
    public void setUp() {
        model = new ServerModel();
    }

    /*
     * This test makes sure that your method throws the appropriate
     * exception for a nonexistent nickname.
     *
     */
    @Test()
    public void testGetUserIdNonexistentUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> model.getUserId("Nick")
        );
    }

    /*
     * This test makes sure that your method throws the appropriate
     * exception for a nonexistent userId.
     *
     */
    @Test
    public void testGetNicknameNonexistentUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> model.getNickname(0)
        );
    }

    /*
     * This test makes sure that the initial version of your ServerModel
     * does not have any registered users.
     */
    @Test
    public void testGetRegisteredUsersEmpty() {
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testGetRegisteredUsers() {
        model.registerUser(0);
        model.registerUser(1);

    }

    /*
     * This test makes sure that the initial version of your ServerModel
     * does not have any channels.
     */


    @Test
    public void testGetChannelsEmpty() {
        assertTrue(model.getChannels().isEmpty(), "No channels");
    }

    /*
     * This test makes sure that asking for the users of a nonexistent channel
     * produces
     * and IllegalArgumentException.
     */
    @Test
    public void testGetUsersInChannelEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> model.getUserNicknamesInChannel("java").isEmpty(), "No channels"
        );
    }

    /*
     * This test makes sure that asking for the owner of a nonexistent channel
     * produces
     * and IllegalArgumentException.
     */
    @Test
    public void testGetOwnerNonexistentChannel() {
        assertThrows(IllegalArgumentException.class, () -> model.getOwner("java"), "No channels");
    }
}