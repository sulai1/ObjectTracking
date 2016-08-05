package com.sulai.gui;

public abstract class UINode<T> {

	public abstract <X extends  UINode<?>>void add(X node);
	public abstract <X extends  UINode<?>> void remove(X node);
	public abstract UIProperty<T> getProperty();
}
