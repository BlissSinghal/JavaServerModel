package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ServerModelTest {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state).
     * The BeforeEach annotation makes it so that the
     * following method will get run before each unit
     * test automatically.
     */
    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    /**
     * Here is an example test that checks the functionality of your
     * changeNickname error handling. Each line has commentary directly above
     * it which you can use as a framework for the remainder of your tests.
     */
    @Test
    public void testInvalidNickname() {
        // A user must be registered before their nickname can be changed,
        // so we first register a user with an arbitrarily chosen id of 0.
        model.registerUser(0);

        // We manually create a Command that appropriately tests the case
        // we are checking. In this case, we create a NicknameCommand whose
        // new Nickname is invalid.
        Command command = new NicknameCommand(0, "!nv@l!d!");

        // To test how the server processes this command, we need to do two things:
        // 1. we need to make sure that the set of responses that it produces matches
        // our expectations
        // 2. we need to make sure that the command updates the state of the server
        // in an appropriate way.

        // We ask the server to process the command using the updateServerModel method.
        // For the Nickname command, dynamic dispatch invokes the changeNickname method
        // in the ServerModel class.
        ResponseSet actual = command.updateServerModel(model);

        // We manually create the *expected* ResponseSet containing a single
        // response, constructed by the error static method. This error message
        // says that the NicknameCommand above contains an invalid name.
        ResponseSet expected = ResponseSet.singleMessage(
                Response.error(
                        command, ErrorCode.INVALID_NAME
                )
        );

        // The first assertEquals tests whether the changeNickname method returns
        // the expected ResponseSet.
        assertEquals(expected, actual, "error response for invalid nickname");

        // We also want to test whether the state has been correctly
        // changed.In this case, the state that would be affected is
        // the user's Collection.
        Collection<String> users = model.getRegisteredUsers();

        // We now check to see if our command updated the state
        // appropriately. In this case, we first ensure that no
        // additional users have been added.
        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        // Finally, we check that the id 0 is still associated with the old,
        // unchanged nickname.
        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }

    /**
     * Here's a simple test modified from testRegisterSingleUser
     * in Task3Test which demonstrates how you can use the constructor
     * and the different methods (addMessage, singleMessage)
     * of the ResponseSet class.
     */
    @Test
    public void testRegisterSingleUserResponseSet() {
        // test: does model.registerUser(0) return what we expect?
        // We create an expected ResponseSet by using the singleMessage
        // method in the ResponseSet class.
        ResponseSet expected = ResponseSet.singleMessage(Response.connected(0, "User0"));
        // The following assertEquals tests whether the changeNickname method returns
        // the expected ResponseSet.
        assertEquals(expected, model.registerUser(0), "User0 registered");

        // Alternatively, we can also instantiate a new ResponseSet using
        // the default constructor.
        ResponseSet expected2 = new ResponseSet();
        // Then, add the response to the ResponseSet using addMessage
        expected2.addMessage(Response.connected(0, "User0"));
        // It should create the same expected ResponseSet as using
        // singleMessage.
        assertEquals(expected, expected2, "Different ways of creating a ResponseSet");
        // Note that you don't have to use both ways to create
        // the expected ResponseSet in each test.
        // This test is just included to show you the two different
        // ways of constructing a ResponseSet. Choose whatever works best
        // in each scenario!

        // test: does the collection contain only one element?
        Collection<String> registeredUsers = model.getRegisteredUsers();
        assertEquals(1, registeredUsers.size(), "Num. registered users");

        // test: does the collection contain the nickname "User0"
        assertTrue(registeredUsers.contains("User0"), "User0 is registered");
    }


    /*
     * Your TAs will be manually grading the tests that you write below this
     * comment block. Don't forget to test the public methods you have added to
     * your ServerModel class, as well as the behavior of the server in
     * different scenarios.
     * You might find it helpful to take a look at the tests we have already
     * provided you with in Task4Test, Task3Test, and Task5Test.
     */

    @Test
    public void testRegisterSameUserID() {
        //registering two users w same id
        model.registerUser(0);
        String name = model.getNickname(0);
        //making sure it throws an exception
        assertThrows(IllegalArgumentException.class, () -> model.registerUser(0));
        //making sure that getRegistered users still has the original user
        Set<String> expectedRegisteredNames = new TreeSet<>();
        expectedRegisteredNames.add(name);
        assertEquals(expectedRegisteredNames, model.getRegisteredUsers());
    }


    @Test
    public void testDeregisterUserIdDoesntExist() {
        model.registerUser(0);
        model.registerUser(1);
        assertThrows(IllegalArgumentException.class,
                () -> model.deregisterUser(2), "ID doesn't exist");
        assertFalse(model.getRegisteredUsers().isEmpty(), "Registered users still exist");
        assertTrue(model.getRegisteredUsers().contains("User0"), "User0 still exists");
        assertTrue(model.getRegisteredUsers().contains("User1"), "User1 still exists");
    }

    @Test
    public void testDeregisterUserUserOwnsChannels() {
        model.registerUser(0);
        model.registerUser(1);
        CreateCommand command = new CreateCommand(0, "channel1", false);
        command.updateServerModel(model);
        //making sure that the channel is created
        Set<String> expectedChannelNames = new TreeSet<>();
        expectedChannelNames.add("channel1");
        assertEquals(model.getChannels(), expectedChannelNames);
        //making user id 0 is owner of this channel
        assertEquals(model.getOwner("channel1"), model.getNickname(0));

        //making sure that deleting user deletes the channel
        ResponseSet deletingUserResponse = model.deregisterUser(0);
        ResponseSet expectedDeletionResponse = ResponseSet.empty();

        //making sure that since there was only one channel, which the deleted user owned, the
        //response set is empty
        assertTrue(model.getChannels().isEmpty());
        assertEquals(expectedDeletionResponse, deletingUserResponse);

    }

    @Test
    public void testDeregisterUserUserMemberofAChannel() {
        model.registerUser(0);
        model.registerUser(1);
        CreateCommand command = new CreateCommand(1, "channel1", false);
        command.updateServerModel(model);
        //making sure that the channel is created
        Set<String> expectedChannelNames = new TreeSet<>();
        expectedChannelNames.add("channel1");
        assertEquals(model.getChannels(), expectedChannelNames);
        //making user id 1 is owner of this channel
        assertEquals(model.getOwner("channel1"), model.getNickname(1));

        //having user 0 join this channel
        Command join = new JoinCommand(0, "channel1");
        join.updateServerModel(model);

        //making sure user 0 is part of this channel
        Set<Integer> expectedUserIds = new TreeSet<>();
        expectedUserIds.add(1);
        expectedUserIds.add(0);
        assertEquals(expectedUserIds, model.getUserIdsInChannel("channel1"));

        //getting the nickname of user 0 (necessary for testing)
        String deregisteredNickname = model.getNickname(0);

        //deregistering user 0
        ResponseSet deletingUserResponse = model.deregisterUser(0);

        //making sure that user1 gets the message that user 0 is deleted
        ResponseSet expectedDeletionResponse = ResponseSet.singleMessage(
                Response.disconnected(1, deregisteredNickname));
        assertEquals(expectedDeletionResponse, deletingUserResponse);

        //making sure that user0 is no longer in the list of channel members
        assertFalse(model.getUserIdsInChannel("channel1").contains(0));
        //making sure that user1 is still in this list
        assertTrue(model.getUserIdsInChannel("channel1").contains(1));

        //making sure that this channel is not deleted
        assertFalse(model.getChannels().isEmpty());

    }

    @Test
    public void testDeregisterUserUserinMultipleChannels() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(1, "channel1", false);
        channel1Command.updateServerModel(model);
        //making sure that the channel is created
        Set<String> expectedChannelNames = new TreeSet<>();
        expectedChannelNames.add("channel1");
        assertEquals(model.getChannels(), expectedChannelNames);
        //making user id 1 is owner of this channel
        assertEquals(model.getOwner("channel1"), model.getNickname(1));

        //creating a second channel
        CreateCommand channel2Command = new CreateCommand(2, "channel2", false);
        channel2Command.updateServerModel(model);
        //making sure that the channel is created
        expectedChannelNames.add("channel2");
        assertEquals(model.getChannels(), expectedChannelNames);
        //making user id 1 is owner of this channel
        assertEquals(model.getOwner("channel2"), model.getNickname(2));


        //having user 0 join both channels
        Command joinChannel1 = new JoinCommand(0, "channel1");
        joinChannel1.updateServerModel(model);

        Command joinChannel2 = new JoinCommand(0, "channel2");
        joinChannel2.updateServerModel(model);

        //making sure user 0 is part of both channels
        Set<Integer> channel1ExpectedIds = new TreeSet<>();
        channel1ExpectedIds.add(1);
        channel1ExpectedIds.add(0);
        assertEquals(channel1ExpectedIds, model.getUserIdsInChannel("channel1"));

        Set<Integer> channel2ExpectedIds = new TreeSet<>();
        channel2ExpectedIds.add(2);
        channel2ExpectedIds.add(0);
        assertEquals(channel2ExpectedIds, model.getUserIdsInChannel("channel2"));

        //getting the nickname of user 0 (necessary for testing)
        String deregisteredNickname = model.getNickname(0);

        //deregistering user 0
        ResponseSet deletingUserResponse = model.deregisterUser(0);

        //making sure that user1 and user2 gets the message that user 0 is deleted
        ResponseSet expectedDeletionResponse = new ResponseSet();
        expectedDeletionResponse.addMessage(Response.disconnected(1, deregisteredNickname));
        expectedDeletionResponse.addMessage(Response.disconnected(2, deregisteredNickname));
        assertEquals(expectedDeletionResponse, deletingUserResponse);

        //making sure that user0 is no longer in either channel member list
        assertFalse(model.getUserIdsInChannel("channel1").contains(0));
        assertFalse(model.getUserIdsInChannel("channel2").contains(0));
        //making sure that user1 and user2 are still in their respective channels
        assertTrue(model.getUserIdsInChannel("channel1").contains(1));
        assertTrue(model.getUserIdsInChannel("channel2").contains(2));

        //making sure that both channels still exist and neither get deleted
        assertEquals(model.getChannels().size(), 2);

    }

    @Test
    public void testDeregisterUserOwnsMultipleChannels() {
        model.registerUser(0);
        //creating two channels that user 0 owns both of
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        CreateCommand channel2Command = new CreateCommand(0, "channel2", false);
        channel2Command.updateServerModel(model);

        //deregistering the user
        model.deregisterUser(0);

        //making sure that both channels are deleted
        assertTrue(model.getChannels().isEmpty());
    }

    @Test
    public void testDeregisteredUserUserAlreadyDeregistered() {
        model.registerUser(0);
        model.registerUser(1);
        model.deregisterUser(0);
        //deregistering the user again
        //making sure it throws an illegal argument exception
        assertThrows(IllegalArgumentException.class, () -> model.deregisterUser(0));

        //making sure the registered users only contains user 1
        Set<String> expectedRegisteredUsers = new TreeSet<>();
        expectedRegisteredUsers.add(model.getNickname(1));
        assertEquals(expectedRegisteredUsers, model.getRegisteredUsers());
    }

    @Test
    public void testDeregisterUserLastUser() {
        model.registerUser(0);
        ResponseSet response = model.deregisterUser(0);
        //since it was the last user, the method should not be sending a response right now
        ResponseSet expectedResponse = ResponseSet.empty();
        assertEquals(response, expectedResponse);
    }

    @Test
    public void testDeregisterUserResponseSenttoCorrectUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);

        //saving user 0's nickname
        String name = model.getNickname(0);

        //having user 1 and user 2 create channels
        CreateCommand command = new CreateCommand(1, "channel1", false);
        command.updateServerModel(model);
        CreateCommand command2 = new CreateCommand(2, "channel2", false);
        command2.updateServerModel(model);

        //having user 0 join user 1's channel
        JoinCommand joinCommand = new JoinCommand(0, "channel1");
        joinCommand.updateServerModel(model);

        //making sure that user 0 is in user 1's channel
        assertTrue(model.getUserIdsInChannel("channel1").contains(0));
        //making sure that user 0 is not in user 2's channel
        assertFalse(model.getUserIdsInChannel("channel2").contains(0));

        //deregistering user 0
        ResponseSet actualResponses = model.deregisterUser(0);
        //should only send response to user 1, not user 2
        ResponseSet expectedResponse = new ResponseSet();
        expectedResponse.addMessage(Response.disconnected(1, name));
        assertEquals(expectedResponse, actualResponses);
    }

    @Test
    public void testChangeNicknameWorks() {
        model.registerUser(0);
        model.registerUser(1);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(1, "channel1", false);
        channel1Command.updateServerModel(model);

        //creating a second channel
        CreateCommand channel2Command = new CreateCommand(0, "channel2", false);
        channel2Command.updateServerModel(model);

        //having user 0 join the first channel
        JoinCommand joinCommand = new JoinCommand(0, "channel1");
        joinCommand.updateServerModel(model);

        //saving user 0's old nickname and new nickname
        String oldName = model.getNickname(0);
        String newName = "bliss";

        //making sure that the set of registered usernames contains the old name
        Set<String> expectedUserNames = new TreeSet<>();
        expectedUserNames.add(oldName);
        expectedUserNames.add(model.getNickname(1));
        assertEquals(expectedUserNames, model.getRegisteredUsers());

        //making sure the ownerName of the channel that userid 0 created is correct
        assertEquals(oldName, model.getOwner("channel2"));

        //making sure that the channels usernames contains user 0's old name
        assertTrue(model.getUserNicknamesInChannel("channel1").contains(oldName));
        assertTrue(model.getUserNicknamesInChannel("channel2").contains(oldName));

        //changing nickname
        NicknameCommand nicknameCommand = new NicknameCommand(0, newName);
        ResponseSet nickResponses = nicknameCommand.updateServerModel(model);

        //making sure that the set of registered usernames has updated
        //making sure that the set of registered usernames contains the old name
        expectedUserNames.remove(oldName);
        expectedUserNames.add(newName);
        assertEquals(expectedUserNames, model.getRegisteredUsers());

        //making sure the ownerName for the channel that user 0 created is changed
        assertEquals(model.getOwner("channel2"), newName);

        //making sure that both channels list of usernicknames is updated
        assertTrue(model.getUserNicknamesInChannel("channel2").contains(newName));
        assertFalse(model.getUserNicknamesInChannel("channel2").contains(oldName));
        assertTrue(model.getUserNicknamesInChannel("channel1").contains(newName));
        assertFalse(model.getUserNicknamesInChannel("channel1").contains(oldName));

        //making sure that we are returning the correct response set
        ResponseSet expectedResponses = new ResponseSet();
        expectedResponses.addMessage(Response.okay(0, oldName, nicknameCommand));
        expectedResponses.addMessage(Response.okay(1, oldName, nicknameCommand));

        assertEquals(expectedResponses, nickResponses);

    }

    @Test
    public void changeNicknameWorksWhenUserNicknameIsAlreadyDeregistered() {
        model.registerUser(0);
        String name = model.getNickname(0);
        model.registerUser(1);
        String oldName = model.getNickname(1);
        //deregistering user 0
        model.deregisterUser(0);
        //making sure their name is no longer in the server model
        assertFalse(model.getRegisteredUsers().contains(name));

        //changing user id 1's name to be user 0's name
        NicknameCommand nicknameCommand = new NicknameCommand(1, name);
        ResponseSet nickResponses = nicknameCommand.updateServerModel(model);
        //making sure that this works and that it gives an okay message
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.okay(1, oldName, nicknameCommand));
        assertEquals(expectedResponse, nickResponses);
        //making sure that registered users has this name
        Set<String> expectedUserNames = new TreeSet<>();
        expectedUserNames.add(name);
        assertEquals(expectedUserNames, model.getRegisteredUsers());

    }

    @Test
    public void simultaneouslyChangeToSameName() {
        model.registerUser(0);
        model.registerUser(1);
        String oldName = model.getNickname(1);
        NicknameCommand nicknameCommand = new NicknameCommand(0, "bliss");
        nicknameCommand.updateServerModel(model);
        //having userid 1 change to the same nickname
        NicknameCommand nicknameCommand2 = new NicknameCommand(1, "bliss");
        //making sure that this returns an error response
        ResponseSet actualResponse = nicknameCommand2.updateServerModel(model);
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(nicknameCommand2, ErrorCode.NAME_ALREADY_IN_USE));
        assertEquals(expectedResponse, actualResponse);
        //making sure that only user 0 has this name and user 1 doesn't
        assertEquals(model.getNickname(0), "bliss");
        assertNotEquals("bliss", model.getNickname(1));
        //making sure that user 1 has old nickname still
        assertEquals(oldName, model.getNickname(1));

    }

    @Test
    public void changeNicknameUnregisteredUser() {
        model.registerUser(0);
        //trying to change nickname of unregistered user
        NicknameCommand nicknameCommand = new NicknameCommand(1, "bliss");
        //making sure it throws an illegal arg exception
        assertThrows(IllegalArgumentException.class,
                () -> nicknameCommand.updateServerModel(model));

        //making sure that the new nickname doesn't exist
        assertFalse(model.getRegisteredUsers().contains("bliss"));
        //making sure that the user id doesn't exist
        assertFalse(model.existingUserId(1));

    }


    @Test
    public void createChannelChannelAlreadyExists() {
        //registering a user
        model.registerUser(0);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //creating a second channel with the same name
        CreateCommand channel1CommandCopy = new CreateCommand(0, "channel1", false);
        ResponseSet response  = channel1CommandCopy.updateServerModel(model);

        //making sure that it returned an error response
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(channel1CommandCopy, ErrorCode.CHANNEL_ALREADY_EXISTS));
        assertEquals(expectedResponse, response);

        //making sure that there is only one channel in the servermodel channels
        Set<String> expectedChannelNames = new TreeSet<>();
        expectedChannelNames.add("channel1");

        assertEquals(model.getChannels(), expectedChannelNames);
    }

    @Test
    public void createChannelInvalidName() {
        model.registerUser(0);
        //creating a channel with invalid name
        CreateCommand channel1Command = new CreateCommand(0, "_", false);
        //making sure it returns a single error message
        ResponseSet actualErrorResponse = channel1Command.updateServerModel(model);
        ResponseSet expectedErrorResponse = ResponseSet.singleMessage(
                Response.error(channel1Command, ErrorCode.INVALID_NAME));
        assertEquals(expectedErrorResponse, actualErrorResponse);

        //making sure that the channel doesn't actually get made and the channel set is empty
        assertTrue(model.getChannels().isEmpty());


    }

    @Test
    public void createChannelChannelExists() {
        model.registerUser(0);
        model.registerUser(1);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //creating a second channel w same name
        CreateCommand channel2Command = new CreateCommand(1, "channel1", false);
        ResponseSet actualResponse = channel2Command.updateServerModel(model);
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(channel2Command, ErrorCode.CHANNEL_ALREADY_EXISTS));
        //making sure that it returns the correct error response
        assertEquals(expectedResponse, actualResponse);

        //making sure that only the original channel is there which has owner user 0
        assertEquals(model.getOwner("channel1"), model.getNickname(0));
        assertNotEquals(model.getOwner("channel1"), model.getNickname(1));

        //making sure that there is only one channel in the server model
        assertEquals(model.getChannels().size(), 1);

    }

    @Test
    public void createChannelUserDoesNotExist() {
        model.registerUser(0);
        CreateCommand channel1Command = new CreateCommand(1, "channel1", false);
        //making sure that it throws an illegal exception
        assertThrows(IllegalArgumentException.class,
                () -> channel1Command.updateServerModel(model));

        //making sure that this channel does not exist
        assertTrue(model.getChannels().isEmpty());

    }

    @Test
    public void joinChannelWorks() {
        model.registerUser(0);
        model.registerUser(1);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user id 1 join the channel
        JoinCommand joinCommand = new JoinCommand(1, "channel1");
        ResponseSet actualResponse = joinCommand.updateServerModel(model);
        //saving the nickname of user 1
        String name = model.getNickname(1);

        //making sure that it returns a okay message to both user id 0 and 1
        //and it returns a names message
        ResponseSet expectedResponse = new ResponseSet();
        expectedResponse.addMessage(Response.okay(0, name, joinCommand));
        expectedResponse.addMessage(Response.okay(1, name, joinCommand));
        expectedResponse.addMessage(
                Response.names(1, name, "channel1", "@User0 User1"));
        assertEquals(expectedResponse, actualResponse);

        //making sure that the channel users has the new user
        Set<String> expectedUserNames = new TreeSet<>();
        expectedUserNames.add(name);
        expectedUserNames.add(model.getNickname(0));
        assertEquals(expectedUserNames, model.getUserNicknamesInChannel("channel1"));




    }

    @Test
    public void joinChannelUserAlreadyInChannel() {
        model.registerUser(0);
        model.registerUser(1);
        //creating the channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user id 1 join the channel
        JoinCommand joinCommand = new JoinCommand(1, "channel1");
        joinCommand.updateServerModel(model);

        //getting the users in the channel
        Collection<Integer> originalUserIds = model.getUserIdsInChannel("channel1");
        Collection<String> originalUserNames = model.getUserNicknamesInChannel("channel1");

        //having user id 1 join the channel again
        JoinCommand joinCommand2 = new JoinCommand(1, "channel1");
        String userName = model.getNickname(1);
        ResponseSet actualResponses = joinCommand2.updateServerModel(model);
        //it should send an okay response to both user id 0 and user id 1
        ResponseSet expectedResponses = new ResponseSet();
        expectedResponses.addMessage(Response.okay(0, userName, joinCommand2));
        expectedResponses.addMessage(Response.okay(1, userName, joinCommand2));
        expectedResponses.addMessage(
                Response.names(1, userName, "channel1", "@User0 User1"));

        //making sure it still sends an okay response
        assertEquals(actualResponses, expectedResponses);

        //making sure that the channel's user ids and user names don't change
        assertEquals(originalUserIds, model.getUserIdsInChannel("channel1"));
        assertEquals(originalUserNames, model.getUserNicknamesInChannel("channel1"));
    }

    @Test
    public void joinChannelUserIsOwner() {
        model.registerUser(0);
        //creating the channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //getting the original set of user member names
        Collection<String> originalUsers = model.getUserNicknamesInChannel("channel1");

        //having the user join the channel
        JoinCommand joinCommand = new JoinCommand(0, "channel1");
        ResponseSet actualResponses = joinCommand.updateServerModel(model);

        //the expected response set is an okay and names response
        //bc it should still work but won't change anything
        ResponseSet expectedResponses = new ResponseSet();
        expectedResponses.addMessage(Response.okay(0, model.getNickname(0), joinCommand));
        expectedResponses.addMessage(
                Response.names(0, model.getNickname(0), "channel1", "@User0"));
        assertEquals(actualResponses, expectedResponses);

        //making user that the set of users in channel doesn't change
        assertEquals(originalUsers, model.getUserNicknamesInChannel("channel1"));

        //making sure that user 0 is still the owner
        assertEquals(model.getOwner("channel1"), model.getNickname(0));

    }

    @Test
    public void joinChannelChannelDoesntExist() {
        model.registerUser(0);
        JoinCommand joinCommand = new JoinCommand(0, "channel1");
        ResponseSet actualResponse = joinCommand.updateServerModel(model);

        //should return an error because channel name doesn't exist
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(joinCommand, ErrorCode.NO_SUCH_CHANNEL));
        assertEquals(expectedResponse, actualResponse);

        //making sure that the channel still doesn't exist
        assertTrue(model.getChannels().isEmpty());
    }

    @Test
    public void joinChannelUserDoesNotExist() {
        model.registerUser(0);
        //creating the channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user 1 who doesn't exist try to join the channel
        JoinCommand joinCommand = new JoinCommand(1, "channel1");

        //making sure it throws an illegal arg exception
        assertThrows(IllegalArgumentException.class, () -> joinCommand.updateServerModel(model));

        //making sure that channel doesn't contain this user
        assertFalse(model.getUserIdsInChannel("channel1").contains(1));

    }

    @Test
    public void sendMessageChannelDoesntExist() {
        model.registerUser(0);
        MessageCommand messageCommand = new MessageCommand(0, "channel", "hi");
        ResponseSet actualResponse = messageCommand.updateServerModel(model);

        //making sure channel doesn't exist
        assertFalse(model.getChannels().contains("channel"));

        //make sure that this sends a response error
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(messageCommand, ErrorCode.NO_SUCH_CHANNEL));
        assertEquals(expectedResponse, actualResponse);


    }

    @Test
    public void sendMessageUserDoesntExist() {
        model.registerUser(0);
        //creating channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //making sure user w id 1 doesn't exist
        assertFalse(model.existingUserId(1));
        //making sure user w id 1 is not a member of the channel
        assertFalse(model.getUserIdsInChannel("channel1").contains(1));
        //having user 1 (who doesn't exist) send message
        MessageCommand messageCommand = new MessageCommand(1, "channel", "hi");
        //making sure it throws an illegal argument exception
        assertThrows(IllegalArgumentException.class, () -> messageCommand.updateServerModel(model));
    }

    @Test
    public void sendMessageUserNotInChannel() {
        model.registerUser(0);
        model.registerUser(1);

        //creating channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //making sure user id 1 is not in channel
        assertFalse(model.getUserIdsInChannel("channel1").contains(1));

        //having user id 1 try to send message in channel
        MessageCommand messageCommand = new MessageCommand(1, "channel1", "hi");
        ResponseSet actualResponse = messageCommand.updateServerModel(model);

        //making sure that it returns an error response
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(messageCommand, ErrorCode.USER_NOT_IN_CHANNEL));

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void sendMessageToMultipleMembers() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);

        //creating 2 channels owned by user 0
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        CreateCommand channel2Command = new CreateCommand(0, "channel2", false);
        channel2Command.updateServerModel(model);

        //having user 1 join channel 1 and user 2 join channel2
        JoinCommand joinCommand1 = new JoinCommand(1, "channel1");
        joinCommand1.updateServerModel(model);
        JoinCommand joinCommand2 = new JoinCommand(2, "channel2");
        joinCommand2.updateServerModel(model);

        //making sure user 2 not in channel 1
        assertFalse(model.getUserIdsInChannel("channel1").contains(2));

        //having user 0 send message in channel1
        MessageCommand messageCommand1 = new MessageCommand(0, "channel1", "hi");
        ResponseSet actualResponse = messageCommand1.updateServerModel(model);

        //response set should send message only to user 0 and user 1 but not user 2
        ResponseSet expectedResponse = new ResponseSet();
        expectedResponse.addMessage(Response.okay(0, model.getNickname(0), messageCommand1));
        expectedResponse.addMessage(Response.okay(1, model.getNickname(0), messageCommand1));

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void sendMessageWithUpdatedNickname() {
        model.registerUser(0);
        model.registerUser(1);

        //saving user 1's original nickname
        String oldName = model.getNickname(1);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user id 1 join channel
        JoinCommand joinCommand1 = new JoinCommand(1, "channel1");
        joinCommand1.updateServerModel(model);

        //making sure that channel members contains this user
        assertTrue(model.getUserNicknamesInChannel("channel1").contains(oldName));

        //changing user id 1's nickname
        String newName = "bliss";
        NicknameCommand nicknameCommand = new NicknameCommand(1, newName);
        nicknameCommand.updateServerModel(model);

        //making sure that channel members updated this nickname
        assertFalse(model.getUserNicknamesInChannel("channel1").contains(oldName));
        assertTrue(model.getUserNicknamesInChannel("channel1").contains(newName));

        //having user 1 send message
        MessageCommand messageCommand = new MessageCommand(1, "channel1", "hi");
        ResponseSet actualResponse = messageCommand.updateServerModel(model);

        //responses should have the new name not the old name assigned to it
        ResponseSet expectedResponse = new ResponseSet();
        expectedResponse.addMessage(Response.okay(0, newName, messageCommand));
        expectedResponse.addMessage(Response.okay(1, newName, messageCommand));

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void leaveChannelUserDoesntExist() {
        model.registerUser(0);

        //creating channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //making sure user id 1 doesn't exist in server or channel
        assertFalse(model.existingUserId(1));
        assertFalse(model.getUserIdsInChannel("channel1").contains(1));

        //having user id 1 try to leave channel
        LeaveCommand leaveCommand = new LeaveCommand(1, "channel");
        //making sure this throws illegal argument exception
        assertThrows(IllegalArgumentException.class, () -> leaveCommand.updateServerModel(model));
    }

    @Test
    public void leaveChannelUserNotInChannel() {
        model.registerUser(0);
        model.registerUser(1);

        //creating channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //making sure user id 1 is not in channel
        assertFalse(model.getUserIdsInChannel("channel1").contains(1));

        //having user id 1 try to leave channel
        LeaveCommand leaveCommand = new LeaveCommand(1, "channel1");
        ResponseSet actualResponse = leaveCommand.updateServerModel(model);

        //making sure that it returns an error response
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(leaveCommand, ErrorCode.USER_NOT_IN_CHANNEL));

        assertEquals(expectedResponse, actualResponse);

    }

    @Test
    public void leaveChannelChannelDoesntExist() {
        model.registerUser(0);
        LeaveCommand leaveCommand = new LeaveCommand(0, "channel");
        ResponseSet actualResponse = leaveCommand.updateServerModel(model);

        //making sure channel doesn't exist
        assertFalse(model.getChannels().contains("channel"));

        //make sure that this sends a response error
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.error(leaveCommand, ErrorCode.NO_SUCH_CHANNEL));
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void leaveChannelUserIsOwner() {
        model.registerUser(0);
        model.registerUser(1);

        //saving nickname of user id 0
        String name = model.getNickname(0);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user id 1 join channel
        JoinCommand joinCommand1 = new JoinCommand(1, "channel1");
        joinCommand1.updateServerModel(model);

        //having user id 0 leave channel who is also owner of channel
        LeaveCommand leaveCommand2 = new LeaveCommand(0, "channel1");
        ResponseSet actualResponse = leaveCommand2.updateServerModel(model);

        //should have still returned a response to that leaving user and user 1
        ResponseSet expectedResponse = new ResponseSet();
        expectedResponse.addMessage(Response.okay(0, name, leaveCommand2));
        expectedResponse.addMessage(Response.okay(1, name, leaveCommand2));
        assertEquals(expectedResponse, actualResponse);

        //making sure that channel is deleted
        assertTrue(model.getChannels().isEmpty());

    }

    @Test
    public void leaveChannelLastPersonLeaves() {
        model.registerUser(0);
        model.registerUser(1);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user id 1 join channel
        JoinCommand joinCommand1 = new JoinCommand(1, "channel1");
        joinCommand1.updateServerModel(model);

        //making sure user 1 joined channel
        Set<String> expectedUsers = new TreeSet<>();
        expectedUsers.add(model.getNickname(0));
        expectedUsers.add(model.getNickname(1));

        assertEquals(expectedUsers, model.getUserNicknamesInChannel("channel1"));

        //having user id 1 leave channel
        LeaveCommand leaveCommand1 = new LeaveCommand(1, "channel1");
        leaveCommand1.updateServerModel(model);

        //making user id 1 is no longer in channel
        expectedUsers.remove(model.getNickname(1));
        assertEquals(expectedUsers, model.getUserNicknamesInChannel("channel1"));

        //having user id 0 leave channel who is also last member in channel
        LeaveCommand leaveCommand2 = new LeaveCommand(0, "channel1");
        ResponseSet actualResponse = leaveCommand2.updateServerModel(model);
        //should have still returned a response to that leaving user
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.okay(0, model.getNickname(0), leaveCommand2));
        assertEquals(expectedResponse, actualResponse);

        //making sure that channel is deleted
        assertTrue(model.getChannels().isEmpty());
    }

    @Test
    public void leaveChannelUserDoesntGetMessages() {
        model.registerUser(0);
        model.registerUser(1);

        //creating a channel
        CreateCommand channel1Command = new CreateCommand(0, "channel1", false);
        channel1Command.updateServerModel(model);

        //having user id 1 join channel
        JoinCommand joinCommand1 = new JoinCommand(1, "channel1");
        joinCommand1.updateServerModel(model);

        //making sure user 1 joined channel
        Set<String> expectedUsers = new TreeSet<>();
        expectedUsers.add(model.getNickname(0));
        expectedUsers.add(model.getNickname(1));

        assertEquals(expectedUsers, model.getUserNicknamesInChannel("channel1"));

        //having user id 1 leave channel
        LeaveCommand leaveCommand1 = new LeaveCommand(1, "channel1");
        leaveCommand1.updateServerModel(model);

        //making user id 1 is no longer in channel
        expectedUsers.remove(model.getNickname(1));
        assertEquals(expectedUsers, model.getUserNicknamesInChannel("channel1"));

        //having user 0 send a message in the channel
        MessageCommand messageCommand = new MessageCommand(0, "channel1", "hi");
        ResponseSet actualResponse = messageCommand.updateServerModel(model);

        //making sure user 1 doesn't get this message
        ResponseSet expectedResponse = ResponseSet.singleMessage(
                Response.okay(0, model.getNickname(0), messageCommand));
        assertEquals(expectedResponse, actualResponse);

    }


}
