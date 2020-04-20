package timeseries.languages.interval;

import java.io.Serializable;

import timeseries.languages.Ontology;

public abstract class QualitativeIntervalRelation extends IntervalRelation implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 8709481043942140550L;
	
	/**
	 * Default constructor that sets the measure type.
	 */
	public QualitativeIntervalRelation() {
		m_measureType = QUALITATIVE;
	}

	/**
	 * Function for checking if a given relation holds between a pair of
	 * ontologies.
	 * 
	 * @param ontology1 world actually on
	 * @param ontology2 world to be checked
	 * @return true if it holds
	 */
	public abstract boolean checkRelation(Ontology ontology1, Ontology ontology2);
}
