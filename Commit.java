package gitlet;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/** Class for each commit object.
 * @author Daniel Detchev
 */
public class Commit implements Serializable {
    /** Variable for this commit's log message. */
    private String message;
    /** Variable for the time this commit was done, shown in
     * string format. */
    private String time;
    /** Variable for this commit's parent represented by its SHA-1 in
     * String format. */
    private String parent;
    /** Variable for the files this commit is tracking; keys are file
     * names,
     * values are blob SHA-1 IDs.
     */
    private HashMap<String, String> commitBlobs;
    /** Variable for the SHA-1 ID of this commit. */
    private String sha1;

    /** Constructor for commit object passing in message and parent.
     * @param message String commit log message
     * @param parent String commit parent
     */
    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        commitBlobs = new HashMap<>();
        if (parent == null) {
            SimpleDateFormat data =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
            data.setTimeZone(TimeZone.getTimeZone("PST"));
            this.time = data.format(new Date(0L));
        } else {
            SimpleDateFormat data =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
            data.setTimeZone(TimeZone.getTimeZone("PST"));
            this.time = data.format(new Date());
        }
    }

    /** Getter method for the log message of this commit.
     * @return this commit's log message in string format.
     */
    public String getMessage() {
        return this.message;
    }

    /** Getter method for the timestamp of this commit.
     * @return this commit's timestamp in string format.
     */
    public String getTime() {
        return this.time;
    }

    /** Getter method for the parent of this commit.
     * @return this commit's parent(SHA-1 ID) in string format.
     */
    public String getParent() {
        return "" + this.parent;
    }

    /** Method to set the contents of the HashMap this commit uses
     * to track files.
     * @param blobs HashMap of this commit''s blobs to be set.
     */
    public void setBlobs(HashMap<String, String> blobs) {
        this.commitBlobs = blobs;
    }

    /** Getter method for the HashMap used to track this commit's
     * files.
     * @return the files this commit tracks in format (String fileName
     * , blob SHA-1)
     */
    public HashMap<String, String> getBlobs() {
        return this.commitBlobs;
    }

    /** Getter method for the unique SHA-1 ID of this commit.
     * @return this commit's unique SHA-1 ID.
     */
    public String getSha1() {
        sha1 = Utils.sha1("commit", message, ("" + parent), time,
                commitBlobs.toString());
        return sha1;
    }
}