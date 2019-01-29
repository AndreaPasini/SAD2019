package anomalies;


import processing.Relationships;
import scenery.AScenery;
import anomalies.entities.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by XauthorX on 15/04/17.
 * Computes the context model
 */
public class ContextModelBuilder {
    private int numImages;
    //Smartrules: Triplets and Histograms
    private Map<SmartTriplet, HistogramBuilder> hist;           //set of histograms, selected using triplets as key

    //Jaccard computation: (A & B)/(A U B) = (A & B)/(A + B - (A & B))
    //Pair probability computation: P(A | B) = (A & B)/(A)
    //Number of pairs which contain A-B
    private HashMap<ClassPairUnordered, Counter> pairCount;
    //Number of pairs which contain A
    private HashMap<String, Counter> singleCount;

    //Confusion Matrix
    private int[][] confMatrix;     //Confusion matrix [actual][predicted]
    private String[] labels;        //Class labels


    /**
     * Constructor
     */
    public ContextModelBuilder() {
        hist = new HashMap<>();
        pairCount = new HashMap<>();
        singleCount = new HashMap<>();
    }

    /**
     * Add a new image to the statistics.
     * @param scenery the input scenery (with ground truth labels)
     * @param rel the object relationships
     * @param labels the list of class labels (string)
     */
    public void addImage(AScenery scenery, Relationships rel, String[] labels) {

        int nObjects = rel.getNumObjects();
        int[] objLabels = scenery.getConnectedComponents().getLabels();
        int[] objectsSize = rel.getObjectsSize();
        Relationships.PosType[][] positions = rel.getPositions();
        Relationships.Size[][][] relSize = rel.getRelativeSize();

        //Transactional itemset:
        //Count object pairs
        HashSet<ClassPairUnordered> foundPairs = new HashSet<>();
        //Count objects
        HashSet<String> foundSingles = new HashSet<>();

        //For each object pair
        for (int i=0; i<nObjects; i++) {
            for (int j=i+1; j<nObjects; j++) {


                //Consider only true objects (denoising by object size)
                if (objectsSize[i]==0 || objectsSize[j]==0)
                    continue;

                //Create the triplet (subj, reference in alphabetical order)
                IndexStringPair isp = new IndexStringPair(i, labels[objLabels[i]-1], j, labels[objLabels[j]-1]);
                int s = isp.s;
                int r = isp.r;
                SmartTriplet triplet = isp.triplet;

                //Position
                if (positions[s][r]!=null) {
                    triplet.relType = Relationships.RelType.POSITION;
                    getHistogram(triplet.clone()).addOccurrence(positions[s][r].ordinal());
                }

                //Width
                if (relSize[s][r][Relationships.RelType.WIDTH.ordinal()]!=null) {
                    triplet.relType = Relationships.RelType.WIDTH;
                    getHistogram(triplet.clone()).addOccurrence(relSize[s][r][Relationships.RelType.WIDTH.ordinal()].ordinal());
                }
                //Height
                if (relSize[s][r][Relationships.RelType.HEIGHT.ordinal()]!=null) {
                    triplet.relType = Relationships.RelType.HEIGHT;
                    getHistogram(triplet.clone()).addOccurrence(relSize[s][r][Relationships.RelType.HEIGHT.ordinal()].ordinal());
                }
                //Area
                if (relSize[s][r][Relationships.RelType.AREA.ordinal()]!=null) {
                    triplet.relType = Relationships.RelType.AREA;
                    getHistogram(triplet.clone()).addOccurrence(relSize[s][r][Relationships.RelType.AREA.ordinal()].ordinal());
                }


                //Count only once <x,y>, but not <y,x>
                String a = triplet.subject;
                String b = triplet.reference;
                foundPairs.add(new ClassPairUnordered(a,b));
                foundSingles.add(a);
                foundSingles.add(b);
            }
        }

        //Pairs (A-B)
        for (ClassPairUnordered pair : foundPairs)
            getPairCount(pair).addOccurrence();
        //Count singles
        for (String single : foundSingles)
            getSingleCount(single).addOccurrence();

        numImages++;
    }

