package timeseries.languages.interval;

import java.util.ArrayList;

import timeseries.languages.Ontology;

public class AfterThan extends QualitativeIntervalRelation {
	
	private static final long serialVersionUID = -464187215455062779L;

	public AfterThan() {
		m_relationName = "AfterThan";
	}
	
	@Override
	public String existentialRelation() {
		return "<A>";
	}
	
	@Override
	public String universalRelation() {
		return "[A]";
	}
	
	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If y1 == x2 	==> true
		 * Otherwise	==> false
		 */
		return i1.getEnd() == i2.getStart() ? true : false; 
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
		
		int x = reference.getEnd();
		ArrayList<Interval> intervals = new ArrayList<Interval>();
		
		for (int y = x+1; y <= maxPoint; y++)
			intervals.add(new Interval(x,y));
		
		return intervals;
	}
	
	public static void main(String[] argv) {
		
		IntervalRelation rel = new AfterThan();
		Interval reference = new Interval(0,1);
		
		ArrayList<Interval> intervals = rel.getIntervals(reference, 5);
		
		System.out.println(rel.getRelationName() + " ==> on " + reference + ":");
		
		for (Interval interval : intervals) 
			System.out.println(interval);
	}
}
