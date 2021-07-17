package achecrawler.util;

@SuppressWarnings("serial")
public class StorageException extends Exception {

    public StorageException() {
        super();
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Throwable detail) {
        super(detail);
    }

    public StorageException(String message, Throwable detail) {
        super(message, detail);
    }

}