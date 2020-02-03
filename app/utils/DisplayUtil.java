package utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import play.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * General purpose utility for displaying "stuff" Prefer open source libraries that are already included in the project
 * before writing a new method here.  These should really just be one-off very specific utilities
 */
public class DisplayUtil {

    /**
     * Builds a properly punctuated list of items.  Null elements will not be printed
     * Empty List: just returns empty string
     * Single Element List: just returns the toString() value of the single element
     * Exactly 2 elements: element1 and element2
     * n > 2 elements: element1, element2, and elementN
     * @param
     * @return
     */
    public static String listAsProperlyPunctuatedEnglish(List list){
        return listAsProperlyPunctuatedEnglish(list, null);
    }


    /**
     * Overloaded version of listAsProperlyPunctuatedEnglish with invoke the accessor (getter) method for the specified
     * field
     */
    public static String listAsProperlyPunctuatedEnglish(List list, String fieldName){
        if (list == null){
            throw new IllegalArgumentException("Argument: list cannot be null");
        }

        //remove any null elements
        list.removeAll(Collections.singleton(null));

        if (list.isEmpty()){
            return "";
        }

        final int size = list.size();


        if (size == 1){
            return getFieldOrCallToString(list.get(0), fieldName);
        }

        if (size == 2){
            return getFieldOrCallToString(list.get(0), fieldName) + " and " +
                    getFieldOrCallToString(list.get(1), fieldName);
        }

        //pop the last element off the list to modify it separately
        Object lastElement = list.remove(size - 1);
        String lastElementString = ", and " + getFieldOrCallToString(lastElement, fieldName);

        List<String> stringifiedList = new ArrayList<String>();
        for (Object o : list) {
            stringifiedList.add(getFieldOrCallToString(o, fieldName));
        }

        String firstNElements = StringUtils.join(stringifiedList, ", ");

        return firstNElements + lastElementString;

    }

    /**
     * This fancy little method will take a String argument and shrink it down to size.  If the String argument is null
     * or empty, just returns an empty string.  If the String argument is less than the maxLength specified the String
     * will be returned as-is.  The real magic happens when the String is longer than the maxLength.  In that case it
     * trims it down so that the first part of the String + an ellipsis (...) is the length of the maxLengthSpecified
     * and returns that value.
     * @param text The String value to shorten if necessary
     * @param maxLength The maximum length that the returned value should be
     * @return String
     */
    public static String getShortenedString(String text, int maxLength){
        if(StringUtils.isEmpty(text)){
            return "";
        }

        if(text.length() <= maxLength){
            return text;
        }

        return text.substring(0, maxLength-3) + "...";
    }

    private static String getFieldOrCallToString(Object object, String fieldName){
        try {
            if(fieldName != null){
                return FieldUtils.readDeclaredField(object, fieldName).toString();
            } else {
                return object.toString();
            }
        } catch (Exception e) {
            Logger.error("Could not get property " + fieldName + " on object " + object, e);
            return null;
        }
    }

}
