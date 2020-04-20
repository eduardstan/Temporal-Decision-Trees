package timeseries.languages.interval;

import java.io.Serializable;
import java.util.ArrayList;

import timeseries.languages.Ontology;

public class MetricDuringGE extends MetricIntervalRelation implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 2934954356939760466L;

	public MetricDuringGE() {
		m_relationName = "MetricDuringGE";
	}
	
	@Override
	public String existentialRelation() {
		return "<D len >= " + getIntervalLength() + ">";
	}
	
	@Override
	public String universalRelation() {
		return "[D len >= " + getIntervalLength() + "]";
	}

	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2, int length) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If x1 < x2 and y2 < y1 	==> true
		 * Otherwise				==> false
		 */
		return i1.getStart() < i2.getStart() && i2.getEnd() < i1.getEnd() && i2.getEnd() - i2.getStart() + 1 >= length ? true : false;
	}

	/**
	 * Function computing the intervals.
	 * 
	 * @param reference current world (i.e., reference) to start from
	 * @param maxPoint maximum point in the domain
	 * 
	 * @return computed list of intervals
	 */
	@Override
	public ArrayList<Interval> getIntervals(Interval reference, int maxPoint) {
		
		int start = reference.getStart();
		int end = reference.getEnd();
		ArrayList<Interval> intervals = new ArrayList<Interval>();
		
		for (int x = start + 1; x < end-1; x++)
			for (int y = x+1; y < end; y++)
				intervals.add(new Interval(x,y));
		
		return intervals;
	}
}
