package gitlet;

import java.io.File;
import java.io.Serializable;

/** Class for blobs or contents of files.
 * @author Daniel Detchev
 */
public class Blob implements Serializable {
    /** Variable for contents of blob. */
    private String contentsString;
    /** Variable for the read file of the blob. */
    private File read;

    /** Blog constructor passing in file.
     * @param f file to be passed in.
     */
    public Blob(File f) {
        read = new File(f.getName());
        contentsString = Utils.readContentsAsString(read);
    }

    /** Getter method for the file this blob points to.
     * @return the file path of the file this blob points to.
     */
    public File getRead() {
        return read;
    }

    /** Getter method for the contents of this blob.
     * @return contents of the blob in string format.
     */
    public String getContentsString() {
        return contentsString;
    }

    /** Getter method for the SHA-1 ID of this blob.
     * @return this blob's SHA-1 ID.
     */
    public String getSha1() {
        return Utils.sha1("blob", contentsString);
    }
}