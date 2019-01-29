package anomalies;


import anomalies.entities.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by XauthorX on 26/08/17.
 * Contains the trained Smart-Model
 */
public class ContextModelOld implements Serializable {
    final HashMap<SmartTriplet, Histogram> histograms;       //Feature histograms


    final HashMap<String, Float> singleCounts;
    final HashMap<ClassPair, Float> pairCounts;


    final HashMap<ClassPair, Float> pairProb;                     //P(A|B) pair class probabilities
    final HashMap<ClassPair, Float> labelProb;                    //P(actual | predicted) label probability
    final HashMap<ClassPair, Float> transProb;                    //P(predicted | actual) transition probability
    final double[] precision, recall, f1score;                    //Class accuracy statistics
    final String[] labels;

    /**
     * Constructor
     * @param histograms smartrules
     *
     *                   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @param pairProb pair probabilities
     * @param transProb transition probabilities
     * @param labelProb label probabilities
     * @param precision class precision
     * @param recall class recall
     * @param f1score class f1 score
     * @param classLabels the ordered set of class labels.
     */
    public ContextModelOld(HashMap<SmartTriplet, Histogram> histograms,

                           HashMap<String, Float> singleCounts,
                           HashMap<ClassPair, Float> pairCounts,

                           HashMap<ClassPair, Float> pairProb,
                           HashMap<ClassPair,Float> transProb,
                           HashMap<ClassPair,Float> labelProb,
                           double[] precision, double[] recall, double[] f1score, String [] classLabels) {
        this.histograms = histograms;
        this.pairCounts = pairCounts;
        this.singleCounts = singleCounts;

        this.pairProb = pairProb;
        this.transProb = transProb;
        this.labelProb = labelProb;
        this.labels = classLabels;
        this.precision = precision;
        this.recall = recall;
        this.f1score =f1score;
    }

    /**
     * Print a summary of the learned data.
     */
    public void printSummary() {
        //Display histograms
        for (Map.Entry<SmartTriplet, Histogram> entry : histograms.entrySet()) {
            SmartTriplet t = entry.getKey();
            Histogram h =  entry.getValue();
            String tripletStr = String.format("<%15s, %15s, %15s>", t.subject, t.relType, t.reference);
            int[] arr = h.getHistCountValues();
            String arrStr = "[";
            String explain="";
            if (arr.length==3)
                explain="\t\t";
            else
                explain="\t\t\t";
            for (int i=0; i<arr.length; i++) {
                arrStr += String.format(" %4d", arr[i]);

                explain +=  RelativeFeaturesExtractor.getFeatureString(t.relType, i) + "\t\t\t";
            }
            arrStr += "]";

            //String params = String.format(" impur=%.3f import=%.3f numSamp=%4d jacc=%.3f imp2=%.3f", h.impurity, h.importance, h.support, h.jaccard, h.importance2);
            //System.out.println(tripletStr + params + arrStr + explain);
        }

        //Display transition probabilities
        for (Map.Entry<ClassPair, Float> t : transProb.entrySet()) {
            ClassPair p = t.getKey();
            String pairStr = String.format("<%15s, %15s>", p.a, p.b);
            System.out.println(pairStr + " " + String.format("%.3f",t.getValue()));
        }

    }

    /**
     * Print a summary of the learned data.
     */
    public void printSummary(File outputFile) throws FileNotFoundException {
        float neverThresh = 0.05f;
        List<Map.Entry<SmartTriplet, Histogram>> entries = histograms.entrySet().stream()
                .filter(e -> {
                    if (e.getValue().support <20)
                        return false;
                    for (float val : e.getValue().histValues) {
                        if (val<=neverThresh)
                            return true;
                    }
                    return false;
                })
                ///.filter(e-> e.getKey().relType!=RelativeFeaturesExtractor.FeatureTypes.Distance)
                //.filter(e -> e.getValue().logicalImportance>=0.1)
                //.filter(e -> e.getValue().jaccard>0.1)
                //.sorted((a,b)->Math.round(Math.signum(a.getValue().logicalImportance-b.getValue().logicalImportance)))

                //.sorted((a,b)->Math.round(Math.signum(a.getHistogram().impurity-b.getHistogram().impurity)))
                //.sorted((a,b)->Math.round(Math.signum(b.getHistogram().importance-a.getHistogram().importance)))
                //.sorted((a,b)->Math.round(Math.signum(b.getValue().jaccard-a.getValue().jaccard)))


                //.sorted((a,b)->Math.round(Math.signum(b.getValue().logicalImportance-a.getValue().logicalImportance)))
                .sorted((a,b)->{

                        if (a.getKey().relType.ordinal()<b.getKey().relType.ordinal())
                            return 1;
                        else if (a.getKey().relType.ordinal()>b.getKey().relType.ordinal())
                            return -1;

                        else {
                            /*float v1 = a.getValue().logicalImportance;
                            float v2 = b.getValue().logicalImportance;
                            return Math.round(Math.signum(v2-v1));*/
                            return a.getKey().subject.compareTo(b.getKey().subject);
                        }

                })


                //.limit(500)
                .collect(Collectors.toList());

        PrintWriter fout = new PrintWriter(outputFile);

        //Display histograms
        for (Map.Entry<SmartTriplet, Histogram> entry : entries) {
            SmartTriplet t = entry.getKey();
            Histogram h =  entry.getValue();
            //String tripletStr = String.format("%20s(%6d), %5s, %20s, %15s(%6d)", t.subject, h.countA,
            //        t.relType.toString().substring(0,4), SmartRule.explain(t,h, neverThresh), t.reference, h.countB);
            int[] arr = h.getHistCountValues();
            String arrStr = "[";
            String explain="";
            if (arr.length==3)
                explain="\t\t";
            else
                explain="\t\t\t";
            for (int i=0; i<arr.length; i++) {
                arrStr += String.format(" %.2f", (float)arr[i]/h.support);

                explain +=  RelativeFeaturesExtractor.getFeatureString(t.relType, i) + "\t\t";
            }
            arrStr += "]";
            //String params = String.format(" supp=%6d imp=%.3f jacc=%.3f", h.support, h.logicalImportance, h.jaccard);

            //String params = String.format(" impur=%.3f import=%.3f supp=%4d jacc=%.3f imp2=%.3f", h.impurity, h.importance, h.support, h.jaccard, h.importance2);
            //fout.println(tripletStr + params + arrStr);
        }
        fout.close();
        //Display transition probabilities
        /*for (Map.Entry<ClassPair, Float> t : transProb.entrySet()) {
            ClassPair p = t.getKey();
            String pairStr = String.format("<%15s, %15s>", p.a, p.b);
            fout.println(pairStr + " " + String.format("%.3f",t.getValue()));
        }*/

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
    public static ContextModelOld load(File inputFile) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(inputFile));
        ContextModelOld res = null;
        try {
            res = (ContextModelOld) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException();
        }
        in.close();
        return res;
    }
}
