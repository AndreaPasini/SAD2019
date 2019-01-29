/**
 * Author: XauthorX
 * 18/12/17
 */
package scenery;

import matrix.Matrix2D;

/**
 * Annotated Scenery, with evaluation data (ground truth).
 */
public class ASceneryEval extends AScenery {
    private Matrix2D groundTruth;       //ground truth labels

    /**
     * Constructor.
     * @param aScenery the annotated scenery.
     * @param groundTruth the ground truth labels.
     */
    public ASceneryEval(AScenery aScenery, Matrix2D groundTruth) {
        super(aScenery.predictions);
        this.groundTruth = groundTruth;
    }

    /**
     * Constructor.
     * @param predictions class predictions.
     * @param groundTruth the ground truth labels.
     */
    public ASceneryEval(Matrix2D predictions, Matrix2D groundTruth) {
        super(predictions);
        this.groundTruth = groundTruth;
    }

    /**
     * Matrix values:
     * 0  : unlabeled
     * >0 : class label
     * @return the ground truth labels
     */
    public Matrix2D getGroundTruth() { return groundTruth; }

    /**
     * Return the pixel accuracy for this sample (% correct pixels).
     * @return an array [numCorrectPixels, numPixels]
     */
    public long[] computePixelAccuracy() {
        int w = groundTruth.getWidth();
        int h = groundTruth.getHeight();
        int[][] truth = groundTruth.getData();
        int[][] pred = predictions.getData();
        long[] res = new long[2];
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                int truthVal = truth[y][x];
                if (truthVal>0) {     //do not count unlabeled pixels
                    if (truthVal == pred[y][x])
                        res[0]++;
                    res[1]++;
                }
            }
        }

        return res;
    }

    /**
     * Return the pixel accuracy for the specified object Id (% correct pixels).
     * @param objectId the id of the object (included in connected components)
     * @return an array [numCorrectPixels, numPixels]
     */
    public long[] computePixelAccuracy(int objectId) {
        int w = groundTruth.getWidth();
        int h = groundTruth.getHeight();
        int[][] truth = groundTruth.getData();
        int[][] pred = predictions.getData();
        int[][] cComp = connectedComponents.getMatrix().getData();
        long[] res = new long[2];

        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                if (cComp[y][x]==objectId) {    //selected object
                    if (truth[y][x]!=-1) {      //count only labeled pixels in the ground truth
                        if (truth[y][x] == pred[y][x])
                            res[0]++;
                        res[1]++;
                    }
                }
            }
        }

        return res;
    }

}
