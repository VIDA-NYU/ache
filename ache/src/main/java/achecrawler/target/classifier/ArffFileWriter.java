package achecrawler.target.classifier;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import achecrawler.util.vsm.VSMElement;
import achecrawler.util.vsm.VSMVector;

public class ArffFileWriter {

    public void writeArff(String outputFile, VSMVector[][] trainingData,
            List<VSMElement> features) throws IOException {

        StringBuffer header = new StringBuffer();
        header.append("@RELATION TSFC");
        header.append("\n");
        header.append("\n");

        List<String> attributes = new ArrayList<String>();
        for (VSMElement elem : features) {
            header.append("@ATTRIBUTE ");
            if (elem.getWord().equals("class")) {
                // This is a hack, weka does not allow attribute with name class.
                elem.setWord("class-random-string");
            }
            header.append(elem.getWord());
            attributes.add(elem.getWord());
            header.append(" REAL");
            header.append("\n");
        }
        header.append("@ATTRIBUTE class {");
        for (int i = 0; i < trainingData.length - 1; i++) {
            header.append("CLASS_" + i + ",");
        }
        header.append("CLASS_" + (trainingData.length - 1) + "}");

        FileWriter writer = new FileWriter(outputFile, false);
        writer.write(header.toString());
        writer.write(writeSparseDataEntries(trainingData, attributes));
        writer.close();
    }


    /**
     * Writes ARFF data entries using the sparse format, e.g.:
     * 
     * <pre>
     * {@literal @}DATA
     * {1 X, 3 Y, 4 "class A"}
     * {2 W, 4 "class B"}
     * </pre>
     * 
     * @param examples
     * @param attributes
     * @param sb
     * @return
     */
    private String writeSparseDataEntries(VSMVector[][] examples, List<String> attributes) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("\n");
        sb.append("@DATA");
        sb.append("\n");
        for (int classNumber = 0; classNumber < examples.length; classNumber++) {
            for (int i = 0; i < examples[classNumber].length; i++) {
                VSMVector example = examples[classNumber][i];
                sb.append("{");
                for (int j = 0; j < attributes.size(); j++) {
                    VSMElement feature = example.getElement(attributes.get(j));
                    if (feature != null) {
                        sb.append(j);
                        sb.append(" ");
                        sb.append((int) feature.getWeight());
                        sb.append(",");
                    }
                }
                sb.append(attributes.size());
                sb.append(" CLASS_" + classNumber + "}");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
