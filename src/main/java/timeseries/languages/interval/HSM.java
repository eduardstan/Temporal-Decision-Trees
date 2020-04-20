package timeseries.languages.interval;

import timeseries.languages.Language;
import timeseries.languages.Relation;
import timeseries.languages.Relations;

import java.util.ArrayList;

public class HSM extends IntervalLanguage {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -2837151196914998838L;

	/**
	 * Constructor for HS interval-based language.
	 * 
	 * @param relationsMask relations' mask to compute activated relations
	 */
	public HSM(String relationsMask) {
		
		m_languageName = "HSMetric";
		
		m_nameAllRelations = new String[]{
				"MetricLaterThanGE",
				"MetricLaterThanLE",
				"MetricAfterThanGE",
				"MetricAfterThanLE",
				"MetricOverlapsGE",
				"MetricOverlapsLE",
				"MetricEndsGE",
				"MetricEndsLE",
				"MetricDuringGE",
				"MetricDuringLE",
				"MetricBeginsGE",
				"MetricBeginsLE",
				"MetricInverseLaterThanGE",
				"MetricInverseLaterThanLE",
				"MetricInverseAfterThanGE",
				"MetricInverseAfterThanLE",
				"MetricInverseOverlapsGE",
				"MetricInverseOverlapsLE",
				"MetricInverseEndsGE",
				"MetricInverseEndsLE",
				"MetricInverseDuringGE",
				"MetricInverseDuringLE",
				"MetricInverseBeginsGE",
				"MetricInverseBeginsLE"
		};
		
		m_languageType = INTERVAL;
		
		String[] stringFlags = relationsMask.split(",");
		
		if (stringFlags.length != 24) {
			System.out.println("The relation mask of HSM must contain 24 values; now there are " + stringFlags.length + " values");
			System.exit(0);
		}
		
		m_nameActivatedRelations = new ArrayList<String>();
		m_activatedRelations = new Relations();
		
		// Activate all relations
		for (int i = 0; i < m_nameAllRelations.length; i++) {
			if (stringFlags[i].equals("0") || stringFlags[i].equals("1")) {
				if (stringFlags[i].equals("1")) {
					try {
						/**
						 * Create interval relation from class name by instantiating a new object with the default constructor,
						 * as specified in:
						 * https://stackoverflow.com/questions/5658182/initializing-a-class-with-class-forname-and-which-have-a-constructor-which-tak
						 */
						IntervalRelation intervalRelation = (MetricIntervalRelation) Class.
								forName("timeseries.languages.interval." + m_nameAllRelations[i]).
								getConstructor().
								newInstance();
						
						/**
						 * Add the ith relation to the activated relations
						 */
						m_nameActivatedRelations.add(m_nameAllRelations[i]);
						m_activatedRelations.add(intervalRelation);
					}
					catch (Exception e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
			else {
				System.out.println("The mask of HS must contain 0s or 1s");
				System.exit(0);
			}
		}
		
		m_relationsMask = relationsMask;
	}
	
//	public static void main(String[] args) {
//		
//		Language hs = new HSMetric("1,1,1,1,1,1,1,1,1,1,1,1,1");
//		
//		for (Relation rel : hs.getActivatedRelations()) {
//			
//			System.out.println(((IntervalRelation) rel).existentialRelation() + " " + ((IntervalRelation) rel).universalRelation() + " " + rel.getRelationName() + " " + rel.getRelationType());
//		}
//	}
}
