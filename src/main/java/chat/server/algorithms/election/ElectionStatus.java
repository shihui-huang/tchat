package chat.server.algorithms.election;

public enum ElectionStatus {
    /**
     * Dormant election status.
     */
    DORMANT,
    /**
     * Initiator election status.
     */
    INITIATOR,
    /**
     * Leader election status.
     */
    LEADER,
    /**
     * Non leader election status.
     */
    NON_LEADER,
}
