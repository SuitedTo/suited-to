package logic.questions.finder;

import java.util.List;

public interface ListQualifier <C,T,W> {

	/**
	 * Determine whether or not the given applicant is allowed to
	 * become a member of winners.
	 * 
	 * @param applicant
	 * @param list
	 * @return
	 */
	public boolean qualifies(C context,T applicant, W winners);
}
