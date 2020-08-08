package chat.server.algorithms.mutex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutexToken implements Serializable {
    /**
     * the map of the token. The key is the identity (integer) of the process
     * and the object is the value (integer) of the clock of the process.
     */
    private Map<Integer, Integer> token;


    /**
     * the constructor with an empty {@link java.util.Map}.
     */
    public MutexToken() {
        token = new HashMap<>();
        assert invariant();
    }

    /**
     * the constructor with a {@link java.util.Map} built as a clone of an existing
     * object.
     *
     * @param mutexToken the origin object to clone
     */
    public MutexToken(final MutexToken mutexToken) {
        token = new HashMap<>(mutexToken.token);
        assert invariant();
    }

    /**
     * checks the invariant of the class: scalar clock are greater than or equal to
     * 0.
     *
     * NB: the method is final so that the method is not overridden in potential
     * subclasses because it is called in the constructor.
     *
     * @return a boolean stating whether the invariant is maintained.
     */
    public final boolean invariant() {
        boolean result = true;
        for (Integer value : token.values()) {
            if (value.intValue() < 0) {
                return false;
            }
        }
        return result;
    }

    /**
     * gets the value (integer) of the clock of a process (integer key). The
     * returned value is either the value found or the default value {@code 0}.
     *
     * @param key the identifier (integer) of the process.
     * @return the clock value.
     */
    public int getEntry(final int key) {
        return token.getOrDefault(key, 0);
    }


    /**
     * sets the value (integer) of the clock of the process (integer key). If the
     * corresponding key does not already exists in the map, it is inserted.
     *
     * @param key   the identifier (integer) of the process. An
     *              IllegalArgumentException is thrown in case of a negative value.
     * @param value the value of the clock.
     */
    public void setEntry(final int key, final int value) {
        if (key < 0) {
            throw new IllegalArgumentException("identite de processus non valide (" + key + ")");
        }
        token.put(key, value);
        assert invariant();
    }

    /**
     * states whether this token equals the token provided in the
     * argument {@code other}. This method does not override
     * {@link Object#equals(Object)} because the two collections may be different
     * while this method returns {@code true}.
     *
     * @param other the token to compare to.
     * @return the boolean of the condition greater or equal.
     */
    public boolean isEqualTo(final MutexToken other) {
        if (other != null) {
            List<Integer> keys = new ArrayList<>(token.keySet());
            keys.addAll(other.token.keySet());
            for (Integer key : keys) {
                if (this.getEntry(key) != other.getEntry(key)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return token.toString();
    }
}
