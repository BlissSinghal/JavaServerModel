package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * These tests are provided for testing the server's handling of
 * channels and messages. You can and should use these tests as a model
 * for your own testing, but write your tests in ServerModelTest.java.
 *
 * Note that "assert" commands used for testing can have a String as
 * their last argument, denoting what the "assert" command is testing.
 * 
 * Remember to check:
 * https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html
 * for assert method documention!
 */
public class Task4Test {
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
    public void testCreateNewChannel() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "java", false);
        ResponseSet expected = ResponseSet.singleMessage(
                Response.okay(0, "User0", create)
        );
        assertEquals(expected, create.updateServerModel(model), "broadcast");

        assertTrue(model.getChannels().contains("java"), "channel exists");
        assertTrue(
                model.getUserNicknamesInChannel("java").contains("User0"),
                "channel has creator"
        );
        assertEquals("User0", model.getOwner("java"), "channel has owner");
    }

    @Test
    public void testJoinChannelExistsNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "java", false);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "java");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        ResponseSet expected = new ResponseSet();
        expected.addMessage(Response.okay(0, "User1", join));
        expected.addMessage(Response.okay(1, "User1", join));
        expected.addMessage(Response.names(1, "User1", "java", "@User0 User1"));
        assertEquals(expected, join.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUserNicknamesInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertTrue(
                model.getUserNicknamesInChannel("java").contains("User1"),
                "User1 in channel"
        );
        assertEquals(
                2, model.getUserNicknamesInChannel("java").size(),
                "num. users in channel"
        );
    }

    @Test
    public void testNickBroadcastsToChannelWhereMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "java");
        join.updateServerModel(model);

        Command nick = new NicknameCommand(0, "Duke");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("Duke");
        ResponseSet expected = new ResponseSet();
        expected.addMessage(Response.okay(0, "User0", nick));
        expected.addMessage(Response.okay(1, "User0", nick));
        assertEquals(expected, nick.updateServerModel(model), "broadcast");

        assertFalse(
                model.getUserNicknamesInChannel("java").contains("User0"),
                "old nick not in channel"
        );
        assertTrue(
                model.getUserNicknamesInChannel("java").contains("Duke"),
                "new nick is in channel"
        );
        assertTrue(
                model.getUserNicknamesInChannel("java").contains("User1"),
                "unaffected user still in channel"
        );
    }

    @Test
    public void testLeaveChannelExistsMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "java");
        join.updateServerModel(model);

        Command leave = new LeaveCommand(1, "java");

        ResponseSet expected = new ResponseSet();
        expected.addMessage(Response.okay(0, "User1", leave));
        expected.addMessage(Response.okay(1, "User1", leave));
        assertEquals(expected, leave.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUserNicknamesInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertFalse(
                model.getUserNicknamesInChannel("java").contains("User1"),
                "User1 not in channel"
        );
        assertEquals(
                1, model.getUserNicknamesInChannel("java").size(),
                "num. users in channel"
        );
    }

    @Test
    public void testDeregisterSendsDisconnectedWhereMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "java", false);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "java");
        join.updateServerModel(model);

        ResponseSet expected = ResponseSet.singleMessage(Response.disconnected(0, "User1"));
        assertEquals(expected, model.deregisterUser(1), "broadcast");

        assertTrue(
                model.getChannels().contains("java"),
                "channel still exists"
        );
        assertEquals(
                1, model.getUserNicknamesInChannel("java").size(),
                "num. users in channel"
        );
        assertTrue(
                model.getUserNicknamesInChannel("java").contains("User0"),
                "unaffected user still in channel"
        );
    }

    @Test
    public void testMesgChannelExistsMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "java");
        join.updateServerModel(model);

        Command mesg = new MessageCommand(0, "java", "hey whats up hello");
        ResponseSet expected = new ResponseSet();
        expected.addMessage(Response.okay(0, "User0", mesg));
        expected.addMessage(Response.okay(1, "User0", mesg));
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testEncapsulationgetUsersInChannelOwner() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(1, "java", false);
        create.updateServerModel(model);
        Collection<String> usersBefore = model.getUserNicknamesInChannel("java");
        try {
            usersBefore.remove("User1");
        } catch (UnsupportedOperationException uox) {
            // Ok to use Collections.unmodifiableSet
        }
        Collection<String> usersAfter = model.getUserNicknamesInChannel("java");
        assertTrue(usersAfter.contains("User1"), "User1 not removed");
    }

    @Test
    public void testEncapsulationgetUsersInChannelNonOwner() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "java");
        join.updateServerModel(model);
        Collection<String> usersBefore = model.getUserNicknamesInChannel("java");
        try {
            usersBefore.remove("User1");
        } catch (UnsupportedOperationException uox) {
            // Ok to use Collections.unmodifiableSet
        }
        Collection<String> usersAfter = model.getUserNicknamesInChannel("java");
        assertTrue(usersAfter.contains("User1"), "User1 not removed");
    }

    @Test
    public void testEncapsulationGetChannels() {
        model.registerUser(0);
        Command create1 = new CreateCommand(0, "java", false);
        create1.updateServerModel(model);
        Command create2 = new CreateCommand(0, "ocaml", false);
        create2.updateServerModel(model);
        Collection<String> channelsBefore = model.getChannels();
        try {
            channelsBefore.remove("java");
            channelsBefore.remove("ocaml");
        } catch (UnsupportedOperationException uox) {
            // Ok to use Collections.unmodifiableSet
        }
        Collection<String> channelsAfter = model.getChannels();
        assertTrue(channelsAfter.contains("java"), "channel1 still present");
        assertTrue(channelsAfter.contains("ocaml"), "channel2 still present");
    }

}
