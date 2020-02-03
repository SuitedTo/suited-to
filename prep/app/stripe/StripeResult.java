package stripe;

import java.util.concurrent.ExecutionException;

import play.libs.F.Promise;

public class StripeResult<T> extends Promise<T>{
	
	protected void setResult(T result){
		super.invoke(result);
	}
	
	protected void setException(Exception exception){
		super.invokeWithException(exception);
	}
	
	public T get() {
		try {
			return super.get();
		} catch (InterruptedException e) {
			throw new RuntimeException();
		} catch (ExecutionException e) {
			throw new RuntimeException();
		}
	}
}
