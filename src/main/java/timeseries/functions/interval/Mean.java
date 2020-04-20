package timeseries.functions.interval;

import weka.core.Utils;

public class Mean extends IntervalFunction {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -3622735680712678051L;

	/**
	 * Default constructor that sets the function type and the function value
	 * to Double.NaN because it has no values parameter.
	 */
	public Mean() {
		
		m_functionType = INTERVAL;
		m_functionValue = Double.NaN;
	}
	
	/**
	 * Constructor that sets the function type, and the function value computed
	 * on the given values.
	 * 
	 * @param values double array of values
	 */
	public Mean(double[] values) {
		
		m_functionType = INTERVAL;
		m_functionValue = compute(values);
	}
	
	/**
	 * Function for computing the mean.
	 * 
	 * @param values an array of doubles representing the values for a specified interval
	 * @return the mean
	 */
	@Override
	public double compute(double[] values) {
		
		return Utils.mean(values);
	}
	
}
