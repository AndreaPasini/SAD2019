package anomalies.entities;


import java.io.Serializable;

/**
 * Created by XauthorX on 15/04/17.
 * Class to store a histogram and its importance measures.
 */
public class Histogram implements Serializable {
    public float[] histValues;
    public int support;
    public int countA;
    public int countB;

    /**
     * Gets the values of this histogram, in the form of integer numbers.
     * @return an array with the values.
     */
    public int[] getHistCountValues() {
        int[] res = new int[histValues.length];
        for (int i=0; i<histValues.length; i++)
            res[i] = (int)(histValues[i]* support);
        return res;
    }

    /**
     * Swap element i with element j.
     */
    public void swap(int i, int j) {
        float tmp = histValues[i];
        histValues[i]=histValues[j];
        histValues[j]=tmp;
    }
}
