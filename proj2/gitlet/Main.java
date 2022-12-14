package gitlet;

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
        }
    }
}
