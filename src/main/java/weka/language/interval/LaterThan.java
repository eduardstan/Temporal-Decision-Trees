package weka.language.interval;

import java.util.ArrayList;

import weka.language.Ontology;

public class LaterThan extends IntervalRelation {

	private static final long serialVersionUID = 6012652365860006433L;

	public LaterThan() {
		m_relationName = "LaterThan";
		m_relationNumber = 1;
		m_relationType = INTERVAL;
	}
	
	@Override
	public String existentialRelation() {
		return "<L>";
	}
	
	@Override
	public String universalRelation() {
		return "[L]";
	}
	
	@Override
	public boolean checkRelation(Ontology ontology1, Ontology ontology2) {
		Interval i1 = (Interval) ontology1;
		Interval i2 = (Interval) ontology2;
		
		/**
		 * Let i1 = [x1,y1] and i2 = [x2,y2]
		 * 
		 * If y1 < x2 	==> true
		 * Otherwise	==> false
		 */
		return i1.getEnd() < i2.getStart() ? true : false; 
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
		
		int end = reference.getEnd();
		ArrayList<Interval> intervals = new ArrayList<Interval>();
		
		for (int x = end + 1; x < maxPoint; x++)
			for (int y = x+1; y <= maxPoint; y++)
				intervals.add(new Interval(x,y));
		
		return intervals;
	}
	
	public static void main(String[] argv) {
		
		IntervalRelation rel = new LaterThan();
		Interval reference = new Interval(0,1);
		
		ArrayList<Interval> intervals = rel.getIntervals(reference, 5);
		
		System.out.println(rel.getRelationName() + " ==> on " + reference + ":");
		for (Interval interval : intervals) 
			System.out.println(interval);
	}
}
