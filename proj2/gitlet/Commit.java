package gitlet;

// TODO: any imports you need here

import jdk.jshell.execution.Util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String curSha1;
    private String parSha1;
    private Map<String,String> containingBlobs; // Map<fileName,Blob.Sha1> e.g. {"Hello.txt","0e93cac"}
    Date timeStamp;
    DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z",Locale.ENGLISH);
    private List<String> mergedInParSha1 = new ArrayList<>();
    private boolean hasMutiplePars = false;

    /* TODO: fill in the rest of this class. */
    public Commit() {
        timeStamp = new Date(0);
        parSha1  = ""; // note : parSha1 is not null,instead,an empty String.For later execution of Sha1
        message = "initial commit";
        containingBlobs = new TreeMap<>(); // Map<fileName,Blob.Sha1> e.g. {"Hello.txt","0e93cac"}
        curSha1 = Utils.sha1(message,parSha1,containingBlobs.toString(),df.format(timeStamp));
    }
    public Commit(String message,String parSha1,Map<String,String> containingBlobs) {
        this.message = message;
        this.parSha1 = parSha1;
        this.timeStamp = new Date();
        this.containingBlobs = containingBlobs;
        this.curSha1 = Utils.sha1(message,parSha1,containingBlobs.toString(),df.format(timeStamp));
    }

    String getCurSha1() {
        return this.curSha1;
    }

    public Map<String, String> getContainingBlobs() {
        return containingBlobs;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public String getParSha1() {
        return parSha1;
    }


    public List<String> getMergedInParSha1s() {
        return mergedInParSha1;
    }

    public boolean isHasMutiplePars() {
        return hasMutiplePars;
    }
    public String DateInString() {
        return df.format(this.timeStamp);
    }

}
