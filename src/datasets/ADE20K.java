/**
 * Author: XauthorX
 * 11/12/17
 */
package datasets;

import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ADE20K {
    private final String relImagePath = "images";       //Relative path to images
    private String relAnnotationPath = "annotations";   //Relative path to annotations
    private String homeDir;                         //Home directory

    private String testImagePath;
    private String testAnnotationPath;
    private String trainImagePath;
    private String trainAnnotationPath;

    private String[] classLabels;
    private int numClasses = 150;

    /**
     * @param homeDir: location where to find "images/" and "annotations/" directories.
     */
    public ADE20K(String homeDir) throws IOException {
        this.homeDir = new File(homeDir).getAbsolutePath();
        this.testImagePath =  Paths.get(homeDir, relImagePath, "validation").toString() + File.separator;
        this.testAnnotationPath = Paths.get(homeDir, relAnnotationPath, "validation").toString() + File.separator;
        this.trainImagePath =  Paths.get(homeDir, relImagePath, "training").toString() + File.separator;
        this.trainAnnotationPath = Paths.get(homeDir, relAnnotationPath, "training").toString() + File.separator;
        this.classLabels = new String[numClasses];
        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(homeDir, "labels").toFile()))) {
            String label;
            int i=0;
            while ((label = br.readLine()) != null)
                classLabels[i++]=label;
        } catch (FileNotFoundException e) {
            throw new IOException("Labels file not found.");
        } catch (IOException e) {
            throw new IOException("Error while reading labels file.");
        }

    }

    /**
     * Get training set file names
     * @param limit max number of files; -1 if unbounded
     * @return the file names
     */
    public List<String> getTrainingSet(int limit) {
        List<String> res = new LinkedList<>();
        File[] files = new File(trainAnnotationPath).listFiles();
        if (files == null) return null;
        //Get files, without extensions
        int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                res.add(file.getName().split("\\.")[0]);
                count++;
                if (count>=limit && limit!=-1) break;
            }
        }
        return res;
    }

    /**
     * Get test set file names
     * @param limit max number of files; -1 if unbounded
     * @return the file names
     */
    public List<String> getTestSet(int limit) {
        List<String> res = new LinkedList<>();
        File[] files = new File(testAnnotationPath).listFiles();
        if (files == null) return null;
        //Get files, without extensions
        int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                res.add(file.getName().split("\\.")[0]);
                count++;
                if (count>=limit && limit!=-1) break;
            }
        }
        return res;
    }

    /**
     * @param sampleName the sample name (e.g. obtained from getTrainingSet())
     * @return a File with image location
     */
    public File getTrainImageFile(String sampleName) {
        return new File(trainImagePath + sampleName + ".jpg");
    }

    /**
     * @param sampleName the sample name (e.g. obtained from getTrainingSet())
     * @return a File with annotation file (.xml) location
     */
    public File getTrainAnnotationFile(String sampleName) {
        return new File(trainAnnotationPath + sampleName + ".png");
    }

    /**
     * @param sampleName the sample name (e.g. obtained from getTestSet())
     * @return a File with image location
     */
    public File getTestImageFile(String sampleName) {
        return new File(testImagePath + sampleName + ".jpg");
    }

    /**
     * @param sampleName the sample name (e.g. obtained from getTestSet())
     * @return a File with annotation file (.xml) location
     */
    public File getTestAnnotationFile(String sampleName) {
        return new File(testAnnotationPath + sampleName + ".png");
    }

    /**
     * return class label, from id (0 to N-1)
     */
    public String getClassLabel(int classId) {
        return classLabels[classId];
    }

    /**
     * return class id, from label
     */
    public int getClassId(String classLabel) {
        for (int i=0; i<classLabels.length; i++)
            if (classLabel.equals(classLabels[i]))
                return i;
        return -1;
    }

    /**
     * @return the 150 ADE20K class labels.
     */
    public String[] getClassLabels() {
        return classLabels;
    }
}
