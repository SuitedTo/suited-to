package logic.questions.finder;

import java.util.List;

/**
 * This interface provides a way for any list building mechanism
 * to know when to stop building.
 * 
 * @author joel
 *
 * @param <T>
 */
public interface ListStopper <C,T,W> extends ListQualifier<C,T,W>{
	
	
	/**
	 * @return This is the absolute cap - return a number that
	 * represents the most you could possibly need. You want this
	 * number to be big enough but as small as possible to prevent
	 * 
	 */
	public int cap();
	
}
