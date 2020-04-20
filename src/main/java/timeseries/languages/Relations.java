package timeseries.languages;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;

import timeseries.functions.Function;
import weka.core.WekaEnumeration;

public class Relations extends AbstractList<Relation> implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 761052573234102963L;
	
	/**
	 * The relations.
	 */
	protected ArrayList<Relation> m_relations;
	
	/**
	 * Default constructor with no parameters that creates an empty relations object.
	 */
	public Relations() {
		m_relations = new ArrayList<Relation>();
	}
	
	/**
	 * Add a relation to the end of the relations object. Increases the size
	 * it is not large enough.
	 * 
	 * @param relation the relation to be added
	 * @return true (as wanted by AbstractList)
	 */
	@Override
	public boolean add(Relation relation) {
		m_relations.add(relation);
		return true;
	}
	
	/**
	 * Delete the relation at the given position.
	 * 
	 * @param relIndex the relation's position (index starts with 0)
	 */
	public void delete(int relIndex) {
		m_relations.remove(relIndex);
	}
	
	/**
	 * Delete all relations.
	 */
	public void delete() {
		m_relations = new ArrayList<Relation>();
	}

	/**
	 * Returns an enumeration of the relations.
	 * 
	 * @return enumeration of the relations
	 */
	public Enumeration<Relation> enumerateRelations() {
		return new WekaEnumeration<Relation>(m_relations);
	}
	
	/**
	 * Get a relation at the given position.
	 * 
	 * @param relIndex the relation's index (index starts with 0)
	 * @return the function at the given position
	 */
	public Relation relation(int relIndex) {
		return m_relations.get(relIndex);
	}
	/**
	 * Get a relation at the given position.
	 * 
	 * @param relIndex the relation's index (index starts with 0)
	 * @return the function at the given position
	 */
	@Override
	public Relation get(int relIndex) {
		return m_relations.get(relIndex);
	}

	/**
	 * Get the number of relations.
	 * 
	 * @return the number of relations.
	 */
	public int numRelations() {
		return m_relations.size();
	}
	
	/**
	 * Get the number of relations.
	 * 
	 * @return the number of relations.
	 */
	@Override
	public int size() {
		return m_relations.size();
	}

}
