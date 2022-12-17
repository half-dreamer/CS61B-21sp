package gitlet;

import java.io.File;
import java.io.Serializable;

public class StagedFile implements Serializable {
    String fileName;
    String fileContent;
    String blobSha1;

    public StagedFile(String StagedFileName,File toStagedFile,String addedBlobSha1) {
        fileContent = Utils.readContentsAsString(toStagedFile);
        fileName = StagedFileName;
        this.blobSha1 = addedBlobSha1;
    }

    public StagedFile(String StagedFileName, String fileContent,String BlobSha1) {
        this.fileName = StagedFileName;
        this.fileContent = fileContent;
        this.blobSha1 = BlobSha1;
    }

    public String getBolbSha1() {
        return blobSha1;
    }

    public String getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }
}
