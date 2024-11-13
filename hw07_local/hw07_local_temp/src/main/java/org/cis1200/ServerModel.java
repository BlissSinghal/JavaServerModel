package org.cis1200;

import java.util.*;

/**
 * The {@code ServerModel} is the class responsible for tracking the
 * state of the server, including its current users and the channels
 * they are in.
 * This class is used by subclasses of {@link Command} to:
 * 1. handle commands from clients, to create channels, send messages, etc.
 * and
 * 2. handle commands from {@link ServerBackend} to coordinate
 * client connection/disconnection.
 */
public class ServerModel {

    /**
     * Constructs a {@code ServerModel}. Make sure to initialize any collections
     * used to model the server state here.
     */

    private Map<String, Integer> users;
    private Set<ChannelInfo> channels;

    public ServerModel() {
        users = new TreeMap<>();
        channels = new TreeSet<>();
    }

    // =========================================================================
    // == Task 2: Basic Server model queries
    // == These functions provide helpful ways to test the state of your model.
    // == You may also use them in later tasks to process commands.
    // =========================================================================

    /**
     * Is this an existing nickname?
     *
     * @param nickname any string
     * @return whether the nickname is currently in use by any user in any channel
     */
    public boolean existingNickname(String nickname) {
        return users.containsKey(nickname);
    }

    /**
     * Is this a userId that is known to the server?
     *
     * @param userId any integer
     * @return whether the userId has been registered for any client
     */
    public boolean existingUserId(Integer userId) {
        return users.containsValue(userId);
    }

    /**
     * Gets the userId currently associated with the given nickname.
     *
     * @param userNickname The nickname for which to get the associated userId
     * @return The userId of the user with the provided nickname if
     * such a user exists
     * @throws IllegalArgumentException if there is no userId associated
     *                                  with the nickname.
     */
    public int getUserId(String userNickname) {
        if (users.containsKey(userNickname)) {
            return users.get(userNickname);
        }
        throw new IllegalArgumentException(
                "User " + userNickname + " does not exist"
        );
    }

    /*
     * Make sure to write your own tests in ServerModelTest.java.
     * The tests we provide for each task are NOT comprehensive!
     */

    /**
     * Gets the nickname currently associated with the given userId.
     *
     * @param userId The userId for which to get the associated
     *               nickname
     * @return The nickname of the user with that userId if
     *         such a user exists
     * @throws IllegalArgumentException if the userId is not in use
     */
    public String getNickname(int userId) {
        if (users.containsValue(userId)) {
            //iterating through each key value pair
            for (Map.Entry<String, Integer> entry : users.entrySet()) {
                if (entry.getValue() == userId) {
                    return entry.getKey();
                }

            }
        }
        throw new IllegalArgumentException(
                "User Id " + userId
                        + " has no associate nickname."
        );

    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Modifications to the returned collection
     * should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        return new TreeSet<>(users.keySet());
    }

    /**
     * Gets a collection of the channels who are
     * registered with the server. Modifications to the returned collection
     * should not affect the server state.
     *
     * This method is provided for testing.
     *
     * @return The collection of channels
     */
    public Collection<ChannelInfo> getRegisteredChannels() {
        return new TreeSet<>(channels);
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Modifications to the returned collection
     * should not affect the server state.
     *
     * This method is public for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        TreeSet<String> channelNames = new TreeSet<>();
        for (ChannelInfo channel : channels) {
            channelNames.add(channel.getChannelName());
        }
        return channelNames;
    }



    /**
     * Gets a collection of the userIds of all users who are
     * in the channel. Modifications to the returned collection
     * should not affect the server state.
     *
     * This method is public for testing.
     *
     * @param channelName The channel
     * @return The collection of IDs
     * @throws IllegalArgumentException if the channel does not exist
     */
    public Collection<Integer> getUserIdsInChannel(String channelName) {
        for (ChannelInfo channel : channels) {
            if (channel.getChannelName().equals(channelName)) {
                return channel.getUserIds();
            }
        }
        throw new IllegalArgumentException("Unknown channel: " + channelName);

    }

