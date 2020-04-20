package timeseries.languages.interval;

import java.io.Serializable;
import java.util.ArrayList;

import timeseries.languages.Ontology;

public class MetricOverlapsGE extends MetricIntervalRelation implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -7750398481652952196L;

	public MetricOverlapsGE() {
		m_relationName = "MetricOverlapsGE";
	}
	
	@Override
	public String existentialRelation() {
		return "<O len >= " + getIntervalLength() + ">";
	}
	
	@Override
	public String universalRelation() {
		return "[O len >= " + getIntervalLength() + "]";
	}

	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2, int length) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If x1 < x2, x2 < y1 and y1 < y2 	==> true
		 * Otherwise						==> false
		 */
		return i1.getStart() < i2.getStart() && i2.getStart() < i1.getEnd() && i1.getEnd() < i2.getEnd() && i2.getEnd() - i2.getStart() + 1 >= length ? true : false;
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
		
		for (int x = start+1; x < end; x++) 
			for (int y = end+1; y <= maxPoint; y++)
				intervals.add(new Interval(x,y));
		
		return intervals;
	}
}

