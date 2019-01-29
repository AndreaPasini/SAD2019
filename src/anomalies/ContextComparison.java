package anomalies;


public class ContextComparison {

    private String subject;
    private String relType;
    private String reference;
    private String relValue;

    private float confidence;
    
    private int subjectId;
    private int referenceId;
    
    public ContextComparison(String subject, String relType, String reference, String relValue, float confidence, int subjectId, int referenceId) {
        this.subject = subject;
        this.relType = relType;
        this.reference = reference;
        this.relValue = relValue;
        
        this.confidence = confidence;
        
        this.subjectId = subjectId;
        this.referenceId = referenceId;
    }

   
    @Override
    public String toString() {
        return "<" + subject + " " + relType + " " + reference + "> " + relValue + ": " + confidence;
    }

    public String getSubject() {
        return subject;
    }

    public String getRelType() {
        return relType;
    }

    public String getReference() {
        return reference;
    }

    public String getRelValue() {
        return relValue;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getReferenceId() {
        return referenceId;
    }
}
