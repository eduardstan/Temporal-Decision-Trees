package weka.language.interval;

import java.util.ArrayList;

import weka.language.Ontology;

public class Overlaps extends IntervalRelation {
	
	private static final long serialVersionUID = 6231304366769877673L;

	public Overlaps() {
		m_relationName = "Overlaps";
		m_relationNumber = 3;
		m_relationType = INTERVAL;
	}
	
	@Override
	public String existentialRelation() {
		return "<O>";
	}
	
	@Override
	public String universalRelation() {
		return "[O]";
	}
	
	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If x1 < x2, x2 < y1 and y1 < y2 	==> true
		 * Otherwise						==> false
		 */
		return i1.getStart() < i2.getStart() && i2.getStart() < i1.getEnd() && i1.getEnd() < i2.getEnd() ? true : false;
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
	
	public static void main(String[] argv) {
		
		IntervalRelation rel = new Overlaps();
		Interval reference = new Interval(0,3);
		
		ArrayList<Interval> intervals = rel.getIntervals(reference, 5);
		
		System.out.println(rel.getRelationName() + " ==> on " + reference + ":");
		for (Interval interval : intervals) 
			System.out.println(interval);
	}
}
