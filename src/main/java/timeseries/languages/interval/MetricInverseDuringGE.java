package timeseries.languages.interval;

import java.io.Serializable;
import java.util.ArrayList;

import timeseries.languages.Ontology;

public class MetricInverseDuringGE extends MetricIntervalRelation implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 5607371967261890145L;

	public MetricInverseDuringGE() {
		m_relationName = "MetricInverseDuringGE";
	}
	
	@Override
	public String existentialRelation() {
		return "<InvD len >= " + getIntervalLength() + ">";
	}
	
	@Override
	public String universalRelation() {
		return "[InvD len >= " + getIntervalLength() + "]";
	}

	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2, int length) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If x2 < x1 and y1 < y2 	==> true
		 * Otherwise				==> false
		 */
		return i2.getStart() < i1.getStart() && i1.getEnd() < i2.getEnd() && i2.getEnd() - i2.getStart() + 1 >= length ? true : false; 
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
		
		for (int x = 1; x < start; x++)
			for (int y = end+1; y <= maxPoint; y++)
				intervals.add(new Interval(x,y));
		
		return intervals;
	}
}
