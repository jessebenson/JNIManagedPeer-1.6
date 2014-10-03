package com.jnitest;

import com.jni.annotation.JNIClass;
import com.jni.annotation.JNIMethod;

@JNIClass("JNI.Test")
public class Car {

	private String mName;
	private double mCost = 17000.0;
	private int mWheels = 4;

	public Car(String name) {
		mName = name;
	}

	@JNIMethod
	public double getCost() {
		return mCost;
	}

	@JNIMethod
	public void setCost(double cost) {
		mCost = cost;
	}

	@JNIMethod
	public int getWheels() {
		return mWheels;
	}

	@JNIMethod
	public String getName() {
		return mName;
	}

	@JNIMethod
	public static int getCount() {
		return 10;
	}

	public native int getColor();
}
