package scenery;

/**
 * Created by XauthorX on 12/03/17.
 * A Rectangular Region of Interest
 */
public class RectangularROI extends ROI  {
    private float x, y;
    private float width, height;

    /**
     * Rectangle position and size: every measure in % (0-1) with respect to the image.
     * @param id identifier of the object.
     * @param x top left corner x
     * @param y top left corner y
     * @param width rectangle width
     * @param height rectangle height
     * @throws IllegalArgumentException if parameters are not between 0 and 1
     */
    public RectangularROI(int id, String label, float x, float y, float width, float height) throws IllegalArgumentException {
        if (x<0 || x>1) throw new IllegalArgumentException("x must be between 0 and 1");
        if (y<0 || y>1) throw new IllegalArgumentException("y must be between 0 and 1");
        if (width<0 || width>1) throw new IllegalArgumentException("width must be between 0 and 1");
        if (height<0 || height>1) throw new IllegalArgumentException("height must be between 0 and 1");
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
    }

    /**
     * Clone this object.
     * @return a copy of this object.
     */
    public RectangularROI clone() {
        return new RectangularROI(id, label, x, y, width, height);
    }

    //Getters

    /**
     * @return x coordinate (top-left corner), range [0-1]
     */
    public float getX() { return x; }

    /**
     * @return y coordinate (top-left corner), range [0-1]
     */
    public float getY() { return y; }

    /**
     * @return width, range [0-1]
     */
    public float getWidth() {
        return width;
    }

    /**
     *
     * @return height, range [0-1]
     */
    public float getHeight() {
        return height;
    }
}
