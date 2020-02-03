package data.binding;

import data.binding.types.*;
import models.embeddable.PhoneNumber;
import play.data.binding.Binder;

public final class Binders {

	public static void registerAll(){
		
		Binder.register(PhoneNumber.class, new PhoneNumberBinder());
	}
}
