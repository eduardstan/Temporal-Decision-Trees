package timeseries.functions;

import java.io.Serializable;

public abstract class Function implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 2328774351240632425L;
	
	/**
	 * Constant for point-based function.
	 */
	protected final int POINT = 0;
	
	/**
	 * Constant for interval-based function.
	 */
	protected final int INTERVAL = 1;
	
	/**
	 * Function type.
	 */
	protected int m_functionType;
	
	/**
	 * Function value.
	 */
	protected double m_functionValue;
	
	/**
	 * Is point-based function?
	 * 
	 * @return true if it is
	 */
	public boolean isPointType() {
		
		return m_functionType == POINT;
	}
	
	/**
	 * Is interval-based function?
	 * 
	 * @return true if it is
	 */
	public boolean isIntervalType() {
		
		return m_functionType == INTERVAL;
	}
	
	/**
	 * Get function type represented as an integer.
	 * 
	 * @return integer representation of the function type
	 */
	public int getFunctionType() {
		
		return m_functionType;
	}
	
	/**
	 * Get function value based on its semantics.
	 * 
	 * @return function value
	 */
	public double getFunctionValue() {
		
		return m_functionValue;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
