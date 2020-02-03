package utils;

import java.util.ArrayList;
import java.util.List;

public class ListDiff<T>{
	private final List<T> previousValues;
	private final List<T> nextValues;
	private final List<T> intersection;
	
	
	public ListDiff(List<T> previousValues, List<T> nextValues){
		this.previousValues = (previousValues == null)?new ArrayList<T>():previousValues;
		this.nextValues = (nextValues == null)?new ArrayList<T>():nextValues;
		intersection = new ArrayList<T>(nextValues);
		intersection.retainAll(previousValues);
	}
	
	public List<T> added(){
		List<T> result = new ArrayList<T>(nextValues);
		result.removeAll(intersection);
		return result;
		
	}
	
	public List<T> removed(){
		List<T> result = new ArrayList<T>(previousValues);
		result.removeAll(intersection);
		return result;
	}
}

