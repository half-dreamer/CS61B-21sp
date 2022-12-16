package gitlet;

import edu.princeton.cs.algs4.StdIn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author HalfDream
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    //TODO: I still don't know whether the HEAD and BRANCHES can be stored during the Main execution.
    //TODO: though I can store the HEAD and Branches in some File or Folder.
    public static int BRANCHS_COUNT = 1;

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR,"Commits");
    public static final File BLOB_DIR = join(GITLET_DIR,"Blobs");
    public static final File POINTERS_DIR = join(GITLET_DIR,"CommitPointers");
    public static final File HEAD_FILE = join(POINTERS_DIR,"HEAD");
    public static final File Master_FILE = join(POINTERS_DIR,"master");
    public static final File ADDSTAGE_DIR = join(GITLET_DIR,"AddStage");
    public static final File RMSTAGE_DIR = join(GITLET_DIR,"RmStage");
/**
     *  .gitlet (folder)
     *          Commits (folder)
     *                  >commit (flie)(filename: commit.sha1 , content:Commit(Object))
     *                  >commit (file)
     *          Blobs (folder)
     *                  >blob (file) (filename: blob.sha1, content: Blob(Object)
     *                  >blob (file)
     *         CommitPointers(folder)
     *                  >Commit(filename:Pointer Name, content: commit sha1 the pointer point at)
     *                  >e.g. note: Special case : filename:HEAD content
     *                  >e.g. (filename:master,content :some commit sha1)
     *         AddStage(folder)
     *                  >StagedFile (fliename: added file name , content: StagedFile(Object) (which has pointed Blob's Sha1 )
     *         RmStage(folder)
     *                  >RmStagedFile (fliename: removed file name , content: StagedFile(Object) (which has pointed Blob's Sha1 )


    /* TODO: fill in the rest of this class. */
    static void InitCommand() {
        if (GITLET_DIR.exists()) {
            throw Utils.error("A Gitlet version-control system already exists in the current directory.");
        }
        // create all needed folders
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        POINTERS_DIR.mkdir();
        ADDSTAGE_DIR.mkdir();
        RMSTAGE_DIR.mkdir();
        Commit initCommit = new Commit();
        writeObject(join(COMMIT_DIR,initCommit.getCurSha1()),initCommit);
        String master = initCommit.getCurSha1();
        writeObject(HEAD_FILE,"master");
        writeObject(Master_FILE,master);
    }

    static void AddCommand(String AddedFileName) {
        //TODO: add a file:
        //TODO: 1.check whether the file(blob) is the same as HEAD commit containingBlobs
        //TODO: 2.if same,don't add ;otherwise,add it to the Stage(and now we know
        // StagedFile need three variables(1.the file content  2.the blob's sha1  3.the file name("world.txt")
        // So we create a new class called StagedFile to store these information)
        File AddedFile = join(CWD,AddedFileName);
        if (!AddedFile.exists()) {
            Utils.error("File does not exist.");
        }

        //TODO:figure out whether the file (or say blob) have existed in the Blobs dir
        List<String> Bolbs = Utils.plainFilenamesIn(BLOB_DIR);
        Blob addedBlob = new Blob(AddedFileName,AddedFile);
        String addedBlobSha1 = addedBlob.getSha1();
        StagedFile AddedStagedFile =  new StagedFile(AddedFileName,AddedFile,addedBlobSha1);
        File BlobFile = join(BLOB_DIR, addedBlobSha1);
        writeObject(BlobFile, addedBlob); // store the blob into blob file under the Blobs dir

        // TODO:If the current working version of the file is identical to the version in the current commit,
        //  do not stage it to be added, and remove it from the staging area if it is already there
        Commit HEADCommit = Utils.getCommitFromPointer("HEAD");
        if (HEADCommit.getContainingBlobs().containsValue(addedBlob.getSha1())) {
            //meaning the addedBlob has existed in the HEAD commit.so do not stage it and remote it from the Staging area if it existed.
            join(ADDSTAGE_DIR,AddedFileName).delete();
        } else {
            //addedBlob do not exist in HEAD commit ,so we add it to the AddStage.
            File toStagedFile = join(ADDSTAGE_DIR, AddedFileName);
            writeObject(toStagedFile,AddedStagedFile);
        }

    }


    static void CommitCommand(String CommitMessage) {
        // TODO: 1.copy the prevCommit containingBlobs to newCommit and adjust it with addStage.(done)
        //  2.create the right newCommit with correct timestamp,parent etc.(done)
        //  3.write the newCommit to the Commits Folder.(done)
        //  4.update the branches(done)
        Commit prevCommit = Utils.getCommitFromPointer("HEAD");
        String parSha1 = prevCommit.getCurSha1();
        Map<String,String> newCommitContainingBlobs = prevCommit.getContainingBlobs(); //not updated
        // updateNewCommitContaingBlobs
        List<String> addStageFilesList = Utils.plainFilenamesIn(ADDSTAGE_DIR);
        List<String> rmStageFilesList = Utils.plainFilenamesIn(RMSTAGE_DIR);
        if (addStageFilesList.isEmpty() && rmStageFilesList.isEmpty()) {
            errorMessage("No changes added to the commit.");
        }
        for (String addFileName : addStageFilesList) {
            StagedFile inAddStageFile = readObject(join(ADDSTAGE_DIR,addFileName), StagedFile.class);
            newCommitContainingBlobs.put(addFileName,inAddStageFile.getBolbSha1());
        }
        for (String rmFileName : rmStageFilesList) {
            StagedFile inRmStageFile = readObject(join(RMSTAGE_DIR,rmFileName), StagedFile.class);
            newCommitContainingBlobs.remove(rmFileName,inRmStageFile.getBolbSha1());
        }
        Commit newCommit = new Commit(CommitMessage,parSha1,newCommitContainingBlobs);
        writeObject(join(COMMIT_DIR,newCommit.getCurSha1()),newCommit);//step3
        String curBranchName = readObject(HEAD_FILE,String.class);
        writeObject(join(POINTERS_DIR,curBranchName),newCommit.getCurSha1());
        clearTwoStages();
    }


    static void rmCommand(String rmFileName) {
        boolean isStaged = false;
        boolean isTracked = false;
        boolean isExisted = false;
        if (Utils.plainFilenamesIn(ADDSTAGE_DIR).contains(rmFileName)) {
            isStaged = true;
        }
        if (Utils.getCommitFromPointer("HEAD").getContainingBlobs().containsKey(rmFileName)) {
            isTracked = true;
        }
        if (Utils.plainFilenamesIn(CWD).contains(rmFileName)) {
            isExisted = true;
        }

        if (isStaged) {
            Utils.unstageAddStageFile(rmFileName);
        } else if(isTracked) {
            //Stage it for removal
            File rmStageFileDes = join(RMSTAGE_DIR,rmFileName); // the position
            Blob rmBlob = new Blob(rmFileName,join(CWD,rmFileName));
            StagedFile RemovedStageFile = new StagedFile(rmFileName,join(CWD,rmFileName),rmBlob.getSha1());
            writeObject(rmStageFileDes,RemovedStageFile); // write it to the RmStage folder

            //remove the file from the working directory if the user has not already done so
            restrictedDelete(join(CWD,rmFileName));
        } else {
            throw error("No reason to remove the file.");
        }
    }

    static void logCommand() {
        Commit curCommit = getCommitFromPointer("HEAD");
        while(!(curCommit.getParSha1().equals(""))) {
            showCommitInfo(curCommit);
            String parSha1 = curCommit.getParSha1();
            curCommit = readObject(join(COMMIT_DIR,parSha1), Commit.class);
        }
        showCommitInfo(curCommit);
    }

    static void globalLogCommand() {
        for (String CommitSha1 : plainFilenamesIn(COMMIT_DIR)) {
            Commit curCommit = readObject(join(COMMIT_DIR,CommitSha1),Commit.class);
            showCommitInfo(curCommit);
        }
    }

    static void findCommand(String message) {
        int findCount = 0;
        for (String CommitSha1 : plainFilenamesIn(COMMIT_DIR)) {
            Commit curCommit = readObject(join(COMMIT_DIR,CommitSha1), Commit.class);
            if (curCommit.getMessage().equals(message)) {
                System.out.println(CommitSha1);
                findCount ++;
            }
        }
        if (findCount == 0) {
            throw error("Found no commit with that message.");
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
    static void checkoutToBranch(String desBranchName) {
        List<String> branches = plainFilenamesIn(POINTERS_DIR);
        boolean isTheBranchExisted = false;
        String desBranchCommitSha1 = "";
        for (String branch : branches) {
            if (branch.equals(desBranchName)) {
                isTheBranchExisted = true;
                desBranchCommitSha1 = readObject(join(POINTERS_DIR,branch),String.class);
                break;
            }
        }
        if (!isTheBranchExisted) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (readObject(HEAD_FILE,String.class).equals(desBranchName)) {
            errorMessage("No need to checkout the current branch.");
        }
        Commit desCommit = readObject(join(COMMIT_DIR,desBranchCommitSha1), Commit.class);
        String curCommitSha1 = getCommitFromPointer("HEAD").getCurSha1();
        Commit curCommit = readObject(join(COMMIT_DIR,curCommitSha1), Commit.class);
        Map<String,String> curCommitContaingBlobs = curCommit.getContainingBlobs();// Map<fileName,Blob.sha1> e.g.{"Hello.txt","0e93"}
        Map<String,String> desCommitContaingBlobs = desCommit.getContainingBlobs();
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
        //delete files tracked in curCommit but untracked in desCommit
        for (Map.Entry<String,String> entry : curCommitContaingBlobs.entrySet()) {
            String curFileName = entry.getKey();
            if (!desCommitContaingBlobs.containsKey(curFileName)) {
                restrictedDelete(join(CWD,curFileName));
            }
        }
        //overwrite files tracked in the desCommit
        for (Map.Entry<String,String> entry : desCommitContaingBlobs.entrySet()) {
            String curBlobSha1 = entry.getValue();
            Blob curBlob = readObject(join(BLOB_DIR,curBlobSha1), Blob.class);
            String curBlobFileName = curBlob.getFileName();
            String curBlobContent = curBlob.getFileContent();
            writeContents(join(CWD,curBlobFileName),curBlobContent);
        }
        //update the HEAD pointer
        writeObject(HEAD_FILE,desBranchName);
        //clear stages
        clearTwoStages();
    }

    // checkout -- [file name]
    static void checkoutToHeadWithOneFile(String fileName) {
        String HeadSha1 = getCommitFromPointer("HEAD").getCurSha1();
        checkoutToSpecificCommitWithOneFile(HeadSha1,fileName);
    }

    // checkout [commit id] -- [file name]
    static void checkoutToSpecificCommitWithOneFile(String desCommitSha1,String fileName){
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
        Commit desCommit = readObject(join(COMMIT_DIR,fullDesCommitSha1), Commit.class);
        String desBlobSha1 = desCommit.getContainingBlobs().get(fileName);
        if (desBlobSha1 == null) {
            errorMessage("File does not exist in that commit.");
        }
        Blob desBlob = readObject(join(BLOB_DIR,desBlobSha1),Blob.class);
        writeContents(join(CWD,fileName),desBlob.getFileContent());
    }

    static void branchCommand(String newBranchName) {
        List<String> branches = plainFilenamesIn(POINTERS_DIR);
        if (branches.contains(newBranchName)) {
            errorMessage("A branch with that name already exists.");
        } else {
            String HEADCommitSha1 = getCommitFromPointer("HEAD").getCurSha1();
            writeObject(join(POINTERS_DIR,newBranchName),HEADCommitSha1);
        }
    }

    static void rmBranchCommand(String rmBranchName) {
         String curBranchName = readObject(HEAD_FILE,String.class);
         List<String> allBranches = plainFilenamesIn(POINTERS_DIR);
         if (!allBranches.contains(rmBranchName)) {
             errorMessage("A branch with that name does not exist.");
         }
         if (rmBranchName.equals(curBranchName)) {
             errorMessage("Cannot remove the current branch.");
         }
         join(POINTERS_DIR,rmBranchName).delete();
    }

    static void resetCommand(String desCommitSha1) {
        String curBranchName = readObject(HEAD_FILE,String.class);
        writeObject(join(POINTERS_DIR,curBranchName),desCommitSha1);
        checkoutToBranch(curBranchName);
    }


}
