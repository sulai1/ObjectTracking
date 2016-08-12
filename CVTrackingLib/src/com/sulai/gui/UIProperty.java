package com.sulai.gui;

import java.util.LinkedList;

public class UIProperty<T> {

	private T property;

	private LinkedList<PropertyObserver<T>> o = new LinkedList<>();

	public UIProperty(T val) {
		set(val);
	}

	public void onChange(PropertyObserver<T> o) {
		this.o.add(o);
	}

	public T get() {
		for (PropertyObserver<T> p: o)
			p.propertyChange(property);
		return property;
	}

	public void set(T property) {
		for (PropertyObserver<T> p: o)
			p.propertyChange(property);
		this.property = property;
	}

}