    /**
     * Gets an alphabetically sorted set of the nicknames of all the users
     * in a given channel. Modifications to the returned collection should
     * not affect the server state.
     *
     * This method is public for testing.
     *
     * @param channelName The channel for which to get member nicknames
     * @return A collection of all user nicknames in the channel
     * @throws IllegalArgumentException if there is no channel with the given name
     */
    public SortedSet<String> getUserNicknamesInChannel(String channelName) {
        for (ChannelInfo channel : channels) {
            if (channel.getChannelName().equals(channelName)) {
                return new TreeSet<>(channel.getUserNames());
            }
        }

        throw new IllegalArgumentException("Unknown channel: " + channelName);
    }

    /**
     * Gets the nickname of the owner of the given channel.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel exists
     * @throws IllegalArgumentException if there is no channel with
     *                                  the given name
     */
    public String getOwner(String channelName) {
        for (ChannelInfo channel : channels) {
            if (channel.getChannelName().equals(channelName)) {
                return channel.getOwner();
            }
        }

        throw new IllegalArgumentException("Unknown channel: " + channelName);
    }

// ===============================================
// == Task 3: Connections and Setting Nicknames ==
// ===============================================

    /**
     * This method is automatically called when a new client connects
     * to the server. It should generate a default nickname with
     * {@link #generateUniqueNickname()}, store the new userId and nickname
     * in your {@link ServerModel} state.
     * The method should return a single {@link Response#connected} response to the
     * client.
     *
     * @param userId The new user's unique identifier (created by the backend)
     * @return A {@link ResponseSet} object indicating that the connection was
     *         successful.
     */
    public ResponseSet registerUser(int userId) {
        String userNickname = generateUniqueNickname();
        // We have taken care of generating the nickname and returning
        // the ResponseSet for you. You need to modify this method to
        // store the new user in this model's internal state.
        if (!existingUserId(userId)) {
            users.put(userNickname, userId);
            return ResponseSet.singleMessage(Response.connected(userId, userNickname));
        }

        throw new IllegalArgumentException(
                "user " + userId + " already exists"
        );

    }

    /**
     * Helper for {@link #registerUser(int)}. (Nothing to do here.)
     *
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     *
     * @return The generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String userNickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            userNickname = "User" + suffix++;
        } while (existingUsers.contains(userNickname));
        return userNickname;
    }

    /**
     * This method is automatically called when a client
     * disconnects from the server. This method should take the following
     * actions, not necessarily in this order, and notify other users
     * that the client has left.
     *
     * (1) The disconnected user's information should be deleted from
     * the {@link ServerModel}'s internal state
     * (2) All channels owned by the disconnected user should be deleted
     * the {@link ServerModel}'s internal state
     *
     * @param userId The unique userId of the user to deregister
     * @return a {@link ResponseSet} object containing {@link Response#disconnected}
     *         responses addressed to all users who shared a channel with
     *         the disconnected user, excluding the disconnected user.
     * @throws IllegalArgumentException if the userID is not currently registered.
     *                                  (If
     *                                  you correctly use previous methods you've
     *                                  written, this could happen
     *                                  automatically!)
     *
     */
    public ResponseSet deregisterUser(int userId) {
        //variable to hold all the userIds that share a channel with this one
        Set<Integer> allConnectedUsers = new TreeSet<>();
        //getting the user's nickname
        String userName = getNickname(userId);
        //removing from server users
        users.remove(userName);
        //iterating through the channels
        for (ChannelInfo channel : getRegisteredChannels()) {
            //deleting the channel if the user owns it
            if (channel.getOwnerID() == userId) {
                channel.deleteUser(userId);
                allConnectedUsers.addAll(channel.getUserIds());
                channels.remove(channel);
            } else if (channel.getUserIds().contains(userId)) {
                //deleting the user from list of users
                channel.deleteUser(userId);
                allConnectedUsers.addAll(channel.getUserIds());
            }

        }

        //adding response for every single user who shared channel w disconnected user
        ResponseSet responses = new ResponseSet();
        for (int id: allConnectedUsers) {
            responses.addMessage(Response.disconnected(id, userName));
        }

        return responses;

    }

