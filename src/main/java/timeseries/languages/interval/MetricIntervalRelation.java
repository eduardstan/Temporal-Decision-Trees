package timeseries.languages.interval;

import java.io.Serializable;

import timeseries.languages.Ontology;

public abstract class MetricIntervalRelation extends IntervalRelation implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -5887501785686799147L;
	
	/**
	 * Interval length.
	 */
	protected int m_intervalLength;
	
	/**
	 * Default constructor that sets the measure type.
	 */
	public MetricIntervalRelation() {
		m_measureType = METRIC;
	}
	
	/**
	 * Function for checking if a given relation holds between a pair of
	 * ontologies.
	 * 
	 * @param ontology1 world actually on
	 * @param ontology2 world to be checked
	 * @param length the length of the interval to be checked
	 * @return true if it holds
	 */
	public abstract boolean checkRelation(Ontology ontology1, Ontology ontology2, int length);
	
	/**
	 * Set interval length.
	 * 
	 * @param length interval length
	 */
	public void setIntervalLength(int length) {
		m_intervalLength = length;
	}
	
	/**
	 * Get interval length.
	 * 
	 * @return interval length
	 */
	public int getIntervalLength() {
		return m_intervalLength;
	}
	
}
