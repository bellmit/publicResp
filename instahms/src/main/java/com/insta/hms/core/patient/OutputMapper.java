package com.insta.hms.core.patient;


public abstract class OutputMapper<I, O> /*implements DataMapper*/ {

	// @Override
	public abstract O map(I input);
}
