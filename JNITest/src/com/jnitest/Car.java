package com.jnitest;

import com.jni.annotation.JNIClass;
import com.jni.annotation.JNIMethod;

// Generate a C++ managed peer class for the Java "Car" class.
@JNIClass("JNI.Test")
public class Car {

	private String mName;
	private double mCost = 17000.0;
	private int mWheels = 4;

	public Car(String name) {
		mName = name;
	}

	// Include the instance methods for getting and setting the cost in the C++ managed peer.
	@JNIMethod
	public double getCost() {
		return mCost;
	}

	@JNIMethod
	public void setCost(double cost) {
		mCost = cost;
	}

	// Exclude this method from the C++ managed peer.
	public int getWheels() {
		return mWheels;
	}

	// Include an instance method with a more complex return type in the C++ managed peer.
	@JNIMethod
	public String getName() {
		return mName;
	}

	// Include a static method in the C++ managed peer.
	@JNIMethod
	public static int getCount() {
		return 10;
	}

	public native int getColor();
}
