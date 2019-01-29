/**
 * Author: XauthorX
 * 10/01/18
 */
package processing;

import scenery.AScenery;
import scenery.BBoxes;

import java.util.HashSet;
import java.util.Set;

public class Relationships {

    private static final float thr = 1.8f;      //Size threshold (1+x)
    private static final float areaThr = 1.9f;  //Size threshold (1+x)


    public interface RelValue {//Relationship value
        int ordinal();
    }
    //Relationship values
    public enum PosType implements RelValue {ABOVE, BELOW, ON, HANGING, INSIDE, AROUND, SIDE, SIDE_UP, SIDE_DOWN}  //Position types
    public enum Size implements RelValue {BIGGER, SAME, SMALLER}

    //Relationship types
    public enum RelType {WIDTH, HEIGHT, AREA, POSITION}

    private SegString[] strings;            //String segmentation
    private int[] objectsSize;              //Size of each object. objectsSize[conn. component id]
    private PosType[][] positions;          //Result with the relative object positions
    private Size[][][] relativeSize;        //Relative size between objects relativeSize[i][j][WIDTH,HEIGHT,AREA]

    private BBoxes bBoxes;                  //Object bounding boxes

    public SegString[] getStrings() { return strings; }
    public void setStrings(SegString[] strings) { this.strings = strings; }

    /**
     * @return Size of each object. objectsSize[conn. component id]
     */
    public int[] getObjectsSize() { return objectsSize; }
    public void setObjectsSize(int[] objectsSize) { this.objectsSize = objectsSize; }
    public int getNumObjects() { return objectsSize.length;}

    /**
     * @return Result with the relative object positions
     */
    public PosType[][] getPositions() { return positions; }
    public void setPositions(PosType[][] positions) { this.positions = positions; }

    /**
     * @return Relative size between objects relativeSize[i][j][WIDTH,HEIGHT,AREA]
     */
    public Size[][][] getRelativeSize(){ return relativeSize; }
    public void setRelativeSize(Size[][][] relativeSize) { this.relativeSize=relativeSize;}

    /**
     * @return Bounding boxes for each object
     */
    public BBoxes getbBoxes() { return bBoxes; }
    public void setbBoxes(BBoxes bBoxes) { this.bBoxes = bBoxes; }

