package anomalies.entities;

import processing.Relationships;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by XauthorX on 24/08/17.
 * Class to contain a SmartRule:
 * A triplet.
 * The associated Histogram.
 */
public class SmartRule {
    private SmartTriplet triplet;                   //triplet <subject, featureType, reference>
    private Histogram histogram;               //histogram [...]

    /**
     * Constructor.
     * @param triplet  <subject, featureType, reference>
     * @param histogram the associated histogram
     */
    public SmartRule(SmartTriplet triplet, Histogram histogram) {
        this.triplet = triplet;
        this.histogram = histogram;
    }

    public SmartTriplet getTriplet() { return triplet; }
    public Histogram getHistogram() { return histogram; }

    public void rearrange() {
        if (histogram.countA>histogram.countB) {    //reverse if subject cardinality is bigger

            String tmp = triplet.subject;
            triplet.subject = triplet.reference;
            triplet.reference = tmp;
            int tmpCount = histogram.countA;
            histogram.countA=histogram.countB;
            histogram.countB=tmpCount;

            if (triplet.relType==Relationships.RelType.POSITION) {
                histogram.swap(Relationships.PosType.ABOVE.ordinal(), Relationships.PosType.BELOW.ordinal());
                histogram.swap(Relationships.PosType.ON.ordinal(), Relationships.PosType.HANGING.ordinal());
                histogram.swap(Relationships.PosType.INSIDE.ordinal(), Relationships.PosType.AROUND.ordinal());
                histogram.swap(Relationships.PosType.SIDE_UP.ordinal(), Relationships.PosType.SIDE_DOWN.ordinal());
            }
            if (triplet.relType==Relationships.RelType.WIDTH ||
                    triplet.relType==Relationships.RelType.HEIGHT ||
                    triplet.relType==Relationships.RelType.AREA) {
                histogram.swap(Relationships.Size.BIGGER.ordinal(), Relationships.Size.SMALLER.ordinal());
            }
        }
    }

    public static String explain(SmartTriplet t, Histogram h, float neverThresh) {
        Map<String,Float> values = new HashMap<>();
        for (int i=0; i<h.histValues.length; i++)
            values.put(RelativeFeaturesExtractor.getFeatureString(t.relType, i),h.histValues[i]);

        List<Map.Entry<String,Float>> sorted = values.entrySet().stream()
                .sorted((a,b)-> Float.compare(b.getValue(),a.getValue())).collect(Collectors.toList());

        String res = "";
        for (Map.Entry<String,Float> e : sorted)
            if (e.getValue()>0.1)
                res+=e.getKey()+" ("+e.getValue()+") ";

        return res;
        /*
        List<String> nonZero = new LinkedList<>();
        List<String> zero = new LinkedList<>();
        int i=0;
  /*      for (float val : h.histValues) {
            if (val<neverThresh)
                zero.add(RelativeFeaturesExtractor.getFeatureString(t.featureType, i));
            else
                nonZero.add(RelativeFeaturesExtractor.getFeatureString(t.featureType, i));
            i++;
        }
*/
        /*
        if (nonZero.size()==1)
            return "ALWAYS "+nonZero.get(0);
        else {
            String res = "NEVER";
            for (String attr:zero)
                res+=" "+attr;
            return res;*/
        /*}*/




    }
}
