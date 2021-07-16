package achecrawler.util;

@SuppressWarnings("serial")
public class DataNotFoundException extends Exception {

    private boolean ranOutOfLinks;

    public DataNotFoundException() {}

    public DataNotFoundException(boolean ranOutOfLinks, String message) {
        super(message);
        this.ranOutOfLinks = ranOutOfLinks;
    }

    public boolean ranOutOfLinks() {
        return ranOutOfLinks;
    }

}
