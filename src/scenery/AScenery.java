/**
 * Author: XauthorX
 * 18/12/17
 */
package scenery;

import matrix.Matrix2D;
import processing.ConnectedComponents;

/**
 * Annotated Classified Scenery.
 * Contains the labels for each pixel and connected components.
 */
public class AScenery {
    Matrix2D predictions;
    ConnectedComponents connectedComponents;        //connected components segmentation, relative to the predictions.

    public AScenery(Matrix2D predictions) {
        this.predictions = predictions;
        this.connectedComponents = ConnectedComponents.compute(this.predictions);
    }

    /**
     * Matrix values:
     * 0  : unlabeled
     * >0 : class labels
     * @return the computed predictions
     */
    public Matrix2D getPredictions() { return predictions; }

    /**
     * @return Connected components segmentation, relative to the predictions.
     */
    public ConnectedComponents getConnectedComponents() { return connectedComponents; }

    /**
     * @return the number of objects in the predictions (connected components).
     */
    public int getNumObjects() {
        return connectedComponents.getNumCComponents();
    }
}
