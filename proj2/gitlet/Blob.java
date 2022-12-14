package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    String fileName;
    String fileContent;
    String sha1;

    public Blob(String BlobedFileName, File toBlobedFile) {
        fileContent = Utils.readContentsAsString(toBlobedFile);
        fileName = BlobedFileName;
        sha1 = Utils.sha1(fileContent);
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
