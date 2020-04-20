package timeseries.languages.interval;

import java.util.ArrayList;

import timeseries.languages.Relation;

public abstract class IntervalRelation extends Relation {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -4077952282736114362L;
	
	public IntervalRelation() {
		m_relationType = INTERVAL;
	}
	
	/**
	 * Function computing the intervals.
	 * 
	 * @param reference current world (i.e., reference) to start from
	 * @param maxPoint maximum point in the domain
	 * 
	 * @return computed list of intervals
	 */
	public abstract ArrayList<Interval> getIntervals(Interval reference, int maxPoint);

	/**
	 * Existential relation.
	 * 
	 * @return existential string representation
	 */
	public abstract String existentialRelation();
	
	/**
	 * Universal relation.
	 * 
	 * @return universal string representation
	 */
	public abstract String universalRelation();
}
