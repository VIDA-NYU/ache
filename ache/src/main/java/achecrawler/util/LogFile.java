package achecrawler.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogFile implements Closeable {

    private PrintWriter filePrinter;

    public LogFile(Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
            this.filePrinter = openLogFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file at path: " + filePath.toString(), e);
        }
    }

    private PrintWriter openLogFile(Path path) throws FileNotFoundException {
        boolean append = true;
        boolean autoFlush = true;
        FileOutputStream fos = new FileOutputStream(path.toFile(), append);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        return new PrintWriter(bos, autoFlush);
    }

    public void printf(String format, Object... args) {
        filePrinter.printf(format, args);
    }

    @Override
    public void close() {
        if (filePrinter != null) {
            filePrinter.close();
        }
    }

}
