package gitlet;

import com.sun.tools.corba.se.idl.Util;
import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.io.FileWriter;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Daniel Detchev
 */
public class Main implements Serializable {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    /** Variable for the repository. */
    private static Repository repo;

    /** Main method. */
    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        if (args[0].equals("init")) {
            initiate(args);
        } else if (args[0].equals("add")) {
            add(args);
        } else if (args[0].equals("commit")) {
            commit(args);
        } else if (args[0].equals("rm")) {
            rm(args);
        } else if (args[0].equals("log")) {
            log(args);
        } else if (args[0].equals("global-log")) {
            globalLog(args);
        } else if (args[0].equals("find")) {
            find(args);
        } else if (args[0].equals("status")) {
            status(args);
        } else if (args[0].equals("checkout")) {
            checkout(args);
        } else if (args[0].equals("branch")) {
            branch(args);
        } else if (args[0].equals("rm-branch")) {
            rmBranch(args);
        } else if (args[0].equals("reset")) {
            reset(args);
        } else if (args[0].equals("merge")) {
            merge(args);
        } else {
            System.out.println("No command with that name exists.");
        }
        return;
    }

    /** Method for the init command.
     * @param args arguments passed into the program, init command name.
     * @throws IOException if file can't be made.
     */
    public static void initiate(String[] args) throws IOException {
        if (RepositoryTracker.getFile().exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        } else if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        } else {
            repo = new Repository();
            RepositoryTracker.makeInitFile();
            Commit first = new Commit("initial commit", null);
            String sha1 = first.getSha1();
            File commit = Utils.join(Repository.getCommits(), sha1);
            commit.createNewFile();
            Utils.writeObject(commit, first);
            Track track = new Track();
            Track.getTree().put(sha1, first);
            Track.setCurrentBranch(sha1);
            Utils.writeObject(Repository.getBranchHead(), Track.
                    getCurrentBranch());
            File branch = Utils.join(Repository.getBranches(),
                    "master");
            branch.createNewFile();
            Utils.writeContents(branch, Track.getCurrentBranch());
            Utils.writeObject(Repository.getCommitTree(), Track.getTree());
            ArrayList<Commit> commitAdd = new ArrayList<>();
            commitAdd.add(first);
            Track.getNiceBranches().put("master", commitAdd);
            Utils.writeObject(Repository.getGoodBranches(), Track.
                    getNiceBranches());
        }
    }

    /** Method for the add command.
     * @param args arguments passed into the program, command name
     *             and file name.
     */
    public static void add(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        List<String> files = Utils.plainFilenamesIn(Repository.getwDir());
        boolean exists = false;
        if (files != null) {
            for (String f : files) {
                if (f.equals(args[1])) {
                    exists = true;
                    break;
                }
            }
        } else {
            System.out.println("File does not exist.");
        }
        if (!exists) {
            System.out.println("File does not exist.");
            return;
        }
        NewHashBlob staging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        NewHashBlobReverse blobs = Utils.readObject(Repository.getAllBlobs(),
                NewHashBlobReverse.class);
        File pointer = new File(args[1]);
        Blob fileBlob = new Blob(pointer);
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        NewHashBlob removal = Utils.readObject(Repository.getRemoval(),
                NewHashBlob.class);
        removal.remove(args[1]);
        Utils.writeObject(Repository.getRemoval(), removal);
        Commit c = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                c = elem;
                break;
            }
        }

        assert c != null;
        if (c.getBlobs().entrySet().size() != 0 && c.getBlobs().
                containsKey(args[1])) {
            String blobSha = c.getBlobs().get(args[1]);
            if (fileBlob.getSha1().equals(blobSha)) {
                staging.remove(args[1]);
                return;
            }
        }
        if (staging.entrySet().size() != 0) {
            for (NewHashBlob.Entry elem: staging.entrySet()) {
                if (elem.getKey().equals(args[1])) {
                    staging.put(args[1], fileBlob);
                    Utils.writeObject(Repository.getStaging(), staging);
                    blobs.put(fileBlob, args[1]);
                    Utils.writeObject(Repository.getAllBlobs(), blobs);
                    return;
                }
            }
        }
        blobs.put(fileBlob, args[1]);
        Utils.writeObject(Repository.getAllBlobs(), blobs);
        staging.put(args[1], fileBlob);
        Utils.writeObject(Repository.getStaging(), staging);
    }

    /** Method for the commit command.
     * @param args arguments passed into the program, commit command
     *             and commit message.
     */
    public static void commit(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet "
                   + "directory.");
            return;
        }
        if (args[1].length() == 0) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        NewHashBlob staging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        NewHashBlob removal = Utils.readObject(Repository.getRemoval(),
                NewHashBlob.class);
        if (staging.entrySet().size() == 0 && removal.entrySet().size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        Commit current = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                current = elem;
                break;
            }
        }
        assert current != null;
        Commit thisOne = new Commit(args[1], current.getSha1());
        thisOne.setBlobs(current.getBlobs());
        for (Blob elem: staging.values()) {
            thisOne.getBlobs().put(elem.getRead().getName(), elem.getSha1());
        }
        for (String elem: removal.keySet()) {
            thisOne.getBlobs().remove(elem);
        }
        NewHashCommit tree = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        tree.put(thisOne.getSha1(), thisOne);
        Utils.writeObject(Repository.getCommitTree(), tree);
        Utils.writeObject(Repository.getBranchHead(), thisOne.getSha1());
        String newHead = thisOne.getSha1();
        NewHashBlob newStaging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        newStaging.clear();
        Utils.writeObject(Repository.getStaging(), newStaging);
        NewHashBlob newRemoval = Utils.readObject(Repository.getRemoval(),
                NewHashBlob.class);
        newRemoval.clear();
        Utils.writeObject(Repository.getRemoval(), newRemoval);
        NewHashBranch branchesMap = Utils.readObject(Repository.
                        getGoodBranches(),
                NewHashBranch.class);
        List<String> branches = Utils.plainFilenamesIn(Repository.
                getBranches());
        assert branches != null;
        String branchName = "";
        for (String elem: branches) {
            File pointer = Utils.join(Repository.getBranches(), elem);
            String contents = Utils.readContentsAsString(pointer);
            if (contents.equals(headID)) {
                Utils.writeContents(pointer, newHead);
                branchName = elem;
                break;
            }
        }
        branchesMap.get(branchName).add(thisOne);
        Utils.writeObject(Repository.getGoodBranches(), branchesMap);
    }

    /** Method for the rm command.
     * @param args arguments passed into the program, rm command and
     *             fileName to remove.
     */
    public static void rm(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        NewHashBlob staging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        NewHashBlob removal = Utils.readObject(Repository.getRemoval(),
                NewHashBlob.class);
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        Commit current = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                current = elem;
                break;
            }
        }
        assert current != null;
        if (staging.remove(args[1]) == null && !current.getBlobs().
                containsKey(args[1])) {
            System.out.println("No reason to remove the file.");
            return;
        }
        staging.remove(args[1]);
        Utils.writeObject(Repository.getStaging(), staging);
        NewHashBlobReverse blobs = Utils.readObject(Repository.getAllBlobs(),
                NewHashBlobReverse.class);
        Blob fileBlob = null;
        if (current.getBlobs().containsKey(args[1])) {
            for (Blob elem: blobs.keySet()) {
                if (elem.getSha1().equals(current.getBlobs().get(args[1]))
                        && elem.getRead().getName().equals(args[1])) {
                    fileBlob = elem;
                    break;
                }
            }
            removal.put(args[1], fileBlob);
            Utils.writeObject(Repository.getRemoval(), removal);
            Utils.restrictedDelete(args[1]);
        }
    }

    /** Method for the log command.
     * @param args log command passed into the program.
     */
    public static void log(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized "
                   + "Gitlet directory.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        Commit current = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                current = elem;
                break;
            }
        }
        ArrayList<Commit> list = new ArrayList<>();
        assert current != null;
        while (current.getParent() != null) {
            list.add(current);
            String parent = current.getParent();
            if (parent.equals("null")) {
                break;
            }
            current = commits.get(parent);

        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println("===");
            System.out.println("commit " + list.get(i).getSha1());
            System.out.println("Date: " + list.get(i).getTime());
            if (i != list.size() - 1) {
                System.out.println(list.get(i).getMessage() + "\n");
            } else {
                System.out.println(list.get(i).getMessage());
            }
        }
    }

    /** Method for the global log command.
     * @param args global log command passed into the program.
     */
    public static void globalLog(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        int count = 0;
        for (Commit elem: commits.values()) {
            System.out.println("===");
            System.out.println("commit " + elem.getSha1());
            System.out.println("Date: " + elem.getTime());
            if (count != commits.entrySet().size() - 1) {
                System.out.println(elem.getMessage() + "\n");
            } else {
                System.out.println(elem.getMessage());
            }
            count++;
        }
    }

    /** Method for the fina command.
     * @param args find command passed into the program with commit
     *             message to find.
     */
    public static void find(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        NewHashCommit commits = Utils.readObject(Repository.
                        getCommitTree(),
                NewHashCommit.class);
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Commit> printCommits = new ArrayList<>();
        for (Commit elem: commits.values()) {
            list.add(elem.getMessage());
            printCommits.add(elem);
        }
        if (!list.contains(args[1])) {
            System.out.println("Found no commit with that message.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(args[1])) {
                System.out.println(printCommits.get(i).getSha1());
            }
        }
    }

    /** Method for the status command.
     * @param args status command passed into the program.
     */
    public static void status(String[] args) {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        NewHashBlob staging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        NewHashBlob removed = Utils.readObject(Repository.getRemoval(),
                NewHashBlob.class);
        List<String> branches = Utils.plainFilenamesIn(Repository.
                getBranches());
        ArrayList<String> stagedFiles = new ArrayList<>(staging.keySet());
        stagedFiles = RepositoryTracker.lexiSort(stagedFiles);
        ArrayList<String> removedFiles = new ArrayList<>(removed.keySet());
        removedFiles = RepositoryTracker.lexiSort(removedFiles);
        ArrayList<String> modNotStaged = new ArrayList<>();
        HashMap<String, String> modNotStagedTracker = new HashMap<>();
        ArrayList<String> untracked = new ArrayList<>();
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        Commit current = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                current = elem;
                break;
            }
        }
        assert current != null;
        NewHashBlobReverse allBlobs = Utils.readObject(Repository.getAllBlobs(),
                NewHashBlobReverse.class);
        ArrayList<String> allFileNames = new ArrayList<>(allBlobs.values());
        HashMap<String, String> deletedFiles = new HashMap<>();
        if (Repository.getwDir().listFiles() != null) {
            for (String elem: allFileNames) {
                boolean track = false;
                for (File file: Repository.getwDir().listFiles()) {
                    if (file.getName().equals(elem)) {
                        track = true;
                    }
                }
                if (!track) {
                    deletedFiles.put(elem, "test");
                }
            }
        }
        if (Repository.getwDir().listFiles() != null) {
            for (File elem: Repository.getwDir().listFiles()) {
                if (elem.isFile()) {
                    int x = 4;
                } else {
                    continue;
                }
                Blob wBlob = new Blob(elem);
                if (current.getBlobs().containsKey(elem.getName())
                        && (!current.getBlobs().get(elem.getName()).
                        equals(wBlob.getSha1()))
                        && !staging.containsKey(elem.getName())) {
                    modNotStaged.add(elem.getName());
                    modNotStagedTracker.put(elem.getName(), " (modified)");
                } else if (staging.containsKey(elem.getName())
                        && (!staging.get(elem.getName()).getSha1().
                        equals(wBlob.getSha1()))) {
                    modNotStaged.add(elem.getName());
                    modNotStagedTracker.put(elem.getName(), " (modified)");
                } else if (staging.containsKey(elem.getName())
                        && deletedFiles.containsKey(elem.getName())) {
                    modNotStaged.add(elem.getName());
                    modNotStagedTracker.put(elem.getName(), " (deleted)");
                } else if (!removed.containsKey(elem.getName())
                        && current.getBlobs().containsKey(elem.getName())
                        && deletedFiles.containsKey(elem.getName())) {
                    modNotStaged.add(elem.getName());
                    modNotStagedTracker.put(elem.getName(), " (deleted)");
                } else if (deletedFiles.containsKey(elem)
                        && !current.getBlobs().containsKey(elem.getName())
                        && !staging.containsKey(elem.getName())) {
                    untracked.add(elem.getName());
                }
            }
        }
        modNotStaged = RepositoryTracker.lexiSort(modNotStaged);
        untracked = RepositoryTracker.lexiSort(untracked);
        System.out.println("=== Branches ===");
        assert branches != null;
        for (String elem: branches) {
            File pointer = Utils.join(Repository.getBranches(), elem);
            if (Utils.readContentsAsString(pointer).equals(headID)) {
                System.out.println("*" + elem);
            } else {
                System.out.println(elem);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String elem: stagedFiles) {
            System.out.println(elem);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String elem: removedFiles) {
            System.out.println(elem);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String elem: modNotStaged) {
            System.out.println(elem + modNotStagedTracker.get(elem));
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String elem: untracked) {
            System.out.println(elem);
        }
    }

    /** Central method for checkout.
     * @param args passed into program, either fileName/commitID and
     *             fileName/branchName.
     * @throws IOException if can't create file to overwrite working
     * directory files.
     */
    public static void checkout(String[] args) throws IOException {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length == 3 && args[1].equals("--")) {
            checkout1(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkout2(args[1], args[3]);
        } else if (args.length == 2) {
            checkout3(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            return;
        }
    }

    /** Helper method for first checkout.
     * @param file name of file passed in to be checked out.
     * @throws IOException if can't create file to overwrite
     * working directory files.
     */
    public static void checkout1(String file) throws IOException {
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        NewHashCommit commits = Utils.readObject(Repository.
                        getCommitTree(),
                NewHashCommit.class);
        NewHashBlobReverse blobs = Utils.readObject(Repository.
                        getAllBlobs(),
                NewHashBlobReverse.class);
        Commit current = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                current = elem;
                break;
            }
        }
        assert current != null;
        if (!current.getBlobs().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String blobID = current.getBlobs().get(file);
            Blob winner = null;
            for (Blob elem: blobs.keySet()) {
                if (elem.getSha1().equals(blobID)) {
                    winner = elem;
                    break;
                }
            }
            File overwrite = new File(file);
            overwrite.createNewFile();
            assert winner != null;
            Utils.writeContents(overwrite, winner.getContentsString());
        }
    }

    /** Helper method for second checkout command.
     * @param commitID commit ID passed in signifying version of file to
     *                 be checked out.
     * @param file name of file to be checked out.
     * @throws IOException if can't create file to overwrite working
     * directory files.
     */
    public static void checkout2(String commitID, String file)
            throws IOException {
        boolean contains = false;
        NewHashBlobReverse blobs = Utils.readObject(Repository.
                        getAllBlobs(),
                NewHashBlobReverse.class);
        NewHashCommit commits = Utils.readObject(Repository.
                        getCommitTree(),
                NewHashCommit.class);
        String keyWinner = "";
        for (String key: commits.keySet()) {
            if (key.startsWith(commitID)) {
                contains = true;
                keyWinner = key;
                break;
            }
        }
        if (!contains) {
            System.out.println("No commit with that id exists.");
            return;
        } else if (!commits.get(keyWinner).getBlobs().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String blobID = commits.get(keyWinner).getBlobs().get(file);
            Blob winner = null;
            for (Blob elem: blobs.keySet()) {
                if (elem.getSha1().equals(blobID)) {
                    winner = elem;
                    break;
                }
            }
            File overwrite = new File(file);
            overwrite.createNewFile();
            assert winner != null;
            Utils.writeContents(overwrite, winner.getContentsString());
        }
    }

    /** Helper method for third checkout command.
     * @param branch name of branch name to be checked out.
     * @throws IOException if can't create overwrite file to
     * overwrite working
     * directory files.
     */
    public static void checkout3(String branch) throws IOException {
        NewHashBranch branches = Utils.readObject(Repository.
                        getGoodBranches(),
                NewHashBranch.class);
        NewHashCommit commits = Utils.readObject(Repository.
                        getCommitTree(),
                NewHashCommit.class);
        if (!branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        String currentBranch = "";
        for (File file: Repository.getBranches().listFiles()) {
            if (Utils.readContentsAsString(file).equals(headID)) {
                currentBranch = file.getName();
            }
        }
        String checkOutBranch = "";
        for (String elem: branches.keySet()) {
            if (!elem.equals(currentBranch)) {
                checkOutBranch = elem;
            }
        }
        ArrayList<Commit> list = new ArrayList<>(branches.
                get(branch));
        for (File file: Repository.getBranches().listFiles()) {
            if (file.isFile() && Utils.readContentsAsString(file).
                    equals(headID)
                    && file.getName().equals(branch)) {
                System.out.println("No need to checkout the "
                       + "current branch.");
                return;
            }
        }
        Commit checkoutCommit = list.get(list.size() - 1);
        Commit currentCommit = null;
        for (Commit elem: commits.values()) {
            if (elem.getSha1().equals(headID)) {
                currentCommit = elem;
                break;
            }
        }
        List<String> wDirFiles = Utils.plainFilenamesIn(Repository.
                getwDir());
        assert wDirFiles != null;
        for (String fileName: wDirFiles) {
            assert currentCommit != null;
            if (checkoutCommit.getBlobs().containsKey(fileName)
                    && !currentCommit.getBlobs().containsKey(fileName)) {
                System.out.println("There is an untracked file in the "
                       + "way; delete it, or add and commit it first.");
                return;
            }
        }
        NewHashBlobReverse blobs = Utils.readObject(Repository.getAllBlobs(),
                NewHashBlobReverse.class);
        for (String fileName: checkoutCommit.getBlobs().keySet()) {
            BufferedWriter overwrite = new BufferedWriter(
                    new FileWriter(fileName));
            for (Blob elem: blobs.keySet()) {
                if (elem.getSha1().equals(checkoutCommit.getBlobs().
                        get(fileName))) {
                    overwrite.write(elem.getContentsString());
                    break;
                }
            }
        }
        assert currentCommit != null;
        for (String fileName: currentCommit.getBlobs().keySet()) {
            ArrayList<Commit> listCurrent = branches.get(currentBranch);
            ArrayList<Commit> listCheckout = branches.get(checkOutBranch);
            if (listCurrent.get(listCurrent.size() - 1).getBlobs().
                    containsKey(fileName)
                    && !listCheckout.get(listCheckout.size() - 1).getBlobs()
                    .containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        if (!branch.equals("master")) {
            BufferedWriter overwrite = new BufferedWriter(
                    new FileWriter(Utils.join(Repository
                    .getBranches(), "master")));
            overwrite.write("");
            for (String branchName: branches.keySet()) {
                if (!branchName.equals("master")) {
                    File set = Utils.join(Repository.getBranches(), branchName);
                    Utils.writeContents(set, headID);
                }
            }
        } else {
            for (String branchName : branches.keySet()) {
                if (branchName.equals("master")) {
                    File set = Utils.join(Repository.getBranches(), branchName);
                    Utils.writeContents(set, headID);
                } else {
                    BufferedWriter overwrite = new BufferedWriter(
                            new FileWriter(Utils
                            .join(Repository.getBranches(), branchName)));
                    overwrite.write("");
                }
            }
        }
        Utils.writeObject(Repository.getBranchHead(), checkoutCommit.
                getSha1());
        NewHashBlob staging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        staging.clear();
        Utils.writeObject(Repository.getStaging(), staging);
    }

    /** Method for branch command.
     * @param args name of branch to be created.
     * @throws IOException if can't create branch file with the given
     * branch name.
     */
    public static void branch(String[] args) throws IOException {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        NewHashBranch branches = Utils.readObject(Repository.
                        getGoodBranches(),
                NewHashBranch.class);
        if (branches.containsKey(args[1])) {
            System.out.println("A branch with that name already exists.");
        }
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        String headBranch = "";
        for (File elem: Repository.getBranches().listFiles()) {
            if (elem.isFile() && Utils.readContentsAsString(elem).
                    equals(headID)) {
                headBranch = elem.getName();
                break;
            }
        }
        File branch = Utils.join(Repository.getBranches(), args[1]);
        branch.createNewFile();

        ArrayList<Commit> copyCommit = branches.get(headBranch);
        branches.put(args[1], copyCommit);
        Utils.writeObject(Repository.getGoodBranches(), branches);
    }

    /** Method for rm-branch command.
     * @param args name of branch passed in to be removed.
     */
    public static void rmBranch(String[] args) {
        NewHashBranch branches = Utils.readObject(Repository.
                        getGoodBranches(),
                NewHashBranch.class);
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (!branches.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        for (String branchName: Utils.plainFilenamesIn(Repository.
                getBranches())) {
            File pointer = Utils.join(Repository.getBranches(), branchName);
            if (Utils.readContentsAsString(pointer).equals(headID)) {
                if (branchName.equals(args[1])) {
                    System.out.println("Cannot remove the current branch.");
                    return;
                }
            }
        }
        Utils.restrictedDelete(args[1]);
        NewHashBranch branchCommits = Utils.readObject(Repository.
                        getGoodBranches(),
                NewHashBranch.class);
        branchCommits.remove(args[1]);
        Utils.writeObject(Repository.getGoodBranches(), branchCommits);
    }

    /** Method for reset command.
     * @param args reset command and name of commit ID to reset to.
     * @throws IOException
     */
    public static void reset(String[] args) throws IOException {
        if (!Repository.getGitDir().exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        boolean contains = false;
        NewHashBlobReverse blobs = Utils.readObject(Repository.
                        getAllBlobs(),
                NewHashBlobReverse.class);
        NewHashCommit commits = Utils.readObject(Repository.getCommitTree(),
                NewHashCommit.class);
        String keyWinner = "";
        for (String key : commits.keySet()) {
            if (key.startsWith(args[1])) {
                contains = true;
                keyWinner = key;
                break;
            }
        }
        if (!contains) {
            System.out.println("No commit with that id exists.");
            return;
        }
        NewHashBranch branches = Utils.readObject(Repository.
                        getGoodBranches(),
                NewHashBranch.class);
        String headID = Utils.readObject(Repository.getBranchHead(),
                String.class);
        Commit current = null;
        for (String elem: commits.keySet()) {
            if (elem.equals(keyWinner)) {
                current = commits.get(elem);
                break;
            }
        }
        String headBranch = "";
        for (File elem: Repository.getBranches().listFiles()) {
            if (elem.isFile() && Utils.readContentsAsString(elem).
                    equals(headID)) {
                headBranch = elem.getName();
                break;
            }
        }
        ArrayList<Commit> list = branches.get(headBranch);
        Commit wanted = list.get(list.size() - 1);
        for (String elem: wanted.getBlobs().keySet()) {
            if (!current.getBlobs().containsKey(elem)) {
                Utils.restrictedDelete(elem);
            }
        }

        for (String elem: current.getBlobs().keySet()) {
            BufferedWriter overwrite = new BufferedWriter(
                    new FileWriter(elem));
            NewHashBlobReverse blob = Utils.readObject(Repository.
                            getAllBlobs(),
                    NewHashBlobReverse.class);
            String contents = "";
            for (Blob elem2: blob.keySet()) {
                if (elem2.getSha1().equals(current.getBlobs().get(elem))) {
                    contents = elem2.getContentsString();
                    break;
                }
            }
            overwrite.write(contents);
        }


        Utils.writeObject(Repository.getBranchHead(), current.getSha1());
        for (File f: Repository.getBranches().listFiles()) {
            if (Utils.readContentsAsString(f).equals(headID)) {
                Utils.writeObject(f, current.getSha1());
            }
        }
        NewHashBlob staging = Utils.readObject(Repository.getStaging(),
                NewHashBlob.class);
        staging.clear();
        Utils.writeObject(Repository.getStaging(), staging);

    }

    /** Method for merge command.
     * @param args branch name to be merged into.
     */
    public static void merge(String[] args) {

    }
}