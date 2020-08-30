package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Stores a Hashmap that matches message with commit code(s).
 * @author Shelden Shi
 */
public class MessageCode implements Serializable {
    /** HashMap that MessageCode object is storing.*/
    private HashMap<String, String> map;

    /**
     * Constructor of Message Code Objects.
     */
    public MessageCode() {
        map = new HashMap<>();
    }

    /**
     * Gets the map variable.
     * @return map
     */
    public HashMap<String, String> getMap() {
        return map;
    }

    /**
     * Updates the MessageCode object,
     * writes it in a file.
     */
    public void update() {
        Utils.writeObject(Files.MESSAGECODE, this);
    }
}
