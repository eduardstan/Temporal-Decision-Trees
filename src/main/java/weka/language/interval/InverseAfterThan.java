package weka.language.interval;

import java.util.ArrayList;

import weka.language.Ontology;

public class InverseAfterThan extends IntervalRelation {
	
	private static final long serialVersionUID = 1054093388444463174L;

	public InverseAfterThan() {
		m_relationName = "InverseAfterThan";
		m_relationNumber = 8;
		m_relationType = INTERVAL;
	}
	
	@Override
	public String existentialRelation() {
		return "<InvA>";
	}
	
	@Override
	public String universalRelation() {
		return "[InvA]";
	}
	
	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If y2 == x1 	==> true
		 * Otherwise	==> false
		 */
		return i2.getEnd() == i1.getStart() ? true : false; 
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
	
	public static void main(String[] argv) {
		
		IntervalRelation rel = new InverseAfterThan();
		Interval reference = new Interval(4,5);
		
		ArrayList<Interval> intervals = rel.getIntervals(reference, 5);
		
		System.out.println(rel.getRelationName() + " ==> on " + reference + ":");
		for (Interval interval : intervals) 
			System.out.println(interval);
	}
}
