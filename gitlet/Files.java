package gitlet;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Stores all the File Object.
 * @author Shelden Shi
 */
public class Files {
    /** Current Working Directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** gitlet folder. */
    public static final File GITLET = Utils.join(CWD, ".gitlet");

    /** Txt that contains a directory to the head Commit. */
    public static final File HEAD = Utils.join(GITLET, "HEAD");

    /** A folder in .gitlet. */
    public static final File REFS = Utils.join(GITLET, "refs");

    /** logs folder in .gitlet. Contains a commits folder and a refs folder. */
    public static final File LOGS = Utils.join(GITLET, "logs");

    /** a folder in LOGS, contains a file for each of the commit we make,
     * named after it's code. */
    public static final File LOGSCOMMITS = Utils.join(
            LOGS, "commits");

    /** log for each commits. */
    public static final File LOGSCOMMITSHIS = Utils.join(
            LOGSCOMMITS, "commit logs");

    /** a folder in LOGS, contains a heads folder and a remotes folder. */
    public static final File LOGSREFS = Utils.join(
            LOGS, "refs");

    /** a folder in LOGSREFS, contains a file for each branch.*/
    public static final File LOGSREFSHEADS = Utils.join(
            LOGSREFS, "heads");


    /** a folder in LOGSREFS, contains log of the remote. */
    public static final File LOGSREFSREMOTE = Utils.join(
            LOGSREFS, "remotes");

    /** a txt that stores all the log histories lf the master head.*/
    public static final File LOGSREFSHEADSMASTER = Utils.join(
            LOGSREFSHEADS, "master");

    /** a txt that stores all the log histories lf the master head.*/
    public static final File LOGSREFSHEADSSHARED = Utils.join(
            LOGSREFSHEADS, "shared");

    /** objects folder in .gitlet. Contains all the blobs.
     * each of the blob object is stored in a file. */
    public static final File OBJECTS = Utils.join(
            GITLET, "objects");

    /** a file in .gitlet for stage objects. */
    public static final File STAGE = Utils.join(
            GITLET, "stage");

    /** a file that stores a hashMap Object for find. */
    public static final File MESSAGECODE = Utils.join(
            GITLET, "find");


    /** A folder in .gitlet/refs. */
    public static final File REFSHEADS = Utils.join(
            REFS, "heads");

    /** A txt that stores the sh1 code for the head. */
    public static final File REFSHEADSMASTER = Utils.join(
            REFSHEADS, "master");

    /** a folder in .gitlet that will contain a file for each remote.
     * each file contains information about the remote's working directory.
     */
    public static final File REMOTES = Utils.join(GITLET, "remotesDir");

    /** Creates folders and texts to store. */
    public static void fileInitializer() throws IOException {
        GITLET.mkdir();
        REFS.mkdir();
        REFSHEADS.mkdir();
        REFSHEADSMASTER.createNewFile();
        LOGS.mkdir();
        LOGSCOMMITS.mkdir();
        LOGSREFS.mkdir();
        LOGSREFSHEADS.mkdir();
        LOGSREFSHEADSMASTER.createNewFile();
        LOGSREFSHEADSSHARED.createNewFile();
        LOGSREFSREMOTE.mkdir();
        STAGE.createNewFile();
        MESSAGECODE.createNewFile();
        OBJECTS.mkdir();
        LOGSCOMMITSHIS.mkdir();
        REMOTES.mkdir();
    }

    /**
     * Update the head to a newly created Commit.
     * @param branch current branch name
     * @param code code of this Commit
     */
    public static void updateHead(String branch, String code) {
        File file = Utils.join(Files.REFSHEADS, branch);
        Utils.writeContents(Files.HEAD, file.getPath());
        Utils.writeContents(file, code);
    }

    /**
     * A FileFilter that exclude .gitlet.
     * @return a filter.
     */
    public static FileFilter filter() {
        FileFilter filter = file -> {
            if (file.isDirectory() || file.isHidden()) {
                return false;
            }
            return true;
        };
        return filter;
    }

}

