/**
 * Author: XauthorX
 * 08/12/17
 */
package matrix;

/**
 * A 3D float matrix, for class scores.
 */
public class Matrix3D {
    private float[][][] data;

    public Matrix3D(float[][][] data) {this.data = data;}

    public float get(int x, int y, int label) { return data[label][x][y]; }
    public float[][] get(int label) { return data[label]; }
    /**
     * @return array [label][y][x] -> class score
     */
    public float[][][] getData() {return data;}
}
