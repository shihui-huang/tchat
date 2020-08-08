package chat.server.algorithms.mutex;

public enum MutexStatus {
    /**
     * dans la section critique.
     */
    DANS_SC,
    /**
     * hors de la section critique.
     */
    HORS_SC,
    /**
     * en attente à l'entrée de la section critique.
     */
    EN_ATT,
}
