package weka.language;

import java.io.Serializable;

import java.util.ArrayList;

public abstract class Language implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7237163656463577132L;

	protected final int POINT = 0;
	
	protected final int INTERVAL = 1;
	
	protected int m_languageType;
	
	protected String m_languageName;
	
	protected String[] m_nameAllRelations;
	
	protected String m_relationsMask;
	
	protected ArrayList<String> m_nameActivatedRelations;
	
	protected ArrayList<Relation> m_activatedRelations;
	
	public String getLanguageName() {
		return m_languageName;
	}
	
	public void setLanguageName(String languageName) {
		m_languageName = languageName;
	}
	
	public int getLanguageType() {
		return m_languageType;
	}
	
	public boolean isPointType() {
		return m_languageType == POINT;
	}
	
	public boolean isIntervalType() {
		return m_languageType == INTERVAL;
	}
	
	public String[] getNameAllRelations() {
		return m_nameAllRelations;
	}
	
	public ArrayList<String> getNameActivedRelations() {
		return m_nameActivatedRelations;
	}
	
	public ArrayList<Relation> getActivatedRelations() {
		return m_activatedRelations;
	}
	
	public String getRelationsMask() {
		return m_relationsMask;
	}
	
	public void setRelationsMask(String relationsMask) {
		m_relationsMask = relationsMask;
	}
}
