package aarsy.github.com.ez_vcard_android;

/**
 * Created by abhay yadav on 01-Jan-17.
 */
public class VCFFiles {
    int fileId;
    String fileName;
    String filePath;

    public VCFFiles(int file_id, String file_name, String file_fullpath) {
        this.fileId=file_id;
        this.fileName=file_name;
        this.filePath=file_fullpath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}