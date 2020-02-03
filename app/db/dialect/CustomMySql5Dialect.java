package db.dialect;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class CustomMySql5Dialect extends MySQL5InnoDBDialect{

	public CustomMySql5Dialect() {
		super();
		registerFunction("group_concat", new GroupConcatFunction());
		registerFunction("distinct_group_concat", new DistinctGroupConcatFunction());
	}

}
