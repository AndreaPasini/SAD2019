package anomalies.entities;

import java.io.Serializable;

/**
 * Created by XauthorX on 24/08/17.
 */
public class Counter implements Serializable {
    private long numOccurrences;

    /** Add one occurrence for this element **/
    public void addOccurrence() {
        numOccurrences++;
    }

    //Getters

    /** Get the number of occurrences **/
    public long getNumOccurrences() {return numOccurrences;}

}