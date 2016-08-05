package com.sulai.gui;

public class UIProperty<T> {

	private T property;

	private PropertyObserver o;

	public abstract class PropertyObserver {
		public abstract void propertyChange(T property);
	}

	public void onChange(PropertyObserver o) {
		this.o = o;
	}

	public T get() {
		if (o != null)
			o.propertyChange(property);
		return property;
	}

	public void set(T property) {
		if (o != null)
			o.propertyChange(property);
		this.property = property;
	}

}
