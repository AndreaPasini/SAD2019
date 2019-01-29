/**
 * Author: XauthorX
 * 08/12/17
 */
package matrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class MatrixWriter {
    /**
     * Write sparse Matrix3D
     */
    public static void writeSparseMatrix3D(Matrix3D matrix) {

    }

    /**
     * Write Matrix2D
     */
    public static void writeMatrix2D(Matrix2D matrix, File outputImage) throws IOException {
        ImageIO.write(getBufferedImage(matrix), "png", outputImage);
    }

    /**
     * Write Matrix2D, colors
     * Allowed matrix values: >=-1 && <colors.length-1
     */
    public static void writeMatrix2D(Matrix2D matrix, int[] colors, File outputImage) throws IOException {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[][] mat = matrix.getData();

        int[] pixels = new int[w*h];
        int i=0;
        for (int y=0; y<h; y++)
            for (int x = 0; x < w; x++)
                pixels[i++] = colors[mat[y][x]+1];

        BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_INT_BGR);

        int[] dataBuffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, dataBuffer, 0, pixels.length);

        ImageIO.write(image, "png", outputImage);
    }

    /**
     * Get buffered image from Matrix2D (grayscale)
     */
    public static BufferedImage getBufferedImage(Matrix2D matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[][] mat = matrix.getData();

        byte[] pixels = new byte[w*h];
        int i=0;
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                pixels[i++] = (byte)mat[y][x];

        BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] dataBuffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, dataBuffer, 0, pixels.length);
        return image;
    }


    /**
     * Get buffered image from Matrix2D (color)
     */
    public static BufferedImage getColorBufferedImage(Matrix2D matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[][] mat = matrix.getData();

        byte[] pixels = new byte[w*h];
        int i=0;
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                pixels[i++] = (byte)mat[y][x];

        BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] dataBuffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, dataBuffer, 0, pixels.length);
        return image;
    }
}
