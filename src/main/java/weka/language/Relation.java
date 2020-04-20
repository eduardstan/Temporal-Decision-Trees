package weka.language;

import java.io.Serializable;
import java.util.ArrayList;

import weka.language.Ontology;
import weka.language.interval.Interval;

public abstract class Relation implements Serializable {

	private static final long serialVersionUID = 511502451912543172L;

	protected final int POINT = 0;
	
	protected final int INTERVAL = 1;
	
	protected String m_relationName;
	
	protected int m_relationNumber;
	
	protected int m_relationType;
	
	public String getRelationName() {
		return m_relationName;
	}

	public void setRelationName(String relationName) {
		m_relationName = relationName;
	}

	public int getRelationNumber() {
		return m_relationNumber;
	}

	public void setRelationNumber(int relationNumber) {
		m_relationNumber = relationNumber;
	}
	
	public boolean isPointType() {
		return m_relationType == POINT;
	}
	
	public boolean isIntervalType() {
		return m_relationType == INTERVAL;
	}
	
	public int getRelationType() {
		return m_relationType;
	}
	
	public abstract String existentialRelation();
	
	public abstract String universalRelation();
	
	public abstract boolean checkRelation(Ontology ontology1, Ontology ontology2);
	
	/**
	 * TODO
	 * Add point ontology
	 */
	public abstract ArrayList<Interval> getIntervals(Interval reference, int maxPoint);
}
