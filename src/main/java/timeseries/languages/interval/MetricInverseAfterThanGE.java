package timeseries.languages.interval;

import java.io.Serializable;
import java.util.ArrayList;

import timeseries.languages.Ontology;

public class MetricInverseAfterThanGE extends MetricIntervalRelation implements Serializable {
	
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 5496238403420993902L;

	public MetricInverseAfterThanGE() {
		m_relationName = "MetricInverseAfterThanGE";
	}
	
	@Override
	public String existentialRelation() {
		return "<InvA len >= " + getIntervalLength() + ">";
	}
	
	@Override
	public String universalRelation() {
		return "[InvA len >= " + getIntervalLength() + "]";
	}

	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2, int length) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If y2 == x1 	==> true
		 * Otherwise	==> false
		 */
		return i2.getEnd() == i1.getStart() && i2.getEnd() - i2.getStart() + 1 >= length ? true : false; 
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
		ArrayList<Interval> intervals = new ArrayList<Interval>();
		
		for (int x = 1; x < start; x++)
			intervals.add(new Interval(x,start));
		
		return intervals;
	}
}
