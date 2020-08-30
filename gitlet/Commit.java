package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;

/**
 * Commit Object.
 * @author Shelden Shi
 */
public class Commit implements Serializable {
    /** Commit message. */
    private String _message;
    /** Parent of this Commit. */
    private String _parent1;
    /** Parent2 of this Commit. */
    private String _parent2 = null;
    /** Time this Commit is created. */
    private String time;
    /** File name maped with blob object ID. */
    private HashMap<String, String> nameBlobMap;
    /** All parent of this Commit. */
    private ArrayList<String> partents;

    /** Constructor for Commit class.
     * @param message message input
     * @param parent parent of this Commit object
     */
    public Commit(String message, String parent) throws IOException {
        _message = message;
        _parent1 = parent;
        this.nameBlobMap = new HashMap<>();
        this.partents = new ArrayList<>();
        if (_parent1 == null) {
            this.time = getTime(true);
        } else {
            this.time = getTime(false);
        }
        if (_parent1 != null) {
            File parentFile = Utils.join(Files.LOGSCOMMITS, _parent1);
            Commit parentObj = Utils.readObject(parentFile, Commit.class);
            Stage stage = Stage.getSTAGE();
            this.partents.addAll(parentObj.partents);
            partents.add(parent);
            nameBlobMap.putAll(parentObj.getNameBlobMap());
            for (String fileName : stage.getAdditionStage()) {
                nameBlobMap.put(fileName,
                        stage.getAddNameCodeMap().get(fileName));
            }
            stage.clearAdditionStage();

            for (String fileName : stage.getRemovalStage()) {
                nameBlobMap.remove(fileName);
            }
            stage.clearRemovalStage();
            Stage.updateStage(stage);
        }
        storeCommit();
    }

    /**
     * Constructor for Commit class.
     * @param message message of the Commit.
     * @param parent1 parent 1 of Commit.
     * @param parent2 parent 2 of this Commit.
     * @throws IOException
     */
    public Commit(String message, String parent1,
                  String parent2) throws IOException {
        _message = message;
        _parent1 = parent1;
        _parent2 = parent2;
        this.nameBlobMap = new HashMap<>();
        this.time = getTime(false);
        this.partents = new ArrayList<>();
        File parentFile = Utils.join(Files.LOGSCOMMITS, parent1);
        Commit parentObj = Utils.readObject(parentFile, Commit.class);
        Stage stage = Stage.getSTAGE();
        partents.addAll(parentObj.partents);
        partents.add(parent2); partents.add(parent1);
        nameBlobMap.putAll(parentObj.getNameBlobMap());
        for (String fileName : stage.getAdditionStage()) {
            nameBlobMap.put(fileName,
                    stage.getAddNameCodeMap().get(fileName));
        }
        stage.clearAdditionStage();

        for (String fileName : stage.getRemovalStage()) {
            nameBlobMap.remove(fileName);
        }
        stage.clearRemovalStage();
        Stage.updateStage(stage);
        storeCommit();
    }

    /**
     * Returns the head commit.
     * @return head commit
     */
    public static Commit getHeadCommitObj() {
        File head = Utils.join(Files.LOGSCOMMITS, getHeadCommitCode());
        return Utils.readObject(head, Commit.class);
    }

    /**
     * Returns the head Commit's code.
     * @return sha1 code
     */
    public static String getHeadCommitCode() {
        File headCodeFile = new File(Utils.readContentsAsString(Files.HEAD));
        return Utils.readContentsAsString(headCodeFile);
    }

    /**
     * Returns the head Commit's name.
     * @return head Commit's name
     */
    public static String getHeadCommitName() {
        File headCodeFile = new File(Utils.readContentsAsString(Files.HEAD));
        if (!headCodeFile.getParentFile().getName().equals("heads")) {
            return headCodeFile.getParentFile().toString() + "/"
                    + headCodeFile.getName();
        }
        return headCodeFile.getName();
    }

    /**
     * Get Name:Blobcode hash map.
     * @return nameBlobMap
     */
    public HashMap<String, String> getNameBlobMap() {
        return nameBlobMap;
    }

    /**
     * Get parents array.
     * @return parents array
     */
    public ArrayList<String> getPartents() {
        return partents;
    }

