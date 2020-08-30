package gitlet;




import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

/**
 * A class that contains all the commands for gitlet.
 * @author Shelden Shi
 */
public class Commands {
    /** Creates a new Gitlet version-control system in the current directory.
     * @param args Array in format: {'init'}
     */
    public static void init(String[] args) throws IOException {
        validateNumArgs(args, 1);
        if (!Files.GITLET.exists()) {
            Files.fileInitializer();
        } else {
            Main.exitWithError("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
        Stage stage = new Stage();
        Stage.updateStage(stage);
        Utils.writeObject(Files.MESSAGECODE, new MessageCode());
        new Commit("initial commit", null);
    }


    /** Adds a copy of the file as it currently exist to the staging area.
     * @param args Array in format: {'add' 'file name'}
     */
    public static void add(String[] args) throws IOException {
        validateNumArgs(args, 2);
        File file = Utils.join(Files.CWD, args[1]);
        if (!file.exists()) {
            Main.exitWithError("File does not exist.");
        }
        Stage stage = Stage.getSTAGE();
        stage.processFile(file);
        Stage.updateStage(stage);
    }

    /** Saves a snapshot of certain files in the current commit
     * and staging area,
     * so they can be restored at a later time.
     * Creating a new commit.
     *  @param args Array in format: {'commit' 'message'}
     */
    public static void commit(String[] args) throws IOException {
        if (args.length == 1 || args[1].isEmpty()) {
            Main.exitWithError("Please enter a commit message.");
        }
        validateNumArgs(args, 2);
        String message = args[1];
        Stage stage = Stage.getSTAGE();
        if (!stage.hasUpdate()) {
            Main.exitWithError("No changes added to the commit.");
        }
        String headCode = Commit.getHeadCommitCode();
        new Commit(message, headCode);
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * @param args Array in format: {'rm' 'file name'}
     */
    public static void rm(String[] args) {
        validateNumArgs(args, 2);
        File file = Utils.join(Files.CWD, args[1]);
        String fileName = file.getName();
        Stage stage = Stage.getSTAGE();
        Commit head = Commit.getHeadCommitObj();
        if (!head.getNameBlobMap().containsKey(fileName)
                && !stage.getAdditionStage().contains(fileName)) {
            Main.exitWithError("No reason to remove the file.");
        }
        if (stage.getAdditionStage().contains(fileName)) {
            stage.getAdditionStage().remove(fileName);
        } else {
            stage.getRemovalStage().add(fileName);
            Utils.restrictedDelete(file);
        }
        Stage.updateStage(stage);
    }

    /** Displays what branches currently exist,
     * and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * @param args Array in format: {'status'}
     */
    public static void status(String[] args) throws IOException {
        validateNumArgs(args, 1);
        Stage stage = Stage.getSTAGE();
        ArrayList<String> branchNames = new ArrayList<>();
        for (File file : Files.REFSHEADS.listFiles()) {
            String filename = file.getName();
            branchNames.add(filename); }
        if (!branchNames.get(0).equals("master")) {
            String other = branchNames.remove(0);
            branchNames.add(other);
        }
        ArrayList<String> addNames = stage.getAdditionStage();
        ArrayList<String> rmNames = stage.getRemovalStage();
        ArrayList<String> deleted = new ArrayList<>();
        ArrayList<String> modified = new ArrayList<>();
        ArrayList<String> untracked = new ArrayList<>();
        ArrayList<String> curr = new ArrayList<>();
        Commit head = Commit.getHeadCommitObj();
        HashMap<String, String> headMap = head.getNameBlobMap();
        for (File file : Files.CWD.listFiles(Files.filter())) {
            curr.add(file.getName());
            String fileName = file.getName();
            Blob blob = new Blob(file);
            if (headMap.get(fileName) == null) {
                if (!stage.getAdditionStage().contains(fileName)
                        && !stage.getRemovalStage().contains(fileName)) {
                    untracked.add(fileName);
                }
            } else if (!blob.equals(headMap.get(fileName))
                    && !blob.equals(stage.getAddNameCodeMap().get(fileName))) {
                modified.add(fileName + " (modified)");
            }
        }
        for (String s: headMap.keySet()) {
            if (!curr.contains(s) && !rmNames.contains(s)) {
                deleted.add(s + " (deleted)");
            }
        }
        Formatter out = new Formatter();
        out.format("=== Branches ===%n");
        for (String s : branchNames) {
            if (s.equals(Commit.getHeadCommitName())) {
                out.format("*%s%n", s);
            } else {
                out.format("%s%n", s);
            }
        }
        out.format("%n=== Staged Files ===%n");
        addNames.forEach(s -> out.format("%s%n", s));
        out.format("%n=== Removed Files ===%n");
        rmNames.forEach(s -> out.format("%s%n", s));
        out.format("%n=== Modifications Not Staged For Commit ===%n");
        modified.forEach(s -> out.format("%s%n", s));
        deleted.forEach(s -> out.format("%s%n", s));
        out.format("%n=== Untracked Files ===%n");
        untracked.forEach(s -> out.format("%s%n", s));
        System.out.println(out.toString());
    }

    /**
     * Starting at the current head commit,
     * display information about each commit backwards
     * along the commit tree until the initial commit,
     * following the first parent commit links,
     * ignoring any second parents found in merge commits.
     * @param args Array in format: {'log'}
     */
    public static void log(String[] args) {
        validateNumArgs(args, 1);
        String head = Commit.getHeadCommitCode();
        String log = Utils.readContentsAsString(
                Utils.join(Files.LOGSCOMMITSHIS, head));
        System.out.println(log);
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     * @param args Array in format: {'global-log'}
     */
    public static void globalLog(String[] args) {
        validateNumArgs(args, 1);
        String log = Utils.readContentsAsString(Files.LOGSREFSHEADSSHARED);
        System.out.println(log);
    }

    /**
     * Prints out the ids of all commits that have
     * the given commit message, one per line.
     * @param args Array in format: {'find' 'message'}
     */
    public static void find(String[] args) {
        validateNumArgs(args, 2);
        MessageCode mapObject = Utils.readObject(
                Files.MESSAGECODE, MessageCode.class);
        HashMap<String, String> map = mapObject.getMap();
        if (map.get(args[1]) == null) {
            Main.exitWithError("Found no commit with that message.");
        } else {
            System.out.println(map.get(args[1]));
        }
    }

    /**
     * Checkout is a kind of general command that can do a few different
     * things depending on what its arguments are.
     * There are 3 possible use cases.
     * In each section below, you'll see 3 bullet points.
     * Each corresponds to the respective usage of checkout.
     * @param args Array in format: {'checkout' '--' 'file name'} or
     *             Array in format: {'checkout' 'commit id' '--' 'file name'}
     *             Array in format: {'checkout' 'branch name'}
     */
    public static void checkout(String[] args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                Main.exitWithError("Incorrect operands.");
            }
            File file = Utils.join(Files.CWD, args[2]);
            Commit head = Commit.getHeadCommitObj();
            checkoutHelper(head, file);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                Main.exitWithError("Incorrect operands.");
            }
            File file = Utils.join(Files.CWD, args[3]);
            File commitFile = null;
            for (File f : Files.LOGSCOMMITS.listFiles(Files.filter())) {
                if (f.getName().contains(args[1])) {
                    commitFile = f;
                }
            }

            if (commitFile == null) {
                Main.exitWithError("No commit with that id exists.");
            }
            Commit commit = Utils.readObject(commitFile, Commit.class);
            checkoutHelper(commit, file);
        } else if (args.length == 2) {
            File fileBranch = Utils.join(Files.REFSHEADS, args[1]);
            if (!fileBranch.exists()) {
                Main.exitWithError("No such branch exists.");
            } else if (args[1].equals(Commit.getHeadCommitName())) {
                Main.exitWithError("No need to checkout the current branch.");
            }
            String headCode = Utils.readContentsAsString(fileBranch);
            Commit commit = Utils.readObject(Utils.join(
                    Files.LOGSCOMMITS, headCode), Commit.class);
            checkoutBranchHelper(args[1], commit, headCode);
        } else {
            throw new GitletException("Incorrect operands.");
        }
    }

    /**
     * Does most of the work for checking out a branch.
     * 1. checks untracked files, if they will be modified error.
     * 2. checkout each of the files
     * @param branch branch name
     * @param commit the current commit
     * @param headCode current commit sha1 code
     */
    private static void checkoutBranchHelper(String branch,
                                             Commit commit, String headCode) {
        HashMap<String, String> map  = commit.getNameBlobMap();
        Stage stage = Stage.getSTAGE();
        Commit head = Commit.getHeadCommitObj();
        ArrayList<String> untracked = new ArrayList<>();
        for (File file : Files.CWD.listFiles(Files.filter())) {
            String fileName = file.getName();
            if (head.getNameBlobMap().get(fileName) == null
                    && !stage.getAdditionStage().contains(fileName)
                    || stage.getRemovalStage().contains(fileName)) {
                untracked.add(fileName);
            }
        }
        for (String s : map.keySet()) {
            File file = Utils.join(Files.CWD, s);
            checkUntracked(untracked, s);
            checkoutHelper(commit, file);
        }

        for (String s : head.getNameBlobMap().keySet()) {
            if (map.get(s) == null) {
                checkUntracked(untracked, s);
                Utils.restrictedDelete(s);
            }
        }
        stage.clearRemovalStage();
        stage.clearAdditionStage();
        Stage.updateStage(stage);
        Files.updateHead(branch, headCode);
    }
    /**
     * Helps checkout a file.
     * @param commit takes a commit to restore the file from
     * @param file file that is going to be overwriten.
     */
    private static void checkoutHelper(Commit commit, File file) {
        if (commit.getNameBlobMap().get(file.getName()) == null) {
            Main.exitWithError("File does not exist in that commit.");
        }
        String blobCode = commit.getNameBlobMap().get(file.getName());
        File blobFile = Utils.join(Files.OBJECTS, blobCode);
        Blob blob = Utils.readObject(blobFile, Blob.class);
        String contents = blob.getContents();
        Utils.writeContents(file, contents);
    }

    /**
     * Creates a new branch.
     * @param args Array in format: {'branch' 'branch name'}
     * @throws IOException
     */
    public static void branch(String[] args) throws IOException {
        validateNumArgs(args, 2);
        File branch;
        if (!args[0].equals("branch")) {
            File remoteRepo = new File(args[0]);
            branch = Utils.join(remoteRepo, "refs/heads/" + args[1]);
        } else {
            branch = Utils.join(Files.REFSHEADS, args[1]);
            if (branch.exists()) {
                Main.exitWithError("A branch with that name already exists");
            }
            branch.createNewFile();
        }
        Utils.writeContents(branch, Commit.getHeadCommitCode());
    }

    /**
     * Deletes the branch with the given name.
     * @param args Array in format: {'rm-branch' 'branch name'}
     * @throws IOException
     */
    public static void rmBranch(String[] args) throws IOException {
        validateNumArgs(args, 2);
        File branch = Utils.join(Files.REFSHEADS, args[1]);
        if (!branch.exists()) {
            Main.exitWithError("A branch with that name does not exist.");
        } else if (args[1].equals(Commit.getHeadCommitName())) {
            Main.exitWithError("Cannot remove the current branch.");
        }
        branch.delete();
        String masterLog = Utils.readContentsAsString(
                Files.LOGSREFSHEADSMASTER);
        String sharedLog = Utils.readContentsAsString(
                Files.LOGSREFSHEADSSHARED);
        sharedLog = masterLog + sharedLog;
        Utils.writeContents(Files.LOGSREFSHEADSSHARED, sharedLog);
        Files.LOGSREFSHEADSMASTER.delete();
        Files.LOGSREFSHEADSMASTER.createNewFile();
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * @param args Array in format: {'reset' 'commit id'}
     */
    public static void reset(String[] args) {
        validateNumArgs(args, 2);
        File commitFile = Utils.join(Files.LOGSCOMMITS, args[1]);
        if (!commitFile.exists()) {
            Main.exitWithError("No commit with that id exists.");
        }
        Commit commit = Utils.readObject(Utils.join(
                Files.LOGSCOMMITS, args[1]), Commit.class);
        checkoutBranchHelper(Commit.getHeadCommitName(), commit, args[1]);
    }

    /**
     * merges two commits together.
     * @param args Array in format: {'merge' 'branch name'}
     * @throws IOException
     */
    public static void merge(String[] args) throws IOException {
        validateNumArgs(args, 2);
        File branch = Utils.join(Files.REFSHEADS, args[1]);
        Stage stage = Stage.getSTAGE();
        ArrayList<String> untracked = new ArrayList<>();
        String headCode = Commit.getHeadCommitCode();
        Commit head = Commit.getHeadCommitObj();
        HashMap<String, String> headMap = head.getNameBlobMap();
        if (!stage.getRemovalStage().isEmpty()
                || !stage.getAdditionStage().isEmpty()) {
            Main.exitWithError("You have uncommitted changes.");
        } else if (!branch.exists()) {
            Main.exitWithError("A branch with that name does not exist.");
        } else if (Commit.getHeadCommitName().equals(args[1])) {
            Main.exitWithError("Cannot merge a branch with itself.");
        } else {
            for (File file : Files.CWD.listFiles(Files.filter())) {
                String fileName = file.getName();
                if (!headMap.keySet().contains(fileName)) {
                    untracked.add(fileName);
                }
            }
        }
        String branchCode = Utils.readContentsAsString(branch);
        Commit branchHead = Utils.readObject(Utils.join(
                Files.LOGSCOMMITS, branchCode), Commit.class);
        HashMap<String, String> branchHeadMap = branchHead.getNameBlobMap();
        ArrayList<String> headParents = head.getPartents();
        ArrayList<String> branchParents = branchHead.getPartents();
        boolean encontered = false;
        if (headParents.contains(branchCode)) {
            Main.exitWithError(
                    "Given branch is an ancestor of the current branch.");
        } else if (branchParents.contains(headCode)) {
            checkout(new String[]{"checkout", args[1]});
            Main.exitWithError("Current branch fast-forwarded.");
        }
        String lastCommonID = null;
        for (int i = headParents.size() - 1; i > -1; i--) {
            if (branchParents.contains(headParents.get(i))) {
                lastCommonID = headParents.get(i);
                break;
            }
        }
        Commit lastShared = Utils.readObject(
                Utils.join(Files.LOGSCOMMITS, lastCommonID), Commit.class);
        HashMap<String, String> lastSharedMap = lastShared.getNameBlobMap();
        ArrayList<String> checkedFiles = new ArrayList<>();
        branchHeadMap.keySet().forEach(s -> checkUntracked(untracked, s));
        Stage.updateStage(stage);
        encontered = checkSplitPoint(lastSharedMap, headMap, branchHeadMap,
                checkedFiles, untracked, branchCode);
        encontered = checkBranchHead(headMap, branchHeadMap,
                checkedFiles, untracked, branchCode, encontered);
        mergeCommit("Merged " + args[1] + " into "
                + Commit.getHeadCommitName() + ".", headCode, branchCode);
        if (encontered) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Check branch head for merge.
     * @param headMap headmap
     * @param branchHeadMap branch head map
     * @param checkedFiles files that have been checked
     * @param untracked untracked files
     * @param branchCode branch head code
     * @param encontered if encontered conflict
     * @return encontered
     * @throws IOException
     */
    private static boolean checkBranchHead(HashMap<String, String>
                                                   headMap,
                                           HashMap<String, String>
                                                   branchHeadMap,
                                           ArrayList<String>
                                                   checkedFiles,
                                           ArrayList<String>
                                                   untracked,
                                           String branchCode,
                                           boolean encontered)
            throws IOException {
        Stage stage = Stage.getSTAGE();
        for (String s : branchHeadMap.keySet()) {
            if (!checkedFiles.contains(s)) {
                String headBlob = headMap.get(s);
                String branchBlob = branchHeadMap.get(s);
                checkedFiles.add(s);
                checkUntracked(untracked, s);
                if (headBlob == null) {
                    checkout(new String[]{"checkout", branchCode, "--", s});
                    stage.getAdditionStage().add(s);
                    stage.getAddNameCodeMap().put(s, new Blob(s).code());
                } else if (branchBlob.equals(headBlob)) {
                    continue;
                } else if (!branchBlob.equals(headBlob)) {
                    encontered = true;
                    mergeConflict(s, headBlob, branchBlob);
                    stage.getAdditionStage().add(s);
                    stage.getAddNameCodeMap().put(s, new Blob(s).code());
                } else {
                    Main.exitWithError(
                            "I missed something in merge: checking branchhead");
                }
            }
        }
        Stage.updateStage(stage);
        return encontered;
    }

    /**
     * Check split commit for merge.
     * @param lastSharedMap last shared map
     * @param headMap head map
     * @param branchHeadMap branch head map
     * @param checkedFiles files that have been checked
     * @param untracked untracked files
     * @param branchCode branch head code
     * @return encontered
     * @throws IOException
     */
    private static boolean checkSplitPoint(HashMap<String, String>
                                                   lastSharedMap,
                                        HashMap<String, String>
                                                headMap,
                                        HashMap<String, String>
                                                   branchHeadMap,
                                        ArrayList<String>
                                                   checkedFiles,
                                        ArrayList<String>
                                                   untracked,
                                           String branchCode)
            throws IOException {
        boolean encontered = false;
        Stage stage = Stage.getSTAGE();
        for (String s : lastSharedMap.keySet()) {
            String headBlob = headMap.get(s);
            String branchBlob = branchHeadMap.get(s);
            String splitBlob = lastSharedMap.get(s);
            checkedFiles.add(s);
            checkUntracked(untracked, s);
            if (headBlob == null && branchBlob == null) {
                continue;
            } else if (headBlob == null
                    && !splitBlob.equals(branchBlob)) {
                encontered = true;
                mergeConflict(s, headBlob, branchBlob);
                stage.getAdditionStage().add(s);
                stage.getAddNameCodeMap().put(s, new Blob(s).code());
            } else if (headBlob == null
                    && splitBlob.equals(branchBlob)) {
                continue;
            } else if (branchBlob == null
                    && !splitBlob.equals(headBlob)) {
                encontered = true;
                mergeConflict(s, headBlob, branchBlob);
                stage.getAdditionStage().add(s);
                stage.getAddNameCodeMap().put(s, new Blob(s).code());
            } else if (branchBlob == null
                    && splitBlob.equals(headBlob)) {
                rm(new String[]{"no", s});
                stage = Stage.getSTAGE();
            } else if (splitBlob.equals(headBlob)
                    && splitBlob.equals(branchBlob)) {
                continue;
            } else if (splitBlob.equals(headBlob)
                    && !splitBlob.equals(branchBlob)) {
                checkout(new String[]{"checkout", branchCode, "--", s});
                stage.getAdditionStage().add(s);
                stage.getAddNameCodeMap().put(s, new Blob(s).code());
            } else if (!splitBlob.equals(headBlob)
                    && splitBlob.equals(branchBlob)) {
                continue;
            } else if (!splitBlob.equals(headBlob)
                    && !splitBlob.equals(headBlob)) {
                encontered = true;
                mergeConflict(s, headBlob, branchBlob);
                stage.getAdditionStage().add(s);
                stage.getAddNameCodeMap().put(s, new Blob(s).code());
            }
        }
        Stage.updateStage(stage);
        return encontered;
    }

    /**
     * Commit merge version.
     * @param message merge message
     * @param parent1 current head
     * @param parent2 the other branch's head
     * @throws IOException
     */
    private static void mergeCommit(String message,
                                    String parent1,
                                    String parent2) throws IOException {
        Stage stage = Stage.getSTAGE();
        if (!stage.hasUpdate()) {
            Main.exitWithError("No changes added to the commit.");
        }
        new Commit(message, parent1, parent2);
    }

    /**
     * check if a file is untracked.
     * @param untracked untracked list
     * @param fileName file name
     */
    private static void checkUntracked(ArrayList<String> untracked,
                                       String fileName) {
        if (untracked.contains(fileName)) {
            Main.exitWithError(
                    "There is an untracked file in the way; delete it, "
                            + "or add and commit it first.");
        }
    }
    /**
     * Deals with merge conflict. and overwrite that file.
     * @param fileName name of the file that is conflict at.
     * @param headBlob headBlob code.
     * @param branchBlob branch code.
     */
    private static void mergeConflict(String fileName,
                                      String headBlob,
                                      String branchBlob) {
        File file = Utils.join(Files.CWD, fileName);
        String headContents;
        String branchheadContents;
        if (headBlob == null) {
            headContents = null;
            branchheadContents = Utils.readObject(
                    Utils.join(Files.OBJECTS, branchBlob),
                    Blob.class).getContents();
        } else if (branchBlob == null) {
            headContents = Utils.readObject(
                    Utils.join(Files.OBJECTS, headBlob),
                    Blob.class).getContents();
            branchheadContents = null;
        } else {
            headContents = Utils.readObject(Utils.join(
                    Files.OBJECTS, headBlob),
                    Blob.class).getContents();
            branchheadContents = Utils.readObject(
                    Utils.join(Files.OBJECTS, branchBlob),
                    Blob.class).getContents();
        }
        Formatter out = new Formatter();
        out.format("<<<<<<< HEAD%n");
        if (headContents != null) {
            out.format(headContents.trim() + "%n");
        }
        out.format("=======%n");
        if (branchheadContents != null) {
            out.format(branchheadContents.trim() + "%n");
        }
        out.format(">>>>>>>%n");
        Utils.writeContents(file, out.toString());
    }

    /**
     * adds a remote.
     * @param args Array with format {"add-remote", "remote name" "remote dir"}
     * @throws IOException
     */
    public static void addRemote(String[] args) throws IOException {
        validateNumArgs(args, 3);
        String remoteName = args[1];
        File remoteFolder = new File(args[2]);
        File remoteDir = Utils.join(Files.REMOTES, remoteName);
        if (remoteDir.exists()) {
            Main.exitWithError("A remote with that name already exists.");
        }
        Utils.writeContents(remoteDir, remoteFolder.getPath());
        File remoteBranches = Utils.join(Files.REFSHEADS, remoteName);
        remoteBranches.mkdir();
    }

    /**
     *  Removes information associated with the given remote name.
     * @param args Array with format {"rm-remote", "remote name"}
     */
    public static void rmRemote(String[] args) {
        validateNumArgs(args, 2);
        String remoteName = args[1];
        File remoteDir = Utils.join(Files.REMOTES, remoteName);
        if (!remoteDir.exists()) {
            Main.exitWithError("A remote with that name does not exist.");
        }
        File remoteBranches = Utils.join(Files.REFSHEADS, remoteName);
        remoteBranches.delete();
        remoteDir.delete();
    }

    /**
     * Attempts to append the current branch's commits to the
     * end of the given branch at the given remote.
     * @param args Array with format {"push", "remote name" ,
     *             "remote branch name"}
     * @throws IOException
     */
    public static void push(String[] args) throws IOException {
        validateNumArgs(args, 3);
        String remoteName = args[1];
        String remoteBranchName = args[2];
        File remoteDir = Utils.join(Files.REMOTES, remoteName);
        File remoteRepo = new File(Utils.readContentsAsString(
                remoteDir));
        File remoteBranch = Utils.join(remoteRepo, "refs/heads/"
                + remoteBranchName);
        if (!remoteRepo.exists()) {
            Main.exitWithError("Remote directory not found.");
        } else if (!remoteBranch.exists()) {
            branch(new String[]{remoteRepo.getPath(), remoteBranchName});
        } else {
            String remoteHeadID = Commit.getRemoteBranchHeadID(
                    remoteRepo, remoteBranchName);
            Commit headCommit = Commit.getHeadCommitObj();
            if (!headCommit.getPartents().contains(remoteHeadID)) {
                Main.exitWithError("Please pull down remote "
                        + "changes before pushing.");
            }
            Utils.writeContents(remoteBranch, Commit.getHeadCommitCode());
            Commit currCommit = Commit.getHeadCommitObj();
            File remoteCommits = Utils.join(remoteRepo,
                    "logs/commits");
            Utils.writeObject(Utils.join(remoteCommits,
                    Commit.getHeadCommitCode()),
                    currCommit);
            String currCommitLog = Utils.readContentsAsString(
                    Utils.join(Files.LOGSCOMMITSHIS,
                            Commit.getHeadCommitCode()));
            Utils.writeContents(Utils.join(remoteCommits,
                    "commit logs/"
                    + Commit.getHeadCommitCode()), currCommitLog);
        }


    }

    /**
     * Brings down commits from the remote Gitlet repository into
     * the local Gitlet repository.
     * @param args Array with format {"push", "remote name",
     *             "remote branch name"}
     * @throws IOException
     */
    public static void fetch(String[] args) throws IOException {
        validateNumArgs(args, 3);
        String remoteName = args[1];
        String remoteBranchName = args[2];
        File remoteDir = Utils.join(Files.REMOTES, remoteName);
        File remoteRepo = new File(Utils.readContentsAsString(
                remoteDir));
        if (!remoteRepo.exists()) {
            Main.exitWithError("Remote directory not found.");
        }
        File remoteBranchHead = Utils.join(
                remoteRepo, "refs/heads/" + remoteBranchName);
        if (!remoteBranchHead.exists()) {
            Main.exitWithError("That remote does not have that branch.");
        }
        Commit remoteBranchHeadObj = Commit.getRemoteBranchHeadObj(
                remoteRepo, remoteBranchName);
        ArrayList<String> remoteBranchHeadParents =
                remoteBranchHeadObj.getPartents();
        HashMap<String, String> remoteBranchHeadMap =
                remoteBranchHeadObj.getNameBlobMap();
        String remoteBranchHeadID = Commit.getRemoteBranchHeadID(
                remoteRepo, remoteBranchName);
        if (!Utils.join(Files.LOGSCOMMITS, remoteBranchHeadID).exists()) {
            File remoteBranchHeadLog = Utils.join(remoteRepo,
                    "logs/commits/commit logs/"
                            + remoteBranchHeadID);
            Utils.writeObject(Utils.join(Files.LOGSCOMMITS,
                    remoteBranchHeadID), remoteBranchHeadObj);
            Utils.writeContents(Utils.join(Files.LOGSCOMMITSHIS,
                    remoteBranchHeadID),
                    Utils.readContentsAsString(remoteBranchHeadLog));
        }
        for (String parent : remoteBranchHeadParents) {
            if (!Utils.join(Files.LOGSCOMMITS, parent).exists()) {
                Commit obj = Commit.getRemoteCommit(remoteRepo, parent);
                Utils.writeObject(Utils.join(Files.LOGSCOMMITS, parent), obj);
                File parentLog = Utils.join(remoteRepo,
                        "logs/commits/commit logs/"
                                + remoteBranchHeadID);
                Utils.writeContents(Utils.join(Files.LOGSCOMMITSHIS,
                        parent), Utils.readContentsAsString(parentLog));
            }
        }
        for (String blobID : remoteBranchHeadMap.values()) {
            if (!Utils.join(Files.OBJECTS, blobID).exists()) {
                Blob blob = Blob.getRemoteBlob(remoteRepo, blobID);
                Utils.writeObject(Utils.join(Files.OBJECTS, blobID),
                        blob);
            }
        }
        File branchHead = Utils.join(Files.REFSHEADS,
                remoteName + "/" + remoteBranchName);
        if (!branchHead.exists()) {
            branch(new String[]{"branch", remoteName + File.separator
                    + remoteBranchName});
        }
        Utils.writeContents(branchHead, remoteBranchHeadID);
    }

    /**
     * Fetches branch [remote name]/[remote branch name] as for the
     * fetch command,
     * and then merges that fetch into the current branch.
     * @param args Array with format {"push", "remote name",
     *             "remote branch name"}
     * @throws IOException
     */
    public static void pull(String[] args) throws IOException {
        validateNumArgs(args, 3);
        String remoteName = args[1];
        String remoteBranchName = args[2];
        fetch(new String[]{"fetch", remoteName, remoteBranchName});
        merge(new String[]{"merge", remoteName + "/" + remoteBranchName});
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            throw new GitletException("Incorrect operands.");
        }
    }


}
