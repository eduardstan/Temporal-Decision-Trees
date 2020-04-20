package weka.language.interval;

import weka.language.Ontology;

public class Interval extends Ontology {

	private static final long serialVersionUID = 3814194750524070033L;

	protected final int m_ontologyType = INTERVAL;
	
	protected int m_start;
	
	protected int m_end;
	
	public Interval(int start, int end) {
		m_start = start;
		m_end = end;
		super.m_ontologyType = INTERVAL;
	}
	
	public int getStart() {
		return m_start;
	}
	
	public void setStart(int start) {
		m_start = start;
	}
	
	public int getEnd() {
		return m_end;
	}
	
	public void setEnd(int end) {
		m_end = end;
	}
	
	@Override
	public int getOntologyType() {
		return m_ontologyType;
	}
	
	@Override
	public String toString() {
		return "[" + Integer.toString(m_start) + "," + Integer.toString(m_end) + "]";
	}
}
