package org.cis1200;

import java.util.*;

/**
 * A message that the server sends to a client in response to a command
 *
 * Each response has two parts: the userId for the recipient of the response
 * and a string containing the text of the message itself. For example
 *
 * The server should construct instances of this class using the static
 * methods that automatically create the message text from the arguments
 * of the message.
 * {#link Response#okay} - A Command processed successfully
 * {#link Response#error} - A Command produced an error
 * {#link Response#connected} - A new user connected to the server
 * {#link Response#disconnected} - A user disconnected from the server
 * {#link Response#names} - A listing of names in a channel
 */
public final class Response implements Comparable<Response> {
    final private Integer recipientId; // userId of recipient
    final private String text; // text of response

    /*
     * This constructor is private because your code should use
     * only use the factory methods below to create Responses
     */
    private Response(Integer recipientId, String text) {
        this.recipientId = recipientId;
        this.text = text;
    }

    /** @return the recipient's userId */
    public Integer getRecipientId() {
        return recipientId;
    }

    /** @return the text of the response */
    public String getText() {
        return text;
    }

    @Override
    public int compareTo(Response o) {
        int x = this.recipientId.compareTo(o.recipientId);
        if (x != 0) {
            return x;
        } else {
            return this.text.compareTo(o.text);
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "recipientId=" + recipientId +
                ", message='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Response response1)) {
            return false;
        }
        return recipientId.equals(response1.recipientId) &&
                text.equals(response1.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientId, text);
    }

    // ==============================================================================
    // Factory methods
    // ==============================================================================

    /**
     * A {@code Response} for the case when a user first connects to the
     * server and should be informed of their new nickname
     *
     * @param userId       for the newly connected client
     * @param userNickname for the client, generated by the server
     * @return CONNECT response
     */
    public static Response connected(Integer userId, String userNickname) {
        return new Response(userId, String.format(":%s CONNECT", userNickname));
    }

    /**
     * A {@code Response} for the case when a user disconnects from the
     * server and other clients should be informed of this fact.
     *
     * @param recipientId    the userId of the recipient
     * @param senderNickname the nickname of the disconnected user
     * @return QUIT response
     */
    public static Response disconnected(Integer recipientId, String senderNickname) {
        return new Response(recipientId, String.format(":%s QUIT", senderNickname));
    }

    /**
     * Response indicating no error has occurred when processing the specified
     * command.
     * This response merely echos the command preceded by the nickname of the
     * user that issue it.
     *
     * @param recipientId    the userId of the recipient
     * @param senderNickname the nickname of user who sent the command
     * @param command        {@code Command} to be echoed
     * @return OKAY response
     */
    public static Response okay(Integer recipientId, String senderNickname, Command command) {
        return new Response(recipientId, ":" + senderNickname + " " + command.toString());
    }

    /**
     * Constructs an error {@code Response} for the case where a client's
     * {@link Command}
     * is invalid, and the client should be informed.
     *
     * The response includes a code describing the error.
     *
     * @param command   The command that caused the error
     * @param errorCode The {@link ErrorCode} that the command caused
     * @return ERROR response
     */
    public static Response error(Command command, ErrorCode errorCode) {
        return new Response(
                command.getSenderId(),
                String.format(":%s ERROR %d", "$server", errorCode.getCode())
        );
    }

    /**
     * Generate a {@code NAMES} response that informs a client of the users that
     * are present in a channel.
     *
     * @param userId       that requested the list of names
     * @param userNickname that requested the list of names
     * @param channelName  name of the channel
     * @param channelUsers nicknames of the users in the channel
     * @param channelOwner nickname of the channel owner
     * @return NAMES response
     * @throws IllegalArgumentException if the channelOwner is not present in
     *                                  channelUsers
     */
    public static Response names(
            Integer userId, String userNickname,
            String channelName, SortedSet<String> channelUsers,
            String channelOwner
    ) {
        if (!channelUsers.contains(channelOwner)) {
            throw new IllegalArgumentException(channelOwner + " must be in " + channelUsers);
        }
        StringBuilder payload = new StringBuilder();
        for (String memberNickname : channelUsers) {
            if (memberNickname.equals(channelOwner)) {
                payload.append('@');
            }
            payload.append(memberNickname);
            payload.append(' ');
        }
        return new Response(
                userId,
                String.format(
                        ":%s NAMES %s :%s",
                        userNickname, channelName, payload.toString().trim()
                )
        );
    }

    /**
     * Generate a {@code NAMES} response that informs a client of the users that
     * are present in a channel. This method should only be used for testing
     * purposes. Do not hard-code the payload string from within ServerModel.
     *
     * @param userId       that requested the list of names
     * @param userNickname nickname for user that issued request
     * @param channelName  name of the channel
     * @param payload      string containing all nicknames in channel,
     *                     in alphabetical order,
     *                     with channel owner marked with '@' sign.
     * @return NAMES response
     */

    public static Response names(
            Integer userId, String userNickname,
            String channelName, String payload
    ) {
        return new Response(
                userId,
                String.format(":%s NAMES %s :%s", userNickname, channelName, payload)
        );
    }
}