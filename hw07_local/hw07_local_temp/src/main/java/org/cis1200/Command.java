package org.cis1200;

/**
 * A {@code Command} represents a string sent from a client to the server,
 * after parsing it into a more convenient form. Each concrete subclass of the
 * {@code Command} abstract class corresponds to a different sort of command
 * that
 * can be issued by a client.
 *
 * In addition to the various getter methods, all subclasses of this class
 * override
 * the {@code updateServerModel} method that calls the appropriate method of
 * the {@code ServerModel} class to process the command.
 *
 * You do not need to modify this file, but you should read through it to
 * make sure that you understand the various commands that the server needs
 * to process and the data fields that are included in each of these command
 * objects.
 * 
 */
public abstract class Command {

    /**
     * The userId for the sender of the {@code Command}.
     */
    private final Integer senderId;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId of this command
     */
    Command(Integer senderId) {
        this.senderId = senderId;
    }

    /**
     * Get the userId of the client who issued the {@code Command}.
     *
     * @return The userId of the client who issued this command
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Process the command and update the server model accordingly.
     *
     * @param model An instance of the {@link ServerModel} class which
     *              represents the current state of the server.
     * @return A {@link ResponseSet} object, informing clients about changes
     *         resulting from the command.
     */
    public abstract ResponseSet updateServerModel(ServerModel model);

    /**
     * Compare two commands
     *
     * Returns {@code true} if two {@code Command}s are equal; that is, if
     * they produce the same string representation.
     * 
     * Note that all subclasses of {@code Command} must override their
     * {@code toString} method appropriately for this definition to make sense.
     * (We have done this for you below.)
     *
     * @param o the object to compare with {@code this} for equality
     * @return true iff both objects are non-null and equal to each other
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Command)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }

}

// ==============================================================================
// Command subclasses
// ==============================================================================

/**
 * Represents a {@link Command} issued by a client to change his or her
 * nickname.
 */
class NicknameCommand extends Command {
    private final String newNickname;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId    of this command
     * @param newNickname nickname user would like to use
     */
    public NicknameCommand(Integer senderId, String newNickname) {
        super(senderId);
        this.newNickname = newNickname;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.changeNickname(this);
    }

    /** @return the new nickname from the command */
    public String getNewNickname() {
        return newNickname;
    }

    @Override
    public String toString() {
        return String.format("NICK %s", newNickname);
    }
}

/**
 * Represents a {@link Command} issued by a client to create a new channel.
 */
class CreateCommand extends Command {
    private final String channel;
    private final boolean inviteOnly;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId   of this command
     * @param channel    name of the new channel to create
     * @param inviteOnly true if this should be a private channel (Task 5)
     */
    public CreateCommand(int senderId, String channel, boolean inviteOnly) {
        super(senderId);
        this.channel = channel;
        this.inviteOnly = inviteOnly;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.createChannel(this);
    }

    /** @return the new channel name */
    public String getChannel() {
        return channel;
    }

    /** @return whether this is a private channel */
    public boolean isInviteOnly() {
        return inviteOnly;
    }

    @Override
    public String toString() {
        int flag = inviteOnly ? 1 : 0;
        return String.format("CREATE %s %d", channel, flag);
    }
}

/**
 * Represents a {@link Command} issued by a client to join an existing
 * channel. All users in the channel (including the new one) should be
 * notified about when a "join" occurs.
 */
class JoinCommand extends Command {
    private final String channel;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId of this command
     * @param channel  name of the new channel the user wants to join
     */
    public JoinCommand(int senderId, String channel) {
        super(senderId);
        this.channel = channel;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.joinChannel(this);
    }

    /** @return the channel the user wants to join */
    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return String.format("JOIN %s", channel);
    }
}

/**
 * Represents a {@link Command} issued by a client to send a message to all
 * other clients in the channel.
 */
class MessageCommand extends Command {
    private final String channel;
    private final String message;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId of this command
     * @param channel  where the user wants to send a message to
     * @param message  text to be sent to all users in the channel
     */
    public MessageCommand(int senderId, String channel, String message) {
        super(senderId);
        this.channel = channel;
        this.message = message;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.sendMessage(this);
    }

    /** @return the channel where the message should be sent */
    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return String.format("MESG %s :%s", channel, message);
    }
}

/**
 * Represents a {@link Command} issued by a client to leave a channel.
 */
class LeaveCommand extends Command {
    private final String channel;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId of this command
     * @param channel  where the user wants to leave. If the user is the owner
     *                 of this channel, the channel is destroyed.
     */
    public LeaveCommand(int senderId, String channel) {
        super(senderId);
        this.channel = channel;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.leaveChannel(this);
    }

    /** @return channel the user wishes to leave */
    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return String.format("LEAVE %s", channel);
    }
}

/**
 * Represents a {@link Command} issued by a client to add another client to an
 * invite-only channel owned by the sender.
 */
class InviteCommand extends Command {
    private final String channel;
    private final String userToInvite;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId     of this command, must be the channel owner
     * @param channel      where the user wants to invite a new user to
     * @param userToInvite must not be a member of the channel
     */
    public InviteCommand(int senderId, String channel, String userToInvite) {
        super(senderId);
        this.channel = channel;
        this.userToInvite = userToInvite;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.inviteUser(this);
    }

    /** @return channel name for the invitation */
    public String getChannel() {
        return channel;
    }

    /** @return the nickname of the new user to invite */
    public String getUserToInvite() {
        return userToInvite;
    }

    @Override
    public String toString() {
        return String.format("INVITE %s %s", channel, userToInvite);
    }
}

/**
 * Represents a {@link Command} issued by a client to remove another client
 * from a channel owned by the sender. Everyone in the initial channel
 * (including the user being kicked) should be informed that the user was
 * kicked.
 */
class KickCommand extends Command {
    private final String channel;
    private final String userToKick;

    /**
     * Constructor; initializes the private fields of the object.
     *
     * @param senderId   of this command, should be channel owner
     * @param channel    where the owner wants to remove a user
     * @param userToKick must be a member of the channel
     */
    public KickCommand(int senderId, String channel, String userToKick) {
        super(senderId);
        this.channel = channel;
        this.userToKick = userToKick;
    }

    @Override
    public ResponseSet updateServerModel(ServerModel model) {
        return model.kickUser(this);
    }

    /** @return the channel name */
    public String getChannel() {
        return channel;
    }

    /** @return the nickname of the user to remove */
    public String getUserToKick() {
        return userToKick;
    }

    @Override
    public String toString() {
        return String.format("KICK %s %s", channel, userToKick);
    }
}
