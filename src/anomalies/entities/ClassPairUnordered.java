package anomalies.entities;


import java.io.Serializable;

/**
 * Created by XauthorX on 03/06/17.
 * Unordered Pair of classes: <a,b>.
 * <a,b> == <a,b>
 * <a,b> == <b,a>
 * To be used as HashMap key.
 */
public class ClassPairUnordered implements Serializable {
    public String a;
    public String b;

    public ClassPairUnordered(String a, String b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof ClassPairUnordered) {
            ClassPairUnordered thatT = (ClassPairUnordered) that;
            //a a, b b
            if (a.equals(thatT.a) &&
                    b.equals(thatT.b))
                return true;
            //a b, a b
            if (a.equals(thatT.b) &&
                    b.equals(thatT.a))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + a.hashCode() + b.hashCode();//the same for a,b or b,a
        return result;
    }

    /**
     * Duplicates this object
     */
    public ClassPairUnordered clone() {
        ClassPairUnordered res = new ClassPairUnordered(a, b);
        return res;
    }

    @Override
    public String toString() {
        return "<"+a+", "+b+">";
    }
}
