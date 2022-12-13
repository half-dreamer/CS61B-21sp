package gitlet;

import edu.princeton.cs.algs4.StdIn;

import java.io.File;
import java.io.IOException;

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
    public static String HEAD; // store pointed Commit's sha1 String
    public static int BRANCHS_COUNT = 1;

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR,"Commits");
    public static final File BLOB_DIR = join(GITLET_DIR,"Blobs");
    public static final File POINTERS_DIR = join(GITLET_DIR,"CommitPOinters");
    public static final File HEAD_FILE = join(POINTERS_DIR,"HEAD");
    public static final File Master_FILE = join(POINTERS_DIR,"Master");
    public static final File ADDSTAGE_DIR = join(POINTERS_DIR,"AddStage");
/**
     *  .gitlet (folder)
     *          Commits (folder)
     *                  >commit (flie)(filename: commit.sha1 , content:Commit(Object))
     *                  >commit (file)
     *          Blobs (folder)
     *                  >blob (file) (filename: blob.sha1, ...)
     *                  >blob (file)
     *         CommitPointers(folder)
     *                  >Commit(filename:Pointer Name, content: commit sha1 the pointer point at)
     *                  >e.g. (filename:HEAD,content :some commit sha1)
     *          AddStage(folder)
     *                  >StagedFile (fliename: added file name , content: added file(blob)'s sha1)


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
        Commit initCommit = new Commit();
        writeObject(join(COMMIT_DIR,initCommit.getCurSha1()),initCommit);
        HEAD = initCommit.getCurSha1();
        writeObject(HEAD_FILE,HEAD);
        writeObject(Master_FILE,HEAD);
    }

    static void AddCommand(String AddedFileName) {
        File AddedFile = join(CWD,AddedFileName);
        if (!AddedFile.exists()) {
            Utils.error("File does not exist.");
        }

        File StagedFile = join(ADDSTAGE_DIR,AddedFileName);
        String AddedFileContent = readContentsAsString(AddedFile);
        String AddedFileSha1 = Utils.sha1(AddedFileContent);
        File AddedBlobFile = join(BLOB_DIR,AddedFileSha1);

        writeContents(AddedBlobFile,AddedFileContent); //create a blob of added file
        writeContents(StagedFile,AddedFileContent); //add the added file to AddStage

    }

    static void CommitCommand(String CommitMessage) {

    }
}
