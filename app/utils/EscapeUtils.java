package utils;

/**
 * <p>A collect of methods for escaping and sanitizing inputs.</p>
 */
public class EscapeUtils {
    
    public static String safeSQLLikeString(String original) {
        
        //We want to behave correctly even if the original contains special
        //character that fall at the beginning or end of the string
        original = " " + original + " ";
        
        //We do this first so backslashes introduced by the next two are not
        //escaped
        String[] qSplitSlash = original.split("\\\\");  //Split on backslash
        original = 
                org.apache.commons.lang.StringUtils.join(qSplitSlash, "\\\\");
        
        String[] qSplitPercent = original.split("%");
        original = 
                org.apache.commons.lang.StringUtils.join(qSplitPercent, "\\%");
        
        String[] qSplitUnderscore = original.split("_");
        original = org.apache.commons.lang.StringUtils.join(
                    qSplitUnderscore, "\\_");
        
        //Trim out the spaces we added above and then return
        return original.substring(1, original.length() - 1);
    }
}
