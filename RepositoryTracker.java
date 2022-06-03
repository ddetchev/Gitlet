package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Class to keep track of whether a .gitlet directory was made.
 * @author Daniel Detchev
 */
public class RepositoryTracker {
    /** Boolean to keep track of whether a .gitlet directory was made. */
    private static boolean created;
    /** File path of file used to track whether a .gitlet directory was made. */
    private static File tracker =
            Utils.join(Repository.getGitDir(), "initFile");

    /** Method used to make a file after calling init so that init can
     * be called only
     * once if this file exists.
     * @throws IOException if a problem occurs creating the init file.
     */
    public static void makeInitFile() throws IOException {
        if (!getCreated()) {
            tracker.createNewFile();
            created = true;
        }
    }

    /** Getter method for the boolean "created" to keep track of the init call.
     * @return the created boolean.
     */
    public static boolean getCreated() {
        return created;
    }

    /** Getter method for the file path of the initFile used to keep track of
     * the presence of the .gitlet directory.
     * @return File path of the initFile.
     */
    public static File getFile() {
        return tracker;
    }

    /** Method used to sort ArrayLists in lexicographic order for the status
     * command.
     * @param list ArrayList of strings to be sorted alphabetically.
     * @return the new alphabetically sorted list.
     */
    public static ArrayList<String> lexiSort(ArrayList<String> list) {
        ArrayList<String> list2 = list;
        for (int i = 0; i < list2.size() - 1; ++i) {
            for (int j = i + 1; j < list2.size(); ++j) {
                if (list2.get(i).compareTo(list2.get(j)) > 0) {
                    String temp = list2.get(i);
                    list2.set(i, list2.get(j));
                    list2.set(j, temp);
                }
            }
        }
        return list2;
    }

}