    /**
     * Compute the object relationships.
     * @param scenery the input scenery.
     * @return the computed relationships.
     */
    public static Relationships compute(AScenery scenery) {
        Relationships res = new Relationships();

        //one string for each position
        SegString[] strings = SegString.compute(scenery.getConnectedComponents());
        res.setStrings(strings);
        //number of objects
        int nObjects = scenery.getConnectedComponents().getNumCComponents();
        //Bounding boxes
        BBoxes bBoxes = BBoxes.computeBoundingBoxes(scenery, new String[150]);

        //Object bounding boxes
        res.setbBoxes(bBoxes);

        //Objects dimension
        int[] objSize = getObjectsSize(strings, nObjects);
        int[] objWidth = getObjectsWidth(strings, nObjects);
        res.setObjectsSize(objSize);

        //String descriptors
        int[][]   inside = new int[nObjects][nObjects];     //inside[a][b] -> a inside b (area of a)
        int[][]     on = new int[nObjects][nObjects];       //on[a][b] -> a on b (width)
        int[][][] above = new int[nObjects][nObjects][2];   //above[a][b][0] = area of min(a,b); above[a][b][1] = area of max(a,b);   -> a above b

        //Compute the descriptors, for each string
        //for (int s = 0; s < strings.length; s++) {
        for (SegString str : strings) {
            StringItem[] items = str.getItems();

            //Get the set of object IDs inside the string.
            Set<Integer> objectsSet = new HashSet<>();
            //for (int si = 0; si < items.length; si++) {
            for (StringItem item : items) {
                int id = item.value;
                if (id != -1)
                    objectsSet.add(id);
            }
            Integer[] objects = objectsSet.toArray(new Integer[objectsSet.size()]);

            //For each Object pair:
            for (int i = 0; i < objects.length; i++) {
                for (int j = i + 1; j < objects.length; j++) {
                    int ob1 = objects[i];
                    int ob2 = objects[j];
                    int i1 = findFirst(ob1, str);
                    int i2 = findFirst(ob2, str);

                    //Find the indexes r1,r2
                    int r1,r2;
                    if (ob1<ob2) {
                        r1=0;//r1-first
                        r2=1;//r2-second
                    }
                    else {
                        r1=1;//r1-second
                        r2=0;//r2-first
                    }

                    //found sequence "ob1...ob2"
                    if (i1<i2) {
                        //obj2 inside obj1;
                        if (isInside(i1,i2,str))
                            inside[ob2][ob1]+=items[i2].weight; //area of obj2
                        //obj1 on obj2;
                        else if (isOn(i1,i2,str))
                            on[ob1][ob2]++;//width
                        else {
                            //obj1 above obj2;
                            for (int i3=i1; i3<i2; i3++)
                                if (items[i3].value==ob1)   //all the ob1 above ob2
                                    above[ob1][ob2][r1] += items[i3].weight;

                            for (int i3=i2; i3<items.length; i3++)
                                if (items[i3].value==ob2)   //all the ob2 below ob1
                                    above[ob1][ob2][r2] += items[i3].weight;
                        }
                    }
                    //found sequence "ob2...ob1"
                    else {
                        //obj1 inside obj2;
                        if (isInside(i2,i1,str))
                            inside[ob1][ob2]+=items[i1].weight; //area of obj1
                        //obj2 on obj1
                        else if (isOn(i2,i1,str))
                            on[ob2][ob1]++;//width
                        else {
                            //obj2 above obj1;
                            for (int i3=i2; i3<i1; i3++)
                                if (items[i3].value==ob2)   //all the ob2 above ob1
                                    above[ob2][ob1][r2] += items[i3].weight;

                            for (int i3=i1; i3<items.length; i3++)
                                if (items[i3].value==ob1)   //all the ob1 below ob2
                                    above[ob2][ob1][r1] += items[i3].weight;
                        }
                    }
                }
            }
        }

        //Table of the positions:
        PosType[][] positions = new PosType[nObjects][nObjects];
        Size[][][] relSize = new Size[nObjects][nObjects][3];
        int sceneryArea = scenery.getPredictions().getWidth()*scenery.getPredictions().getHeight();
        //For each object pair in the scene.
        for (int i = 0; i < nObjects; i++) {
            for (int j = i + 1; j < nObjects; j++) {

                float y1 = bBoxes.getObjects().get(i).getY();
                float y2 = bBoxes.getObjects().get(j).getY();
                float w1 = bBoxes.getObjects().get(i).getWidth();
                float w2 = bBoxes.getObjects().get(j).getWidth();
                float h1 = bBoxes.getObjects().get(i).getHeight();
                float h2 = bBoxes.getObjects().get(j).getHeight();
                float a1 = 1.0f*objSize[i]/sceneryArea;
                float a2 = 1.0f*objSize[j]/sceneryArea;


                //ATTENTION: Thin objects are not considered ... (suppose they are just noise)
                if (h1<SegString.minSegmentHeight || h2<SegString.minSegmentHeight) {
                    continue;
                }


                //Width
                float wRatio12 = w1/w2;
                float wRatio21 = w2/w1;
                if (wRatio12>thr) {
                    relSize[i][j][RelType.WIDTH.ordinal()]=Size.BIGGER;
                    relSize[j][i][RelType.WIDTH.ordinal()]=Size.SMALLER;
                }
                else if (wRatio21>thr) {
                    relSize[i][j][RelType.WIDTH.ordinal()]=Size.SMALLER;
                    relSize[j][i][RelType.WIDTH.ordinal()]=Size.BIGGER;
                }
                else {
                    relSize[i][j][RelType.WIDTH.ordinal()]=Size.SAME;
                    relSize[j][i][RelType.WIDTH.ordinal()]=Size.SAME;
                }
                //Height
                float hRatio12 = h1/h2;
                float hRatio21 = h2/h1;
                if (hRatio12>thr) {
                    relSize[i][j][RelType.HEIGHT.ordinal()]=Size.BIGGER;
                    relSize[j][i][RelType.HEIGHT.ordinal()]=Size.SMALLER;
                }
                else if (hRatio21>thr) {
                    relSize[i][j][RelType.HEIGHT.ordinal()]=Size.SMALLER;
                    relSize[j][i][RelType.HEIGHT.ordinal()]=Size.BIGGER;
                }
                else {
                    relSize[i][j][RelType.HEIGHT.ordinal()]=Size.SAME;
                    relSize[j][i][RelType.HEIGHT.ordinal()]=Size.SAME;
                }
                //Area
                float aRatio12 = a1/a2;
                float aRatio21 = a2/a1;
                if (aRatio12>areaThr) {
                    relSize[i][j][RelType.AREA.ordinal()]=Size.BIGGER;
                    relSize[j][i][RelType.AREA.ordinal()]=Size.SMALLER;
                }
                else if (aRatio21>areaThr) {
                    relSize[i][j][RelType.AREA.ordinal()]=Size.SMALLER;
                    relSize[j][i][RelType.AREA.ordinal()]=Size.BIGGER;
                }
                else {
                    relSize[i][j][RelType.AREA.ordinal()]=Size.SAME;
                    relSize[j][i][RelType.AREA.ordinal()]=Size.SAME;
                }




                //i/j Inside j/i
                if (assignInside(i,j,inside,objSize,positions))
                    continue;
                //i/j On j/i
                if (assignOn(i,j,on,above,objSize,objWidth,positions))
                    continue;
                //i/j Above j/i
                if (assignAbove(i,j,above,objSize,positions))
                    continue;


                //Side: with bounding boxes
                float tol = Math.min(h1,h2)*0.3f;

                if (y1 + h1 - tol < y2) {
                    positions[i][j]= PosType.SIDE_UP;
                    positions[j][i]= PosType.SIDE_DOWN;
                    continue;
                }
                if (y1 > y2 + h2 - tol) {
                    positions[i][j]= PosType.SIDE_DOWN;
                    positions[j][i]= PosType.SIDE_UP;
                    continue;
                }
                positions[i][j]= PosType.SIDE;
                positions[j][i]= PosType.SIDE;
            }
        }

        //Positions
        res.setPositions(positions);
        //Size:
        res.setRelativeSize(relSize);
        return res;
    }

