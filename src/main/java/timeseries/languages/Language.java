package timeseries.languages;

import java.io.Serializable;

import java.util.ArrayList;

public abstract class Language implements Serializable {
	
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -7237163656463577132L;

	/**
	 * Constant for point-based language.
	 */
	protected final int POINT = 0;
	
	/**
	 * Constant for interval-based language.
	 */
	protected final int INTERVAL = 1;
	
	/**
	 * Language type.
	 */
	protected int m_languageType;
	
	/**
	 * Language name.
	 */
	protected String m_languageName;
	
	/**
	 * Name of all possible relations of the language.
	 */
	protected String[] m_nameAllRelations;
	
	/**
	 * Relations' mask for computing the activated relations.
	 */
	protected String m_relationsMask;
	
	/**
	 * Name of all activated relations of the language.
	 */
	protected ArrayList<String> m_nameActivatedRelations;
	
	/**
	 * Activated relations of the language.
	 */
	protected Relations m_activatedRelations;
	
	/**
	 * Get language name.
	 * 
	 * @return language name
	 */
	public String getLanguageName() {
		return m_languageName;
	}
	
	/**
	 * Set language name.
	 * 
	 * @param languageName language name
	 */
	public void setLanguageName(String languageName) {
		m_languageName = languageName;
	}
	
	public int getLanguageType() {
		return m_languageType;
	}
	
	/**
	 * Is point-based language?
	 * 
	 * @return true if it is
	 */
	public boolean isPointType() {
		return m_languageType == POINT;
	}
	
	/**
	 * Is interval-based language?
	 * 
	 * @return true if it is
	 */
	public boolean isIntervalType() {
		return m_languageType == INTERVAL;
	}
	
	/**
	 * Get the names for all relations of the language.
	 * 
	 * @return names for all relations
	 */
	public String[] getNameAllRelations() {
		return m_nameAllRelations;
	}
	
	/**
	 * Get the names for activated relations of the language.
	 * 
	 * @return names for activated relations of the language.
	 */
	public ArrayList<String> getNameActivedRelations() {
		return m_nameActivatedRelations;
	}
	
	/**
	 * Get activated relations of the language.
	 * 
	 * @return activated relations of the language.
	 */
	public Relations getActivatedRelations() {
		return m_activatedRelations;
	}
	
	/**
	 * Get relations' mask.
	 * 
	 * @return relations' mask
	 */
	public String getRelationsMask() {
		return m_relationsMask;
	}
	
	/**
	 * Set relations' mask.
	 * 
	 * @param relationsMask relations' mask.
	 */
	public void setRelationsMask(String relationsMask) {
		m_relationsMask = relationsMask;
	}
}
