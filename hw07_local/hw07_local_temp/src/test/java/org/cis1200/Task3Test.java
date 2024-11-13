package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

/**
 * These tests are provided for testing client connection and disconnection,
 * and nickname changes. You can and should use these tests as a model for
 * your own testing, but write your tests in ServerModelTest.java.
 *
 * Note that "assert" commands used for testing can have a String as
 * their last argument, denoting what the "assert" command is testing.
 * 
 * Remember to check:
 * https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html
 * for assert method documention!
 */
public class Task3Test {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        model = new ServerModel();
    }

    @Test
    public void testEmptyOnInit() {
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    // The questions to ask when writing a test for ServerModel:
    // 1. does the method return what is expected?
    // 2. was the state updated as expected?
    @Test
    public void testRegisterSingleUser() {
        // test: does model.registerUser(0) return what we expect?
        // registerUser should return a 'connected' message with the nickname "User0"
        // for the first registered user
        ResponseSet expected = ResponseSet.singleMessage(Response.connected(0, "User0"));
        assertEquals(expected, model.registerUser(0), "User0 registered");

        // test: does the collection contain only one element?
        // Recall: getRegisteredUsers returns a Collection of the nicknames of all
        // registered users!
        // We expect this collection to have one element, which is "User0"
        Collection<String> registeredUsers = model.getRegisteredUsers();

        assertEquals(1, registeredUsers.size(), "Num. registered users");

        // test: does the collection contain the nickname "User0"
        assertTrue(registeredUsers.contains("User0"), "User0 is registered");
    }

    @Test
    public void testRegisterMultipleUsers() {
        ResponseSet expected0 = ResponseSet.singleMessage(Response.connected(0, "User0"));
        assertEquals(expected0, model.registerUser(0), "ResponseSet for User0");
        ResponseSet expected1 = ResponseSet.singleMessage(Response.connected(1, "User1"));
        assertEquals(expected1, model.registerUser(1), "ResponseSet for User1");
        ResponseSet expected2 = ResponseSet.singleMessage(Response.connected(2, "User2"));
        assertEquals(expected2, model.registerUser(2), "ResponseSet for User2");

        Collection<String> registeredUsers = model.getRegisteredUsers();
        assertEquals(3, registeredUsers.size(), "Num. registered users");
        assertTrue(registeredUsers.contains("User0"), "User0 is registered");
        assertTrue(registeredUsers.contains("User1"), "User1 is registered");
        assertTrue(registeredUsers.contains("User2"), "User2 is registered");
    }

    @Test
    public void testDeregisterSingleUser() {
        model.registerUser(0);
        model.deregisterUser(0);
        assertTrue(model.getRegisteredUsers().isEmpty(), "No registered users");
    }

    @Test
    public void testDeregisterOneOfManyUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.deregisterUser(0);
        assertFalse(model.getRegisteredUsers().isEmpty(), "Registered users still exist");
        assertFalse(model.getRegisteredUsers().contains("User0"), "User0 does not exist");
        assertTrue(model.getRegisteredUsers().contains("User1"), "User1 still exists");
    }

    @Test
    public void testNickNotInChannels() {
        model.registerUser(0);
        Command command = new NicknameCommand(0, "cis120");
        ResponseSet expected = ResponseSet.singleMessage(
                Response.okay(0, "User0", command)
        );
        assertEquals(expected, command.updateServerModel(model), "ResponseSet");
        Collection<String> users = model.getRegisteredUsers();
        assertFalse(users.contains("User0"), "Old nick not registered");
        assertTrue(users.contains("cis120"), "New nick registered");
    }

    @Test
    public void testNickCollision() {
        model.registerUser(0);
        model.registerUser(1);
        Command command = new NicknameCommand(0, "User1");
        ResponseSet expected = ResponseSet.singleMessage(
                Response.error(
                        command, ErrorCode.NAME_ALREADY_IN_USE
                )
        );
        assertEquals(expected, command.updateServerModel(model), "ResponseSet");
        Collection<String> users = model.getRegisteredUsers();
        assertTrue(users.contains("User0"), "Old nick still registered");
        assertTrue(users.contains("User1"), "Other user still registered");
    }

    @Test
    public void testNickCollisionOnConnect() {
        model.registerUser(0); // has nickname User0
        Command command = new NicknameCommand(0, "User1"); // change to User1
        command.updateServerModel(model);
        ResponseSet expected = ResponseSet.singleMessage(Response.connected(1, "User0")); // second
                                                                                          // user
                                                                                          // gets
                                                                                          // User0
        assertEquals(expected, model.registerUser(1), "ResponseSet");
        Collection<String> users = model.getRegisteredUsers();
        assertEquals(2, users.size(), "Num. registered users");
        assertTrue(users.contains("User0"), "User0 registered");
        assertTrue(users.contains("User1"), "User1 registered");
        assertEquals(0, model.getUserId("User1"), "User1 has ID 0");
        assertEquals(1, model.getUserId("User0"), "User0 has ID 1");
    }

    @Test
    public void testEncapsulationGetRegisteredUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Collection<String> usersBefore = model.getRegisteredUsers();
        try {
            usersBefore.remove("User0");
            usersBefore.remove("User1");
        } catch (UnsupportedOperationException uox) {
            // Ok to return a Collections.unmodifiableSet
        }
        Collection<String> usersAfter = model.getRegisteredUsers();
        assertTrue(usersAfter.contains("User0"), "User0 not removed");
        assertTrue(usersAfter.contains("User1"), "User1 not removed");
    }

}
