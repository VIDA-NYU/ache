package achecrawler.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import smile.classification.SoftClassifier;

public class SmileUtil {

    public static SoftClassifier<double[]> loadSmileClassifier(String modelFilePath) {
        try {
            InputStream is = new FileInputStream(modelFilePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            @SuppressWarnings("unchecked")
            SoftClassifier<double[]> classifier = (SoftClassifier<double[]>) objectInputStream.readObject();
            objectInputStream.close();
            return classifier;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Failed to load smile classifier instance from file: " + modelFilePath, e);
        }
    }


    public static void writeSmileClassifier(String modelFilePath,
            SoftClassifier<double[]> classifier) {
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
