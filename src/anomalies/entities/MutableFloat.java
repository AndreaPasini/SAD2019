package anomalies.entities;

public class MutableFloat {
    private float val;

    public MutableFloat(float init) {
        this.val=init;
    }

    public MutableFloat() {
        this.val=0;
    }

    public void add(float val) {
        this.val+=val;
    }

    public float get() {
        return val;
    }
}
