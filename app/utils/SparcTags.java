    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import controllers.Security;
import enums.AccountLimitedAction;
import groovy.lang.Closure;
import models.User;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;
import play.templates.TagContext;

import java.io.PrintWriter;
import java.util.Map;

/**
 *
 * @author hamptos
 */
public class SparcTags extends FastTags {
    
    /**
     * <p>Formats some HTML as a Javascript String value.  Useful for working
     * with jQuery, where HTML is often expected to be formatted as a String.
     * Newlines will be replaced with &lt;br/&gt;s and other characters that
     * cannot appear in a Javascript String will be escaped.</p>
     * 
     * <p>Note that the result of this tag is a String value, not the contents
     * of a String value.  So the proper use is:</p>
     * 
     * <p><code>var someString = #{jsString}&lt;div&gt;Some html&lt;div&gt;#{/jsString}</code></p>
     * 
     * <p>Rather than:</p>
     * 
     * <p><code>var someString = "#{jsString}&lt;div&gt;Some html&lt;div&gt;#{/jsString}"</code></p>
     */
    public static void _jsString(Map<?, ?> args, Closure body, 
                PrintWriter out, ExecutableTemplate template, int fromLine) {
        
        out.print("\"" + JavaExtensions.escapeJavaScript(
                JavaExtensions.toString(body).replace("\n", "<br/>")) + "\"");

    }

    /**
     * <p>Permits the conditional inclusion of some template code based on
     * whether or not the account type associated with the currently-logged-in
     * user is permitted to consume one more of a limited resource.  For
     * example, the "Add candidate" button can be made to display only if the 
     * account associated with the currently logged in user hasn't used all its 
     * permitted candidates.</p>
     * 
     * <p>If no user is presently logged in, this tag behaves exactly as if 
     * there <em>were</em> a logged-in user and that user's account does not
     * permit the given action.</p>
     * 
     * <p>This tag interacts with the #{else} tag such that the following else
     * block will be executed if the condition fails.</p>
     * 
     * <p><code>args</code> must include a mapping for <code>arg</code> (which
     * corresponds to the first, unnamed argument of a tag) that contains a
     * {@link enums.AccountLimitedAction} specifying which action must be 
     * permissible for the body to be included.</p>
     */
    public static void _accountAllows(Map<?, ?> args, Closure body, 
                PrintWriter out, ExecutableTemplate template, int fromLine) {
        
        if (!args.containsKey("arg")) {
            throw new RuntimeException("accountAllows tag expects a single " +
                    "parameter.");
        }
        
        Object objectAction = args.get("arg");
        
        if (!(objectAction instanceof AccountLimitedAction)) {
            throw new RuntimeException("Parameter must be an " +
                    "AccountLimitedAction.");
        }
        
        AccountLimitedAction action = (AccountLimitedAction) objectAction;

        final User connectedUser = Security.connectedUser();
        if (connectedUser != null &&
                action.canPerform((models.AccountHolder) connectedUser)) {
            
            body.call();
            TagContext.parent().data.put("_executeNextElse", false);
        }
        else {
            TagContext.parent().data.put("_executeNextElse", true);
        }
    }
    
    private static <T> T getArgument(String name, Class<T> expectedType, 
                Map<?, ?> args) {
        
        if (!args.containsKey(name)) {
            throw new RuntimeException("Expected argument named '" + name + 
                    "'.");
        }
        
        Object arg = args.get(name);
        
        if (!expectedType.isAssignableFrom(arg.getClass())) {
            throw new RuntimeException("Argument '" + name + "' must be of " +
                    "type " + expectedType + ".");
        }
        
        return (T) arg;
    }
}
