package models.embeddable;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Name implements Comparable<Name>, Serializable {
    public String title;
    public String first;
    public String middle;
    public String last;
    public String suffix;

    public Name() {
    }

    public Name(String title, String first, String middle, String last, String suffix) {
        this.title = title;
        this.first = first;
        this.middle = middle;
        this.last = last;
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        List<String> fields = new ArrayList<String>();
        fields.add(title);
        fields.add(first);
        fields.add(middle);
        fields.add(last);
        fields.add(suffix);

        for (int i = 0; i < fields.size(); i++) {
            String value = fields.get(i);
            String nextValue = null;
            if (i < fields.size() - 1) {
                nextValue = fields.get(i + 1);
            }

            if (value != null) {
                builder.append(value);
                if (nextValue != null) {
                    builder.append(" ");
                }
            }

        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name = (Name) o;

        if (first != null ? !first.equals(name.first) : name.first != null) return false;
        if (last != null ? !last.equals(name.last) : name.last != null) return false;
        if (middle != null ? !middle.equals(name.middle) : name.middle != null) return false;
        if (suffix != null ? !suffix.equals(name.suffix) : name.suffix != null) return false;
        if (title != null ? !title.equals(name.title) : name.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (first != null ? first.hashCode() : 0);
        result = 31 * result + (middle != null ? middle.hashCode() : 0);
        result = 31 * result + (last != null ? last.hashCode() : 0);
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Name o) {
        return (o == null) ? 0 : (last + first).compareTo(o.last + o.first);
    }
}
