package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A stage that gitlet works on.
 * Keeps track of files added, removed.
 * Stage is cleared after a commit.
 * a gitlet works on one stage.
 * @author Shelden Shi
 */
public class Stage implements Serializable {

    /** addition Stage. */
    private ArrayList<String> additionStage;

    /** removal Stage. */
    private ArrayList<String> removalStage;

    /** a HashMap that matches name of the file
     * that is in the add stage and the file blob code. */
    private HashMap<String, String> addNameCodeMap;

    /** Constructor of Stage. */
    public Stage() {
        additionStage = new ArrayList<>();
        removalStage = new ArrayList<>();
        addNameCodeMap = new HashMap<>();
    }

    /** Checks if that has been committed.
     * if not add to AS
     * create a blob of the file and compare it with the last commit
     * @param file input file
     */
    public void processFile(File file) throws IOException {
        String fileName = file.getName();
        Commit head = Commit.getHeadCommitObj();
        HashMap<String, String> headMap = head.getNameBlobMap();
        Blob newBlob = new Blob(file);
        if (!headMap.containsKey(fileName)) {
            additionStage.add(fileName);
            addNameCodeMap.put(fileName, newBlob.code());
        } else {
            if (removalStage.contains(fileName)
                    && headMap.get(fileName).equals(newBlob.code())) {
                removalStage.remove(fileName);
            } else if (!newBlob.equals(headMap.get(fileName))) {
                if (additionStage.contains(fileName)) {
                    additionStage.remove(fileName);
                    addNameCodeMap.remove(fileName);
                }
                additionStage.add(fileName);
                addNameCodeMap.put(fileName, newBlob.code());
            }
        }
    }

    /**
     * Checks if there is anything new.
     * @return if updated
     */
    public boolean hasUpdate() {
        if (additionStage.isEmpty() && removalStage.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Clears addition stage.
     */
    public void clearAdditionStage() {
        additionStage.clear();
        addNameCodeMap.clear();
    }

    /**
     * Clears removal stage.
     */
    public void clearRemovalStage() {
        removalStage.clear();
    }

    /**
     * get the addNameCodeMap instance variable.
     * @return addNameCodeMap
     */
    public HashMap<String, String> getAddNameCodeMap() {
        return addNameCodeMap;
    }

    /**
     * gets the additialStage instance variable.
     * @return AdditionStage
     */
    public ArrayList<String> getAdditionStage() {
        return additionStage;
    }

    /**
     * gets the removalStage instance variable.
     * @return removalStage
     */
    public ArrayList<String> getRemovalStage() {
        return removalStage;
    }

    /**
     * updates Stage object in the stage file.
     * @param stage stage object.
     */
    public static void updateStage(Stage stage) {
        Utils.writeObject(Files.STAGE, stage);
    }


    /**
     * reads Stage from stage file.
     * @return Stage from stage file
     */
    public static Stage getSTAGE() {
        return Utils.readObject(Files.STAGE, Stage.class);
    }
}
