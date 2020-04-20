package weka.language.interval;

import java.util.ArrayList;

import weka.language.Ontology;

public class Equals extends IntervalRelation {
	
	private static final long serialVersionUID = 3987258363279638685L;

	public Equals() {
		m_relationName = "Equals";
		m_relationNumber = 13;
		m_relationType = INTERVAL;
	}
	
	@Override
	public String existentialRelation() {
		return "<=>";
	}
	
	@Override
	public String universalRelation() {
		return "[=]";
	}
	
	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If x1 == x2 and y1 == y2 	==> true
		 * Otherwise					==> false
		 */
		return i1.getStart() == i2.getStart() && i1.getEnd() == i2.getEnd() ? true : false;
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
		
		if (start == 0 && end == 1) return intervals;
		
		intervals.add(new Interval(start,end));
		
		return intervals;
	}
	
	public static void main(String[] argv) {
		
		IntervalRelation rel = new Equals();
		Interval reference = new Interval(0,5);
		
		ArrayList<Interval> intervals = rel.getIntervals(reference, 5);
		
		System.out.println(rel.getRelationName() + " ==> on " + reference + ":");
		for (Interval interval : intervals) 
			System.out.println(interval);
	}
}
