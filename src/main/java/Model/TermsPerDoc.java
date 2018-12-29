package Model;

        import java.io.Serializable;

/**
 * The class is used to find the 5 most frequent essences
 */
public class TermsPerDoc implements Serializable, Comparable  {


    private int tf;
    private String value;


    public TermsPerDoc(int tf, String value) {
        this.tf = tf;
        this.value = value;

    }

    public int getTf() {
        return tf;
    }

    public String getValue() {
        return value;
    }



    @Override
    public int compareTo(Object o) {
        if (((TermsPerDoc)o).tf>this.tf)
            return -1;
        if (((TermsPerDoc)o).tf<this.tf)
            return 1;
        if (((TermsPerDoc)o).tf==this.tf)
            return 0;
        return 0;
    }

}