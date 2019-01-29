package anomalies;

import processing.Relationships;
import anomalies.entities.SmartTriplet;

public class IndexStringPair {

    public int s;                   //subject index
    public int r;                   //reference index
    public SmartTriplet triplet;    //triplet

    /**
     * Sorts between a, b (alphabetical order).
     * Returns: s=i, subject=a: r=j, reference=b if (a<b)
     * Returns: s=j, subject=b: r=i, reference=a otherwise
     */
    public IndexStringPair(int i, String a, int j, String b) {
        triplet = new SmartTriplet();
        if (a.compareTo(b)<=0) {
            triplet.subject = a;
            triplet.reference = b;
            s = i;
            r = j;
        }
        else {
            triplet.subject = b;
            triplet.reference = a;
            s = j;
            r = i;
        }
        triplet.relType = Relationships.RelType.values()[0];
    }


}
