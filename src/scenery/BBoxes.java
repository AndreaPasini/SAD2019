package scenery;


import matrix.Matrix2D;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by XauthorX on 05/04/17.
 * Class to represent a scenery with its bounding boxes.
 */
public class BBoxes {
    private static class ObjBorders {
        int minX, minY, maxX, maxY;
        int area;
        int width, height;
        int classId;

        ObjBorders(int width, int height) {
            this.width=width;
            this.height=height;
            maxX=0;
            maxY=0;
            minX=width;
            minY=height;
        }

        public void update(int x, int y, int classId) {
            if (x<minX)
                minX=x;
            if (x>maxX)
                maxX=x;
            if (y<minY)
                minY=y;
            if (y>maxY)
                maxY=y;
            area++;
            this.classId = classId;
        }

        public RectangularROI computeRectangularROI(int id, String label) {
            return new RectangularROI(id, label, (float)minX/width, (float)minY/height,
                    (float)(maxX-minX)/width, (float)(maxY-minY)/height);
        }
    }

    /**
     * Extract bounding boxes from connected components objects of the annotated scenery.
     * @param aScenery the annotated scenery.
     * @param classLabels the set of class label names.
     * @return the extracted bounding boxes
     */
    public static BBoxes computeBoundingBoxes(AScenery aScenery, String[] classLabels) {
        //image size
        Matrix2D cComp = aScenery.getConnectedComponents().getMatrix();
        int width = cComp.getWidth();
        int height = cComp.getHeight();
        //init find bounding boxes
        ObjBorders[] objBorders = new ObjBorders[aScenery.getConnectedComponents().getNumCComponents()];
        for (int i=0; i<objBorders.length; i++)
            objBorders[i]=new ObjBorders(width, height);

        int[][] cc = cComp.getData();
        int[][] labels = aScenery.getPredictions().getData();
        //List<RectangularROI> objects = new LinkedList<>();

        //Find bounding boxes
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int id = cc[y][x];
                if (id!=-1) {
                    objBorders[id].update(x, y, labels[y][x]-1);
                }
            }
        }

        List<RectangularROI> rois = new LinkedList<>();
        for (int i=0; i<objBorders.length; i++)
            rois.add(objBorders[i].computeRectangularROI(i, classLabels[objBorders[i].classId]));

        return new BBoxes(rois);
    }

    private List<RectangularROI> objects;

    /**
     * Constructor.
     * @param objects scenery objects (ROIs)
     */
    public BBoxes(List<RectangularROI> objects) {
        this.objects = objects;
    }

    /**
     * @return the scenery ROIs
     */
    public List<RectangularROI> getObjects() {return objects;}

}
