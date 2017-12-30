package fr.free.homebank.mobile.util;

import org.apache.http.NameValuePair;
import org.apache.http.util.LangUtils;

/**
 * Created by wolfgang on 2/2/14.
 */
public class ContentValues implements NameValuePair, Cloneable {

    private final String name;
    private final String value;

    /**
     * Default Constructor taking a name and a value. The value may be null.
     *
     * @param name The name.
     * @param value The value.
     */
    public ContentValues(final String name, final String value) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return this.name;
    }

    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object instanceof NameValuePair) {
            ContentValues that = (ContentValues) object;
            return this.name.equals(that.name)
                    && LangUtils.equals(this.value, that.value);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.name);
        hash = LangUtils.hashCode(hash, this.value);
        return hash;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}