package weka.language;

import java.io.Serializable;

public abstract class Ontology implements Serializable {
	
	private static final long serialVersionUID = -7428851589198078060L;

	protected int m_ontologyType;

	protected final int POINT = 0;
	
	protected final int INTERVAL = 1;
	
	public abstract int getOntologyType();
	
	public boolean isPointType() {
		return m_ontologyType == POINT;
	}
	
	public boolean isIntervalType() {
		return m_ontologyType == INTERVAL;
	}
}
