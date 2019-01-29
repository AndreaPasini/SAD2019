import datasets.ADE20Kresults;
import drawing.Drawing;
import matrix.Matrix2D;
import matrix.MatrixReader;
import matrix.MatrixWriter;
import processing.ConnectedComponents;
import processing.Relationships;
import scenery.AScenery;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class RelationshipTesting {

    public static void debugRelationships(String rootPath, ADE20Kresults dataset, List<String> testSetFiles) throws IOException {
        Random random = new Random();
        int countImg=0;
        for (String img : testSetFiles) {
            countImg++;
            System.out.println("Image: " + countImg);
            //Read classified scenery
            Matrix2D mat = MatrixReader.readMatrix2D(dataset.getTestAnnotationFile(img));   //Ground truth predictions
            AScenery sample = new AScenery(mat);
            Relationships rel = Relationships.compute(sample);                              //Relationships on ground truth


            //Print images, for debugging
            Paths.get(rootPath,"relationships").toFile().mkdir();
            Paths.get(rootPath,"relationships","positions").toFile().mkdir();
            Paths.get(rootPath,"relationships","widths").toFile().mkdir();
            Paths.get(rootPath,"relationships","heights").toFile().mkdir();
            Paths.get(rootPath,"relationships","area").toFile().mkdir();

            //Save Position examples
            savePositionSamples(rootPath, random.nextInt(), img, sample, rel);
            //Save height examples
            saveHeightSamples(rootPath, random.nextInt(), img, sample, rel);
            //Save width examples
            saveWidthSamples(rootPath, random.nextInt(), img, sample, rel);
            //Save area examples
            saveAreaSamples(rootPath, random.nextInt(), img, sample, rel);
        }
    }

    public static void savePositionSamples(String rootPath, int rand, String img, AScenery sample, Relationships rel) throws IOException {
        Relationships.PosType[][] pos = rel.getPositions();
        int p = rand%5;
        String pStr;
        if (p==0)
            pStr= Relationships.PosType.INSIDE.toString();
        else if (p==1)
            pStr= Relationships.PosType.ABOVE.toString();
        else if (p==2)
            pStr= Relationships.PosType.ON.toString();
        else if (p==3)
            pStr= Relationships.PosType.SIDE.toString();
        else
            pStr= Relationships.PosType.SIDE_UP.toString();



        Paths.get(rootPath,"relationships","positions", pStr).toFile().mkdir();

        ConnectedComponents cc = sample.getConnectedComponents();
        Matrix2D cCompMat = cc.getMatrix().clone();
        int area=cCompMat.getHeight()*cCompMat.getWidth();
        int[]posColors = getColorForPositions(cc.getNumCComponents());
        int[] objSize = rel.getObjectsSize();



        for (int i=pos.length-1; i>=0; i--) {
            boolean done=false;
            for (int j=0; j<pos.length; j++) {
                //print images for objects with relative size>10% of the image
                if (pos[i][j]!=null && pos[i][j].toString().equals(pStr) && 1.0*objSize[i]/area>0.1 && 1.0*objSize[j]/area>0.1) {
                    Drawing.replace(cCompMat, i, cc.getNumCComponents());
                    Drawing.replace(cCompMat, j, cc.getNumCComponents()+1);
                    MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"relationships","positions", pStr,pStr+img+".png").toFile());
                    done=true;
                    break;
                }
            }
            if (done)break;
        }

    }

    public static void saveWidthSamples(String rootPath, int rand, String img, AScenery sample, Relationships rel) throws IOException {
        Relationships.Size[][][] sizes = rel.getRelativeSize();
        int p = rand%2;
        String pStr;
        if (p==0)
            pStr= Relationships.Size.BIGGER.toString();
        else
            pStr= Relationships.Size.SAME.toString();



        Paths.get(rootPath,"relationships","widths", pStr).toFile().mkdir();

        ConnectedComponents cc = sample.getConnectedComponents();
        Matrix2D cCompMat = cc.getMatrix().clone();
        int area=cCompMat.getHeight()*cCompMat.getWidth();
        int[]posColors = getColorForPositions(cc.getNumCComponents());
        int[] objSize = rel.getObjectsSize();



        for (int i=sizes.length-1; i>=0; i--) {
            boolean done=false;
            for (int j=0; j<sizes.length; j++) {
                if (sizes[i][j][Relationships.RelType.WIDTH.ordinal()]!=null && sizes[i][j][Relationships.RelType.WIDTH.ordinal()].toString().equals(pStr)
                        && 1.0*objSize[i]/area>0.1 && 1.0*objSize[j]/area>0.1) {
                    Drawing.replace(cCompMat, i, cc.getNumCComponents());
                    Drawing.replace(cCompMat, j, cc.getNumCComponents()+1);
                    MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"relationships","widths", pStr,pStr+img+".png").toFile());
                    done=true;
                    break;
                }
            }
            if (done)break;
        }

    }

    public static void saveHeightSamples(String rootPath, int rand, String img, AScenery sample, Relationships rel) throws IOException {
        Relationships.Size[][][] sizes = rel.getRelativeSize();
        int p = rand%2;
        String pStr;
        if (p==0)
            pStr= Relationships.Size.BIGGER.toString();
        else
            pStr= Relationships.Size.SAME.toString();


        Paths.get(rootPath,"relationships","heights", pStr).toFile().mkdir();

        ConnectedComponents cc = sample.getConnectedComponents();
        Matrix2D cCompMat = cc.getMatrix().clone();
        int area=cCompMat.getHeight()*cCompMat.getWidth();
        int[]posColors = getColorForPositions(cc.getNumCComponents());
        int[] objSize = rel.getObjectsSize();



        for (int i=sizes.length-1; i>=0; i--) {
            boolean done=false;
            for (int j=0; j<sizes.length; j++) {
                if (sizes[i][j][Relationships.RelType.HEIGHT.ordinal()]!=null && sizes[i][j][Relationships.RelType.HEIGHT.ordinal()].toString().equals(pStr)
                        && 1.0*objSize[i]/area>0.1 && 1.0*objSize[j]/area>0.1) {
                    Drawing.replace(cCompMat, i, cc.getNumCComponents());
                    Drawing.replace(cCompMat, j, cc.getNumCComponents()+1);
                    MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"relationships","heights", pStr,pStr+img+".png").toFile());
                    done=true;
                    break;
                }
            }
            if (done)break;
        }

    }

    public static void saveAreaSamples(String rootPath, int rand, String img, AScenery sample, Relationships rel) throws IOException {
        Relationships.Size[][][] sizes = rel.getRelativeSize();
        int p = rand%2;
        String pStr;
        if (p==0)
            pStr= Relationships.Size.BIGGER.toString();
        else
            pStr= Relationships.Size.SAME.toString();

        Paths.get(rootPath,"relationships","area", pStr).toFile().mkdir();

        ConnectedComponents cc = sample.getConnectedComponents();
        Matrix2D cCompMat = cc.getMatrix().clone();
        int area=cCompMat.getHeight()*cCompMat.getWidth();
        int[]posColors = getColorForPositions(cc.getNumCComponents());
        int[] objSize = rel.getObjectsSize();



        for (int i=sizes.length-1; i>=0; i--) {
            boolean done=false;
            for (int j=0; j<sizes.length; j++) {
                if (sizes[i][j][Relationships.RelType.AREA.ordinal()]!=null && sizes[i][j][Relationships.RelType.AREA.ordinal()].toString().equals(pStr)
                        && 1.0*objSize[i]/area>0.1 && 1.0*objSize[j]/area>0.1) {
                    Drawing.replace(cCompMat, i, cc.getNumCComponents());
                    Drawing.replace(cCompMat, j, cc.getNumCComponents()+1);
                    MatrixWriter.writeMatrix2D(cCompMat, posColors, Paths.get(rootPath,"relationships","area", pStr,pStr+img+".png").toFile());
                    done=true;
                    break;
                }
            }
            if (done)break;
        }

    }

    static int[] getColorForPositions(int n) {
        Random random = new Random();
        int[] colors=new int[n+3];//2 for selecting objects, 1 for unlabeled

        for (int i=1; i<=n; i++) {//for the objects
            float hue = 0.2f;
            // Saturation between 0.1 and 0.3
            float saturation = 0.0f;
            float luminance = random.nextFloat();
            Color color = Color.getHSBColor(hue, saturation, luminance);

            colors[i] = color.getRGB();
        }


        colors[0]=0;
        //Color blindness-safe palette
        colors[n+1]=new Color(255,182,109).getRGB();
        colors[n+2]=new Color(109,255,255).getRGB();

        return colors;
    }


}
