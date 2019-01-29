package anomalies.entities;


/**
 * Created by XauthorX on 15/04/17.
 * Class to store a histogram
 */
public class HistogramBuilder {
    private int numSamples;             //number of analyzed samples
    private int[] hist;                 //histogram (count occurrences)

    /**
     * Constructor
     * @param size the number of x values for this histogram
     */
    public HistogramBuilder(int size) {
        hist = new int[size];
    }

    /**
     * Add 1 occurrence to this histogram
     * @param index the index where to accumulate the new sample
     */
    public void addOccurrence(int index) {
        hist[index]++;
        numSamples++;
    }

    /**
     * @return the computed histograms (elements of this array sum up to 1)
     */
    public Histogram getHistogram(int countA, int countB) {
        int size = hist.length;

        Histogram h = new Histogram();
        float[] res = new float[size];

        for (int i=0; i<size; i++)
            res[i] = (float)hist[i]/numSamples;  //computes percent

        h.histValues = res;
        h.countA = countA;
        h.countB = countB;
        h.support = numSamples;

       /* h.importance = computeImportance(maxSamples);
        h.importance2 = computeImportance2(jaccard);
        h.impurity = computeImpurityIndex();
        h.impurityNorm = computeImpurityNormIndex();
        h.jaccard = jaccard;




h.logicalImportance = computeLogicalImportance(h);8*/
        return h;
    }



    public float computeLogicalImportance(Histogram h) {


        return 1.0f*h.support /(Math.min(h.countA, h.countB));


        /*if (hist.length==2)
            return Math.min(hist[0],hist[1]);
        else {//3
            //take minimum
            int[]sorted = hist.clone();
            Arrays.sort(sorted);
            return (sorted[0]+sorted[1])/support;
        }
*/
    }





    /**
     * Computes impurity index with elements of the histogram
     * @return the computed purity Index (range 0-1)
     */
    public float computeImpurityIndex() {
        int size = hist.length;
        float sum = 0;
        for (int i=0; i<size; i++) {
            float prob = (float)hist[i]/numSamples;
            sum += prob*prob;
        }
        return 1.0f - sum;
    }

    public float computeImpurityNormIndex() {
        int size = hist.length;
        float impurity = computeImpurityIndex();
        float normFactor = 1.0f - 1.0f / size;

        //Normalize
        return impurity/normFactor;
    }

    public float computeImportance(int maxSamples) {
        return (1.0f - computeImpurityNormIndex())*numSamples/maxSamples;
    }

    public float computeImportance2(float jaccard) {
        float a =  1.0f - computeImpurityNormIndex();
        float b = jaccard;
        //return 2*(a*b)/(a+b);
        return a*b;
    }

    /**
     * @return the number of inserted samples
     */
    public int getNumSamples() { return numSamples; }

    /**
     * @return occurrences vector
     */
    public int[] getOccurrencesVector() { return hist; }
}
