/**
 * Author: XauthorX
 * 11/12/17
 */
package processing;

import matrix.Matrix2D;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ConnectedComponents {
    private Matrix2D matrix;        //connected components IDs
    private int numCComponents;     //number of connected components
    private int[] labels;           //label for each connected component (label identifiers from 1 to N)

    public ConnectedComponents(Matrix2D labeledImage, Matrix2D cComponents, int[]labels) {
        this.matrix = cComponents;
        this.labels = labels;
        this.numCComponents = labels.length;
    }

    /**
     * Get connected components segmentation.
     * Matrix values:
     * -1  : unlabeled
     * >=0 : object IDs
     * @return the connected components matrix
     */
    public Matrix2D getMatrix() {
        return matrix;
    }

    /**
     * @return the number of connected components
     */
    public int getNumCComponents() {
        return numCComponents;
    }

    /**
     * @return an array labels[objId] with the labels for each object
     */
    public int[] getLabels() { return labels; }

    private static class Point {
        int x,y;
        Point(int x, int y) {this.x=x; this.y=y;}
    }

    private static final int VOID = -2;
    private static final int UNLABELED = -1;

    /**
     * Compute connected components of the input matrix
     * @param inputMatrix with values >0; if value is 0: the result in the output matrix is unlabeled (-1)
     * @return the computed connected components.
     */
    public static ConnectedComponents compute(Matrix2D inputMatrix) {
        List<Integer> labels = new LinkedList<>();
        int width = inputMatrix.getWidth();
        int height = inputMatrix.getHeight();
        int[][] inputMat = inputMatrix.getData();
        int[][] outputMat = new int[height][width];
        for (int y=0; y<height; y++)
            for (int x=0; x<width; x++)
                outputMat[y][x]=VOID;

        int objectId = 0;

        //Breadth first search
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int classId = inputMat[y][x];               //class id for the pixel

                if (classId==0)
                    outputMat[y][x]=UNLABELED;              //unlabeled pixels
                else if (outputMat[y][x]==VOID) {
                    //Label new object
                    outputMat[y][x] = objectId;
                    Queue<Point> points = new LinkedList<>();
                    points.add(new Point(x, y));

                    int nextX;
                    int nextY;

                    while (!points.isEmpty()) {
                        Point p = points.remove();
                        int pX = p.x;
                        int pY = p.y;

                        if (pX > 0) {
                            nextX = pX - 1;
                            nextY = pY;
                            labelMatrix(nextX, nextY, objectId, classId, inputMat, outputMat, points);
                        }
                        if (pY > 0) {
                            nextX = pX;
                            nextY = pY - 1;
                            labelMatrix(nextX, nextY, objectId, classId, inputMat, outputMat, points);
                        }
                        if (pX + 1 < width) {
                            nextX = pX + 1;
                            nextY = pY;
                            labelMatrix(nextX, nextY, objectId, classId, inputMat, outputMat, points);
                        }
                        if (pY + 1 < height) {
                            nextX = pX;
                            nextY = pY + 1;
                            labelMatrix(nextX, nextY, objectId, classId, inputMat, outputMat, points);
                        }
                    }
                    labels.add(classId);
                    objectId++;
                }
            }
        }

        int[] labelIds = new int[labels.size()];
        int i = 0;
        for (Integer label : labels)
            labelIds[i++]=label;
        return new ConnectedComponents(inputMatrix, new Matrix2D(outputMat), labelIds);
    }

    private static void labelMatrix(int nextX, int nextY, int objectId, int classId, int[][]inputMat, int[][]outputMat, Queue<Point> points) {
        if (outputMat[nextY][nextX]==VOID && inputMat[nextY][nextX]==classId) {    //if still unlabeled
            outputMat[nextY][nextX] = objectId;
            points.add(new Point(nextX, nextY));
        }
    }
}
