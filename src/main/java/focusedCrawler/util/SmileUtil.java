package focusedCrawler.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import smile.classification.Classifier;

public class SmileUtil {

	public static Classifier<double[]> loadSmileClassifier(String modelFilePath) {
        try {
            InputStream is = new FileInputStream(modelFilePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            Classifier<double[]> classifier = (Classifier<double[]>) objectInputStream.readObject();
            objectInputStream.close();
            return classifier;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Failed to load smile classifier instance from file: " + modelFilePath, e);
        }
    }
	
	
	public static void writeSmileClassifier(String modelFilePath, Classifier<double[]> classifier) {
        try {
            OutputStream os = new FileOutputStream(modelFilePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            objectOutputStream.writeObject(classifier);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to write smile classifier instance from file: " + modelFilePath, e);
        }
    }
	
}
