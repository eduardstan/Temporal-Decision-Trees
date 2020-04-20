package weka.language.interval;

import weka.language.Language;
import weka.language.Relation;

import java.util.ArrayList;

public class HS extends IntervalLanguage {
	
	private static final long serialVersionUID = 269058411394086897L;

	public HS(String relationsMask) {
		
		m_languageName = "HS";
		
		m_nameAllRelations = new String[]{
				"LaterThan",
				"AfterThan",
				"Overlaps",
				"Ends",
				"During",
				"Begins",
				"InverseLaterThan",
				"InverseAfterThan",
				"InverseOverlaps",
				"InverseEnds",
				"InverseDuring",
				"InverseBegins",
				"Equals"
		};
		
		m_languageType = INTERVAL;
		
		String[] stringFlags = relationsMask.split(",");
		
		if (stringFlags.length != 13) {
			System.out.println("The relation mask of HS must contain 13 values; now there are " + stringFlags.length + " values");
			System.exit(0);
		}
		
		m_nameActivatedRelations = new ArrayList<String>();
		m_activatedRelations = new ArrayList<Relation>();
		
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
						IntervalRelation intervalRelation = (IntervalRelation) Class.
								forName("weka.language.interval." + m_nameAllRelations[i]).
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
	
	public static void main(String[] args) {
		
		Language hs = new HS("1,1,1,1,1,1,1,1,1,1,1,1,1");
		
		for (Relation rel : hs.getActivatedRelations()) {
			System.out.println(rel.existentialRelation() + " " + rel.universalRelation() + " " + rel.getRelationName() + " " + rel.getRelationNumber() + " " + rel.getRelationType());
		}
	}
}
