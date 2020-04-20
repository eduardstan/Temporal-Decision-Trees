package timeseries.languages.interval;

import timeseries.languages.Ontology;

public class Interval extends Ontology {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 3814194750524070033L;

	/**
	 * Interval-based ontology type.
	 */
	protected final int m_ontologyType = INTERVAL;
	
	/**
	 * Start point.
	 */
	protected int m_start;
	
	/**
	 * End point.
	 */
	protected int m_end;
	
	/**
	 * Constructor for interval-based ontology.
	 * 
	 * @param start start point
	 * @param end end point
	 */
	public Interval(int start, int end) {
		m_start = start;
		m_end = end;
		super.m_ontologyType = INTERVAL;
	}
	
	/**
	 * Get start point of interval.
	 * 
	 * @return start point
	 */
	public int getStart() {
		return m_start;
	}
	
	/**
	 * Set start point of interval.
	 * 
	 * @param start start point
	 */
	public void setStart(int start) {
		m_start = start;
	}
	
	/**
	 * Get end point of interval.
	 * 
	 * @return end point
	 */
	public int getEnd() {
		return m_end;
	}
	
	/**
	 * Set end point of interval.
	 * 
	 * @param start end point
	 */
	public void setEnd(int end) {
		m_end = end;
	}
	
	/**
	 * Get ontology type.
	 * 
	 * @return integer representing the type
	 */
	@Override
	public int getOntologyType() {
		return m_ontologyType;
	}
	
	/**
	 * String representation.
	 */
	@Override
	public String toString() {
		return "[" + Integer.toString(m_start) + "," + Integer.toString(m_end) + "]";
	}
}
