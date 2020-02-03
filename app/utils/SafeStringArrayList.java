package utils;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;

/**
 * Just like an ArrayList but better.  Escapes any characters that would be unsafe to display in an html page before
 * storing Strings in the list.
 */
public class SafeStringArrayList extends ArrayList {

    @Override
    public boolean add(Object o) {
        return super.add(sanitize(o));
    }

    @Override
    public Object set(int i, Object o) {
        return super.set(i, sanitize(o));
    }

    @Override
    public void add(int i, Object o) {
        super.add(i, sanitize(o));
    }

    private Object sanitize(Object o){
        if(o != null && o instanceof String){
            o = StringEscapeUtils.escapeHtml((String) o);
        }

        return o;
    }
}
