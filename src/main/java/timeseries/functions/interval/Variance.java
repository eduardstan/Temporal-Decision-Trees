package timeseries.functions.interval;

import weka.core.Utils;

public class Variance extends IntervalFunction {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -5874452985616426350L;

	/**
	 * Default constructor that sets the function type and the function value
	 * to Double.NaN because it has no values parameter.
	 */
	public Variance() {
		
		m_functionType = INTERVAL;
		m_functionValue = Double.NaN;
	}
	
	/**
	 * Constructor that sets the function type, and the function value computed
	 * on the given values.
	 * 
	 * @param values double array of values
	 */
	public Variance(double[] values) {
		
		m_functionType = INTERVAL;
		m_functionValue = compute(values);
	}
	
	/**
	 * Function for computing the variance.
	 * 
	 * @param values an array of doubles representing the values for a specified interval
	 * @return the variance
	 */
	@Override
	public double compute(double[] values) {
		
		return Utils.variance(values);
	}
}