    /**
     * This method is called when a user wants to change their nickname.
     *
     * @param nickCommand The {@link NicknameCommand} object containing
     *                    all information needed to attempt a nickname change
     * @return If the nickname change is successful,
     *         a {@link ResponseSet} containing a {@link Response#okay} response for
     *         each user that shares at least one channel with the sender, including
     *         the sender. Each response should contain the original
     *         {@link NicknameCommand},
     *         and the old nickname.
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#INVALID_NAME} if the proposed nickname
     *         is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ErrorCode#NAME_ALREADY_IN_USE} if there is
     *         already a user with the proposed nickname. This includes
     *         the current nickname of the user requesting the change.
     */
    public ResponseSet changeNickname(NicknameCommand nickCommand) {
        int userId = nickCommand.getSenderId();
        String oldName = getNickname(userId);
        String newName = nickCommand.getNewNickname();

        //variable to store all users that share at least one channel w this one
        Set<Integer> allConnectedUsers = new TreeSet<>();

        //check if nickname is not valid
        if (!isValidName(newName)) {
            return ResponseSet.singleMessage(Response.error(nickCommand, ErrorCode.INVALID_NAME));
        }
        //check if someone else already has this name
        if (existingNickname(newName)) {
            return ResponseSet.singleMessage(
                    Response.error(nickCommand, ErrorCode.NAME_ALREADY_IN_USE));
        }

        //need to modify the nickname in the list of registered users
        //deleting the old key value pair
        users.remove(oldName);
        //adding it back in with new nickname
        users.put(newName, userId);

        //updating their names in all the channels
        for (ChannelInfo channel: channels) {
            if (channel.userInChannel(userId)) {
                channel.updateNickName(userId, newName);
                allConnectedUsers.addAll(channel.getUserIds());
            }

        }

        //adding the user itself to all connected users in case they had no channels
        allConnectedUsers.add(userId);

        //sending response to everyone who shared channel w this user
        return sendResponseToEveryone(allConnectedUsers, oldName, nickCommand);
    }

    /**
     * Determines if a given name is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * (Nothing to do here.)
     *
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

// ===================================
// == Task 4: Channels and Messages ==
// ===================================

    /**
     * This method is called when a user wants to create a channel.
     * You can ignore the privacy aspect of this method for task 4, but
     * if you choose to do the task 5 Kudos problem, make sure you come back
     * and implement the privacy aspect.
     *
     * @param createCommand The {@link CreateCommand} object containing all
     *                      information needed to attempt channel creation
     * @return If the channel creation is successful, {@link ResponseSet} containing
     *         a
     *         single {@link Response#okay} message. The recipient should be owner
     *         of
     *         the new channel.
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#INVALID_NAME} if the proposed
     *         channel name is not valid according to
     *         {@link ServerModel#isValidName(String)}
     *         (2) {@link ErrorCode#CHANNEL_ALREADY_EXISTS} if there is
     *         already a channel with the proposed name
     */
    public ResponseSet createChannel(CreateCommand createCommand) {
        //initializing all the inputs needed to create a channel
        int ownerId = createCommand.getSenderId();
        String ownerName = getNickname(ownerId);
        String channelName = createCommand.getChannel();
        TreeMap<Integer, String> channelUsers = new TreeMap<>();
        channelUsers.put(ownerId, ownerName);

        //checking to make sure that the channel name isn't already in use
        if (channelExists(channelName)) {
            return ResponseSet.singleMessage(
                    Response.error(createCommand, ErrorCode.CHANNEL_ALREADY_EXISTS));
        }

        //checking to make sure the name is valid
        if (!isValidName(channelName)) {
            return ResponseSet.singleMessage(Response.error(createCommand, ErrorCode.INVALID_NAME));
        }

        //creating our channel
        ChannelInfo newChannel = new ChannelInfo(ownerId, ownerName, channelName, channelUsers);

        //adding channel to our list of channels
        channels.add(newChannel);


        return ResponseSet.singleMessage(Response.okay(ownerId, ownerName, createCommand));
    }


    /**
     * This method is called when a user wants to join a channel.
     *
     * You can ignore the privacy aspect of this method for task 4.
     * If you choose to do the task 5 Kudos problem, make sure you come
     * back and implement the privacy aspect.
     *
     * @param joinCommand The {@link JoinCommand} object containing all
     *                    information needed for the user's join attempt
     * @return If the user can successfully join the channel, the
     *         {@link ResponseSet} should include a {@link Response#okay} response
     *         sent to all users in the channel (including the newly joined user).
     *         Furthermore, the {@link ResponseSet} should include a
     *         {@link Response#names}
     *         response to the newly joined user containing the names of all users
     *         in the channel (also including the newly joined user).
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) (after Task 5) {@link ErrorCode#JOIN_PRIVATE_CHANNEL} if
     *         the sender is attempting to join a private channel
     */

