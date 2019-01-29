/**
 * Author: XauthorX
 * 12/01/18
 */
package anomalies;

import anomalies.entities.ClassPairUnordered;
import anomalies.entities.Histogram;
import anomalies.entities.SmartTriplet;

import java.io.*;
import java.util.HashMap;

public class ContextStatistics implements Serializable {
    public final HashMap<SmartTriplet, Histogram> histograms;      //Feature histograms
    public final HashMap<String, Long> singleCounts;
    public final HashMap<ClassPairUnordered, Long> pairCounts;
    public final int numImages;                                    //Number of analyzed images
    public final String[] labels;

    /**
     * Constructor.
     * @param histograms the set of histogram statistics
     * @param singleCounts n. images containing each single class
     * @param pairCounts n. images containing each class pair
     * @param numImages n. of analyzed images
     * @param classLabels class labels
     */
    public ContextStatistics(HashMap<SmartTriplet, Histogram> histograms,
                      HashMap<String, Long> singleCounts,
                      HashMap<ClassPairUnordered, Long> pairCounts,
                      int numImages,
                      String [] classLabels) {
        this.histograms = histograms;
        this.pairCounts = pairCounts;
        this.singleCounts = singleCounts;
        this.numImages = numImages;
        this.labels = classLabels;
    }

    /**
     * Write this model to file.
     * @param outputFile the output file
     * @throws IOException if some error occurred.
     */
    public void save(File outputFile) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputFile));
        out.writeObject(this);
        out.close();
    }

    /**
     * Read model from file
     * @param inputFile the input file
     * @return the loaded model.
     * @throws IOException if some error occurred
     */
    public static ContextStatistics load(File inputFile) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(inputFile));
        ContextStatistics res = null;
        try {
            res = (ContextStatistics) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException();
        }
        in.close();
        return res;
    }
}
