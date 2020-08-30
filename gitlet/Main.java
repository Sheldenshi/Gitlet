package gitlet;

import java.io.IOException;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Shelden Shi
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String[] args) throws IOException {
        if (checkArgs(args)) {
            switch (args[0]) {
            case "add":
                Commands.add(args);
                break;
            case "commit":
                Commands.commit(args);
                break;
            case "rm":
                Commands.rm(args);
                break;
            case "log":
                Commands.log(args);
                break;
            case "global-log":
                Commands.globalLog(args);
                break;
            case "find":
                Commands.find(args);
                break;
            case "status":
                Commands.status(args);
                break;
            case "checkout":
                Commands.checkout(args);
                break;
            case "branch":
                Commands.branch(args);
                break;
            case "rm-branch":
                Commands.rmBranch(args);
                break;
            case "reset":
                Commands.reset(args);
                break;
            case "merge":
                Commands.merge(args);
                break;
            case "add-remote":
                Commands.addRemote(args);
                break;
            case "rm-remote":
                Commands.rmRemote(args);
                break;
            case "fetch":
                Commands.fetch(args);
                break;
            case "pull":
                Commands.pull(args);
                break;
            case "push":
                Commands.push(args);
                break;
            default:
                exitWithError("No command with that name exists.");
            }
            return;
        }
    }

    /**
     * Checks args and inital.
     * make this function bc main function is longer than 60 lines.
     * which is too long according to my professor's style check.
     * @param args input args
     * @return true if it is not init.
     * @throws IOException
     */
    private static boolean checkArgs(String[] args) throws IOException {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        if (args[0].equals("init")) {
            Commands.init(args);
            return false;
        }
        initialized();
        return true;
    }

    /**
     * Prints out MESSAGE and exits with error code 0.
     * @param message message to print
     */
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }

    /** Checks if it has been initialized. */
    private static void initialized() {
        if (!Files.GITLET.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
    }


}