    /**
     * Build statistics form local data
     * @return the extracted model
     */
    public ContextStatistics createStatistics() {
        //Preparing output data
        HashMap<String, Long> singleCountMap = new HashMap<>();
        singleCount.forEach((str,count)->singleCountMap.put(str,count.getNumOccurrences()));
        //
        HashMap<ClassPairUnordered, Long> pairCountMap = new HashMap<>();
        pairCount.forEach((pair,count)->pairCountMap.put(pair,count.getNumOccurrences()));

        //Build histograms
        HashMap<SmartTriplet, Histogram> outputHistograms = new HashMap<>();
        hist.entrySet().stream()
                .forEach(e -> outputHistograms.put(e.getKey(), e.getValue().getHistogram((int)singleCount.get(e.getKey().subject).getNumOccurrences(),
                        (int)singleCount.get(e.getKey().reference).getNumOccurrences())));

        //Generate statistics
        ContextStatistics result = new ContextStatistics(outputHistograms,
                singleCountMap,
                pairCountMap,
                numImages,
                labels);

        return result;
    }

    /**
     * Build context model from statistics.
     * @param statistics the context statistics to build the model.
     * @return
     */
    public static ContextModel buildModel(ContextStatistics statistics) {
        int nImages = statistics.numImages;
        HashMap<String, Long> singleCounts=statistics.singleCounts;
        HashMap<ClassPairUnordered, Long> pairCounts=statistics.pairCounts;
        HashMap<ClassPair, Double> cf = new HashMap<>();

        //Computing CF
        //for each class pair:
        for (Map.Entry<ClassPairUnordered, Long> e : statistics.pairCounts.entrySet()) {
            String a = e.getKey().a;
            String b = e.getKey().b;
            ClassPairUnordered pair = new ClassPairUnordered(a, b);

            long countA = singleCounts.get(a);
            long countB = singleCounts.get(b);
            double pa  = 1.0*countA/nImages; // P(a) = #(a)/#(nImages)
            double pb  = 1.0*countB/nImages; // P(b) = #(b)/#(nImages)
            double pab = 1.0*pairCounts.get(pair)/countB;  // P(a|b) = #(a,b)/#(b)
            double pba = 1.0*pairCounts.get(pair)/countA;  // P(b|a) = #(a,b)/#(a)

            //Certainty factors:
            double CFab = (pab>pa)?(pab-pa)/(1-pa):(pab-pa)/pa;
            double CFba = (pba>pb)?(pba-pb)/(1-pb):(pba-pb)/pb;

            cf.put(new ClassPair(a,b),CFab);
            cf.put(new ClassPair(b,a),CFba);
        }

        ContextModel model = new ContextModel(statistics.histograms, singleCounts, pairCounts, nImages, statistics.labels, cf);
        return model;
    }


    /* Auxiliary functions */

    /**
     * Get a histogram, create it if it doesn't exist
     * @param triplet the triplet to select the histogram
     * @return the selected histogram
     */
    private HistogramBuilder getHistogram(SmartTriplet triplet) {
        HistogramBuilder res = hist.get(triplet);
        if (res == null) {
            res = RelativeFeaturesExtractor.createHistogram(triplet.relType);
            hist.put(triplet, res);
        }
        return res;
    }

    /**
     * Get the count of pairs containing A-B
     * Create if it doesn't exist.
     */
    private Counter getPairCount(ClassPairUnordered pair) {
        Counter res = pairCount.get(pair);
        if (res == null) {
            res = new Counter();
            pairCount.put(pair, res);
        }
        return res;
    }

    /**
     * Get the count of pairs containing A
     * Create if it doesn't exist.
     */
    private Counter getSingleCount(String label) {
        Counter p = singleCount.get(label);
        if (p==null) {
            p = new Counter();
            singleCount.put(label,p);
        }
        return p;
    }
}
