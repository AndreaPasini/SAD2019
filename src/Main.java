import datasets.ADE20K;
import datasets.ADE20Kresults;
import drawing.Drawing;
import matrix.Matrix2D;
import matrix.MatrixReader;
import matrix.MatrixWriter;

import processing.ConnectedComponents;
import processing.Relationships;
import scenery.AScenery;
import scenery.ASceneryEval;
import scenery.RectangularROI;
import anomalies.*;
import anomalies.entities.MutableFloat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: XauthorX
 * 08/12/17
 */

public class Main {
    public static String rootPath;
    public static enum actions {BUILD_KB}
    public static void main(String[] args) {

        try {
            //Preparing training set and test set:
            rootPath = Paths.get("../Data/").toString();
            ADE20Kresults dataset = new ADE20Kresults(rootPath);
            List<String> trainingSet = dataset.getTrainingSet(-1);//-1);
            List<String> testSet = dataset.getTestSet(true, -1);

            //Actions
            boolean build_kb = false;           //1. Build the knowledge base
            boolean print_numHist = true;       //2. Print the number of histograms (variable thresholds)
            boolean print_kbSummary = false;    //3. Print a summary of the histograms in the knowledge base
            boolean print_relImages = false;    //4. Save images with relationships examples (useful to provide examples for the papers)
            boolean print_accuracies = false;   //5. Save image/object accuracies (only neural network, no context)
            boolean anom_inspection = false;    //6. Draw precision recall graphs to inspect CF and Position anomalies with different thr
            boolean anom_experiments = false;   //7. The six experiments on anomaly detection

            //Experiments:

            //1. Build the knowledge base
            if (build_kb) {
                System.out.println("Building knowledge base.");
                build(dataset, trainingSet);
                System.out.println("Done.");
            }
            //Load the knowledge base
            System.out.println("Loading knowledge base.");
            ContextStatistics statistics = ContextStatistics.load(Paths.get(rootPath, "contextStatistics").toFile());
            ContextModel contextModel = ContextModelBuilder.buildModel(statistics);
            System.out.println("Done.");

            //2. Print the number of histograms (variable thresholds)
            if (print_numHist) {
                System.out.println("thr_h\t#hist");
                System.out.println("sup>"+5);
                contextModel.printNhistograms(5, 0.6f);
                contextModel.printNhistograms(5, 0.7f);
                contextModel.printNhistograms(5, 0.8f);
                contextModel.printNhistograms(5, 0.85f);
                contextModel.printNhistograms(5, 0.95f);
                contextModel.printNhistograms(5, 0.96f);
                contextModel.printNhistograms(5, 0.98f);
                contextModel.printNhistograms(5, 0.99f);

                System.out.println();

                System.out.println("sup>10");
                contextModel.printNhistograms(10, 0.6f);
                contextModel.printNhistograms(10, 0.7f);
                contextModel.printNhistograms(10, 0.8f);
                contextModel.printNhistograms(10, 0.85f);
                contextModel.printNhistograms(10, 0.95f);
                contextModel.printNhistograms(10, 0.96f);
                contextModel.printNhistograms(10, 0.98f);
                contextModel.printNhistograms(10, 0.99f);

                System.out.println();

                System.out.println("sup>"+50);
                contextModel.printNhistograms(50, 0.6f);
                contextModel.printNhistograms(50, 0.7f);
                contextModel.printNhistograms(50, 0.8f);
                contextModel.printNhistograms(50, 0.85f);
                contextModel.printNhistograms(50, 0.95f);
                contextModel.printNhistograms(50, 0.96f);
                contextModel.printNhistograms(50, 0.98f);
                contextModel.printNhistograms(50, 0.99f);

                System.out.println();

                System.out.println("sup>"+100);
                contextModel.printNhistograms(500, 0.6f);
                contextModel.printNhistograms(500, 0.7f);
                contextModel.printNhistograms(500, 0.8f);
                contextModel.printNhistograms(500, 0.85f);
                contextModel.printNhistograms(500, 0.95f);
                contextModel.printNhistograms(500, 0.96f);
                contextModel.printNhistograms(500, 0.98f);
                contextModel.printNhistograms(500, 0.99f);

                System.out.println();

                contextModel.printNhistogramsForType(10, 0.7f);
                contextModel.printNhistogramsForType(10, 0.99f);
            }

            //3. Print a summary of the histograms in the knowledge base
            if (print_kbSummary) {
                contextModel.printSummary(Paths.get(rootPath, "summaryPosition").toFile(),Relationships.RelType.POSITION);
                contextModel.printSummary(Paths.get(rootPath, "summaryArea").toFile(),Relationships.RelType.AREA);
                contextModel.printCF(Paths.get(rootPath, "cf").toFile());
            }

            //4. Save images with relationships examples (useful to provide examples for the papers)
            if (print_relImages) {
                RelationshipTesting.debugRelationships(rootPath, dataset, testSet);
            }


            //5. Save image/object accuracies (only neural network, no context)
            if (print_accuracies) {
                debugAccuracies(dataset, testSet);
            }

            //6. Draw precision recall graphs to inspect CF and Position anomalies with different thr
            if (anom_inspection) {
                AnomalyInspection.anomInspectionLoop(dataset, testSet, contextModel);
            }

            //7. The six experiments
            if (anom_experiments) {


                //only anomaly, high threshold
                findAnomalies(dataset, testSet, contextModel, 0, "a1");
                //Delta method, high threshold
                findAnomalies(dataset, testSet, contextModel, 0, "a2");
                //WTA method, high threshold
                //findAnomalies(dataset, testSet, contextModel, 0, "a3");
                //only anomaly, low threshold
                findAnomalies(dataset, testSet, contextModel, 0, "b1");
                //Delta method, low threshold
                findAnomalies(dataset, testSet, contextModel, 0, "b2");
                //WTA method, low threshold
                //findAnomalies(dataset, testSet, contextModel, 0, "b3");
            }




        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Read training images and build the knowledge base
     * @param dataset ade20k dataset reader
     * @param trainingSet  training set images
     */
    public static void build(ADE20K dataset, List<String> trainingSet) throws IOException {
        Matrix2D mat;
        ContextModelBuilder contextModelBuilder = new ContextModelBuilder();
        int i = 0;
        for (String img : trainingSet) {
            mat = MatrixReader.readMatrix2D(dataset.getTrainAnnotationFile(img)); //Ground truth predictions
            //Create the scenery
            AScenery scenery = new AScenery(mat);
            //Compute object relationships
            Relationships relationships = Relationships.compute(scenery);
            //Add to statistics
            contextModelBuilder.addImage(scenery, relationships, dataset.getClassLabels());

            if ((i % 100)==0)
                System.out.println("Processed until image: " + i);
            i++;
        }

        ContextStatistics statistics = contextModelBuilder.createStatistics();
        statistics.save(Paths.get(rootPath, "contextStatistics").toFile());
    }


    public static void debugAccuracies(ADE20Kresults dataset, List<String> files) throws IOException {
        int countImg = 0;
        PrintWriter outObj = new PrintWriter(Paths.get(rootPath, "objAccuracies").toFile()); //Format: "objectAccuracy,objectSizeInpixels" the object size in percent with respect to the image
        PrintWriter outImg = new PrintWriter(Paths.get(rootPath, "imgAccuracies").toFile()); //Format: "imageAccuracy"
        //Print images, for debugging
        Paths.get(rootPath,"accuracies").toFile().mkdir();

        for (String img : files) {
            countImg++;
            if (countImg%100==0)
                System.out.println("Processed until Image: " + countImg);
            //Read classified scenery
            ASceneryEval sample = dataset.readASceneryEval(img);

            long[] tot = new long[2];
            long imgSize = sample.getGroundTruth().getHeight()*sample.getGroundTruth().getWidth();
            ConnectedComponents cc = sample.getConnectedComponents();
            Matrix2D cCompMat = cc.getMatrix().clone();
            int[]posColors = RelationshipTesting.getColorForPositions(cc.getNumCComponents());

            boolean done=false;
            for (int i=0; i<sample.getNumObjects(); i++) {
                long[] accVect = sample.computePixelAccuracy(i);
                float acc = 1.0f*accVect[0]/accVect[1];
                tot[0]+=accVect[0];
                tot[1]+=accVect[1];
                outObj.println(acc+","+1.0f*accVect[1]/imgSize);

                if (1.0*accVect[1]/imgSize>0.05 && acc<0.2) {
                    Drawing.replace(cCompMat, i, cc.getNumCComponents());
                    done=true;
                    break;
                }
            }

            float accTot = 1.0f*tot[0]/tot[1];

            if (done) {
                MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"accuracies", img+".png").toFile());
            }

            outImg.println(accTot);
        }

        outObj.close();
        outImg.close();
    }



