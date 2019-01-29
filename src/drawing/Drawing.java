/**
 * Author: XauthorX
 * 11/12/17
 */
package drawing;

import matrix.Matrix2D;
import scenery.RectangularROI;

public class Drawing {
    public static void drawRectangularROI(Matrix2D matrix, RectangularROI roi, int color) {
        int minX = (int)(roi.getX()*matrix.getWidth());
        int maxX = (int)((roi.getX()+roi.getWidth())*matrix.getWidth());
        int minY = (int)(roi.getY()*matrix.getHeight());
        int maxY = (int)((roi.getY()+roi.getHeight())*matrix.getHeight());
        int[][] mat = matrix.getData();

        for (int x=minX; x<=maxX; x++) {
            mat[minY][x] = color;
            mat[maxY][x] = color;
        }
        for (int y=minY+1; y<maxY; y++) {
            mat[y][minX] = color;
            mat[y][maxX] = color;
        }
    }

    /**
     * Replace the pixel.
     * @param oldV value to select pixel
     * @param newV value with the new value
     */
    public static void replace(Matrix2D matrix, int oldV, int newV) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[][] mat = matrix.getData();
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                if (mat[y][x]==oldV)
                    mat[y][x]=newV;
    }
}
