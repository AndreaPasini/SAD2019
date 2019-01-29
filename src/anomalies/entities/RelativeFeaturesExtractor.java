package anomalies.entities;

import processing.Relationships;
import scenery.RectangularROI;

import java.util.HashMap;

/**
 * Created by XauthorX on 15/04/17.
 * Class to extract relative features between ROIs.
 * Relative features are useful to obtain contextual information of objects composing an image.
 */
public class RelativeFeaturesExtractor {
    private static final float posTolerance = 0.8f;
    private static final float distThreshold = 0.5f;
    private static final float dimThresholdBig = 1.5f;
    private static final float dimThresholdSmall = 0.5f;
    private static final float lumThresholdBig = 0.2f;
    private static final float lumThresholdSmall = -0.2f;

    public enum FeatureTypes {xPosition, yPosition, xDimension, yDimension, Distance, Luminosity}

    public interface FeatureValue {
        int ordinal();
    }

    public enum Xposition implements FeatureValue {LEFT, SAME, RIGHT}
    public enum Yposition implements FeatureValue {ABOVE, SAME, BELOW}
    public enum Dimension implements FeatureValue {BIGGER, SAME, SMALLER}
    public enum Distance  implements FeatureValue {NEAR, FAR}
    public enum Luminosity  implements FeatureValue {BIGGER, SAME, SMALLER}

    public class RelativeFeature {
        private FeatureValue enumObject;
        private FeatureTypes type;

        RelativeFeature(FeatureTypes type, FeatureValue feature) {
            this.type = type;
            this.enumObject = feature;
        }

        public FeatureTypes getType() { return type; }
        public FeatureValue getValue() { return enumObject; };

        public int getOrdinalValue() {
            switch (type) {
                case xPosition:
                    return ((Xposition)enumObject).ordinal();
                case yPosition:
                    return ((Yposition)enumObject).ordinal();
                case xDimension:
                    return ((Dimension)enumObject).ordinal();
                case yDimension:
                    return ((Dimension)enumObject).ordinal();
                case Distance:
                    return ((Distance)enumObject).ordinal();
                case Luminosity:
                    return ((Luminosity)enumObject).ordinal();
                default: return -1;
            }
        }

        public String getTextValue() {
            return enumObject.toString();
        }


    }

    /**
     * Create a histogram to containing the specified feature type.
     * @param type the feature type
     * @return the created histogram
     */
    public static HistogramBuilder createHistogram(Relationships.RelType type) {
        switch (type) {
            case POSITION:
                return new HistogramBuilder(Relationships.PosType.values().length);
            default://area,width,height
                return new HistogramBuilder(Relationships.Size.values().length);
        }
    }

   /* public static HashMap<FeatureTypes, FeatureValue> getAllFeatures(ClassifiedROI subject, ClassifiedROI reference) {
        HashMap<FeatureTypes, FeatureValue> res = new  HashMap<>();


        //res.put(FeatureTypes.xPosition, getXposition(subject.getRoi(), reference.getRoi()));      //noise

        res.put(FeatureTypes.yPosition, getYposition(subject.getRoi(), reference.getRoi()));      //important: 0.2%

        res.put(FeatureTypes.xDimension, getXDimension(subject.getRoi(), reference.getRoi()));    //important:1.2%

        res.put(FeatureTypes.yDimension, getYDimension(subject.getRoi(), reference.getRoi()));    //Important: 1%

        res.put(FeatureTypes.Distance, getDistance(subject.getRoi(), reference.getRoi()));        //Important: 0.1%

        //res.put(FeatureTypes.Luminosity, getLuminosity(subject, reference));                      //important: 0.8%


        //6% with only x dimension and y dimension.
        return res;
    }*/

    public static HashMap<FeatureTypes, FeatureValue> getAllFeatures(RectangularROI subject, RectangularROI reference) {
        HashMap<FeatureTypes, FeatureValue> res = new  HashMap<>();


        //res.put(FeatureTypes.xPosition, getXposition(subject.getRoi(), reference.getRoi()));      //noise

        res.put(FeatureTypes.yPosition, getYposition(subject, reference));      //important: 0.2%

        res.put(FeatureTypes.xDimension, getXDimension(subject, reference));    //important:1.2%

        res.put(FeatureTypes.yDimension, getYDimension(subject, reference));    //Important: 1%

        res.put(FeatureTypes.Distance, getDistance(subject, reference));        //Important: 0.1%

        //res.put(FeatureTypes.Luminosity, getLuminosity(subject, reference));                      //important: 0.8%


        //6% with only x dimension and y dimension.
        return res;
    }

    public static String getFeatureString(Relationships.RelType type, int ordinal) {
        switch (type) {
            case POSITION:
                return Relationships.PosType.values()[ordinal].toString();
            default:
                return Relationships.Size.values()[ordinal].toString();
        }
    }



