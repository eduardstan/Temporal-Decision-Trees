package timeseries.functions;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.WekaEnumeration;

public class Functions extends AbstractList<Function> implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 899544783086606065L;

	/**
	 * The functions.
	 */
	protected ArrayList<Function> m_functions;
	
	/**
	 * Default constructor with no parameters that creates an empty functions object.
	 */
	public Functions() {
		
		m_functions = new ArrayList<Function>();
	}
	
	/**
	 * Constructor that creates an empty functions object with a specified size.
	 * 
	 * @param capacity the size of the functions object
	 */
	public Functions(int capacity) {
		
		m_functions = new ArrayList<Function>(capacity);
	}
	
	/**
	 * Add a function to the end of the functions object. Increases the size
	 * it is not large enough.
	 * 
	 * @param function the function to be added
	 * @return true (as wanted by AbstractList)
	 */
	@Override
	public boolean add(Function function) {
		m_functions.add(function);
		return true;
	}
	
	/**
	 * Delete the function at the given position.
	 * 
	 * @param funIndex the function's position (index starts with 0)
	 */
	public void delete(int funIndex) {
		m_functions.remove(funIndex);
	}
	
	/**
	 * Delete all functions.
	 */
	public void delete() {
		m_functions = new ArrayList<Function>();
	}
	
	/**
	 * Returns an enumeration of the functions.
	 * 
	 * @return enumeration of the functions
	 */
	public Enumeration<Function> enumerateFunctions() {
		return new WekaEnumeration<Function>(m_functions);
	}

	/**
	 * Get a function at the given position.
	 * 
	 * @param funIndex the function's index (index starts with 0)
	 * @return the function at the given position
	 */
	public Function function(int funIndex) {
		return m_functions.get(funIndex);
	}
	
	/**
	 * Get a function at the given position.
	 * 
	 * @param funIndex the function's index (index starts with 0)
	 * @return the function at the given position
	 */
	@Override
	public Function get(int funIndex) {
		return m_functions.get(funIndex);
	}

	/**
	 * Get the number of functions.
	 * 
	 * @return the number of functions
	 */
	public int numFunctions() {
		return m_functions.size();
	}
	
	/**
	 * Get the number of functions.
	 * 
	 * @return the number of functions
	 */
	@Override
	public int size() {
		return m_functions.size();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
}