    /**
     * Return true if found the inside relationship between i,j or j,i
     */
    private static boolean assignInside(int i, int j, int[][] inside, int[] objSize, PosType[][] positions) {
        int r1,r2;
        //For the two orderings: ij/ji
        for (int n=0; n<2; n++) {
            if (n==0) {
                r1 = i;
                r2 = j;
            }
            else {
                r1 = j;
                r2 = i;
            }

            if (1.0f*inside[r1][r2]/objSize[r1]>0.6f) {
                //r1 inside r2
                positions[r1][r2]= PosType.INSIDE;
                positions[r2][r1]= PosType.AROUND;
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if found the on relationship between i,j or j,i
     */
    private static boolean assignOn(int i, int j, int[][] on, int[][][] above, int[] objSize, int[] objWidth, PosType[][] positions) {
        int r1,r2;

        //For the two orderings: ij/ji
        for (int n=0; n<2; n++) {
            if (n == 0) {
                r1 = i;
                r2 = j;
            } else {
                r1 = j;
                r2 = i;
            }
            int r1index,r2index;
            if (r1<r2) {
                r1index=0;
                r2index=1;
            }
            else {
                r1index=1;
                r2index=0;
            }

            float onPercent2;
            float onPercent1;

            onPercent2=1.0f*on[r1][r2]/objWidth[r2];
            onPercent1=1.0f*on[r1][r2]/objWidth[r1];
            if ((onPercent2>0.1f && (onPercent2 + 1.0f*above[r1][r2][r2index]/objSize[r2])>0.4f) ||
                    (onPercent1>0.1f && (onPercent1 + 1.0f*above[r1][r2][r1index]/objSize[r1])>0.4f)) {
                //r1 on r2
                positions[r1][r2] = PosType.ON;
                positions[r2][r1] = PosType.HANGING;

                return true;
            }

        }

        return false;
    }

    /**
     * Return true if found the above relationship between i,j or j,i
     */
    private static boolean assignAbove(int i, int j, int[][][] above, int[] objSize, PosType[][] positions) {
        int r1,r2;

        //For the two orderings: ij/ji
        for (int n=0; n<2; n++) {
            if (n == 0) {
                r1 = i;
                r2 = j;
            } else {
                r1 = j;
                r2 = i;
            }
            int r1index,r2index;
            if (r1<r2) {
                r1index=0;
                r2index=1;
            }
            else {
                r1index=1;
                r2index=0;
            }

            if ((1.0f*above[r1][r2][r2index]/objSize[r2]>0.5f) ||
                (1.0f*above[r1][r2][r1index]/objSize[r1]>0.5f)) {
                positions[r1][r2]= PosType.ABOVE;
                positions[r2][r1]= PosType.BELOW;
                return true;
            }
        }

        return false;
    }

    /**
     * Get the objects size, given the id.
     */
    private static int[] getObjectsSize(SegString[] strings, int numObjects) {
        int[] objSize = new int[numObjects];
        for (SegString str : strings) {
            for (int si = 0; si < str.getItems().length; si++) {
                StringItem it = str.getItems()[si];
                if (it.value != -1)
                    objSize[it.value]+=it.weight;
            }
        }
        return objSize;
    }

    /**
     * Get the objects width, given the id.
     */
    private static int[] getObjectsWidth(SegString[] strings, int numObjects) {
        int[] objSize = new int[numObjects];

        for (SegString str : strings) {
            Set<Integer> already = new HashSet<>();
            for (int si = 0; si < str.getItems().length; si++) {
                StringItem it = str.getItems()[si];
                if (it.value != -1 && !already.contains(it.value)) {
                    objSize[it.value]++;
                    already.add(it.value);
                }
            }
        }
        return objSize;
    }

    /**
     * Return the first index in str of objId
     */
    private static int findFirst(int objId, SegString str) {
        for (int i=0; i<str.getItems().length; i++)
            if (str.getItems()[i].value==objId)
                return i;
        return -1;
    }

    /**
     * Return true if obj[i2] is inside obj[i1] (sequence: o1,..o2..,o1)
     */
    private static boolean isInside(int i1, int i2, SegString str) {
        int obj1=str.getItems()[i1].value;
        //find i1-i2-i3 with i1,i3->obj1 and i2=obj2
        for (int i3=i2+1; i3<str.getItems().length; i3++)
            if (str.getItems()[i3].value==obj1)
                return true;
        return false;
    }

    /**
     * Return true if obj[i1] is on obj[i2] (sequence: o1,o2)
     */
    private static boolean isOn(int i1, int i2, SegString str) {
        //we suppose that if we are here the inside descriptor failed
        //then all the segments for o1 are above or on i2
        if (i1+1==i2)
            return true;

        int obj1=str.getItems()[i1].value;
        int obj2=str.getItems()[i2].value;

        //find o1-o2 for positions > i1
        for (int i3=i1+1; i3<str.getItems().length-1; i3++)
            if (str.getItems()[i3].value==obj1 && str.getItems()[i3+1].value==obj2)
                return true;

        return false;
    }
}
