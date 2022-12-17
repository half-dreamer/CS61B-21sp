package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private String fileName;
    private String fileContent;
    private String sha1;

    public Blob(String blobedFileName, File toBlobedFile) {
        fileContent = Utils.readContentsAsString(toBlobedFile);
        fileName = blobedFileName;
        sha1 = Utils.sha1(fileName, fileContent);
    }

    public String getSha1() {
        return sha1;
    }

    public String getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }
}
