package gitlet;

// TODO: any imports you need here

import jdk.jshell.execution.Util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    private Map<String,String> containingBlobs = new HashMap<>(); // Map<blob,Sha1> e.g. {"Hello.txt","0e93cac"}
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date timeStamp;

    /* TODO: fill in the rest of this class. */
    public Commit() {
        timeStamp = new Date(0);
        parSha1  = ""; // note : parSha1 is not null,instead,an empty String.For later execution of Sha1
        message = "initial commit";
        curSha1 = Utils.sha1(message,parSha1,containingBlobs.toString(),df.format(timeStamp));
    }
    public Commit(String message,String parSha1,Date timeStamp) {
        this.message = message;
        this.parSha1 = parSha1;
        this.timeStamp = timeStamp;
    }

    String getCurSha1() {
        return this.curSha1;
    }
}
