package gitlet;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author HalfDream
 */
public class Main {
    public static int BRANCH_COUNT = 1;
    public static String[] Branches = new String[BRANCH_COUNT]; // Branches[0] is Master;

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.InitCommand();
                break;
            case "add":
                // note : we can only add one file to gitlet,so there are only two args in the input array.
                String addedFileName = args[1];
                Repository.AddCommand(addedFileName);
                break;
            // TODO: FILL THE REST IN
            case "commit":    //usage : java gitlet.Main commit [message]
                if (args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    System.exit(1);
                }
                String commitMessage = args[1];
                Repository.CommitCommand(commitMessage);
                break;
            case "rm":
                String rmFileName = args[1];
                Repository.rmCommand(rmFileName);
                break;
            case "log":
                Repository.logCommand();
                break;
            case "global-log":
                Repository.globalLogCommand();
                break;
            case "find":
                String findByMessage = args[1];
                Repository.findCommand(findByMessage);
                break;
            case "status":
                Repository.statusCommand();
                break;
            case "checkout":
                if (args.length == 2) {
                    String BranchName = args[1];
                    Repository.checkoutToBranch(BranchName,false);
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
                    Repository.checkoutToSpecificCommitWithOneFile(commitSha1,fileName);
                    break;
                }
                break;
            case "branch":
                String newBranchName = args[1];
                Repository.branchCommand(newBranchName);
                break;
            case "rm-branch":
                String rmBranchName = args[1];
                Repository.rmBranchCommand(rmBranchName);
                break;
            case "reset":
                String commitSha1 = args[1];
                Repository.resetCommand(commitSha1);
                break;
            case "merge":
                String mergedInBranchName = args[1];
                Repository.mergeCommand(mergedInBranchName);
                break;
        }
    }
}
