package scenery;

import java.io.Serializable;

/**
 * Created by XauthorX on 12/03/17.
 * ROI: region of interest, within an image
 */
public abstract class ROI implements Serializable {
    String label;
    int id;

    /**
     * @return label associated to this ROI
     */
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {this.label = label;}
    public void setId(int id) { this.id=id; }
    public int getId() {return id;}
}