    public ResponseSet joinChannel(JoinCommand joinCommand) {
        int userId = joinCommand.getSenderId();
        String name = getNickname(userId);
        String channelName = joinCommand.getChannel();

        if (!channelExists(channelName)) {
            return ResponseSet.singleMessage(
                    Response.error(joinCommand, ErrorCode.NO_SUCH_CHANNEL));
        }

        ChannelInfo channel = getChannel(channelName);

        //adding the user to channel
        channel.addUser(userId, getNickname(userId));


        //need to add response for every person in the channel
        ResponseSet responses = sendResponseToEveryone(channel, getNickname(userId), joinCommand);
        responses.addMessage(
                Response.names(userId, name, channelName, channel.getUserNames(),
                        channel.getOwner()));
        return responses;
    }

    /**
     * Helper for {@link #leaveChannel(LeaveCommand)} and other methods requiring channel lookup.
     *
     * Retrieves a {@link ChannelInfo} object by its name from the list of channels.
     *
     * @param name The name of the channel to retrieve.
     * @return The {@link ChannelInfo} object with the specified name.
     * @throws IllegalArgumentException if no channel with the specified name is found.
     */
    private ChannelInfo getChannel(String name) {
        for (ChannelInfo channel: channels) {
            if (channel.getChannelName().equals(name)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown channel: " + name);
    }

    /**
     * Helper for broadcasting a response to all users in the specified channel.
     *
     * Creates a {@link ResponseSet} containing an "okay" response for every user in the channel.
     *
     * @param channel The {@link ChannelInfo} object representing the channel.
     * @param senderName The name of the sender.
     * @param command The {@link Command} being broadcasted to users.
     * @return A {@link ResponseSet} with "okay" responses for all users in the channel.
     */
    private ResponseSet sendResponseToEveryone(ChannelInfo channel, String senderName,
                                               Command command) {
        ResponseSet responses = new ResponseSet();
        Collection<Integer> userIds = channel.getUserIds();
        for (Integer userId: userIds) {
            responses.addMessage(Response.okay(userId, senderName, command));
        }
        return responses;
    }

    /**
     * Helper for broadcasting a response to a custom set of user IDs.
     *
     * Creates a {@link ResponseSet} containing an "okay" response
     * for each user in user IDs collection.
     *
     * @param userIds A collection of user IDs to whom the response will be sent.
     * @param senderName The name of the sender.
     * @param command The {@link Command} being broadcasted to users.
     * @return A {@link ResponseSet} with "okay" responses for the specified user IDs.
     */
    private ResponseSet sendResponseToEveryone(Collection<Integer> userIds, String senderName,
                                               Command command) {
        ResponseSet responses = new ResponseSet();
        for (Integer userId: userIds) {
            responses.addMessage(Response.okay(userId, senderName, command));
        }
        return responses;
    }

    /**
     * Helper for {@link #joinChannel(JoinCommand)}, {@link #sendMessage(MessageCommand)},
     * and {@link #leaveChannel(LeaveCommand)}.
     *
     * Determines if a channel with the specified name exists.
     *
     * @param name The name of the channel to check for existence.
     * @return {@code true} if the channel exists; {@code false} otherwise.
     */
    private boolean channelExists(String name) {
        for (ChannelInfo channel: channels) {
            if ((channel.getChannelName()).equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called when a user wants to send a message to a channel.
     *
     * @param messageCommand The {@link MessageCommand} object containing all
     *                       information needed for the messaging attempt.
     * @return If the message can be sent, the {@link ResponseSet} should include
     *         {@link Response#okay} responses addressed to all clients in the
     *         channel.
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ErrorCode#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to send the message to
     */
    public ResponseSet sendMessage(MessageCommand messageCommand) {
        int userId = messageCommand.getSenderId();
        //getting nickname of user
        String name = getNickname(userId);
        String channelName = messageCommand.getChannel();

        //making sure channel exists
        if (!channelExists(channelName)) {
            return ResponseSet.singleMessage(
                    Response.error(messageCommand, ErrorCode.NO_SUCH_CHANNEL));
        }

        ChannelInfo channel = getChannel(channelName);
        //making sure user is in channel
        if (!channel.userInChannel(userId)) {
            return ResponseSet.singleMessage(
                    Response.error(messageCommand, ErrorCode.USER_NOT_IN_CHANNEL));
        }

        //sending message to everyone in channel
        return sendResponseToEveryone(channel, name, messageCommand);
    }

    /**
     * This method is called when a user wants to leave a channel.
     *
     * @param leaveCommand The {@link LeaveCommand} object containing all
     *                     information about the user's leave attempt
     * @return A {@link ResponseSet} object containing {@link Response#okay}
     *         responses to all users in the channel (including the leaving
     *         user), informing them that the leave command was successful.
     *         If the user was the last member of the channel, then the channel
     *         is also removed.
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#NO_SUCH_CHANNEL} if there is no
     *         channel with the specified name
     *         (2) {@link ErrorCode#USER_NOT_IN_CHANNEL} if the sender is
     *         not in the channel they are trying to leave
     */
    public ResponseSet leaveChannel(LeaveCommand leaveCommand) {
        int userId = leaveCommand.getSenderId();
        String channelName = leaveCommand.getChannel();
        String name = getNickname(userId);

        //making sure channel exists
        if (!channelExists(channelName)) {
            return ResponseSet.singleMessage(
                    Response.error(leaveCommand, ErrorCode.NO_SUCH_CHANNEL));
        }

        ChannelInfo channel = getChannel(channelName);

        //making sure user is not in channel
        if (!channel.userInChannel(userId)) {
            return ResponseSet.singleMessage(
                    Response.error(leaveCommand, ErrorCode.USER_NOT_IN_CHANNEL));
        }

        //removing the user
        channel.deleteUser(userId);

        //deleting channel if no users left
        if (channel.getUserIds().isEmpty()) {
            channels.remove(channel);
            //still returning a response to that user who left
            return ResponseSet.singleMessage(Response.okay(userId, name, leaveCommand));
        }

        //getting the list of userIds with the deleted user also there
        Collection<Integer> orgUserIds = channel.getUserIds();
        orgUserIds.add(userId);
        //sending the response to everyone included the leaving user

        //deleting the channel if the owner leaves,
        //but it should still send the leave command to everyone
        if (getOwner(channelName).equals(name)) {
            channels.remove(channel);
        }
        return sendResponseToEveryone(orgUserIds, name, leaveCommand);
    }

// =============================================
// == Kudos problem = Task 5: Channel Privacy ==
// =============================================

    /*
     * This problem is worth zero points, but it will challenge your
     * ability to work with Java Collections.
     * In addition to completing the methods below, make sure to also
     * go back to createChannel and joinChannel and add all privacy-related
     * functionalities
     * If you choose not to complete this task, return null for both methods.
     */

    /**
     * This method is called when a channel's owner adds a user to that channel.
     *
     * @param inviteCommand The {@link InviteCommand} object containing all
     *                      information needed for the invite attempt
     * @return If the user joins the channel successfully as a result of the invite,
     *         a {@link ResponseSet} containing a {@link Response#names} for all
     *         people
     *         in the joined channel (including the new user).
     *         Furthermore, the {@link ResponseSet} should include a
     *         {@link Response#names}
     *         response to the newly joined user containing the names of all users
     *         in the channel (also including the newly joined user).
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#NO_SUCH_USER} if the invited user
     *         does not exist
     *         (2) {@link ErrorCode#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ErrorCode#INVITE_TO_PUBLIC_CHANNEL} if the
     *         invite refers to a public channel
     *         (4) {@link ErrorCode#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public ResponseSet inviteUser(InviteCommand inviteCommand) {
        return null;
    }

    /**
     * This method is called when a channel's owner removes a user from
     * that channel. If the user being kicked is the owner, then the
     * channel should be deleted and all its users removed.
     *
     * @param kickCommand The {@link KickCommand} object containing all
     *                    information needed for the kick attempt
     * @return If the user is successfully kicked from the channel,
     *         {@link ResponseSet} containing a {@link Response#okay} for each user
     *         in the channel, including the user who was kicked.
     *
     *         If an error occurs, use
     *         {@link Response#error} with either:
     *         (1) {@link ErrorCode#NO_SUCH_USER} if the user being kicked
     *         does not exist
     *         (2) {@link ErrorCode#NO_SUCH_CHANNEL} if there is no channel
     *         with the specified name
     *         (3) {@link ErrorCode#USER_NOT_IN_CHANNEL} if the
     *         user being kicked is not a member of the channel
     *         (4) {@link ErrorCode#USER_NOT_OWNER} if the sender is not
     *         the owner of the channel
     */
    public ResponseSet kickUser(KickCommand kickCommand) {
        return null;
    }

}