    public static void printAnomalies(ADE20Kresults dataset, List<String> files, ContextModel contextModel, int mode, String experiment) throws IOException {



        //Paths.get(rootPath,"anomaliesPrint").toFile().mkdir();
        Paths.get(rootPath,"anomaliesPrintHigh").toFile().mkdir();
        Paths.get(rootPath,"anomaliesPrintLow").toFile().mkdir();


        int countImg=0;

        for (String img : files) {
            countImg++;
            if (countImg%100==0)
                System.out.println("Processed until Image: " + countImg);

            //Read classified scenery
            ASceneryEval sample = dataset.readASceneryEval(img);

            Relationships rel = Relationships.compute(sample);


            //Connected components of the predictions and bounding boxes
            Matrix2D pred = sample.getPredictions();

            //
            ContextCoherence coherence = Anomalies.findAnomalies(sample, rel, contextModel, dataset.getClassLabels(), pred.clone(),experiment,0.0f);
            List<ContextComparison> anomalies = coherence.getAnomalies();
            List<ContextComparison> supporters = coherence.getSupporters();

            Set<Integer> anomalyObjects = new HashSet<>();

            ConnectedComponents cc = sample.getConnectedComponents();
            int[]posColors = RelationshipTesting.getColorForPositions(cc.getNumCComponents());
            long imgSize = sample.getGroundTruth().getHeight()*sample.getGroundTruth().getWidth();

            //Compute coverages:
            float[] anCoverage = new float[rel.getNumObjects()];
            float[] supCoverage = new float[rel.getNumObjects()];

            for (ContextComparison an : anomalies) {
                anCoverage[an.getSubjectId()]++;
                anCoverage[an.getReferenceId()]++;
            }

            if (anomalies.size()>=0) {

                //get bufferedImage
                BufferedImage bImg = new BufferedImage(
                        cc.getMatrix().getWidth(), cc.getMatrix().getHeight(), BufferedImage.TYPE_INT_ARGB);





                Graphics2D g2d = bImg.createGraphics();
                g2d.drawImage(MatrixWriter.getBufferedImage(cc.getMatrix()), 0, 0, null);



                long[][] accuracies = new long[rel.getNumObjects()][2];
                for (int i=0; i<accuracies.length; i++) {
                    long[] acc = sample.computePixelAccuracy(i);
                    accuracies[i][0]=acc[0];
                    accuracies[i][1]=acc[1];
                }


                for (ContextComparison an : anomalies) {

                    int sub = an.getSubjectId();
                    int ref = an.getReferenceId();

                    if (an.getRelType().equals("POSITION")) {

                            RectangularROI subBox = rel.getbBoxes().getObjects().get(sub);
                            RectangularROI refBox = rel.getbBoxes().getObjects().get(ref);
                            float x1 = subBox.getX() + subBox.getWidth() / 2;
                            float x2 = refBox.getX() + refBox.getWidth() / 2;
                            float y1 = subBox.getY() + subBox.getHeight() / 2;
                            float y2 = refBox.getY() + refBox.getHeight() / 2;


                            long[] accS = sample.computePixelAccuracy(sub);

                            if (1.0f * accuracies[sub][0] / accuracies[sub][1] < 0.5f){
                                g2d.setColor(Color.RED);
                            }
                            else
                                g2d.setColor(Color.GREEN);
                            g2d.fillOval((int) (x1 * bImg.getWidth()), (int) (y1 * bImg.getHeight()), 5, 5);

                            if (1.0f * accuracies[ref][0] / accuracies[ref][1] < 0.5f) {
                                g2d.setColor(Color.RED);
                            }
                            else
                                g2d.setColor(Color.GREEN);
                            g2d.fillOval((int) (x2 * bImg.getWidth()), (int) (y2 * bImg.getHeight()), 5, 5);


                            g2d.setColor(Color.RED);
                            g2d.drawLine((int) (x1 * bImg.getWidth()), (int) (y1 * bImg.getHeight()), (int) (x2 * bImg.getWidth()), (int) (y2 * bImg.getHeight()));

                    }
                }

                for (ContextComparison sup : supporters) {


                    int sub = sup.getSubjectId();
                    int ref = sup.getReferenceId();

                    if (1.0f*rel.getObjectsSize()[sub]/imgSize>0.01 && 1.0f*rel.getObjectsSize()[ref]/imgSize>0.01) {

                        RectangularROI subBox = rel.getbBoxes().getObjects().get(sub);
                        RectangularROI refBox = rel.getbBoxes().getObjects().get(ref);
                        float x1 = subBox.getX() + subBox.getWidth() / 2;
                        float x2 = refBox.getX() + refBox.getWidth() / 2;
                        float y1 = subBox.getY() + subBox.getHeight() / 2;
                        float y2 = refBox.getY() + refBox.getHeight() / 2;


                        long[] accS = sample.computePixelAccuracy(sub);

                        if (1.0f*accuracies[sub][0]/accuracies[sub][1]<0.5f)
                            g2d.setColor(Color.RED);
                        else
                            g2d.setColor(Color.GREEN);
                        g2d.fillOval((int) (x1 * bImg.getWidth()), (int) (y1 * bImg.getHeight()), 5, 5);

                        if (1.0f*accuracies[ref][0]/accuracies[ref][1]<0.5f)
                            g2d.setColor(Color.RED);
                        else
                            g2d.setColor(Color.GREEN);
                        g2d.fillOval((int) (x2 * bImg.getWidth()), (int) (y2 * bImg.getHeight()), 5, 5);


                        g2d.setColor(Color.GREEN);
                        g2d.drawLine((int) (x1 * bImg.getWidth()), (int) (y1 * bImg.getHeight()), (int) (x2 * bImg.getWidth()), (int) (y2 * bImg.getHeight()));

                    }
                }


                long[] totAcc=sample.computePixelAccuracy();
                float accuracy=1.0f*totAcc[0]/totAcc[1];
                if (accuracy>0.5) {
                    ImageIO.write(bImg, "png", Paths.get(rootPath, "anomaliesPrintHigh", img +"_" + (int)(accuracy*100) + ".png").toFile());
                }
                else {
                    ImageIO.write(bImg, "png", Paths.get(rootPath, "anomaliesPrintLow", img+ "_" + (int)(accuracy*100)  + ".png").toFile());
                }
            }

        }

    }


