package achecrawler.target.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.util.string.StopList;
import achecrawler.util.string.StopListFile;
import achecrawler.util.vsm.VSMElement;
import achecrawler.util.vsm.VSMVector;

public class TargetClassifierBuilder {
    
    private static Logger logger = LoggerFactory.getLogger(TargetClassifierBuilder.class);
    
    private static int MAX_PAGE_SAMPLES = Integer.MAX_VALUE;

    private boolean stem = true; // if words should be stemmed before being used as features
    private StopList stoplist;
    private boolean skipCrossValidation;
    private int maxFeatures;

    public TargetClassifierBuilder(String stopwordsFile, boolean stem,
            boolean skipCrossValidation, int maxFeatures) throws IOException {
        this.stem = stem;
        this.stoplist = loadStopwords(stopwordsFile);
        this.skipCrossValidation = skipCrossValidation;
        this.maxFeatures = maxFeatures;
    }

    private StopList loadStopwords(String stopwordsFile) throws IOException {
        if (stopwordsFile != null && !stopwordsFile.isEmpty()) {
            return new StopListFile(stopwordsFile);
        } else {
            return StopListFile.DEFAULT;
        }
    }

    public void train(String learner, String trainingDataPath, String outputPath) throws Exception {

        String arffFilePath = trainingDataPath + "/smile_input.arff";

        Path positivePath = Paths.get(trainingDataPath, "positive");
        if (!Files.isDirectory(positivePath)) {
            throw new IllegalArgumentException(
                    positivePath + " must be a directory containing relevant training data.");
        }

        Path negativePath = Paths.get(trainingDataPath, "negative");
        if (!Files.isDirectory(negativePath)) {
            throw new IllegalArgumentException(
                    negativePath + " must be a directory containing irrelevant training data.");
        }

        //
        // Loading and parsing pages into feature vectors
        //
        System.out.println("Preparing training data...");

        File[] positiveFiles = positivePath.toFile().listFiles();
        System.out.println("Positive samples: " + positiveFiles.length);

        File[] negativeFiles = negativePath.toFile().listFiles();
        System.out.println("Negative samples: " + negativeFiles.length);

        System.out.println("Featurizing positive samples... ");
        int[] posIndexes = selectRandomNum(1, positiveFiles.length, MAX_PAGE_SAMPLES);
        List<VSMVector> positiveData = createPageVector(positiveFiles, stoplist, posIndexes);

        System.out.println("Featurizing negative samples... ");
        int[] negIndexes = selectRandomNum(1, negativeFiles.length, MAX_PAGE_SAMPLES);
        List<VSMVector> negativeData = createPageVector(negativeFiles, stoplist, negIndexes);

        //
        // Selecting features based on doc frequency
        //
        System.out.println("Selecting best features based on page frequency... ");
        DocumentFrequencyFeatureSelector featureSelector = new DocumentFrequencyFeatureSelector(maxFeatures);
        featureSelector.addFeature(positiveData);
        featureSelector.addFeature(negativeData);
        List<VSMElement> features = featureSelector.getBestFeatures();

        //
        // Writing data to ARFF file
        //
        VSMVector[][] trainingData = new VSMVector[2][];
        trainingData[0] = (VSMVector[]) positiveData.toArray(new VSMVector[positiveData.size()]);
        trainingData[1] = (VSMVector[]) negativeData.toArray(new VSMVector[negativeData.size()]);
        ArffFileWriter builder = new ArffFileWriter();
        builder.writeArff(arffFilePath, trainingData, features);

        //
        // Training SMILE classifier model
        //
        System.out.println("Training target classifier model...");
        SmileTargetClassifierBuilder.trainModel(trainingDataPath, outputPath, learner,
                features.size(), skipCrossValidation);

        //
        // Generating features file
        //
        Path featuresFilePath = Paths.get(outputPath, "pageclassifier.features");
        System.out.println("Writing features file to: " + featuresFilePath.toString());
        createFeaturesFile(featuresFilePath, features);
        
        //
        // Generating pageclassifier.yml
        //
        Path ymlFilePath = Paths.get(outputPath, "pageclassifier.yml");
        System.out.println("Writing pageclassifier.yml file to: " + ymlFilePath.toString());
        createPageClassifierYml(ymlFilePath);

        // Remove temporary file
        Files.delete(Paths.get(arffFilePath));

        System.out.println("done.");
    }

