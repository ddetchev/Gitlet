package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Class to keep track of commit tree, current branch, blobs, and HEAD node.
 * @author Daniel Detchev
 */
public class Track implements Serializable {
    /** Variable extending HashMap<String, Commit> for commit tree. */
    private static NewHashCommit tree;
    /** Variable for HEAD node in string format. */
    private static String head;
    /** Variable for the current branch, also pointing to HEAD. */
    private static String currentBranch;
    /** Variable extending HashMap<Blob, String> for blobs and the
     * file names they point to.
     */
    private static NewHashBlobReverse blobs;
    /** Variable extending HashMap<String, Blob> for the staging area
     * with file names pointing to blobs.
     */
    private static NewHashBlob stagingArea;
    /** Variable extending HashMap<String, Blob> for the removal area. */
    private static NewHashBlob removal;
    /** Variable extending HashMap<String, ArrayList<Commits>> for all
     * branch names mapped to the commits they contain.
     */
    private static NewHashBranch goodBranches;

    /** Constructor for track. */
    public Track() {
        blobs = new NewHashBlobReverse();
        Utils.writeObject(Repository.getAllBlobs(), blobs);
        goodBranches = new NewHashBranch();
        Utils.writeObject(Repository.getGoodBranches(), goodBranches);
        stagingArea = new NewHashBlob();
        Utils.writeObject(Repository.getStaging(), stagingArea);
        removal = new NewHashBlob();
        Utils.writeObject(Repository.getRemoval(), removal);
        head = currentBranch;
        tree = new NewHashCommit();
    }

    /** Getter method for the commit tree.
     * @return the commit tree HashMap<String, Commit>.
     */
    public static NewHashCommit getTree() {
        return tree;
    }

    /** Getter method for the blobs made.
     * @return the blobs made in HashMap<Blob, String> form.
     */
    public static NewHashBlobReverse getBlobs() {
        return blobs;
    }

    /** Getter method for the staging area.
     * @return the staging area in HashMap<String, Blob> form.
     */
    public static HashMap<String, Blob> getStagingArea() {
        return stagingArea;
    }

    /** Getter method for the mapping of branch names to their commits.
     * @return this mapping in HashMap<String, ArrayList<Commit>> form.
     */
    public static NewHashBranch getNiceBranches() {
        return goodBranches;
    }

    /** Getter method for the current branch that head points to.
     * @return the branch name in String format.
     */
    public static String getCurrentBranch() {
        return currentBranch;
    }

    /** Method to set the current branch variable.
     * @param set String of the branch name to be set.
     */
    public static void setCurrentBranch(String set) {
        Track.currentBranch = set;
    }
}