package anomalies.entities;

import java.io.Serializable;

/**
 * Created by XauthorX on 03/06/17.
 * Class to build a probability value, incrementally.
 */
public class Probability implements Serializable {
    private int numOccurrences;
    private float probability;

    /** Add one occurrence for this element **/
    public void addOccurrence() {
        numOccurrences++;
    }

    /**
     * Compute the probability value: numOccurrences/totalNumOccurrences
     * @param totalNumOccurrences total number of occurrences.
     */
    public void computeProbability(int totalNumOccurrences) {
        probability = (float)numOccurrences/totalNumOccurrences;
    }

    //Getters

    /** Get the number of occurrences **/
    public int getNumOccurrences() {return numOccurrences;}
    /** Get the probability value **/
    public float getProbability() {return probability;}
}
