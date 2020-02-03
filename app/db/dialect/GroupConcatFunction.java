package db.dialect;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class GroupConcatFunction implements SQLFunction {

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public boolean hasParenthesesIfNoArguments() {
		return true;
	}

	@Override
	public Type getReturnType(Type firstArgumentType, Mapping mapping)
			throws QueryException {
		return StandardBasicTypes.STRING;
	}

	@Override
	public String render(Type firstArgumentType, List arguments,
			SessionFactoryImplementor factory) throws QueryException {
		if (arguments.size() != 1) {
			throw new QueryException(new IllegalArgumentException(
					"group_concat should have one arg"));
		}
		return "group_concat(" + arguments.get(0) + ")";
	}
}