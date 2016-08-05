package application;

import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;

public abstract class Test {

	public static class P<T>{
		T t;
	}
	
	public static void main(String[] args) {
		Double d = new Double(0);
		P<Double> t = new P<Double>();
		t.t = d;
		d = new Double(1);
		System.out.println(d);
	}

}