    private void createPageClassifierYml(Path ymlFilePath) throws IOException {
        FileWriter fwriter = new FileWriter(ymlFilePath.toFile());
        fwriter.write("type: smile\n");
        fwriter.write("parameters:\n");
        fwriter.write("  features_file: pageclassifier.features\n");
        fwriter.write("  model_file: pageclassifier.model\n");
        fwriter.close();
    }

    private void createFeaturesFile(Path featuresFilePath, List<VSMElement> features) {
        try {
            File featuresFile = featuresFilePath.toFile();
            FileWriter featuresWriter = new FileWriter(featuresFile);
            featuresWriter.write("CLASS_VALUES  S NS\n");
            featuresWriter.write("ATTRIBUTES");
            for (VSMElement word : features) {
                featuresWriter.write(" " + word.getWord());
            }
            featuresWriter.write("\n");
            featuresWriter.close();
        } catch (IOException e) {
            logger.error("Failed to write pageclassifier.features file.", e);
        }
    }

    public List<VSMVector> parseInputFiles(Path dataPath, StopList stoplist, int numOfElems)
            throws Exception {
        File[] pageFiles = dataPath.toFile().listFiles();
        System.out.println("Loaded files: " + pageFiles.length);

        int[] posIndexes = selectRandomNum(1, pageFiles.length, numOfElems);

        System.out.println("Featurizing positive samples... ");
        return createPageVector(pageFiles, stoplist, posIndexes);
    }

    private int[] selectRandomNum(long seed, int range, int elems) {
        if (elems > range) {
            elems = range;
        }
        int count = 0;
        Random random = new Random(seed);
        int next = random.nextInt(range);
        HashSet<Integer> nums = new HashSet<>();
        int[] result = new int[elems];
        while (count < elems) {
            Integer num = new Integer(next);
            if (!nums.contains(num)) {
                result[count] = next;
                nums.add(num);
                count++;
            }
            next = random.nextInt(range);
        }
        return result;
    }

    private List<VSMVector> createPageVector(File[] files, StopList stoplist, int[] indexes) throws IOException {
        List<VSMVector> pageVectors = new ArrayList<VSMVector>();
        for (int i = 0; i < files.length && i < indexes.length; i++) {
            File file = files[indexes[i]];
            try {
                String fileContent = readFileAsString(file);
                pageVectors.add(new VSMVector(fileContent, stoplist, stem));
            } catch (IOException e) {
                logger.error("Failed to process file: "+ file.toString(), e);
            }
        }
        return pageVectors;
    }

    private static String readFileAsString(File file) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(file));
        StringBuffer content = new StringBuffer();
        for (String line = input.readLine(); line != null; line = input.readLine()) {
            content.append(line);
            content.append("\n");
        }
        input.close();
        return content.toString();
    }

    /**
     * Selects best features based on document frequency.
     */
    public class DocumentFrequencyFeatureSelector {

        // max number of features (sample vector size)
        private int maxNumOfFeatures;

        // minimum document frequency for word be considered as feature
        final private int minDF = 5;
        
        public DocumentFrequencyFeatureSelector() {
            this(Integer.MAX_VALUE);
        }
        
        public DocumentFrequencyFeatureSelector(int maxNumOfFeatures) {
            this.maxNumOfFeatures = maxNumOfFeatures;
        }

        // holds document frequency counts
        private Map<String, VSMElement> docFrequencies = new HashMap<>();

        public void addFeature(List<VSMVector> pageVectors) {
            for (VSMVector vsm : pageVectors) {
                Iterator<VSMElement> it = vsm.getElements();
                while (it.hasNext()) {
                    VSMElement element = it.next();
                    String word = element.getWord();
                    VSMElement value = docFrequencies.get(word);
                    if (value == null) {
                        docFrequencies.put(word, new VSMElement(word, 1));
                    } else {
                        double count = value.getWeight() + 1;
                        docFrequencies.put(word, new VSMElement(word, count));
                    }
                }
            }
        }

        public List<VSMElement> getBestFeatures() {
            List<VSMElement> bestWords = new ArrayList<>(docFrequencies.values());
            bestWords.sort(VSMElement.DESC_ORDER_COMPARATOR);
            List<VSMElement> selectedFeatures = new ArrayList<>();
            for (VSMElement vsm : bestWords) {
                if (vsm.getWeight() > minDF) {
                    selectedFeatures.add(vsm);
                }
                if (selectedFeatures.size() >= maxNumOfFeatures) {
                    break;
                }
            }
            return selectedFeatures;
        }

    }
}
