package com.sulai.gui;

/**
 * UI is a abstraction layer for the user interface
 * 
 * @author sascha
 *
 */
public abstract class UI {

	public abstract void init(String args);

	public abstract void createUIDouble(int index, String name, UIProperty<Double> property, double min, double max);

	public abstract void createUIBool(int index, String name, UIProperty<Boolean> property);

}
