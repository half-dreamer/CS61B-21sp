package gitlet;

import static gitlet.Utils.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author HalfDream
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            errorMessage("Please enter a command.");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.InitCommand();
                break;
            case "add":
                // note : we can only add one file to gitlet,so there are only two args in the input array.
                assertHasInitialedGitRepo();
                String addedFileName = args[1];
                Repository.AddCommand(addedFileName);
                break;
            case "commit":    //usage : java gitlet.Main commit [message]
                assertHasInitialedGitRepo();
                if (args.length == 1) {
                    errorMessage("Please enter a commit message.");
                }
                String commitMessage = args[1];
                Repository.CommitCommand(commitMessage);
                break;
            case "rm":
                assertHasInitialedGitRepo();
                String rmFileName = args[1];
                Repository.rmCommand(rmFileName);
                break;
            case "log":
                assertHasInitialedGitRepo();
                Repository.logCommand();
                break;
            case "global-log":
                assertHasInitialedGitRepo();
                Repository.globalLogCommand();
                break;
            case "find":
                assertHasInitialedGitRepo();
                String findByMessage = args[1];
                Repository.findCommand(findByMessage);
                break;
            case "status":
                assertHasInitialedGitRepo();
                Repository.statusCommand();
                break;
            case "checkout":
                assertHasInitialedGitRepo();
                if (args.length == 2) {
                    String branchName = args[1];
                    Repository.checkoutToBranch(branchName, false);
                    break;
                }
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        IncorrectOperands();
                    }
                    String fileName = args[2];
                    Repository.checkoutToHeadWithOneFile(fileName);
                    break;
                }
                if (args.length == 4) {
                    String commitSha1 = args[1];
                    if (!args[2].equals("--")) {
                        IncorrectOperands();
                    }
                    String fileName = args[3];
                    Repository.checkoutToSpecificCommitWithOneFile(commitSha1, fileName);
                    break;
                }
                break;
            case "branch":
                assertHasInitialedGitRepo();
                String newBranchName = args[1];
                Repository.branchCommand(newBranchName);
                break;
            case "rm-branch":
                assertHasInitialedGitRepo();
                String rmBranchName = args[1];
                Repository.rmBranchCommand(rmBranchName);
                break;
            case "reset":
                assertHasInitialedGitRepo();
                String commitSha1 = args[1];
                Repository.resetCommand(commitSha1);
                break;
            case "merge":
                assertHasInitialedGitRepo();
                String mergedInBranchName = args[1];
                Repository.mergeCommand(mergedInBranchName);
                break;
            default:
                errorMessage("No command with that name exists.");
                break;
        }
    }
}
