package timeseries.languages;

import java.io.Serializable;

public abstract class Relation implements Serializable, Cloneable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 511502451912543172L;

	/**
	 * Constant for point-based relation.
	 */
	protected final int POINT = 0;
	
	/**
	 * Constant for interval-based relation.
	 */
	protected final int INTERVAL = 1;
	
	/**
	 * Constant for qualitative-based relation.
	 */
	public final int QUALITATIVE = 0;
	
	/**
	 * Constant for metric-based relation.
	 */
	public final int METRIC = 1;
	
	/**
	 * Relation name.
	 */
	protected String m_relationName;
	
	/**
	 * Relation type.
	 */
	protected int m_relationType;
	
	/**
	 * Measure type.
	 */
	protected int m_measureType;
	
	/**
	 * Get relation name.
	 * 
	 * @return relation name
	 */
	public String getRelationName() {
		return m_relationName;
	}

	/**
	 * Set relation name.
	 * @param relationName relation name
	 */
	public void setRelationName(String relationName) {
		m_relationName = relationName;
	}
	
	/**
	 * The relation is point-based?
	 * 
	 * @return true if it is
	 */
	public boolean isPointType() {
		return m_relationType == POINT;
	}
	
	/**
	 * The relation is interval-based?
	 * 
	 * @return true if it is
	 */
	public boolean isIntervalType() {
		return m_relationType == INTERVAL;
	}
	
	/**
	 * Get relation type represented as an integer.
	 * 
	 * @return integer representation of the relation type
	 */
	public int getRelationType() {
		return m_relationType;
	}
	
	/**
	 * The relation is qualitative-based?
	 * 
	 * @return true if it is
	 */
	public boolean isQualitative() {
		return m_measureType == QUALITATIVE;
	}
	
	/**
	 * The relation is metric-based?
	 * 
	 * @return true if it is
	 */
	public boolean isMetric() {
		return m_measureType == METRIC;
	}
	
	/**
	 * Get measure type represented as an integer.
	 * 
	 * @return integer representation of the measure type
	 */
	public int getMeasureType() {
		return m_measureType;
	}
	
	/**
	 * Cloning an object.
	 * 
	 * @return cloned object
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
//	/**
//	 * Function that checks if the relation holds.
//	 * 
//	 * @param ontology1 world representing the current one
//	 * @param ontology2 world representing the checked one
//	 * @return true if it holds
//	 */
//	public abstract boolean checkRelation(Ontology ontology1, Ontology ontology2);
}
