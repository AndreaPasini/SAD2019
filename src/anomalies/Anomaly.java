/**
 * Author: XauthorX
 * 18/12/17
 */
package anomalies;

import processing.Relationships;
import anomalies.entities.SmartTriplet;

public class Anomaly {

    private SmartTriplet triplet;
    private float confidence;
    private Relationships.RelValue relValue;
    private int subjectId;
    private int referenceId;

    public Anomaly(SmartTriplet triplet, Relationships.RelValue relValue, float confidence, int subjectId, int referenceId) {
        this.triplet = triplet;
        this.confidence = confidence;
        this.relValue = relValue;
        this.subjectId = subjectId;
        this.referenceId = referenceId;
    }

    public SmartTriplet getTriplet() { return triplet; }

    public float getConfidence() { return confidence; }

    public Relationships.RelValue getRelValue() { return relValue; }

    public int getSubjectId() { return subjectId; }

    public int getReferenceId() { return referenceId; }

    @Override
    public String toString() {
        return triplet + " " + relValue + ": " + confidence;
    }
}
