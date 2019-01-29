/**
 * Author: XauthorX
 * 08/12/17
 */
package matrix;

/**
 * A 2D integer matrix.
 */
public class Matrix2D {
    private int[][] data;   //data[y][x]
    private int width;
    private int height;

    /**
     * Constructor, by 2d array
     */
    public Matrix2D(int[][] data) {
        this.data=data;
        this.height=data.length;
        this.width=data[0].length;
    }

    /**
     * @return array [y][x]
     */
    public int[][] getData() {return data;}

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Matrix2D clone() {
        int res[][] = new int[height][width];
        for (int y=0; y<height; y++)
            for (int x=0; x<width; x++)
                res[y][x] = data[y][x];
        return new Matrix2D(res);
    }
}
