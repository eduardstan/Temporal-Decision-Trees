package demos;

import timeseries.languages.*;
import timeseries.languages.interval.*;

public class DemoRelations {
	
	public static void main(String[] argv) {
		
		Relation rel = new MetricLaterThanGE();
		Relation rel2 = null;
		Relation rel3 = null;
		
		((MetricIntervalRelation) rel).setIntervalLength(5);
		
		
		try {
			rel2 = (Relation) rel.clone();
			rel3 = (Relation) rel.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(((IntervalRelation) rel).universalRelation());
		System.out.println(rel);
		System.out.println(((IntervalRelation) rel2).universalRelation());
		System.out.println(rel2);
		System.out.println(((IntervalRelation) rel3).universalRelation());
		System.out.println(rel3);
	}
}
