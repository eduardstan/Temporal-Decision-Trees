package timeseries.languages;

import java.io.Serializable;

public abstract class Ontology implements Serializable {
	
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -7428851589198078060L;

	/**
	 * Ontology type.
	 */
	protected int m_ontologyType;

	/**
	 * Constant for point-based ontology.
	 */
	protected final int POINT = 0;
	
	/**
	 * Constant for interval-based ontology.
	 */
	protected final int INTERVAL = 1;
	
	/**
	 * Get ontology type.
	 * 
	 * @return integer representing the type
	 */
	public abstract int getOntologyType();
	
	/**
	 * Is point-based ontology?
	 * 
	 * @return true if it is
	 */
	public boolean isPointType() {
		return m_ontologyType == POINT;
	}
	
	/**
	 * Is interval-based ontology?
	 * 
	 * @return true if it is
	 */
	public boolean isIntervalType() {
		return m_ontologyType == INTERVAL;
	}
}
