package gitlet;

import java.io.File;
import java.io.Serializable;

public class StagedFile implements Serializable {
    String fileName;
    String fileContent;
    String bolbSha1;

    public StagedFile(String StagedFileName,File toStagedFile,String addedBlobSha1) {
        fileContent = Utils.readContentsAsString(toStagedFile);
        fileName = StagedFileName;
        this.bolbSha1 = addedBlobSha1;
    }

    public String getBolbSha1() {
        return bolbSha1;
    }

    public String getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }
}
