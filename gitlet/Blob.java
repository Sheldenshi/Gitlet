package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Blob object that stores the contents of files.
 * each of the Blob objects are stored in objects files.
 * @author Shelden Shi
 */
public class Blob implements Serializable {
    /** contents of files. */
    private String contents;

    /**
     * Constructor.
     * @param file file that we are storing contents from
     * @throws IOException
     */
    public Blob(File file) throws IOException {
        contents = Utils.readContentsAsString(file);
        storeBlob();
    }

    /**
     * Constructor.
     * @param fileName file name of the file that we are storing info.
     * @throws IOException
     */
    public Blob(String fileName) throws IOException {
        File file = new File(fileName);
        contents = Utils.readContentsAsString(file);
        storeBlob();
    }

    /** Creates a file named by Blob Object's code.
     * And write this Blob into the file.
     * @throws IOException
     */
    public void storeBlob() throws IOException {
        String code = Utils.sha1(Utils.serialize(this));
        File file = Utils.join(Files.OBJECTS, code);
        file.createNewFile();
        Utils.writeObject(file, this);
    }

    /**
     * get contents of a blob object(file contents).
     * @return contents in as a string.
     */
    public String getContents() {
        return contents;
    }

    /**
     * Compares if two blob objects are the same.
     * @param blobCode sha1 code of a blob.
     * @return true of false.
     */
    public boolean equals(String blobCode) {
        if (!code().equals(blobCode)) {
            return false;
        }
        return true;
    }

    /**
     * Gets blob objects from given remoteRepo.
     * @param remoteRepo where the blob objects from.
     * @param blobID which blob object to get.
     * @return a blob obj.
     */
    public static Blob getRemoteBlob(File remoteRepo, String blobID) {
        File objects  = Utils.join(remoteRepo, "objects");
        File blobFile = Utils.join(objects, blobID);
        Blob blob = Utils.readObject(blobFile, Blob.class);
        return blob;
    }

    /**
     * returns sha1 code of a blob object.
     * @return sha1 code
     */
    public String code() {
        return Utils.sha1(Utils.serialize(this));
    }

}
