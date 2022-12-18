package gitlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static gitlet.Repository.*;


/**
 * Assorted utilities.
 * <p>
 * Give this file a good read as it provides several useful utility functions
 * to save you some time.
 *
 * @author P. N. Hilfinger
 */
class Utils {

    /**
     * The length of a complete SHA-1 UID as a hexadecimal numeral.
     */
    static final int UID_LENGTH = 40;

    /* SHA-1 HASH VALUES. */

    /**
     * Returns the SHA-1 hash of the concatenation of VALS, which may
     * be any mixture of byte arrays and Strings.
     */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (
                NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /**
     * Returns the SHA-1 hash of the concatenation of the strings in
     * VALS.
     */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* FILE DELETION */

    /**
     * Deletes FILE if it exists and is not a directory.  Returns true
     * if FILE was deleted, and false otherwise.  Refuses to delete FILE
     * and throws IllegalArgumentException unless the directory designated by
     * FILE also contains a directory named .gitlet.
     */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /**
     * Deletes the file named FILE if it exists and is not a directory.
     * Returns true if FILE was deleted, and false otherwise.  Refuses
     * to delete FILE and throws IllegalArgumentException unless the
     * directory designated by FILE also contains a directory named .gitlet.
     */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /**
     * Return the entire contents of FILE as a byte array.  FILE must
     * be a normal file.  Throws IllegalArgumentException
     * in case of problems.
     */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Return the entire contents of FILE as a String.  FILE must
     * be a normal file.  Throws IllegalArgumentException
     * in case of problems.
     */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /**
     * Write the result of concatenating the bytes in CONTENTS to FILE,
     * creating or overwriting it as needed.  Each object in CONTENTS may be
     * either a String or a byte array.  Throws IllegalArgumentException
     * in case of problems.
     */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str = new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException |
                 ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     * Throws IllegalArgumentException in case of problems.
     */
    static <T extends Serializable> T readObject(File file, Class<T> expectedClass) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException |
                 ClassCastException |
                 ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Write OBJ to FILE.
     */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* DIRECTORIES */

    /**
     * Filter out all but plain files.
     */
    private static final FilenameFilter PLAIN_FILES = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return new File(dir, name).isFile();
        }
    };

    /**
     * Returns a list of the names of all plain files in the directory DIR, in
     * lexicographic order as Java Strings.  Returns null if DIR does
     * not denote a directory.
     */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /**
     * Returns a list of the names of all plain files in the directory DIR, in
     * lexicographic order as Java Strings.  Returns null if DIR does
     * not denote a directory.
     */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */

    /**
     * Return the concatentation of FIRST and OTHERS into a File designator,
     * analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     * method.
     */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /**
     * Return the concatentation of FIRST and OTHERS into a File designator,
     * analogous to the {@link java.nio.file.Paths.#get(String, String[])}
     * method.
     */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    /* SERIALIZATION UTILITIES */

    /**
     * Returns a byte array containing the serialized contents of OBJ.
     */
    static byte[] serialize(Serializable obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }



    /* MESSAGES AND ERROR REPORTING */

    /**
     * Return a GitletException whose message is composed from MSG and ARGS as
     * for the String.format method.
     */
    static GitletException error(String msg, Object... args) {
        return new GitletException(String.format(msg, args));
    }

    /**
     * Print a message composed from MSG and ARGS as for the String.format
     * method, followed by a newline.
     */
    static void message(String msg, Object... args) {
        System.out.printf(msg, args);
        System.out.println();
    }

    static void errorMessage(String errorMessage) {
        System.out.println(errorMessage);
        System.exit(0);
    }

    static Commit getCommitFromPointer(String PointerName) {
        if (PointerName.equals("HEAD")) {
            PointerName = readObject(HEAD_FILE, String.class);
        }
        File CommitPointerFile = join(POINTERS_DIR, PointerName);
        String readCommitSha1 = readObject(CommitPointerFile, String.class);
        File readCommitFile = join(COMMIT_DIR, readCommitSha1);
        return readObject(readCommitFile, Commit.class);
    }

    static void unstageAddStageFile(String toBeUnstagedFile) {
        join(ADDSTAGE_DIR, toBeUnstagedFile).delete(); //unstage file in AddStage
    }

    static void showCommitInfo(Commit toBeShowedCommit) {
        System.out.println("===");
        System.out.println("commit " + toBeShowedCommit.getCurSha1());
        if (toBeShowedCommit.isHasMutiplePars()) {
            // if this commit has multiple parents
            System.out.print("Merge: " + toBeShowedCommit.getParSha1().substring(0, 7));
            for (String mergedInParSha1 : toBeShowedCommit.getMergedInParSha1s()) {
                System.out.print(" " + mergedInParSha1.substring(0, 7));
            }
            System.out.println();
        }
        System.out.println("Date: " + toBeShowedCommit.DateInString());
        System.out.println(toBeShowedCommit.getMessage());
        System.out.println();
    }

    static void printBranches() {
        System.out.println("=== Branches ===");
        Commit curCommit = getCommitFromPointer("HEAD");
        String curBranch = readObject(HEAD_FILE, String.class);
        System.out.println("*" + curBranch);
        if (!(plainFilenamesIn(POINTERS_DIR) == null)) {
            for (String Branch : plainFilenamesIn(POINTERS_DIR)) {
                if (Branch.equals(curBranch) || Branch.equals("HEAD")) {
                    continue;
                } else {
                    System.out.println(Branch);
                }
            }
        }
        System.out.println();
    }

    static void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        if (!(plainFilenamesIn(ADDSTAGE_DIR) == null)) {
            for (String AddStageFileName : plainFilenamesIn(ADDSTAGE_DIR)) {
                System.out.println(AddStageFileName);
            }
        }
        System.out.println();
    }

    static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        if (!(plainFilenamesIn(RMSTAGE_DIR) == null)) {
            for (String removedFileName : plainFilenamesIn(RMSTAGE_DIR)) {
                System.out.println(removedFileName);
            }
        }
        System.out.println();
    }

    // Note : this methode do not print in lexicographical order , but in random order
    // if you want to print in lexicographical order, sort the modifiedButNotStagedFilesMap.keySet() in lexi order
    static void printModifiedButNotStagedFiles() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        final String modified = "modified";
        final String deleted = "deleted";
        List<String> CWD_FileNames = plainFilenamesIn(CWD);
        Map<String,String> modifiedButNotStagedFilesMap = new HashMap<>(); // <fileName,"deleted"/"modified">
        //case 1:Tracked in the current commit, changed in the working directory, but not staged
        Commit curCommit = getCommitFromPointer("HEAD");
        Map<String, String> curCommitContainingBlobs = curCommit.getContainingBlobs();
        for (Map.Entry<String,String> curCommitBlobEntry : curCommitContainingBlobs.entrySet()) {
            String iterFileName = curCommitBlobEntry.getKey();
            String iterFileBlobSha1 = curCommitBlobEntry.getValue();
            String iterFileContentInCommit = readObject(join(BLOB_DIR,iterFileBlobSha1), Blob.class).getFileContent();
            if (CWD_FileNames.contains(iterFileName)) {
                if (!iterFileContentInCommit.equals(readContentsAsString(join(CWD,iterFileName)))) {
                    modifiedButNotStagedFilesMap.put(iterFileName,modified);
                }
            }
        }
        // case 2:Staged for addition, but with different contents than in the working directory
        // or Staged for addition, but deleted in the working directory
        for (String iterFileName : plainFilenamesIn(ADDSTAGE_DIR)) {
            String iterFileBlobSha1 = readContentsAsString(join(ADDSTAGE_DIR,iterFileName));
            String iterFileContent = readObject(join(BLOB_DIR,iterFileBlobSha1),Blob.class).getFileContent();
            if (CWD_FileNames.contains(iterFileName)) {
                if (iterFileContent.equals(readContentsAsString(join(CWD,iterFileName)))) {
                    modifiedButNotStagedFilesMap.put(iterFileName,modified);
                }
            } else {
                modifiedButNotStagedFilesMap.put(iterFileName,deleted);
            }
        }
        //case 3 :Not staged for removal, but tracked in the current commit and deleted from the working directory.
        List<String> removedStageFileNames = plainFilenamesIn(RMSTAGE_DIR);
        for (Map.Entry<String,String> curCommitBlobEntry : curCommitContainingBlobs.entrySet()) {
            String iterFileName = curCommitBlobEntry.getKey();
            String iterFileBlobSha1 = curCommitBlobEntry.getValue();
            if (!removedStageFileNames.contains(iterFileName) && !CWD_FileNames.contains(iterFileName)) {
                modifiedButNotStagedFilesMap.put(iterFileName,deleted);
            }
        }
        for (Map.Entry<String,String> modifiedButNotStagedFileEntry : modifiedButNotStagedFilesMap.entrySet()) {
            String fileName = modifiedButNotStagedFileEntry.getKey();
            String modiOrDele = modifiedButNotStagedFileEntry.getValue();
            System.out.println(fileName + " " + "(" + modiOrDele + ")");
        }
        System.out.println();
    }

    static void printUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        Commit curCommit = getCommitFromPointer("HEAD");
        Map<String,String> curCommitContainingBlobs = curCommit.getContainingBlobs();
        List<String> untrackedFileNames = new ArrayList<>();
        addUntrackedFilesTo(untrackedFileNames);
        for (String untrackedFileName : untrackedFileNames) {
            System.out.println(untrackedFileName);
        }
        System.out.println();
    }

    public static void addUntrackedFilesTo(List<String> untrackedFileNames) {
        Commit curCommit = getCommitFromPointer("HEAD");
        Map<String, String> curCommitContainingBlobs = curCommit.getContainingBlobs();
        for (String workingFileName : plainFilenamesIn(CWD)) {
            if (!curCommitContainingBlobs.containsKey(workingFileName)) {
                untrackedFileNames.add(workingFileName);
            }
        }
    }

    // TODO:above two method needs to be completed
    static void clearTwoStages() {
        if (!(plainFilenamesIn(ADDSTAGE_DIR) == null)) {
            for (String toDeleteFileName : plainFilenamesIn(ADDSTAGE_DIR)) {
                join(ADDSTAGE_DIR, toDeleteFileName).delete();
            }
        }
        if (!(plainFilenamesIn(RMSTAGE_DIR) == null)) {
            for (String toDeleteFileName : plainFilenamesIn(RMSTAGE_DIR)) {
                join(RMSTAGE_DIR, toDeleteFileName).delete();
            }
        }
    }

    static void IncorrectOperands() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }

    /**
     * use DFS to iterate through the rootCommit and update the depthMap
     *
     * @param rootCommit   the start Commit which has not been put into the rooDepthMap
     * @param rootDepthMap the DepthMap of root Commit
     * @param curDepth     the current depth of root Commit
     * @return
     */
    static void changeDepthMapOf(Commit rootCommit, Map<String, Integer> rootDepthMap, int curDepth) {
        if (rootCommit.getParSha1().equals("")) {
            // base case : the rootCommit is the init commit
            rootDepthMap.put(rootCommit.getCurSha1(), curDepth);
            return;
        }
        rootDepthMap.put(rootCommit.getCurSha1(), curDepth);
        Commit firstParCommit = readObject(join(COMMIT_DIR, rootCommit.getParSha1()), Commit.class);
        changeDepthMapOf(firstParCommit, rootDepthMap, curDepth + 1);
        if (rootCommit.isHasMutiplePars()) {
            for (String otherParCommitSha1 : rootCommit.getMergedInParSha1s()) {
                Commit otherParCommit = readObject(join(COMMIT_DIR, otherParCommitSha1), Commit.class);
                changeDepthMapOf(otherParCommit, rootDepthMap, curDepth + 1);
            }
        }
    }

    /**
     * Iterate through two depthMap and find the splitCommit corresponding to minSumOfDistances
     *
     * @param curCommitDepthMap
     * @param mergedInCommitDepthMap
     * @return
     */
    static Commit findSplitCommit(Map<String, Integer> curCommitDepthMap, Map<String, Integer> mergedInCommitDepthMap) {
        int minSumOfDistance = Integer.MAX_VALUE;
        String splitCommitSha1 = null;
        for (Map.Entry<String, Integer> mergedInCommitDepthMapEntry : mergedInCommitDepthMap.entrySet()) {
            String iterateCommitSha1 = mergedInCommitDepthMapEntry.getKey();
            if (curCommitDepthMap.containsKey(iterateCommitSha1)) {
                // i.e. the entry is the cross part Commit of two Maps
                int iterateCommitSumOfDistances = curCommitDepthMap.get(iterateCommitSha1) + mergedInCommitDepthMap.get(iterateCommitSha1);
                if (iterateCommitSumOfDistances < minSumOfDistance) {
                    minSumOfDistance = iterateCommitSumOfDistances;
                    splitCommitSha1 = iterateCommitSha1;
                }
            }
        }
        if (splitCommitSha1 == null) {
            System.out.println("Something wrong occur,because we can't find the splitCommit!");
        }
        return readObject(join(COMMIT_DIR, splitCommitSha1), Commit.class);
    }

    static void fixMergeConflict(boolean isIterBlobExistInCurCommit, boolean isIterBolbExistInMergedInCommit, String curCommitBlobSha1, String mergedInCommitBlobSha1, String iterFileName, Map<String, String> newMergeCommitContainingBlobs) {
        // cur != mergedIn  merge conflict occur ( modified in different ways)
        System.out.println("Encountered a merge conflict.");
        // store conflict string in the file and create a blob storing this file content
        File solveMergeConfilctFile = join(CWD, "solveMergeConflictFile");
        String curBlobFileContent = ""; // when cur doesn't exist, its content is empty
        String mergedInBlobFileContent = ""; // when mergedIn doesn't exist, its content is empty
        if (isIterBlobExistInCurCommit) {
            curBlobFileContent = readObject(join(BLOB_DIR, curCommitBlobSha1), Blob.class).getFileContent();
        }
        if (isIterBolbExistInMergedInCommit) {
            mergedInBlobFileContent = readObject(join(BLOB_DIR, mergedInCommitBlobSha1), Blob.class).getFileContent();
        }
        writeContents(solveMergeConfilctFile, "<<<<<<< HEAD\n" + curBlobFileContent + "=======\n" + mergedInBlobFileContent + ">>>>>>>\n");
        Blob mergeConlictBlob = new Blob(iterFileName, solveMergeConfilctFile);
        newMergeCommitContainingBlobs.put(iterFileName, mergeConlictBlob.getSha1());
        writeObject(join(BLOB_DIR, mergeConlictBlob.getSha1()), mergeConlictBlob);
    }

    public static void assertHasInitialedGitRepo() {
        if (!GITLET_DIR.exists()) {
            errorMessage("Not in an initialized Gitlet directory.");
        }
    }


}
