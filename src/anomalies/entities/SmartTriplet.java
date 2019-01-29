package anomalies.entities;

/**
 * Created by XauthorX on 15/04/17.
 * Contains a triplet <subject, relType, reference>
 * Cam be used as key for a HashMap.
 */

import processing.Relationships;

import java.io.IOException;
import java.io.Serializable;

/**
 * Contains a triplet <subject, featureType, reference>
 * Cam be used as key for a HashMap.
 */
public class SmartTriplet implements Serializable {
    //Triplet values
    public String subject;                     //the subject
    public Relationships.RelType relType;      //the relationship type
    public String reference;                   //the reference

    //Constructor
    public SmartTriplet() {
        relType = Relationships.RelType.values()[0];
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof SmartTriplet) {
            SmartTriplet thatT = (SmartTriplet) that;
            if (subject.equals(thatT.subject) &&
                    reference.equals(thatT.reference) &&
                    relType.equals(thatT.relType))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + subject.hashCode();
        result = 31 * result + reference.hashCode();
        result = 31 * result + relType.ordinal();
        return result;
    }

    //Serialization
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(subject);
        out.writeObject(reference);
        out.writeObject(relType.ordinal());
    }
    //Deserialization
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        subject = (String) in.readObject();
        reference = (String) in.readObject();
        relType = Relationships.RelType.values()[(int) in.readObject()];
    }

    /**
     * Duplicates this object
     */
    public SmartTriplet clone() {
        SmartTriplet res = new SmartTriplet();
        res.subject = subject;
        res.reference = reference;
        res.relType = relType;
        return res;
    }

    @Override
    public String toString() {
        return subject + " " + relType + " " + reference;
    }

    /**
     * Swap subject with reference.
     */
    public void swap() {
        String tmp = subject;
        subject = reference;
        reference = tmp;
    }
}