    /**
     * Stores Commit object in a file.
     * @throws IOException
     */
    public void storeCommit() throws IOException {
        File currCommit = Utils.join(Files.LOGSCOMMITS, code());
        currCommit.createNewFile();
        Utils.writeObject(currCommit, this);
        File currCommitLog = Utils.join(Files.LOGSCOMMITSHIS, code());
        currCommitLog.createNewFile();


        String history = Utils.readContentsAsString(
                Files.LOGSREFSHEADSSHARED);
        Formatter out = new Formatter();
        out.format("===%n");
        out.format("commit %s", code());
        if (_parent2 != null) {
            out.format("%nMerge: " + _parent1.substring(0, 7)
                    + " " + _parent2.substring(0, 7));
        }
        out.format("%nDate: %s%n", time);
        if (_message.equals("initial commit")) {
            out.format(_message);
            String currLog = out.toString();
            Utils.writeContents(
                    Files.LOGSREFSHEADSSHARED, currLog + '\n');
            Utils.writeContents(
                    currCommitLog, out.toString() + '\n');
        } else {
            out.format(_message + "%n");
            String currLog = out.toString();
            Utils.writeContents(
                    Files.LOGSREFSHEADSSHARED, currLog
                            + '\n' + history);
            String lastCommitLog = Utils.readContentsAsString(
                    Utils.join(Files.LOGSCOMMITSHIS,
                            getHeadCommitCode()));
            Utils.writeContents(currCommitLog, out.toString()
                    + '\n' + lastCommitLog);
        }


        if (!Files.HEAD.exists()) {
            Files.updateHead("master", code());
        } else {
            Files.updateHead(getHeadCommitName(), code());
        }

        MessageCode mapObject = Utils.readObject(
                Files.MESSAGECODE, MessageCode.class);
        HashMap<String, String> messageCode = mapObject.getMap();
        if (messageCode.get(_message) != null) {
            String s = messageCode.get(_message);
            s += "\n" + code();
            messageCode.put(_message, s);
        } else {
            messageCode.put(_message, code());
        }
        mapObject.update();
    }
    /**
     * Gets a commit object with given ID in given remote.
     * @param remoteRepo given remote
     * @param commitID Commit ID
     * @return Commit obj
     */
    public static Commit getRemoteCommit(File remoteRepo, String commitID) {
        File commits = Utils.join(remoteRepo,
                "/logs/commits/" + commitID);
        Commit remoteCommit = Utils.readObject(commits, Commit.class);
        return remoteCommit;
    }

    /**
     * Gets commit Id of the given remote and branch name's head.
     * @param remoteRepo given remote
     * @param branchName given branch name
     * @return commit id
     */
    public static String getRemoteBranchHeadID(File remoteRepo,
                                               String branchName) {
        File remoteBranchHeadIDFile = Utils.join(remoteRepo,
                "/refs/heads/" + branchName);
        String remoteBranchHeadID = Utils.readContentsAsString(
                remoteBranchHeadIDFile);
        return remoteBranchHeadID;
    }

    /**
     * Gets given remote and branch name's head Commit obj.
     * @param remoteRepo given remote
     * @param branchName given branch name
     * @return commit obj
     */
    public static Commit getRemoteBranchHeadObj(File remoteRepo,
                                                String branchName) {
        File remoteBranchHeadIDFile = Utils.join(remoteRepo,
                "/refs/heads/" + branchName);
        String remoteBranchHeadID = Utils.readContentsAsString(
                remoteBranchHeadIDFile);
        File remoteBranchHeadCommit = Utils.join(remoteRepo,
                "logs/commits/" + remoteBranchHeadID);
        Commit remoteBranchHeadCommitObj = Utils.readObject(
                remoteBranchHeadCommit, Commit.class);
        return remoteBranchHeadCommitObj;
    }

    /**
     * Gets given remote and branch Name's head's file.
     * @param remoteRepo given remote
     * @param branchName given branch name
     * @return a file.
     */
    public static File getRemoteBranchHeadFile(File remoteRepo,
                                               String branchName) {
        File remoteBranchHeadIDFile = Utils.join(remoteRepo,
                "/refs/heads/" + branchName);
        String remoteBranchHeadID = Utils.readContentsAsString(
                remoteBranchHeadIDFile);
        File remoteBranchHeadCommit = Utils.join(remoteRepo,
                "logs/commits/" + remoteBranchHeadID);
        return remoteBranchHeadCommit;
    }

    /**
     * Returns sha1 code of Commit object.
     * @return sha1 code
     */
    private String code() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Returns the current time after formatting.
     * iff init, returns time 0.
     * @param init iff return time 0
     * @return current time
     */
    private String getTime(boolean init) {
        Date current = new Date();
        SimpleDateFormat format = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        if (init) {
            current.setTime(0);
        }
        return format.format(current);
    }

}
