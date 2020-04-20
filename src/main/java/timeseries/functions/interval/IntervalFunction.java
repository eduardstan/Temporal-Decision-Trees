package timeseries.functions.interval;

import timeseries.functions.Function;

public abstract class IntervalFunction extends Function {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 5114132636500901259L;
	
	/**
	 * Function for computing whatever the function computes.
	 * 
	 * @param values an array of doubles representing the values for a specified interval
	 * @return the computed metric
	 */
	public abstract double compute(double[] values);
	
}
