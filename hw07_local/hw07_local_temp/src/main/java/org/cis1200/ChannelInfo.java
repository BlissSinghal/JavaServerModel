package org.cis1200;

import java.util.*;

public class ChannelInfo implements Comparable<ChannelInfo> {
    //attributes
    private final int ownerId;
    private String ownerName;
    private final String name;
    private Map<Integer, String> users;

    /**
     * Constructs a new ChannelInfo object with the owner info, channel name, and user list.
     *
     * @param ownerId The ID of the owner of the channel.
     * @param ownerName The name of the owner of the channel.
     * @param channelName The name of the channel.
     * @param users A map of user IDs and their names for the users in the channel.
     */
    public ChannelInfo(int ownerId, String ownerName, String channelName,
                       TreeMap<Integer, String> users) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = channelName;
        this.users = users;
    }

    /**
     * Gets the name of the channel owner.
     *
     * @return The name of the channel owner.
     */
    public String getOwner() {
        return ownerName;
    }

    /**
     * Gets the ID of the channel owner.
     *
     * @return The ID of the channel owner.
     */
    public int getOwnerID() {
        return ownerId;
    }

    /**
     * Gets the name of the channel.
     *
     * @return The name of the channel.
     */
    public String getChannelName() {
        return new String(name);
    }

    /**
     * Gets a sorted set of user IDs in the channel.
     *
     * @return A sorted set containing the IDs of all users in the channel.
     */
    public SortedSet<Integer> getUserIds() {
        return new TreeSet<>(users.keySet());
    }

    /**
     * Gets a sorted set of usernames in the channel.
     *
     * @return A sorted set containing the names of all users in the channel.
     */
    public SortedSet<String> getUserNames() {
        return new TreeSet<>(users.values());
    }

    /**
     * Removes a user from the channel by their user ID.
     *
     * @param userId The ID of the user to be removed from the channel.
     */
    public void deleteUser(int userId) {
        users.remove(userId);
    }

    /**
     * Adds a user to the channel with their ID and name.
     *
     * @param userId The ID of the user to add to the channel.
     * @param userName The name of the user to add to the channel.
     */
    public void addUser(int userId, String userName) {
        users.put(userId, userName);
    }

    /**
     * Checks if a user is in the channel.
     *
     * @param userId The ID of the user to check.
     * @return {@code true} if the user is in the channel; {@code false} otherwise.
     */
    public boolean userInChannel(int userId) {
        return users.containsKey(userId);
    }

    /**
     * Updates the nickname of a user in the channel.
     * If the user is the channel owner, their name is also updated.
     *
     * @param userId The ID of the user whose name will be updated.
     * @param newName The new name for the user.
     */
    public void updateNickName(int userId, String newName) {
        //updating the owner name if the user is the owner
        if (this.ownerId == userId) {
            this.ownerName = newName;
        }

        //if user is member in the channel, updating their nickname
        if (users.containsKey(userId)) {
            users.remove(userId);
            users.put(userId, newName);
        }


    }

    /**
     * Compares this ChannelInfo with another based on the ASCII sum of their channel names.
     *
     * @param that The other ChannelInfo object to compare to.
     * @return A negative integer, zero, or a positive integer as this ChannelInfo
     *         is less than, equal to, or greater than the specified ChannelInfo.
     */
    @Override
    public int compareTo(ChannelInfo that) {
        //getting the ascii values of both channel names
        return Integer.compare(getAscii(this.name), that.getAscii(that.getChannelName()));
    }

    /**
     * Helper for {@link #compareTo(ChannelInfo)}.
     *
     * Converts a string to the sum of ASCII values of its characters.
     *
     * @param str The string to convert.
     * @return The ASCII sum of all characters in the string.
     */
    private int getAscii(String str) {
        int ascii = 0;
        for (Character c : str.toCharArray()) {
            ascii += c;
        }
        return ascii;
    }

    /**
     * Compares this ChannelInfo to another object for equality based on the channel name.
     *
     * @param o The object to compare with.
     * @return {@code true} if the specified object is equal to this ChannelInfo;
     * {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelInfo that = (ChannelInfo) o;
        return name.equals(that.getChannelName());
    }

}
