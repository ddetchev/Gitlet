package gitlet;

import java.io.File;
import java.io.IOException;

/** Class for the repository containing all directory paths and names.
 * @author Daniel Detchev
 */
public class Repository {
    /** String representing the path of the working directory. */
    private static final String WORKING_DIR = System.getProperty("user.dir");
    /** String representing the name of the gitlet directory. */
    private static final String GITLET_DIR = ".gitlet";
    /** String for the name of the branches folder where each file is a
     * branch. */
    private static final String BRANCHES = "branches";
    /** String representing the name of the file containing the HEAD
     * node. */
    private static final String BRANCH_HEAD = "head";
    /** String representing the name of the file containing the staging
     * area. */
    private static final String STAGING_AREA = "staging";
    /** String representing the name of the commits folder where each commit
     *  is a file. */
    private static final String COMMITS = "commits";
    /** String representing the name of the file where all blobs ever made are
     *  stored. */
    private static final String BLOBS = "blobs";
    /** String representing the name of the file holding the commit tree in
     * a HashMap. */
    private static final String TREE = "commitTree";
    /** String representing the name of the file containing the staged for
     * removal area. */
    private static final String REMOVAL = "removal";
    /** String representing the file containing a HashMap mapping branch
     * names to the
     * list of commits they contain.
     */
    private static final String GOOD_BRANCHES = "goodBranches";
    /** File variable representing the .gitlet directory in the working
     * directory. */
    private static File gitDir = new File(GITLET_DIR);
    /** File variable representing the working directory path. */
    private static File wDir = new File(WORKING_DIR);
    /** File variable representing the path of the branches folder inside
     * .gitlet directory. */
    private static File branches = Utils.join(gitDir, BRANCHES);
    /** File variable for the file path of the HEAD node in the .gitlet
     * directory. */
    private static File branchHead = Utils.join(gitDir, BRANCH_HEAD);
    /** File variable for the staging area file path in the .gitlet
     * directory. */
    private static File staging = Utils.join(gitDir, STAGING_AREA);
    /** File variable for the commits folder path in the .gitlet
     * directory. */
    private static File commits = Utils.join(gitDir, COMMITS);
    /** File variable for the path of the file of all blobs made, in
     * .gitlet. */
    private static File blobs = Utils.join(gitDir, BLOBS);
    /** File variable for the path of the staged for removal area file
     * in .gitlet. */
    private static File removal = Utils.join(gitDir, REMOVAL);
    /** File variable for the path of the file containing mapping of
     * branch names to the
     * commits they contain.
     */
    private static File goodBranches = Utils.join(gitDir, GOOD_BRANCHES);
    /** File variable for the path of the file containing the commit tree. */
    private static File commitTree = Utils.join(gitDir, TREE);

    /** Constructor for repository. */
    public Repository() throws IOException {
        wDir.mkdir();
        gitDir.mkdir();
        commitTree.createNewFile();
        blobs.createNewFile();
        commits.mkdir();
        staging.createNewFile();
        branches.mkdir();
        goodBranches.createNewFile();
        branchHead.createNewFile();
        removal.createNewFile();
    }

    /** Getter method for the path of the .gitlet directory.
     * @return the file path of .gitlet directory.
     */
    public static File getGitDir() {
        return gitDir;
    }

    /** Getter method for the path of the working directory.
     * @return the file path of working directory.
     */
    public static File getwDir() {
        return wDir;
    }

    /** Getter method for the path of the branches folder.
     * @return the file path of branches folder.
     */
    public static File getBranches() {
        return branches;
    }

    /** Getter method for the path of the file of the HEAD node.
     * @return the file path of the file with the HEAD node.
     */
    public static File getBranchHead() {
        return branchHead;
    }

    /** Getter method for the path of the staged for removal file.
     * @return the file path of the staged for removal area file.
     */
    public static File getRemoval() {
        return removal;
    }

    /** Getter method for the path of the branch name/commit list file.
     * @return the file path of the file with HashMap of branches.
     */
    public static File getGoodBranches() {
        return goodBranches;
    }

    /** Getter method for the path of the staging area file.
     * @return the path of the staging area file.
     */
    public static File getStaging() {
        return staging;
    }

    /** Getter method for the path of all the commits made folder.
     * @return the path of the commits made file.
     */
    public static File getCommits() {
        return commits;
    }

    /** Getter method for the path of the all the blobs ever made file.
     * @return the path of all the blobs made file.
     */
    public static File getAllBlobs() {
        return blobs;
    }

    /** Getter method for the path of the commit tree file.
     * @return the file path of the commit tree file with HashMap of commits.
     */
    public static File getCommitTree() {
        return commitTree;
    }
}