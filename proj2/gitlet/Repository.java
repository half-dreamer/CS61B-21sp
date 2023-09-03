package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author HalfDream
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "Commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "Blobs");
    public static final File POINTERS_DIR = join(GITLET_DIR, "CommitPointers");
    public static final File HEAD_FILE = join(POINTERS_DIR, "HEAD");
    public static final File Master_FILE = join(POINTERS_DIR, "master");
    public static final File ADDSTAGE_DIR = join(GITLET_DIR, "AddStage");
    public static final File RMSTAGE_DIR = join(GITLET_DIR, "RmStage");
    public static String resetCurCommitSha1 = null;
//    @formatter:off
/**
     *  .gitlet (folder)
     *          Commits (folder)
     *                  >commit (flie)(filename: commit.sha1 , content:Commit(Object))
     *                  >commit (file)
     *          Blobs (folder)
     *                  >blob (file) (filename: blob.sha1, content: Blob(Object)
     *                  >blob (file)
     *         CommitPointers(folder) (call it Branch may be better)
     *                  >Commit(filename:Pointer Name, content: commit sha1 the pointer point at)
     *                  >e.g. note: Special case : filename:HEAD content : the branch name it currently points at
     *                  >e.g. (filename:master,content :some commit sha1)
     *         AddStage(folder)
     *                  >StagedFile (fliename: added file name , content: StagedFile(Object) (which has pointed Blob's Sha1 )
     *         RmStage(folder)
     *                  >RmStagedFile (fliename: removed file name , content: StagedFile(Object) (which has pointed Blob's Sha1 )
*/
//  @formatter:on
    static void InitCommand() {
        if (GITLET_DIR.exists()) {
            errorMessage("A Gitlet version-control system already exists in the current directory.");
        }
        // create all needed folders
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        POINTERS_DIR.mkdir();
        ADDSTAGE_DIR.mkdir();
        RMSTAGE_DIR.mkdir();
        Commit initCommit = new Commit();
        writeObject(join(COMMIT_DIR, initCommit.getCurSha1()), initCommit);
        String master = initCommit.getCurSha1();
        writeObject(HEAD_FILE, "master");
        writeObject(Master_FILE, master);
    }

    static void AddCommand(String AddedFileName) {
        // add a file:
        // 1.check whether the file(blob) is the same as HEAD commit containingBlobs
        // 2.if same,don't add; otherwise,add it to the Stage(and now we know
        // StagedFile need three variables(1.the file content  2.the blob's sha1  3.the file name("world.txt")
        // So we create a new class called StagedFile to store these information)
        File AddedFile = join(CWD, AddedFileName);
        if (!AddedFile.exists()) {
            errorMessage("File does not exist.");
        }

        // figure out whether the file (or say blob) have existed in the Blobs dir
        List<String> Bolbs = Utils.plainFilenamesIn(BLOB_DIR);
        Blob addedBlob = new Blob(AddedFileName, AddedFile);
        String addedBlobSha1 = addedBlob.getSha1();
        StagedFile AddedStagedFile = new StagedFile(AddedFileName, AddedFile, addedBlobSha1);
        File BlobFile = join(BLOB_DIR, addedBlobSha1);
        writeObject(BlobFile, addedBlob); // store the blob into blob file under the Blobs dir

        // If the current working version of the file is identical to the version in the current commit,
        // do not stage it to be added, and remove it from the staging area if it is already there
        Commit HEADCommit = Utils.getCommitFromPointer("HEAD");
        if (HEADCommit.getContainingBlobs().containsValue(addedBlob.getSha1())) {
            //meaning the addedBlob has existed in the HEAD commit.so do not stage it and remote it from the Staging area if it existed.
            join(ADDSTAGE_DIR, AddedFileName).delete();
            join(RMSTAGE_DIR, AddedFileName).delete();
        } else {
            //addedBlob do not exist in HEAD commit ,so we add it to the AddStage.
            File toStagedFile = join(ADDSTAGE_DIR, AddedFileName);
            writeObject(toStagedFile, AddedStagedFile);
        }

    }


    static void CommitCommand(String CommitMessage) {
        //  1.copy the prevCommit containingBlobs to newCommit and adjust it with addStage.(done)
        //  2.create the right newCommit with correct timestamp,parent etc.(done)
        //  3.write the newCommit to the Commits Folder.(done)
        //  4.update the branches(done)
        if (CommitMessage.length() == 0) {
            errorMessage("Please enter a commit message.");
        }
        Commit prevCommit = Utils.getCommitFromPointer("HEAD");
        String parSha1 = prevCommit.getCurSha1();
        Map<String, String> newCommitContainingBlobs = prevCommit.getContainingBlobs(); //not updated
        // updateNewCommitContaingBlobs
        List<String> addStageFilesList = Utils.plainFilenamesIn(ADDSTAGE_DIR);
        List<String> rmStageFilesList = Utils.plainFilenamesIn(RMSTAGE_DIR);
        if (addStageFilesList.isEmpty() && rmStageFilesList.isEmpty()) {
            errorMessage("No changes added to the commit.");
        }
        for (String addFileName : addStageFilesList) {
            StagedFile inAddStageFile = readObject(join(ADDSTAGE_DIR, addFileName), StagedFile.class);
            newCommitContainingBlobs.put(addFileName, inAddStageFile.getBolbSha1());
        }
        for (String rmFileName : rmStageFilesList) {
            StagedFile inRmStageFile = readObject(join(RMSTAGE_DIR, rmFileName), StagedFile.class);
            newCommitContainingBlobs.remove(rmFileName, inRmStageFile.getBolbSha1());
        }
        Commit newCommit = new Commit(CommitMessage, parSha1, newCommitContainingBlobs);
        writeObject(join(COMMIT_DIR, newCommit.getCurSha1()), newCommit);//step3
        String curBranchName = readObject(HEAD_FILE, String.class);
        writeObject(join(POINTERS_DIR, curBranchName), newCommit.getCurSha1());
        clearTwoStages();
    }


    static void rmCommand(String rmFileName) {
        boolean isStaged = false;
        boolean isTracked = false;
        boolean isExisted = false;
        if (plainFilenamesIn(ADDSTAGE_DIR).contains(rmFileName)) {
            isStaged = true;
        }
        if (Utils.getCommitFromPointer("HEAD").getContainingBlobs().containsKey(rmFileName)) {
            isTracked = true;
        }
        if (Utils.plainFilenamesIn(CWD).contains(rmFileName)) {
            isExisted = true;
        }

        if (isStaged) {
            unstageAddStageFile(rmFileName);
        } else if (isTracked) {
            // Stage it for removal
            Commit curCommit = getCommitFromPointer("HEAD");
            String rmBlobSha1 = curCommit.getContainingBlobs().get(rmFileName);
            Blob rmBlob = readObject(join(BLOB_DIR, rmBlobSha1), Blob.class);
            StagedFile RemovedStageFile = new StagedFile(rmFileName, rmBlob.getFileContent(), rmBlobSha1);
            writeObject(join(RMSTAGE_DIR, rmFileName), RemovedStageFile);
            /** original solution
             * the problem is : the Removed File may be removed before we
             * execute the git rm command, so we can't read content from
             * the removed file
             *
             File rmStageFileDes = join(RMSTAGE_DIR,rmFileName); // the position
             Blob rmBlob = new Blob(rmFileName,join(CWD,rmFileName));
             StagedFile RemovedStageFile = new StagedFile(rmFileName,join(CWD,rmFileName),rmBlob.getSha1());
             writeObject(rmStageFileDes,RemovedStageFile); // write it to the RmStage folder
             */

            //remove the file from the working directory if the user has not already done so
            restrictedDelete(join(CWD, rmFileName));
        } else {
            errorMessage("No reason to remove the file.");
        }
    }

    static void logCommand() {
        Commit curCommit = getCommitFromPointer("HEAD");
        while (!(curCommit.getParSha1().equals(""))) {
            showCommitInfo(curCommit);
            String parSha1 = curCommit.getParSha1();
            curCommit = readObject(join(COMMIT_DIR, parSha1), Commit.class);
        }
        showCommitInfo(curCommit);
    }

    static void globalLogCommand() {
        for (String CommitSha1 : plainFilenamesIn(COMMIT_DIR)) {
            Commit curCommit = readObject(join(COMMIT_DIR, CommitSha1), Commit.class);
            showCommitInfo(curCommit);
        }
    }

    static void findCommand(String message) {
        int findCount = 0;
        for (String CommitSha1 : plainFilenamesIn(COMMIT_DIR)) {
            Commit curCommit = readObject(join(COMMIT_DIR, CommitSha1), Commit.class);
            if (curCommit.getMessage().equals(message)) {
                System.out.println(CommitSha1);
                findCount++;
            }
        }
        if (findCount == 0) {
            errorMessage("Found no commit with that message.");
        }
    }

    static void statusCommand() {
        printBranches();
        printStagedFiles();
        printRemovedFiles();
        printModifiedButNotStagedFiles();
        printUntrackedFiles();
    }

    // checkout [branch name]
    static void checkoutToBranch(String desBranchName, boolean isUsedForReset) {
        List<String> branches = plainFilenamesIn(POINTERS_DIR);
        boolean isTheBranchExisted = false;
        String desBranchCommitSha1 = "";
        for (String branch : branches) {
            if (branch.equals(desBranchName)) {
                isTheBranchExisted = true;
                desBranchCommitSha1 = readObject(join(POINTERS_DIR, branch), String.class);
                break;
            }
        }
        if (!isTheBranchExisted) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        Commit desCommit = readObject(join(COMMIT_DIR, desBranchCommitSha1), Commit.class);
        String curCommitSha1 = getCommitFromPointer("HEAD").getCurSha1();
        if (isUsedForReset) {
            curCommitSha1 = resetCurCommitSha1;
        }
        if (desCommit.getCurSha1().equals(curCommitSha1) && desBranchName.equals(readObject(HEAD_FILE, String.class))) {
            // the desBranch and curBranch may point at same commit
            errorMessage("No need to checkout the current branch.");
        }
        Commit curCommit = readObject(join(COMMIT_DIR, curCommitSha1), Commit.class);
        Map<String, String> curCommitContaingBlobs = curCommit.getContainingBlobs();// Map<fileName,Blob.sha1> e.g.{"Hello.txt","0e93"}
        Map<String, String> desCommitContaingBlobs = desCommit.getContainingBlobs();
        List<String> untrackedFileNames = new ArrayList<>();
        for (String workingFileName : plainFilenamesIn(CWD)) {
            if (!curCommitContaingBlobs.containsKey(workingFileName)) {
                untrackedFileNames.add(workingFileName);
            }
        }
        for (String untrackFileName : untrackedFileNames) {
            if (desCommitContaingBlobs.containsKey(untrackFileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        // delete files tracked in curCommit but untracked in desCommit
        for (Map.Entry<String, String> entry : curCommitContaingBlobs.entrySet()) {
            String curFileName = entry.getKey();
            if (!desCommitContaingBlobs.containsKey(curFileName)) {
                restrictedDelete(join(CWD, curFileName));
            }
        }
        // overwrite files tracked in the desCommit
        for (Map.Entry<String, String> entry : desCommitContaingBlobs.entrySet()) {
            String curBlobSha1 = entry.getValue();
            Blob curBlob = readObject(join(BLOB_DIR, curBlobSha1), Blob.class);
            String curBlobFileName = curBlob.getFileName();
            String curBlobContent = curBlob.getFileContent();
            writeContents(join(CWD, curBlobFileName), curBlobContent);
        }
        //update the HEAD pointer
        writeObject(HEAD_FILE, desBranchName);
        //clear stages
        clearTwoStages();
    }

    // checkout -- [file name]
    static void checkoutToHeadWithOneFile(String fileName) {
        String HeadSha1 = getCommitFromPointer("HEAD").getCurSha1();
        checkoutToSpecificCommitWithOneFile(HeadSha1, fileName);
    }

    // checkout [commit id] -- [file name]
    static void checkoutToSpecificCommitWithOneFile(String desCommitSha1, String fileName) {
        List<String> allCommitSha1s = plainFilenamesIn(COMMIT_DIR);
        boolean isDesCommitExisted = false;
        String fullDesCommitSha1 = "";
        for (String curCommitSha1 : allCommitSha1s) {
            if (curCommitSha1.startsWith(desCommitSha1)) {
                isDesCommitExisted = true;
                fullDesCommitSha1 = curCommitSha1;
                break;
            }
        }
        if (!isDesCommitExisted) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit desCommit = readObject(join(COMMIT_DIR, fullDesCommitSha1), Commit.class);
        String desBlobSha1 = desCommit.getContainingBlobs().get(fileName);
        if (desBlobSha1 == null) {
            errorMessage("File does not exist in that commit.");
        }
        Blob desBlob = readObject(join(BLOB_DIR, desBlobSha1), Blob.class);
        writeContents(join(CWD, fileName), desBlob.getFileContent());
    }

    static void branchCommand(String newBranchName) {
        List<String> branches = plainFilenamesIn(POINTERS_DIR);
        if (branches.contains(newBranchName)) {
            errorMessage("A branch with that name already exists.");
        } else {
            String HEADCommitSha1 = getCommitFromPointer("HEAD").getCurSha1();
            writeObject(join(POINTERS_DIR, newBranchName), HEADCommitSha1);
        }
    }

    static void rmBranchCommand(String rmBranchName) {
        String curBranchName = readObject(HEAD_FILE, String.class);
        List<String> allBranches = plainFilenamesIn(POINTERS_DIR);
        if (!allBranches.contains(rmBranchName)) {
            errorMessage("A branch with that name does not exist.");
        }
        if (rmBranchName.equals(curBranchName)) {
            errorMessage("Cannot remove the current branch.");
        }
        join(POINTERS_DIR, rmBranchName).delete();
    }

    static void resetCommand(String desCommitSha1) {
        List<String> allCommitSha1s = plainFilenamesIn(COMMIT_DIR);
        boolean isDesCommitExisted = false;
        String fullDesCommitSha1 = "";
        for (String curCommitSha1 : allCommitSha1s) {
            if (curCommitSha1.startsWith(desCommitSha1)) {
                isDesCommitExisted = true;
                fullDesCommitSha1 = curCommitSha1;
                break;
            }
        }
        if (!isDesCommitExisted) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        String curBranchName = readObject(HEAD_FILE, String.class);
        resetCurCommitSha1 = readObject(join(POINTERS_DIR, curBranchName), String.class);
        writeObject(join(POINTERS_DIR, curBranchName), desCommitSha1);
        checkoutToBranch(curBranchName, true);
    }

    static void mergeCommand(String mergedInBranchName) {
        if (!plainFilenamesIn(ADDSTAGE_DIR).isEmpty() || !plainFilenamesIn(RMSTAGE_DIR).isEmpty()) {
            errorMessage("You have uncommitted changes.");
        }
        if (!plainFilenamesIn(POINTERS_DIR).contains(mergedInBranchName)) {
            errorMessage("A branch with that name does not exist.");
        }
        if (readObject(HEAD_FILE, String.class).equals(mergedInBranchName)) {
            errorMessage("Cannot merge a branch with itself.");
        }
        Commit curCommit = getCommitFromPointer("HEAD");
        String mergedInBranchCommitSha1 = readObject(join(POINTERS_DIR, mergedInBranchName), String.class);
        Commit mergedInBranchCommit = readObject(join(COMMIT_DIR, mergedInBranchCommitSha1), Commit.class);

        Map<String, Integer> curCommitDepthMap = new HashMap<>();
        Map<String, Integer> mergedInCommitDepthMap = new HashMap<>();
        // use depthMap to find (first) split commit
        changeDepthMapOf(curCommit, curCommitDepthMap, 0);
        changeDepthMapOf(mergedInBranchCommit, mergedInCommitDepthMap, 0);
        Commit splitCommit = findSplitCommit(curCommitDepthMap, mergedInCommitDepthMap);
        // If the split point is the same commit as the given branch, then we do nothing;
        // the merge is complete, and the operation ends with the message Given branch is an ancestor of the current branch.
        if (splitCommit.getCurSha1().equals(mergedInBranchCommit.getCurSha1())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        // If the split point is the current branch, then the effect is to check out the given branch,
        // and the operation ends after printing the message Current branch fast-forwarded.
        if (splitCommit.getCurSha1().equals(curCommit.getCurSha1())) {
            checkoutToBranch(mergedInBranchName, false);
            System.out.println("Current branch fast-forwarded.");
        }

        Map<String, String> splitCommitContainingBlobs = splitCommit.getContainingBlobs();
        Map<String, String> curCommitContainingBlobs = curCommit.getContainingBlobs();
        Map<String, String> mergedInCommitContainingBlobs = mergedInBranchCommit.getContainingBlobs();
        Map<String, String> newMergeCommitContainingBlobs = new HashMap<>();
        List<String> toDeleteFileList = new ArrayList<>();

        List<String> untrackedFileNames = new ArrayList<>();
        for (String workingFileName : plainFilenamesIn(CWD)) {
            if (!curCommitContainingBlobs.containsKey(workingFileName)) {
                untrackedFileNames.add(workingFileName);
            }
        }
        if (!untrackedFileNames.isEmpty()) {
            errorMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        // iterate through all three BlobMaps, while iterating, remove the corresponding blob in all three maps
        for (Map.Entry<String, String> splitCommitBlobEntry : splitCommitContainingBlobs.entrySet()) {
            boolean isIterBlobExistInCurCommit = false;
            boolean isIterBolbExistInMergedInCommit = false;
            boolean isIterBlobSameAsCurCommitBlob = false;
            boolean isIterBlobSameAsMergedInCommitBlob = false;
            String iterFileName = splitCommitBlobEntry.getKey();
            String iterBlobSha1 = splitCommitBlobEntry.getValue();
            String curCommitBlobSha1 = null;
            String mergedInCommitBlobSha1 = null;

            if (curCommitContainingBlobs.containsKey(iterFileName)) {
                isIterBlobExistInCurCommit = true;
                curCommitBlobSha1 = curCommitContainingBlobs.get(iterFileName);
                if (curCommitBlobSha1.equals(iterBlobSha1)) {
                    isIterBlobSameAsCurCommitBlob = true;
                }
            }
            if (mergedInCommitContainingBlobs.containsKey(iterFileName)) {
                isIterBolbExistInMergedInCommit = true;
                mergedInCommitBlobSha1 = mergedInCommitContainingBlobs.get(iterFileName);
                if (mergedInCommitBlobSha1.equals(iterBlobSha1)) {
                    isIterBlobSameAsMergedInCommitBlob = true;
                }
            }

            if (isIterBlobExistInCurCommit && isIterBolbExistInMergedInCommit) {
                //different cases(four cases)
                if (isIterBlobSameAsCurCommitBlob && isIterBlobSameAsMergedInCommitBlob) {
                    // iter == cur == mergedIn
                    newMergeCommitContainingBlobs.put(iterFileName, iterBlobSha1);
                }

                if (!isIterBlobSameAsCurCommitBlob && isIterBlobSameAsMergedInCommitBlob) {
                    // iter != cur  iter == mergedIn
                    newMergeCommitContainingBlobs.put(iterFileName, curCommitBlobSha1);
                }

                if (isIterBlobSameAsCurCommitBlob && !isIterBlobSameAsMergedInCommitBlob) {
                    // iter == cur  iter != mergedIn
                    newMergeCommitContainingBlobs.put(iterFileName, mergedInCommitBlobSha1);
                }

                if (!isIterBlobSameAsCurCommitBlob && !isIterBlobSameAsMergedInCommitBlob) {
                    // iter != cur   iter != mergedIn
                    if (!isIterBlobExistInCurCommit && !isIterBolbExistInMergedInCommit) {
                        //do nothing.
                        ;
                    } else if (curCommitContainingBlobs.equals(mergedInCommitContainingBlobs)) {
                        // cur == mergedIn  (or say , modified in the same way )
                        newMergeCommitContainingBlobs.put(iterFileName, curCommitBlobSha1);
                    } else {
                        fixMergeConflict(isIterBlobExistInCurCommit, isIterBolbExistInMergedInCommit, curCommitBlobSha1, mergedInCommitBlobSha1, iterFileName, newMergeCommitContainingBlobs);
                    }
                }
            } else if (isIterBlobExistInCurCommit && !isIterBolbExistInMergedInCommit) {
                if (!isIterBlobSameAsCurCommitBlob) {
                    fixMergeConflict(isIterBlobExistInCurCommit, isIterBolbExistInMergedInCommit, curCommitBlobSha1, mergedInCommitBlobSha1, iterFileName, newMergeCommitContainingBlobs);
                }
            } else if (!isIterBlobExistInCurCommit && isIterBolbExistInMergedInCommit) {
                if (!isIterBlobSameAsMergedInCommitBlob) {
                    fixMergeConflict(isIterBlobExistInCurCommit, isIterBolbExistInMergedInCommit, curCommitBlobSha1, mergedInCommitBlobSha1, iterFileName, newMergeCommitContainingBlobs);
                }
            } else {
                //do nothing.
            }

            toDeleteFileList.add(iterFileName);
        }
        for (String iterFileName : toDeleteFileList) {
            // remove the iterating entry in these three maps
            splitCommitContainingBlobs.remove(iterFileName);
            curCommitContainingBlobs.remove(iterFileName);
            mergedInCommitContainingBlobs.remove(iterFileName);
        }
        // above are the iteration part of splitCommit
        if (!splitCommitContainingBlobs.isEmpty()) {
            System.out.println("Error : The splitCommit still has blobs after the iteration!");
        }
        toDeleteFileList.clear();

        // the second loop : now iterate through the remaining curCommitContainingBlobs
        for (Map.Entry<String, String> curCommitBlobEntry : curCommitContainingBlobs.entrySet()) {
            String iterFileName = curCommitBlobEntry.getKey();
            String iterBlobSha1 = curCommitBlobEntry.getValue();
            if (mergedInCommitContainingBlobs.containsKey(iterFileName)) {
                String mergedInCommitBlobSha1 = mergedInCommitContainingBlobs.get(iterFileName);
                String curCommitBlobSha1 = iterBlobSha1;
                if (curCommitBlobSha1.equals(mergedInCommitBlobSha1)) {
                    newMergeCommitContainingBlobs.put(iterFileName, curCommitBlobSha1);
                } else {
                    // merge conflict
                    fixMergeConflict(true, true, curCommitBlobSha1, mergedInCommitBlobSha1, iterFileName, newMergeCommitContainingBlobs);
                }
            } else {
                // split (x) cur (E) mergedIn (x)
                newMergeCommitContainingBlobs.put(iterFileName, iterBlobSha1);
            }
            toDeleteFileList.add(iterFileName);
        }
        for (String iterFileName : toDeleteFileList) {
            curCommitContainingBlobs.remove(iterFileName);
            mergedInCommitContainingBlobs.remove(iterFileName);
        }
        if (!curCommitContainingBlobs.isEmpty()) {
            System.out.println("Error : the curCommitContainingBlobs is not empty after the second loop!");
        }
        toDeleteFileList.clear();

        // the third loop : iterate through the mergedInCommitContainingBlobs
        // split(x) cur(x) mergedIn(E)
        for (Map.Entry<String, String> mergedInCommitBlobEntry : mergedInCommitContainingBlobs.entrySet()) {
            String iterFileName = mergedInCommitBlobEntry.getKey();
            String iterBlobSha1 = mergedInCommitBlobEntry.getValue();
            newMergeCommitContainingBlobs.put(iterFileName, iterBlobSha1);
            toDeleteFileList.add(iterFileName);
        }
        for (String iterFileName : toDeleteFileList) {
            mergedInCommitContainingBlobs.remove(iterFileName);
        }
        if (!mergedInCommitContainingBlobs.isEmpty()) {
            System.out.println("Error : the mergedInCommitContainingBlobs is not empty after the third loop!");
        }


        // create new commit and update its merge-relative attributes
        String curBranchName = readObject(HEAD_FILE, String.class);
        String newCommitMessage = "Merged " + mergedInBranchName + " into " + curBranchName + ".";
        Commit newCommit = new Commit(newCommitMessage, curCommit.getCurSha1(), newMergeCommitContainingBlobs);
        newCommit.addMergedInParSha1(mergedInBranchCommit.getCurSha1());
        newCommit.setHasMutiplePars(true);


        // write the commit to COMMIT folder
        writeObject(join(COMMIT_DIR, newCommit.getCurSha1()), newCommit);
        writeObject(join(POINTERS_DIR, curBranchName), newCommit.getCurSha1()); // update current pointer
        resetCurCommitSha1 = curCommit.getCurSha1();
        checkoutToBranch(curBranchName, true);


    }


}