    /**
     * Anomaly detection, core algorithms
     * @param dataset
     * @param files
     * @param contextModel
     * @param mode
     * @param experiment
     * @throws IOException
     */

    public static void findAnomalies(ADE20Kresults dataset, List<String> files, ContextModel contextModel, int mode, String experiment) throws IOException {
        System.out.println("Running experiment: " + experiment);

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

        //For each line the accuracy of objects with at least 1 anomaly
        PrintWriter outAnomalyObj = new PrintWriter(Paths.get(rootPath,"experiments",experiment, "objAccuraciesAnomaly").toFile()); //Format: "objectAccuracy,objectSizeInpixels" the object size in percent with respect to the image
        //For each line the accuracy of objects without anomalies
        PrintWriter outNoAnomalyObj = new PrintWriter(Paths.get(rootPath, "experiments",experiment, "objAccuraciesNoAnomaly").toFile()); //Format: "objectAccuracy,objectSizeInpixels" the object size in percent with respect to the image
        //For each line the accuracy of all the objects
        PrintWriter outTotObj = new PrintWriter(Paths.get(rootPath, "experiments",experiment, "objAccuraciesAll").toFile()); //Format: "objectAccuracy,objectSizeInpixels" the object size in percent with respect to the image
        //For each line the accuracy of images with anomalies
        PrintWriter outAnomalyImg = new PrintWriter(Paths.get(rootPath,"experiments",experiment, "imgAccuraciesAnomaly").toFile()); //Format: "objectAccuracy,objectSizeInpixels" the object size in percent with respect to the image
        //For each line the accuracy of images without anomalies
        PrintWriter outNoAnomalyImg = new PrintWriter(Paths.get(rootPath, "experiments",experiment, "imgAccuraciesNoAnomaly").toFile()); //Format: "objectAccuracy,objectSizeInpixels" the object size in percent with respect to the image

        outAnomalyObj.println("objAccuracy,objArea,anomalyCoverage,supporterCoverage");
        outNoAnomalyObj.println("objAccuracy,objArea,anomalyCoverage,supporterCoverage");
        outTotObj.println("objAccuracy,objArea,anomalyCoverage,supporterCoverage");
        outAnomalyImg.println("imgAccuracy");
        outNoAnomalyImg.println("imgAccuracy");

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
            ContextCoherence coherence = Anomalies.findAnomalies(sample, rel, contextModel, dataset.getClassLabels(), pred.clone(), experiment, 0.0f);
            List<ContextComparison> anomalies = coherence.getAnomalies();
            List<ContextComparison> supporters = coherence.getSupporters();


            //Keep anomalies only between bigger objects
            long imgSize = sample.getGroundTruth().getHeight()*sample.getGroundTruth().getWidth();
            anomalies = anomalies.stream().filter(an -> {
                float sz1 = 1.0f*rel.getObjectsSize()[an.getSubjectId()]/imgSize;
                float sz2 = 1.0f*rel.getObjectsSize()[an.getReferenceId()]/imgSize;
                if (sz1>minSz && sz2>minSz)
                    return true;
                else return false;
            }).collect(Collectors.toList());
            totCountAnomalies+=anomalies.size();//count found anomalies
            //Anomalies for each type:
            for (ContextComparison a : anomalies) {
                if (a.getRelType().equals("POSITION"))
                    totCountPositionAnomalies++;
                else if (a.getRelType().equals("WIDTH"))
                    totCountWidthAnomalies++;
                else if (a.getRelType().equals("HEIGHT"))
                    totCountHeightAnomalies++;
                else if (a.getRelType().equals("AREA"))
                    totCountAreaAnomalies++;
                else if (a.getRelType().equals("CF"))
                    totCountcfAnomalies++;
            }


            //Keep supporters only between bigger objects
            supporters = supporters.stream().filter(sup -> {
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
            if (!experiment.equals("a1") && !experiment.equals("b1")) {
                //Covering objects with supporters:
                for (ContextComparison sup : supporters) {
                    supCoverage[sup.getSubjectId()] += sup.getConfidence();
                    supCoverage[sup.getReferenceId()] += sup.getConfidence();
                }
            }

            //Computing difference: supporters-anomalies
            float[] coverageScores = new float[rel.getNumObjects()];
            for (int i=0; i<coverageScores.length; i++)
                coverageScores[i]=supCoverage[i]-anCoverage[i];





            //Elimination algorithm: with delta scores (a)

            //Labels for each object: true=anomaly, false=no anomaly
            boolean[] anomalyLabels = new boolean[rel.getNumObjects()];



            //For each object:
            if (!experiment.equals("a3") && !experiment.equals("b3")) {
                for (int objId = 0; objId < sample.getConnectedComponents().getNumCComponents(); objId++) {
                    //Is anomaly?
                    if (coverageScores[objId] < scoreThr)
                        anomalyLabels[objId] = true;
                }

            }


            //Elimination algorithm: worst take all (WTA)
            if (experiment.equals("a3") || experiment.equals("b3")) {
                Map<Integer, MutableFloat> coveragesList = new HashMap<>();
                for (int i = 0; i < coverageScores.length; i++) {
                    if (coverageScores[i] != 0) {
                        coveragesList.put(i, new MutableFloat(coverageScores[i]));
                    }
                }

                //For each object:
                while (anomalies.size() > 0) {
                    //Pick the one with lowest coverage score (more anomalies)
                    Map.Entry<Integer, MutableFloat> worstObj = coveragesList.entrySet().stream()
                            .reduce((o1, o2) -> (o1.getValue().get() > o2.getValue().get()) ? o2 : o1).orElse(null);
                    if (worstObj==null)break;
                    int worstId = worstObj.getKey();
                    //Find the anomalies associated with the worst object and delete
                    Iterator<ContextComparison> it = anomalies.iterator();
                    boolean foundAnomaly=false;
                    while (it.hasNext()) {
                        ContextComparison an = it.next(); //get the anomaly
                        if (an.getSubjectId() == worstId) {
                            worstObj.getValue().add(an.getConfidence());                    //remove anomaly weight
                            coveragesList.getOrDefault(an.getReferenceId(), new MutableFloat()).add(1.0f * an.getConfidence()); //remove anomaly weight
                            it.remove();//Remove the anomaly
                            foundAnomaly=true;
                        } else if (an.getReferenceId() == worstId) {
                            worstObj.getValue().add(an.getConfidence());                    //remove anomaly weight
                            coveragesList.getOrDefault(an.getSubjectId(), new MutableFloat()).add(1.0f * an.getConfidence());   //remove anomaly weight
                            it.remove();//Remove the anomaly
                            foundAnomaly=true;
                        }
                    }

                    //if at least one anomaly was covering the object
                    //WARNING: if no anomalies were found, this object has a low score but all its anomalies were removed by others.
                    if (foundAnomaly)
                        //Label: worstObj = nogood
                        anomalyLabels[worstId] = true;

                    //Remove object from the list
                    coveragesList.remove(worstId);
                }
            }



            //SAVING results
            //For each object:
            float percentAnomalyArea=0;//Percentage of are of the image occupied by anomalous objects
            int numAnomalousObjectsCurrentImage=0;//Number of anomalous objects

            for (int objId = 0; objId < rel.getNumObjects(); objId++) {
                long[] acc = sample.computePixelAccuracy(objId);    //Accuracy of this object
                float percentArea = 1.0f*acc[1]/imgSize;            //Percent of area of this object with respect to image

                if (percentArea>minSz) {
                    totObj++;//total number of objects

                    //All objects
                    outTotObj.println(1.0*acc[0]/acc[1]+","+percentArea+","+anCoverage[objId]+","+supCoverage[objId]);
                    if (anomalyLabels[objId] == true) {
                        outAnomalyObj.println(1.0 * acc[0] / acc[1] + "," + percentArea + "," + anCoverage[objId] + "," + supCoverage[objId]);
                        percentAnomalyArea += percentArea;//add anomalous object area
                        numAnomalousObjectsCurrentImage++;
                    } else {
                        outNoAnomalyObj.println(1.0 * acc[0] / acc[1] + "," + percentArea + "," + anCoverage[objId] + "," + supCoverage[objId]);
                    }
                }
            }
            //Computing image-level anomaly
            long[]imgAcc=sample.computePixelAccuracy();
            float imgAccRatio=1.0f*imgAcc[0]/imgAcc[1];
            if (1.0f*numAnomalousObjectsCurrentImage/rel.getNumObjects()<0.05)//if (percentNoGoodArea<0.5)
                outNoAnomalyImg.println(imgAccRatio);
            else
                outAnomalyImg.println(imgAccRatio);


        }

        outAnomalyObj.close();
        outNoAnomalyObj.close();
        outTotObj.close();
        outAnomalyImg.close();
        outNoAnomalyImg.close();
        System.out.println("totobj="+totObj);
        System.out.println("anomalies="+totCountAnomalies+"("+(1.0f*totCountAnomalies/totObj)+") supporters="+totCountSupporters+" ("+(1.0f*totCountSupporters/totObj)+")");
        System.out.println("Position anomalies: " + totCountPositionAnomalies+String.format(" (%.1f%%)",1.0*totCountPositionAnomalies/totCountAnomalies));
        System.out.println("Area anomalies: " + totCountAreaAnomalies+String.format(" (%.1f%%)",1.0*totCountAreaAnomalies/totCountAnomalies));
        System.out.println("Width anomalies: " + totCountWidthAnomalies+String.format(" (%.1f%%)",1.0*totCountWidthAnomalies/totCountAnomalies));
        System.out.println("Height anomalies: " + totCountHeightAnomalies+String.format(" (%.1f%%)",1.0*totCountHeightAnomalies/totCountAnomalies));
        System.out.println("CF anomalies: " + totCountcfAnomalies+String.format(" (%.1f%%)",1.0*totCountcfAnomalies/totCountAnomalies));
    }






    public static void printImageAnomaliesCF(ADE20Kresults dataset, String img, ASceneryEval sample, Relationships rel, List<Anomaly> anomalies, long imgSize, int[]coverage) throws IOException {
        String subName="",refName="";String anomType="";
        boolean done=false;
        ConnectedComponents cc = sample.getConnectedComponents();
        Matrix2D cCompMat = cc.getMatrix().clone();
        int[]posColors = RelationshipTesting.getColorForPositions(cc.getNumCComponents());

        for (int i=0; i< rel.getNumObjects(); i++) {
            if (coverage[i]>1) {
                if (!done){

                    if (1.0f*rel.getObjectsSize()[i]/imgSize>0.05) {
                        for (Anomaly an : anomalies) {
                            if (an.getRelValue()!=Relationships.Size.BIGGER && an.getSubjectId() == i &&  1.0f*rel.getObjectsSize()[an.getReferenceId()]/imgSize>0.01) {
                                Drawing.replace(cCompMat, i, cc.getNumCComponents());
                                Drawing.replace(cCompMat, an.getReferenceId(), cc.getNumCComponents()+1);
                                refName = dataset.getClassLabel(cc.getLabels()[an.getReferenceId()]-1);
                                subName = dataset.getClassLabel(cc.getLabels()[i]-1);
                                anomType = an.getRelValue().toString();
                                done = true;
                                break;
                            }
                        }

                    }
                }

            }

        }

        if (done)
            MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"anomalies/cf", img+subName+"_"+anomType+"_"+refName+".png").toFile());

    }





    public static void printImageAnomaliesPos(ADE20Kresults dataset, String img, ASceneryEval sample, Relationships rel, List<ContextComparison> anomalies, long imgSize, int[]coverage) throws IOException {
        String subName="",refName="";String anomType="";
        boolean done=false;
        ConnectedComponents cc = sample.getConnectedComponents();
        Matrix2D cCompMat = cc.getMatrix().clone();
        int[]posColors = RelationshipTesting.getColorForPositions(cc.getNumCComponents());

        for (int i=0; i< rel.getNumObjects(); i++) {
            if (coverage[i]>1) {
                if (!done){

                        for (ContextComparison an : anomalies) {
                            if (an.getRelType().equals("POSITION") && an.getSubjectId() == i &&  1.0f*rel.getObjectsSize()[an.getReferenceId()]/imgSize>0.01) {
                                Drawing.replace(cCompMat, i, cc.getNumCComponents());
                                Drawing.replace(cCompMat, an.getReferenceId(), cc.getNumCComponents()+1);
                                refName = dataset.getClassLabel(cc.getLabels()[an.getReferenceId()]-1);
                                subName = dataset.getClassLabel(cc.getLabels()[i]-1);
                                anomType = an.getRelValue().toString();
                                done = true;
                                break;
                            }
                        }
                }

            }

        }

        if (done)
            MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"anomalies/pos", img+subName+"_"+anomType+"_"+refName+".png").toFile());

    }

}