    /**
     * Computes x relative position (LEFT, SAME, RIGHT).
     * e.g. subject to the LEFT of reference
     * @param subject compute features for this ROI.
     * @param reference the reference object.
     * @return the computed feature
     */
    public static Xposition getXposition(RectangularROI subject, RectangularROI reference) {
        float x1 = subject.getX();
        float x2 = reference.getX();
        float w1 = subject.getWidth();
        float w2 = reference.getWidth();

        if (x1 + w1*posTolerance < x2)
            return Xposition.LEFT;
        if (x1 + w1*(1-posTolerance) > x2 + w2)
            return Xposition.RIGHT;
        return Xposition.SAME;
    }



















    /**
     * Computes y relative position (ABOVE, SAME, BELOW).
     * @param subject compute features for this ROI.
     * @param reference the reference object.
     * @return the computed feature
     */
    public static Yposition getYposition(RectangularROI subject, RectangularROI reference) {
        /*float y1 = subject.getY();
        float y2 = reference.getY();
        float h1 = subject.getHeight();
        float h2 = reference.getHeight();

        if (y1 + h1*posTolerance < y2)
            return Yposition.ABOVE;
        if (y1 + h1*(1-posTolerance) > y2 + h2)
            return Yposition.BELOW;
        return Yposition.SAME;*/

        float y1 = subject.getY();
        float y2 = reference.getY();
        float h1 = subject.getHeight();
        float h2 = reference.getHeight();

        float tol = Math.min(h1,h2)*0.2f;

        if (y1 + h1 - tol < y2)
            return Yposition.ABOVE;
        if (y1 > y2 + h2 - tol)
            return Yposition.BELOW;
        return Yposition.SAME;


    }


    /**
     * Computes relative x dimension (BIGGER, SAME, SMALLER).
     * @param subject compute features for this ROI.
     * @param reference the reference object.
     * @return the computed feature
     */
    public static Dimension getXDimension(RectangularROI subject, RectangularROI reference) {
        /*float w1 = subject.getWidth();
        float w2 = reference.getWidth();
        float dim = w1/w2;
        if (dim > dimThresholdBig)
            return Dimension.BIGGER;
        if (dim < dimThresholdSmall)
            return Dimension.SMALLER;
        return Dimension.SAME;*/


        float w1 = subject.getWidth();
        float w2 = reference.getWidth();
        float dim = w1/w2;

        if (dim > 1.1)
            return Dimension.BIGGER;
        if (dim < 0.8)
            return Dimension.SMALLER;
        return Dimension.SAME;


    }

    /**
     * Computes relative y dimension (BIGGER, SAME, SMALLER).
     * @param subject compute features for this ROI.
     * @param reference the reference object.
     * @return the computed feature
     */
    public static Dimension getYDimension(RectangularROI subject, RectangularROI reference) {
        /*float h1 = subject.getHeight();
        float h2 = reference.getHeight();
        float dim = h1/h2;
        if (dim > dimThresholdBig)
            return Dimension.BIGGER;
        if (dim < dimThresholdSmall)
            return Dimension.SMALLER;
        return Dimension.SAME;*/

        float h1 = subject.getHeight();
        float h2 = reference.getHeight();
        float dim = h1-h2;
        if (dim > 0.1)
            return Dimension.BIGGER;
        if (dim < -0.1)
            return Dimension.SMALLER;
        return Dimension.SAME;

    }


































    /**
     * Computes distance between centers (NEAR, FAR).
     * @param subject compute features for this ROI.
     * @param reference the reference object.
     * @return the computed feature
     */
    public static Distance getDistance(RectangularROI subject, RectangularROI reference) {
        float x1 = subject.getX();
        float x2 = reference.getX();
        float w1 = subject.getWidth();
        float w2 = reference.getWidth();

        float y1 = subject.getY();
        float y2 = reference.getY();
        float h1 = subject.getHeight();
        float h2 = reference.getHeight();

        float xDist = (x1 + w1/2) - (x2 + w2/2);
        float yDist = (y1 + h1/2) - (y2 + h2/2);

        //The max distance between points with [0-1] coordinates is the diagonal of the Square with edge 1.
        //diag = edge*sqrt(2) = 1*sqrt(2) = sqrt(2)
        //hence maximum squaredDist = sqrt(2)^2 = 2
        float squaredDist = (xDist*xDist + yDist*yDist);    //in range [0,2]

        if (squaredDist >= distThreshold)
            return Distance.FAR;
        else return Distance.NEAR;
    }

    /**
     * Computes relative luminosity (BIGGER, SAME, SMALLER).
     * @param subject compute features for this ROI.
     * @param reference the reference object.
     * @return the computed feature
     */
/*    public static Luminosity getLuminosity(AttributeClassifiedROI subject, AttributeClassifiedROI reference) {
        float delta = subject.getBrightness() - reference.getBrightness();
        if (delta > lumThresholdBig)
            return Luminosity.BIGGER;
        if (delta < lumThresholdSmall)
            return Luminosity.SMALLER;
        return Luminosity.SAME;
    }*/
}
