/**
 * Author: XauthorX
 * 11/01/18
 */
package processing;

import java.util.LinkedList;
import java.util.List;

/**
 * String representation of image segmentations.
 */
public class SegString {
    private StringItem[] items;

    public SegString(StringItem[] items) { this.items = items; }
    public StringItem[] getItems() { return items; }
    public static final float minSegmentHeight = 1.0f/100;  //minimum segment height, relative to the image height; to remove noise

    @Override
    public String toString() {
        String str = "";
        int i=0;
        for (StringItem si : items) {
            if (i!=items.length-1)
                str += si.value + "-" + si.weight + ",";
            else
                str += si.value + "-" + si.weight;
            i++;
        }
        return str;
    }

    /**
     * Compute the string representation of the image segmentation.
     * @param components the connected components object.
     * @return the computed string representation.
     */
    public static SegString[] compute(ConnectedComponents components) {
        int[][] cComp = components.getMatrix().getData();
        int w = components.getMatrix().getWidth();
        int h = components.getMatrix().getHeight();

        //one string for each position
        SegString[] strings = new SegString[w];

        //for each column
        for (int x=0; x<w; x++) {
            List<StringItem> items = new LinkedList<>();

            int prev;
            int cur = -2;
            StringItem curItem = null;

            //for each row
            for (int y=0; y<h; y++) {
                prev=cur;
                cur = cComp[y][x];
                if (cur!=prev) {
                    //Remove regions which are too thin! ATTENTION
                    if (curItem!=null && curItem.weight<minSegmentHeight*h)
                        items.remove(items.size()-1);

                    curItem = new StringItem();
                    curItem.value = cur;
                    items.add(curItem);
                }
                curItem.weight++;
            }

            strings[x] = new SegString(items.toArray(new StringItem[items.size()]));
        }
        return strings;
    }
}
