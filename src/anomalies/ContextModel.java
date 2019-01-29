/**
 * Author: XauthorX
 * 12/01/18
 */
package anomalies;

import processing.Relationships;
import anomalies.entities.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ContextModel implements Serializable {
    public final HashMap<SmartTriplet, Histogram> histograms;      //Feature histograms
    public final HashMap<String, Long> singleCounts;
    public final HashMap<ClassPairUnordered, Long> pairCounts;
    public final int numImages;                         //Number of analyzed images
    public final String[] labels;
    public Map<ClassPair, Double> cf;        //certainty factors, for pair probabilities

    /**
     * Constructor.
     * @param histograms the set of histogram statistics
     * @param singleCounts n. images containing each single class
     * @param pairCounts n. images containing each class pair
     * @param numImages n. of analyzed images
     * @param classLabels class labels
     */
    public ContextModel(HashMap<SmartTriplet, Histogram> histograms,
                        HashMap<String, Long> singleCounts,
                        HashMap<ClassPairUnordered, Long> pairCounts,
                        int numImages,
                        String [] classLabels,
                        Map<ClassPair, Double> cf) {
        this.histograms = histograms;
        this.pairCounts = pairCounts;
        this.singleCounts = singleCounts;
        this.numImages = numImages;
        this.labels = classLabels;
        this.cf = cf;
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
    public static ContextModel load(File inputFile) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(inputFile));
        ContextModel res = null;
        try {
            res = (ContextModel) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException();
        }
        in.close();
        return res;
    }


    public void printNhistogramsForType(int minsup, float thr_h) {
        long totPos;
        long totArea;
        long totWidth;
        long totHeight;
        long totCF;

        List<Map.Entry<SmartTriplet, Histogram>> entries = histograms.entrySet().stream()
                .filter(e -> {

                    if (e.getValue().support <minsup)
                        return false;
                    for (float val : e.getValue().histValues) {
                        if (val<=1-thr_h)
                            return true;
                        if (val>thr_h)
                            return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        totPos = entries.stream().filter(e->e.getKey().relType.toString().equals("POSITION")).count();
        totArea = entries.stream().filter(e->e.getKey().relType.toString().equals("AREA")).count();
        totWidth = entries.stream().filter(e->e.getKey().relType.toString().equals("WIDTH")).count();
        totHeight = entries.stream().filter(e->e.getKey().relType.toString().equals("HEIGHT")).count();

        List<Double> cfValues = cf.values().stream()
                .map(val->1.0f*(val+1)/2)
                .filter(val->
                {
                    if (val<=1-thr_h)
                        return true;
                    if (val>thr_h)
                        return true;
                    return false;
                })
                .collect(Collectors.toList());

        totCF=cfValues.size();

        System.out.println(thr_h + "\tPos:\t" + totPos);
        System.out.println(thr_h + "\tAre:\t" + totArea);
        System.out.println(thr_h + "\tWid:\t" + totWidth);
        System.out.println(thr_h + "\tHei:\t" + totHeight);
        System.out.println(thr_h + "\tCF.:\t" + totCF);

    }




    public void printNhistograms(int minsup, float thr_h) {
        List<Map.Entry<SmartTriplet, Histogram>> entries = histograms.entrySet().stream()
                .filter(e -> {

                    if (e.getValue().support <minsup)
                        return false;
                    for (float val : e.getValue().histValues) {
                        if (val<=1-thr_h)
                            return true;
                        if (val>thr_h)
                            return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        int total = entries.size();

        List<Double> cfValues = cf.values().stream()
                .map(val->1.0f*(val+1)/2)
                .filter(val->
                {
                    if (val<=1-thr_h)
                        return true;
                    if (val>thr_h)
                        return true;
                    return false;
                })
                .collect(Collectors.toList());

        total+=cfValues.size();

        System.out.println(thr_h + "\t" + total);
    }





    /**
     * Print a summary of the learned data.
     */
    public void printSummary(File outputFile, Relationships.RelType type) throws FileNotFoundException {
        float neverThresh = 0.05f;
        List<Map.Entry<SmartTriplet, Histogram>> entries = histograms.entrySet().stream()
                .filter(e -> {
                    if (e.getKey().relType!=null && e.getKey().relType!=type)
                        return false;
                    if (e.getValue().support <20)
                        return false;
                    for (float val : e.getValue().histValues) {
                        if (val<=neverThresh)
                            return true;
                    }
                    return false;
                })
                /*.sorted((a,b)->{

                    if (a.getKey().relType.ordinal()<b.getKey().relType.ordinal())
                        return 1;
                    else if (a.getKey().relType.ordinal()>b.getKey().relType.ordinal())
                        return -1;

                    else {
                        return a.getKey().subject.compareTo(b.getKey().subject);
                    }

                })*/
                .sorted((a,b)->{
                    float maxA = getMax(a.getValue().histValues);
                    float maxB = getMax(b.getValue().histValues);
                    return Float.compare(maxB, maxA);
                })


                //.limit(500)
                .collect(Collectors.toList());







        PrintWriter fout = new PrintWriter(outputFile);

        String header = "";
        if (type==Relationships.RelType.POSITION) {
            for (int i=0; i<Relationships.PosType.values().length; i++)
                header +=  RelativeFeaturesExtractor.getFeatureString(type, i) + "  ";
        }
        else {
            for (int i=0; i<Relationships.Size.values().length; i++)
                header +=  RelativeFeaturesExtractor.getFeatureString(type, i) + "  ";
        }
        fout.println(header);

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
                arrStr += String.format(" %s=%.2f", RelativeFeaturesExtractor.getFeatureString(t.relType, i).substring(0,1),
                        (float)arr[i]/h.support);

                explain +=  RelativeFeaturesExtractor.getFeatureString(t.relType, i) + "\t\t";
            }
            arrStr += "]";
            //String params = String.format(" supp=%6d imp=%.3f jacc=%.3f", h.support, h.logicalImportance, h.jaccard);

            //String params = String.format(" impur=%.3f import=%.3f supp=%4d jacc=%.3f imp2=%.3f", h.impurity, h.importance, h.support, h.jaccard, h.importance2);
            fout.println(String.format("%17s%17s%12s", t.subject, t.reference, " sup=" + h.support + "  ") + arrStr + " " + SmartRule.explain(t,h,neverThresh));
        }
        fout.close();
        //Display transition probabilities
        /*for (Map.Entry<ClassPair, Float> t : transProb.entrySet()) {
            ClassPair p = t.getKey();
            String pairStr = String.format("<%15s, %15s>", p.a, p.b);
            fout.println(pairStr + " " + String.format("%.3f",t.getValue()));
        }*/

    }

    public void printCF(File outputFile) throws FileNotFoundException {
        PrintWriter fout = new PrintWriter(outputFile);
        List<Map.Entry<ClassPair,Double>> cfList = cf.entrySet().stream().sorted((a,b)->Double.compare(b.getValue(),a.getValue()))
                                                        .collect(Collectors.toList());


        for (Map.Entry<ClassPair,Double> entry : cfList) {
            fout.println(String.format("%40s %.5f",entry.getKey(),entry.getValue()));
        }

        fout.close();
    }

    private float getMax(float[] values) {
        float max = values[0];
        for (float cur : values)
            if (cur>max) max=cur;
        return max;
    }


}
