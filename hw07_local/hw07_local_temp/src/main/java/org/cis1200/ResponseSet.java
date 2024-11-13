package org.cis1200;

import java.util.*;

/**
 * A {@code ResponseSet} stores a set of messages that should be sent in
 * response to processing a command.
 *
 * The server backend sends these messages to inform clients of relevant
 * events in their channels. For instance, many chat services
 * inform you when someone has been removed from a chat that you're in.
 *
 * You do not need to modify this file, but you will need to read and understand
 * how it works to complete the assignment.
 */
public final class ResponseSet {

    private final Set<Response> responses = new TreeSet<>();

    /**
     * Add a response to the set
     *
     * @param response The response to send
     */
    public boolean addMessage(Response response) {
        return responses.add(response);
    }

    // ==========================================================================
    // Factory methods
    // ==========================================================================

    /**
     * Convenience method for creating an empty response set.
     * 
     * @return an empty response set
     */
    public static ResponseSet empty() {
        return new ResponseSet();
    }

    /**
     * Convenience method for sending a single response.
     *
     * @param response the response to send
     * @return response that contains only that response
     */
    public static ResponseSet singleMessage(Response response) {
        ResponseSet responseSet = new ResponseSet();
        responseSet.addMessage(response);
        return responseSet;
    }

    // ==========================================================================
    // ResponseSet dispatch
    // ==========================================================================

    /**
     * You should not call this method yourself. Associates the stored responses
     * with the userIds
     * of the recipients. This * function will be called by the
     * {@link ServerBackend} before
     * dispatching the {@code ResponseSet}.
     *
     * @return a mapping from userId to a list of response strings that should be
     *         delivered by the
     *         {@link ServerBackend}
     */

    public Set<Response> getResponses() {
        return responses;
    }

    // ==========================================================================
    // Private utility methods
    // ==========================================================================

    // ==========================================================================
    // Overrides from Object
    // ==========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != ResponseSet.class) {
            return false;
        }
        return this.responses.equals(((ResponseSet) o).responses);
    }

    @Override
    public int hashCode() {
        return responses.hashCode();
    }

    @Override
    public String toString() {
        return responses.toString();
    }

}
