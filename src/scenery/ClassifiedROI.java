package scenery;

import java.io.Serializable;

/**
 * Created by XauthorX on 10/04/17.
 * Class to store results of a classification model for a given ROI.
 */
public class ClassifiedROI implements Serializable {
    RectangularROI roi;        //Region
    float[] classScores;        //Class scores
    private String[] labels;    //Ordered set of labels to interpret classScores vector
    private int actualIndex;   //Index of the actual class label.

    /**
     * Constructor
     * @param roi the classified roi
     * @param classScores class scores vector (0-1)
     * @param labels the ordered set of labels in form of string, used to interpret getClassScores() result.
     */
    public ClassifiedROI (RectangularROI roi, float[] classScores, String[] labels) {
        this.roi = roi;
        this.classScores = classScores;
        this.labels = labels;

        //Getting actual class index
        int i = 0;
        this.actualIndex = -1;
        for (String label : labels) {
            if (label.equals(roi.getLabel())) {
                this.actualIndex = i;
                break;
            }
            i++;
        }
    }

    /**
     * Clone this object.
     * The roi and the class scores are copied.
     * The vector String labels[] remains a reference.
     * @return a copy of this object.
     */
    public ClassifiedROI clone() {
        return new ClassifiedROI(roi.clone(), classScores.clone(), labels);
    }

    /**
     * @return the encapsulated ROI.
     */
    public RectangularROI getRoi() { return roi; }

    /**
     * @return the class scores.
     */
    public float[] getClassScores() { return classScores; }

    /**
     * @return the ordered set of labels in form of string, used to interpret getClassScores() result.
     */
    public String[] getLabels() { return labels; }

    /**
     * @return the predicted class (with highest score)
     */
    public String getPredictedClass() {
        return labels[getPredictedClassIndex()];
    }

    /**
     * @return the predicted class index (with highest score)
     */
    public int getPredictedClassIndex() {
        double max = 0;
        int best = 0;
        int i=0;

        for (double val : classScores) {
            if (val > max) {
                max = val;
                best = i;
            }
            i++;
        }
        return best;
    }

    /**
     * @return the index of the actual class.
     */
    public int getActualClassIndex() {
        return actualIndex;
    }

    /**
     * @param classLabel the selected class label
     * @return the score for the given class
     */
    public float getClassScore(String classLabel) {
        int i = 0;
        for (i=0; i<labels.length; i++)
            if (labels[i].equals(classLabel))
                return classScores[i];
        return -1;
    }
}
