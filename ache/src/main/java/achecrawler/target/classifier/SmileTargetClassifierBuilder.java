package achecrawler.target.classifier;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import achecrawler.util.SmileUtil;
import smile.classification.RandomForest;
import smile.classification.SVM;
import smile.classification.SoftClassifier;
import smile.data.AttributeDataset;
import smile.data.parser.ArffParser;
import smile.math.Math;
import smile.math.kernel.LinearKernel;
import smile.validation.Accuracy;
import smile.validation.ConfusionMatrix;
import smile.validation.CrossValidation;

public class SmileTargetClassifierBuilder {

    public static void trainModel(String trainingPath, String outputPath, String learner,
            int responseIndex, boolean skipCrossValidation) throws Exception {

        if (learner == null) {
            learner = "SVM";
        }

        System.out.println("Learning algorithm: " + learner);
        String modelFilePath = Paths.get(outputPath, "pageclassifier.model").toString();

        ArffParser arffParser = new ArffParser();
        arffParser.setResponseIndex(responseIndex);

        Path arffFilePath = Paths.get(trainingPath, "/smile_input.arff");
        FileInputStream fis = new FileInputStream(arffFilePath.toFile());
        System.out.println("Writting temporarily data file to: " + arffFilePath.toString());

        AttributeDataset trainingData = arffParser.parse(fis);
        double[][] x = trainingData.toArray(new double[trainingData.size()][]);
        int[] y = trainingData.toArray(new int[trainingData.size()]);

        SoftClassifier<double[]> finalModel = null;
        if (skipCrossValidation) {
            System.out.println("Starting model training on whole dataset...");
            finalModel = trainClassifierNoCV(learner, x, y);
        } else {
            System.out.println("Starting cross-validation...");
            finalModel = trainModelCV(learner, x, y);
        }
        System.out.println("Writing model to file: " + modelFilePath);
        SmileUtil.writeSmileClassifier(modelFilePath, finalModel);
    }

    private static SoftClassifier<double[]> trainModelCV(String learner, double[][] x, int[] y) {

        int numberOfFolds = 5;
        int bestFold = 0;
        double bestAccuracy = 0d;
        SoftClassifier<double[]> bestModel = null;

        System.out.printf("\n=== %d-Fold Cross-validation ===\n", numberOfFolds);

        CrossValidation cv = new CrossValidation(y.length, numberOfFolds);

        for (int i = 0; i < numberOfFolds; i++) {
            System.out.printf("\nTraining model for fold %d...\n\n", i);

            double[][] trainx = Math.slice(x, cv.train[i]);
            int[] trainy = Math.slice(y, cv.train[i]);

            SoftClassifier<double[]> model = trainModel(learner, trainx, trainy);

            double[][] testx = Math.slice(x, cv.test[i]);
            int[] testy = Math.slice(y, cv.test[i]);

            int[] predictions = model.predict(testx);

            double accuracy = printSummary(testy, predictions);

            if (accuracy > bestAccuracy) {
                bestModel = model;
                bestFold = i;
            }
        }

        System.out.printf("\nKeeping best model (from Fold %d).\n", bestFold);

        int[] predictedY = bestModel.predict(x);

        System.out.println("\n=== Summary of Best Model on Full Dataset ===\n");
        printSummary(predictedY, y);

        return bestModel;
    }

    private static SoftClassifier<double[]> trainClassifierNoCV(String learner,
            double[][] x, int[] y) {

        System.out.println("\n=== Train Model (No Cross-Validation) ===\n");

        // Train the Model
        long start = System.currentTimeMillis();
        SoftClassifier<double[]> model = trainModel(learner, x, y);
        long timeTaken = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Time taken to build model: " + timeTaken + " seconds");

        // Test on training data
        int[] predictedY = new int[y.length];
        start = System.currentTimeMillis();
        predictedY = model.predict(x);
        timeTaken = (System.currentTimeMillis() - start) / 1000;

        System.out.println("Time taken to test model on training data: " + timeTaken + " seconds");
        System.out.println();

        System.out.println("=== Error on TRAINING data ===\n");
        System.out.println("(DON'T USE AS AN EVALUATION MEASURE!)\n");
        printSummary(y, predictedY);

        return model;
    }

    private static SoftClassifier<double[]> trainModel(String learner, double[][] x, int[] y) {
        if (learner.equals("SVM")) {
            SVM<double[]> svm = new SVM<>(new LinearKernel(), 0.01);
            svm.learn(x, y);
            svm.finish();
            svm.trainPlattScaling(x, y);
            return svm;
        } else if (learner.equals("RandomForest")) {
            RandomForest randomForest = new RandomForest(x, y, 100);
            return randomForest;
        } else {
            throw new IllegalArgumentException("Unknow learning algorithm: " + learner);
        }
    }

    private static double printSummary(int[] truth, int[] predictions) {
        int errors = countErrors(predictions, truth);
        double accuracy = new Accuracy().measure(truth, predictions);;
        int correctPredictions = truth.length - errors;

        System.out.printf("     Test instances: %d\n", truth.length);
        System.out.printf("Correct predictions: %d\n", correctPredictions);
        System.out.printf("  Prediction errors: %d\n", errors);
        System.out.printf("           Accuracy: %.5f\n", accuracy);
        System.out.printf("\nConfusion Matrix\n\n");
        System.out.println(confusionMatrix(truth, predictions));
        System.out.println();

        return accuracy;
    }

    private static String confusionMatrix(int[] truth, int[] predictions) {
        // SMILE's ConfusionMatrix class throws an ArrayIndexOutOfBoundsException when truth vector
        // contains less classes than possible classes in the predictions vector. Here we just check
        // that condition to avoid stopping due to this error.
        Set<Integer> ySet = new HashSet<>();
        int maxClass = 0;
        for (int i = 0; i < truth.length; i++) {
            ySet.add(truth[i]);
            maxClass = java.lang.Math.max(maxClass, predictions[i]);
            maxClass = java.lang.Math.max(maxClass, truth[i]);
        }
        if (maxClass >= ySet.size()) {
            return String.format("[Can't print confusion matrix for %d class(es)]", ySet.size());
        } else {
            return new ConfusionMatrix(truth, predictions).toString();
        }
    }

    private static int countErrors(int[] predictions, int[] thruth) {
        int errors = 0;
        for (int i = 0; i < thruth.length; i++) {
            if (thruth[i] != predictions[i]) {
                errors++;
            }
        }
        return errors;
    }

}
