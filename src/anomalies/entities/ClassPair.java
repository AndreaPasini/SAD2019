package anomalies.entities;


import java.io.Serializable;

/**
 * Created by XauthorX on 03/06/17.
 * Ordered Pair of classes: <a,b>.
 * <a,b> == <a,b>
 * <a,b> != <b,a>
 * To be used as HashMap key.
 */
public class ClassPair implements Serializable {
    public String a;
    public String b;

    public ClassPair(String a, String b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof ClassPair) {
            ClassPair thatT = (ClassPair) that;
            if (a.equals(thatT.a) &&
                    b.equals(thatT.b))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + a.hashCode();
        result = 31 * result + b.hashCode();
        return result;
    }

    /**
     * Duplicates this object
     */
    public ClassPair clone() {
        ClassPair res = new ClassPair(a, b);
        return res;
    }

    @Override
    public String toString() {
        return "<"+a+", "+b+">";
    }
}
