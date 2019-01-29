import anomalies.Anomalies;
import anomalies.ContextCoherence;
import anomalies.ContextComparison;
import anomalies.ContextModel;
import anomalies.entities.MutableFloat;
import datasets.ADE20Kresults;
import matrix.Matrix2D;
import processing.Relationships;
import scenery.ASceneryEval;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AnomalyInspection {
    public static class Result {
        int objAccGt75anomaly;
        int objAccGt75noanomaly;
        int objAccLt75anomaly;
        int objAccLt75noanomaly;
        float precision;
        float recall;
        float fscore;
    }

    public static Result inspectAnomalies(ADE20Kresults dataset, List<String> files, ContextModel contextModel, String anomalyType, float thr) throws IOException {
        Result res = new Result();
        System.out.println("Running thr: " + thr);

            /*
            Esperimento 1.
            No supporters, solo anomalies. Con delta score elimination (a)

            Esperimento 2.
            supporters e anomalies. Con delta score elimination (a)

            Esperimento 3.
            Supporters, anomalies. Con winner take all (b)
             */

        //***Parameters:

        //Denoising: consider only anomalies between big objects
        float minSz=0.01f;//0.005f;//Remove small objects from the analysis
        //Consider to change this value!

        //Detecting anomalous objects (coverageScore below threshold)
        float scoreThr = 0;

        //***


        int countImg=0;

        int totCountAnomalies=0;    //Total number of anomalies
        int totCountPositionAnomalies=0;
        int totCountAreaAnomalies=0;
        int totCountHeightAnomalies=0;
        int totCountWidthAnomalies=0;
        int totCountcfAnomalies=0;
        int totCountSupporters=0;   //Total number of supporters
        int totObj=0;               //Total number of objects

        for (String img : files) {
            countImg++;
            if (countImg%100==0)
                System.out.println("Processed until Image: " + countImg);

            //Read classified scenery
            ASceneryEval sample = dataset.readASceneryEval(img);
            Relationships rel = Relationships.compute(sample);

            //Connected components of the predictions and bounding boxes
            Matrix2D pred = sample.getPredictions();

            //Find anomalies and supporters
            ContextCoherence coherence = Anomalies.findAnomalies(sample, rel, contextModel, dataset.getClassLabels(), pred.clone(), "inspection", thr);
            List<ContextComparison> anomalies = coherence.getAnomalies();
            List<ContextComparison> supporters = coherence.getSupporters();


            //Keep anomalies only between bigger objects
            long imgSize = sample.getGroundTruth().getHeight()*sample.getGroundTruth().getWidth();
            anomalies = anomalies.stream().filter(an -> {
                if (!an.getRelType().toString().equals(anomalyType))
                    return false;

                float sz1 = 1.0f*rel.getObjectsSize()[an.getSubjectId()]/imgSize;
                float sz2 = 1.0f*rel.getObjectsSize()[an.getReferenceId()]/imgSize;
                if (sz1>minSz && sz2>minSz)
                    return true;
                else return false;
            }).collect(Collectors.toList());
            totCountAnomalies+=anomalies.size();//count found anomalies



            //Keep supporters only between bigger objects
            supporters = supporters.stream().filter(sup -> {
                if (!sup.getRelType().toString().equals(anomalyType))
                    return false;

                float sz1 = 1.0f*rel.getObjectsSize()[sup.getSubjectId()]/imgSize;
                float sz2 = 1.0f*rel.getObjectsSize()[sup.getReferenceId()]/imgSize;
                if (sz1>minSz && sz2>minSz)
                    return true;
                else return false;
            }).collect(Collectors.toList());
            totCountSupporters+=supporters.size();//count found supporters


            //Compute coverages:
            float[] anCoverage = new float[rel.getNumObjects()];
            float[] supCoverage = new float[rel.getNumObjects()];

            //Covering objects with anomalies:
            for (ContextComparison an : anomalies) {
                anCoverage[an.getSubjectId()] += (an.getConfidence());
                anCoverage[an.getReferenceId()] += (an.getConfidence());
            }
            /*for (ContextComparison sup : supporters) {
                supCoverage[sup.getSubjectId()] += sup.getConfidence();
                supCoverage[sup.getReferenceId()] += sup.getConfidence();
            }*/


            //Computing difference: supporters-anomalies
            float[] coverageScores = new float[rel.getNumObjects()];
            for (int i=0; i<coverageScores.length; i++)
                coverageScores[i]=supCoverage[i]-anCoverage[i];





            //Elimination algorithm: with delta scores (a)

            //Labels for each object: true=anomaly, false=no anomaly
            boolean[] anomalyLabels = new boolean[rel.getNumObjects()];

            for (int objId = 0; objId < sample.getConnectedComponents().getNumCComponents(); objId++) {
                //Is anomaly?
                if (coverageScores[objId] < scoreThr)
                    anomalyLabels[objId] = true;

            }




            //SAVING results
            //For each object:
            float percentAnomalyArea=0;//Percentage of are of the image occupied by anomalous objects
            int numAnomalousObjectsCurrentImage=0;//Number of anomalous objects

            for (int objId = 0; objId < rel.getNumObjects(); objId++) {
                long[] acc = sample.computePixelAccuracy(objId);    //Accuracy of this object
                float percentArea = 1.0f*acc[1]/imgSize;            //Percent of area of this object with respect to image
                float percentAcc = 1.0f*acc[0]/acc[1];
                if (percentArea>minSz) {
                    if (percentAcc>0.75f && anomalyLabels[objId] == true){
                        res.objAccGt75anomaly++;
                    }
                    else if (percentAcc>0.75f && anomalyLabels[objId] == false){
                        res.objAccGt75noanomaly++;
                    }
                    else if (percentAcc<=0.75f && anomalyLabels[objId] == true){
                        res.objAccLt75anomaly++;
                    }
                    else if (percentAcc<=0.75f && anomalyLabels[objId] == false){
                        res.objAccLt75noanomaly++;
                    }
                }
            }
        }


        res.precision = 1.0f*(res.objAccLt75anomaly)/(res.objAccGt75anomaly+res.objAccLt75anomaly);
        res.recall = 1.0f*(res.objAccLt75anomaly)/(res.objAccLt75anomaly+res.objAccLt75noanomaly);
        res.fscore = 2.0f*res.precision*res.recall/(res.precision+res.recall);
        return res;
    }

    public static void anomInspectionLoop(ADE20Kresults dataset, List<String> testSet, ContextModel contextModel) throws IOException {

        List<AnomalyInspection.Result> inspections = new LinkedList<>();
        int i;
        //POSITION

        System.out.println("Position:");
        i=0;
        inspections.clear();
        for (float thr=0.6f; thr<1.0f; thr+=0.1f) {
            inspections.add(AnomalyInspection.inspectAnomalies(dataset, testSet, contextModel, "POSITION", thr));
        }
        for (float thr=0.6f; thr<1.0f; thr+=0.1f) {
            System.out.println("thr: " + thr);
            System.out.println(String.format("p=%.3f",inspections.get(i).precision));
            System.out.println(String.format("r=%.3f",inspections.get(i).recall));
            System.out.println(String.format("f1=%.3f",inspections.get(i).fscore));
            System.out.println();
            i++;
        }

        i=0;
        inspections.clear();
        for (float thr=0.91f; thr<1.0f; thr+=0.01f) {
            inspections.add(AnomalyInspection.inspectAnomalies(dataset, testSet, contextModel, "POSITION", thr));
        }
        for (float thr=0.91f; thr<1.0f; thr+=0.01f) {
            System.out.println("thr: " + thr);
            System.out.println(String.format("p=%.3f",inspections.get(i).precision));
            System.out.println(String.format("r=%.3f",inspections.get(i).recall));
            System.out.println(String.format("f1=%.3f",inspections.get(i).fscore));
            System.out.println();
            i++;
        }

        //CF
        System.out.println("Cf:");
        i=0;
        inspections.clear();
        for (float thr=0.6f; thr<1.0f; thr+=0.1f) {
            inspections.add(AnomalyInspection.inspectAnomalies(dataset, testSet, contextModel, "CF", thr));
        }
        for (float thr=0.6f; thr<1.0f; thr+=0.1f) {
            System.out.println("thr: " + thr);
            System.out.println(String.format("p=%.3f",inspections.get(i).precision));
            System.out.println(String.format("r=%.3f",inspections.get(i).recall));
            System.out.println(String.format("f1=%.3f",inspections.get(i).fscore));
            System.out.println();
            i++;
        }

        i=0;
        inspections.clear();
        for (float thr=0.91f; thr<1.0f; thr+=0.01f) {
            inspections.add(AnomalyInspection.inspectAnomalies(dataset, testSet, contextModel, "CF", thr));
        }
        for (float thr=0.91f; thr<1.0f; thr+=0.01f) {
            System.out.println("thr: " + thr);
            System.out.println(String.format("p=%.3f",inspections.get(i).precision));
            System.out.println(String.format("r=%.3f",inspections.get(i).recall));
            System.out.println(String.format("f1=%.3f",inspections.get(i).fscore));
            System.out.println();
            i++;
        }





    }
}
