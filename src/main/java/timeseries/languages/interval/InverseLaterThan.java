package timeseries.languages.interval;

import java.util.ArrayList;

import timeseries.languages.Ontology;

public class InverseLaterThan extends QualitativeIntervalRelation {
	
	private static final long serialVersionUID = -1946068865115441218L;

	public InverseLaterThan() {
		m_relationName = "InverseLaterThan";
	}
	
	@Override
	public String existentialRelation() {
		return "<InvL>";
	}
	
	@Override
	public String universalRelation() {
		return "[InvL]";
	}
	
	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If y2 < x1 	==> true
		 * Otherwise	==> false
		 */
		return i2.getEnd() < i1.getStart() ? true : false; 
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
		
		for (int x = 1; x < start-1; x++)
			for (int y = x+1; y < start; y++)
				intervals.add(new Interval(x,y));
		
		return intervals;
	}
	
	public static void main(String[] argv) {
		
		IntervalRelation rel = new InverseLaterThan();
		Interval reference = new Interval(4,5);
		
		ArrayList<Interval> intervals = rel.getIntervals(reference, 5);
		
		System.out.println(rel.getRelationName() + " ==> on " + reference + ":");
		for (Interval interval : intervals) 
			System.out.println(interval);
	}
}
