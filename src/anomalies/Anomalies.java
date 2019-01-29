/**
 * Author: XauthorX
 * 15/12/17
 */
package anomalies;

import matrix.Matrix2D;
import processing.Relationships;
import scenery.AScenery;
import anomalies.entities.*;

import java.util.List;
import java.util.Map;

public class Anomalies {
    //Usage of different property categories
    static boolean useWidth = true;
    static boolean useHeight = true;
    static boolean useArea = true;
    static boolean usePosition = true;
    static boolean useCF = true;
    //Parameters (fixed)
    static int minsup=10;
    //Experiments
    static class experimentA {
        public static float thr_h=0.99f;
        public static float cfLow = 0.2f;//-0.99f*2-1;//-0.4f;
        public static float cfHigh = 0.8f;//0.99f*2-1;//0.4f;
    }
    static class experimentB {
        public static float thr_h=0.97f;
        public static float cfLow = 0.2f;//-0.97f*2-1;//-0.4f;
        public static float cfHigh = 0.8f;//0.97f*2-1;//0.4f;
    }

    /**
     * Research anomalies and supporters for the specified image. Experiment specifies different parameter values.
     * @param scenery
     * @param relationships
     * @param contextModel
     * @param labels
     * @param mat
     * @param experiment
     * @return
     */
    public static ContextCoherence findAnomalies(AScenery scenery, Relationships relationships, ContextModel contextModel, String[] labels, Matrix2D mat,
                                                 String experiment, float optionalThr) {
        ContextCoherence res = new ContextCoherence();
        List<ContextComparison> anomalies = res.getAnomalies();     //list of anomalies
        List<ContextComparison> supporters = res.getSupporters();   //list of supporters

        int[] objLabels = scenery.getConnectedComponents().getLabels();
        int[] objectsSize = relationships.getObjectsSize();
        Relationships.PosType[][] positions = relationships.getPositions();
        Relationships.Size[][][] relSize = relationships.getRelativeSize();
        Map<SmartTriplet, Histogram> hist = contextModel.histograms;

        int numObjects = scenery.getConnectedComponents().getNumCComponents();
        //Iterate over all object pairs
        for (int i=0; i<numObjects; i++) {
            for (int j=i+1; j<numObjects; j++) {
                //Prepare the triplet:
                IndexStringPair isp = new IndexStringPair(i, labels[objLabels[i]-1], j, labels[objLabels[j]-1]);
                int s = isp.s;
                int r = isp.r;
                SmartTriplet triplet = isp.triplet;



                float posScoreFuzzy;
                float negScoreFuzzy;
                float cfLow;
                float cfHigh;
                float thr_h;

                boolean hard=true;

                //Setting up parameters (experiments)
                useWidth = true;
                useHeight = true;
                useArea = true;
                usePosition = true;
                useCF = true;
                if (experiment.equals("inspection")){
                    thr_h=optionalThr;
                    cfLow=1-optionalThr;
                    cfHigh=optionalThr;
                }
                else if (experiment.equals("b1")||experiment.equals("b2")||experiment.equals("b3")) {
                    //Lower threshold (more anomalies)
                    thr_h=experimentB.thr_h;
                    cfLow=experimentB.cfLow;
                    cfHigh=experimentB.cfHigh;
                }
                else {
                    //Higher threshold (less anomalies)
                    thr_h=experimentA.thr_h;
                    cfLow=experimentA.cfLow;
                    cfHigh=experimentA.cfHigh;
                }


                negScoreFuzzy = 1-thr_h;
                posScoreFuzzy = thr_h;




                double cfSR = contextModel.cf.getOrDefault(new ClassPair(triplet.subject, triplet.reference),-1.0);
                double cfRS = contextModel.cf.getOrDefault(new ClassPair(triplet.reference, triplet.subject),-1.0);


                //CF
                if (useCF) {
                    float normalizedCF;
                    if (Math.abs(cfSR)>Math.abs(cfRS)) {
                        normalizedCF=(float)cfSR;
                    }
                    else {
                        normalizedCF=(float)cfRS;
                    }
                    normalizedCF = (normalizedCF+1)/2;



                    /*if (cfSR < cfLow || cfRS < cfLow) {
                        anomalies.add(new ContextComparison(triplet.subject, "CF", triplet.reference, "occur", (float) -Math.min(cfSR, cfRS), i, j)); //neverOccurred
                    } else if (cfSR > cfHigh || cfRS > cfHigh) {
                        supporters.add(new ContextComparison(triplet.subject, "CF", triplet.reference, "occur", (float)(0.5f*Math.max(cfSR, cfRS)), i, j)); //oftenOccurred
                    }*/
                    if (normalizedCF < cfLow) {
                        anomalies.add(new ContextComparison(triplet.subject, "CF", triplet.reference, "occur", 1.0f-normalizedCF, i, j)); //neverOccurred
                    } else if (normalizedCF > cfHigh) {
                        supporters.add(new ContextComparison(triplet.subject, "CF", triplet.reference, "occur", normalizedCF, i, j)); //oftenOccurred
                    }
                }



                Relationships.PosType index = positions[s][r];
                if (index==null) {
                    //res.add(new Anomaly(triplet, Relationships.Size.BIGGER, 0, i, j)); //neverOccurred
                    continue;
                }

                //Find the histogram
                triplet.relType = Relationships.RelType.POSITION;
                Histogram h = hist.get(triplet);

                if (h==null) {
                    //res.add(new Anomaly(triplet, Relationships.Size.BIGGER, 0, i, j)); //neverOccurred
                    //res.add(new Anomaly(triplet, index, 0, i, j)); //neverOccurred
                    continue;
                }

                float[] vals = h.histValues;


                //Generate anomalies
                float score = vals[index.ordinal()];
                if (usePosition) {
                    if (score <= negScoreFuzzy && h.support > minsup)
                        anomalies.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index.toString(), 1-score, i, j)); //neverOccurred
                    else if (score > posScoreFuzzy && h.support > minsup)
                        supporters.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index.toString(), score, i, j)); //neverOccurred
                }
                Relationships.Size index2;
                //Width
                if (useWidth) {
                    triplet.relType = Relationships.RelType.WIDTH;
                    h = hist.get(triplet);
                    vals = h.histValues;
                    index2 = relSize[s][r][Relationships.RelType.WIDTH.ordinal()];
                    score = vals[index2.ordinal()];

                    if (score <= negScoreFuzzy && h.support > minsup)
                        anomalies.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index2.toString(), 1 - score, i, j)); //neverOccurred
                    else if (score > posScoreFuzzy && h.support > minsup)
                        supporters.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index2.toString(), score, i, j)); //neverOccurred
                }

                //Height
                if (useHeight) {
                    triplet.relType = Relationships.RelType.HEIGHT;
                    h = hist.get(triplet);
                    vals = h.histValues;
                    index2 = relSize[s][r][Relationships.RelType.HEIGHT.ordinal()];
                    score = vals[index2.ordinal()];

                    if (score <= negScoreFuzzy && h.support > minsup)
                        anomalies.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index2.toString(), 1 - score, i, j)); //neverOccurred
                    else if (score > posScoreFuzzy && h.support > minsup)
                        supporters.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index2.toString(), score, i, j)); //neverOccurred
                }

                //Area
                if (useArea) {
                    triplet.relType = Relationships.RelType.AREA;
                    h = hist.get(triplet);
                    vals = h.histValues;
                    index2 = relSize[s][r][Relationships.RelType.AREA.ordinal()];
                    score = vals[index2.ordinal()];

                    if (score <= negScoreFuzzy && h.support > minsup)
                        anomalies.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index2.toString(), 1 - score, i, j)); //neverOccurred
                    else if (score > posScoreFuzzy && h.support > minsup)
                        supporters.add(new ContextComparison(triplet.subject, triplet.relType.toString(), triplet.reference, index2.toString(), score, i, j)); //neverOccurred
                }
            }
        }

        return res;



    }
}
