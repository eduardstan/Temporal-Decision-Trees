package timeseries.functions.interval;

import weka.core.Utils;

public class Kurtosis extends IntervalFunction {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 6757235149153448670L;

	/**
	 * Default constructor that sets the function type and the function value
	 * to Double.NaN because it has no values parameter.
	 */
	public Kurtosis() {
		
		m_functionType = INTERVAL;
		m_functionValue = Double.NaN;
	}
	
	/**
	 * Constructor that sets the function type, and the function value computed
	 * on the given values.
	 * 
	 * @param values double array of values
	 */
	public Kurtosis(double[] values) {
		
		m_functionType = INTERVAL;
		m_functionValue = compute(values);
	}
	
	/**
	 * Function for computing the kurtosis.
	 * 
	 * @param values an array of doubles representing the values for a specified interval
	 * @return the kurtosis
	 */
	@Override
	public double compute(double[] values) {
		
		double mean = Utils.mean(values);
		double variance = Utils.mean(values);
		double standardDeviation = Math.sqrt(variance);
		double accumulator = 0.0;
		
		for (int i = 0; i < values.length; i++)
			accumulator += Math.pow((values[i] - mean)/standardDeviation, 4);
		
		return accumulator / values.length;
	}
}
