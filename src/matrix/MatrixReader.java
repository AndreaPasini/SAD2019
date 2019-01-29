/**
 * Author: XauthorX
 * 08/12/17
 */
package matrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

public class MatrixReader {
    /**
     * Read sparse Matrix3D
     */
    public static Matrix3D readSparseMatrix3D() {
        return null;
    }

    /**
     * Read Matrix2D.
     * For ADE20K dataset:
     * - values from 0 to 150 (0=unlabeled)
     */
    public static Matrix2D readMatrix2D(File inputImage) throws IOException {
        BufferedImage image = ImageIO.read(inputImage);

        //Image size
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] mat = new int[h][w];

        //Each byte is a grayscale color associated to the class
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        int i=0;
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                mat[y][x] = Byte.toUnsignedInt(pixels[i++]);

        return new Matrix2D(mat);
    }
}
