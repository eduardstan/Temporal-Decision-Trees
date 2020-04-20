/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    BinC45Split.java
 *    Copyright (C) 1999-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.trees.j48t;

import java.util.Enumeration;
import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;

import timeseries.languages.*;
import timeseries.languages.interval.Interval;
import timeseries.languages.interval.IntervalRelation;
import timeseries.languages.interval.QualitativeIntervalRelation;
import timeseries.languages.interval.MetricIntervalRelation;

/**
 * Class implementing a binary C4.5-like split on an attribute.
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 14912 $
 */
public class BinC45Split extends ClassifierSplitModel {

  /** for serialization */
  private static final long serialVersionUID = -1278776919563022474L;

  /** Attribute to split on. */
  protected final int m_attIndex;

  /** Minimum number of objects in a split. */
  protected final int m_minNoObj;

  /** Use MDL correction? */
  protected final boolean m_useMDLcorrection;

  /** Value of split point. */
  protected double m_splitPoint;

  /** InfoGain of split. */
  protected double m_infoGain = 0;

  /** GainRatio of split. */
  protected double m_gainRatio = 0;

  /** The sum of the weights of the instances. */
  protected final double m_sumOfWeights;

  /** Static reference to splitting criterion. */
  protected static InfoGainSplitCrit m_infoGainCrit = new InfoGainSplitCrit();

  /** Static reference to splitting criterion. */
  protected static GainRatioSplitCrit m_gainRatioCrit = new GainRatioSplitCrit();
  
  /**
//   * J48T extra
//   */
//  
//  /**
//   * Language
//   */
//  protected Language m_language;
//  
//  /**
//   * Temporal relation to split on
//   */
//  protected Relation m_splitRelation;
  
//  /**
//   * J48T data structures.
//   */
//  
//  /**
//   * Instance map.
//   */
//  protected HashMap<Instance, String> m_instanceMap;
//  
//  /**
//   * For each attribute, identified by an integer number,
//   * it stores the ordered domain for that attribute
//   */
//  protected HashMap<Integer, ArrayList<Double>> m_attributeOrderedDomain;
//  
//  /**
//   * TODO: add method for returning such structure that we change at each split point
//   * so that BinC45ModelSelection can update its own m_referenceOntology
//   *
//   * For each instance, it stores the reference ontology for that instance.
//   */
//  protected HashMap<String, Ontology> m_referenceOntology;
//  
//  protected HashMap<String, Ontology> m_bestReferenceOntology;
//  
//  /**
//   * For each instance and for each ontology, it stores the 
//   * the minimum and maximum for each attribute
//   * 
//   * In particular, the key is the juxtaposing of strings:
//   * 	1) instance index ranging from 0 to m_allData.numInstances()-1
//   * 	2) ontology representing the world (to go on)
//   * 
//   * The mapped value is a set of triples (attributeIndex, minimum, maximum).
//   * If the ontology is a point, the the minimum is the same as maximum.
//   * If the ontology is an interval, the minimum and the maximum are not
//   * necessarily the same.
//   */
//  protected HashMap<String, ArrayList<TripleAttributeVirtualMinMax>> m_attributeVirtualMinMaxValues;  

  /**
   * Initializes the split model.
   * Standard constructor.
   */
  public BinC45Split(int attIndex, 
		  int minNoObj, 
		  double sumOfWeights,
		  boolean useMDLcorrection) {

    // Get index of attribute to split on.
    m_attIndex = attIndex;

    // Set minimum number of objects.
    m_minNoObj = minNoObj;

    // Set sum of weights;
    m_sumOfWeights = sumOfWeights;

    // Whether to use the MDL correction for numeric attributes
    m_useMDLcorrection = useMDLcorrection;
  }
  
  /**
   * Initializes the split model.
   * Time series constructor.
   */
  public BinC45Split(int attIndex, 
		  int minNoObj, 
		  double sumOfWeights,
		  boolean useMDLcorrection,
		  Language language,
		  HashMap<Instance, String> instanceMap,
		  HashMap<Integer, ArrayList<Double>> attributeOrderedDomain,
		  HashMap<String, Ontology> referenceOntology,
		  HashMap<String, ArrayList<TripleAttributeVirtualMinMax>> attributeVirtualMinMaxValues,
		  double fractionValues) {

    // Get index of attribute to split on.
    m_attIndex = attIndex;

    // Set minimum number of objects.
    m_minNoObj = minNoObj;

    // Set sum of weights;
    m_sumOfWeights = sumOfWeights;

    // Whether to use the MDL correction for numeric attributes
    m_useMDLcorrection = useMDLcorrection;
    
    m_language = language;
    
    m_instanceMap = instanceMap;
    
    m_attributeOrderedDomain = attributeOrderedDomain;
    
    m_referenceOntology = referenceOntology;
    
    m_attributeVirtualMinMaxValues = attributeVirtualMinMaxValues;
    
    m_fractionValues = fractionValues;
  }

  /**
   * Creates a C4.5-type split on the given data.
   * 
   * @exception Exception if something goes wrong
   */
  @Override
  public void buildClassifier(Instances trainInstances) throws Exception {

    // Initialize the remaining instance variables.
    m_numSubsets = 0;
    m_splitPoint = Double.MAX_VALUE;
    m_infoGain = 0;
    m_gainRatio = 0;

    //System.out.println("BinC45Split # inside buildClassifier(Instances) -- m_attIndex " + m_attIndex + " trainInstances.numInstances() " + trainInstances.numInstances());

    // Enumerated attributes; now it is useless
    if (trainInstances.attribute(m_attIndex).isNominal()) {
      handleEnumeratedAttribute(trainInstances);
    } 
    // Numeric attributes; now it is useless
    else if (trainInstances.attribute(m_attIndex).isNumeric()) {
      trainInstances.sort(trainInstances.attribute(m_attIndex));
      handleNumericAttribute(trainInstances);
    }
    // String attributes; our case
    else if (trainInstances.attribute(m_attIndex).isString()) {
    	handleStringAttribute(trainInstances);
    }
    else {
    	System.err.println("What type of attribute is this??");
    	System.exit(0);
    }
  }

  /**
   * Returns index of attribute for which split was generated.
   */
  public final int attIndex() {

    return m_attIndex;
  }

  /**
   * Returns the split point (numeric attribute only).
   * 
   * @return the split point used for a test on a numeric attribute
   */
  public double splitPoint() {
    return m_splitPoint;
  }

  /**
   * Returns (C4.5-type) gain ratio for the generated split.
   */
  public final double gainRatio() {
    return m_gainRatio;
  }

  /**
   * Gets class probability for instance.
   * 
   * @exception Exception if something goes wrong
   */
  @Override
  public final double classProb(int classIndex, Instance instance, int theSubset)
    throws Exception {

    if (theSubset <= -1) {
      double[] weights = weights(instance);
      if (weights == null) {
        return m_distribution.prob(classIndex);
      } else {
        double prob = 0;
        for (int i = 0; i < weights.length; i++) {
          prob += weights[i] * m_distribution.prob(classIndex, i);
        }
        return prob;
      }
    } else {
      if (Utils.gr(m_distribution.perBag(theSubset), 0)) {
        return m_distribution.prob(classIndex, theSubset);
      } else {
        return m_distribution.prob(classIndex);
      }
    }
  }

  /**
   * Creates split on enumerated attribute.
   * 
   * @exception Exception if something goes wrong
   */
  private void handleEnumeratedAttribute(Instances trainInstances)
    throws Exception {

    Distribution newDistribution, secondDistribution;
    int numAttValues;
    double currIG, currGR;
    Instance instance;
    int i;

    numAttValues = trainInstances.attribute(m_attIndex).numValues();
    newDistribution = new Distribution(numAttValues,
      trainInstances.numClasses());

    // Only Instances with known values are relevant.
    Enumeration<Instance> enu = trainInstances.enumerateInstances();
    while (enu.hasMoreElements()) {
      instance = enu.nextElement();
      if (!instance.isMissing(m_attIndex)) {
        newDistribution.add((int) instance.value(m_attIndex), instance);
      }
    }
    m_distribution = newDistribution;

    // For all values
    for (i = 0; i < numAttValues; i++) {

      if (Utils.grOrEq(newDistribution.perBag(i), m_minNoObj)) {
        secondDistribution = new Distribution(newDistribution, i);

        // Check if minimum number of Instances in the two
        // subsets.
        if (secondDistribution.check(m_minNoObj)) {
          m_numSubsets = 2;
          currIG = m_infoGainCrit.splitCritValue(secondDistribution,
            m_sumOfWeights);
          currGR = m_gainRatioCrit.splitCritValue(secondDistribution,
            m_sumOfWeights, currIG);
          if ((i == 0) || Utils.gr(currGR, m_gainRatio)) {
            m_gainRatio = currGR;
            m_infoGain = currIG;
            m_splitPoint = i;
            m_distribution = secondDistribution;
          }
        }
      }
    }
  }

  public HashMap<String, Ontology> getBestReferenceOntology() {
	  return m_bestReferenceOntology;
  }
  
  private void handleStringAttribute(Instances trainInstances) throws Exception {
	  
	  double minSplit;
//	  double currMinIG = 0.0;
//	  double currMinGR = 0.0;
//	  double currMaxIG = 0.0;
//	  double currMaxGR = 0.0;
	  double defaultEnt;
	  
	  /**
	   * TODO
	   * HAS TO BE MODIFIED !!!!
	   */
//	  int numPoints = trainInstances.instance(0).stringValue(0).split(",").length;
//	  int numPoints = 100;
//	  System.out.println("numPoints=" + numPoints);
	  
//	  /**
//	   * Virtual minimum
//	   */
//	  Distribution auxMinDistribution;
//	  ArrayList<Interval> satisfyingMinInterval;
//	  HashMap<String, Ontology> auxReferenceMinOntology;
//	  
//	  /**
//	   * Virtual maximum
//	   */
//	  Distribution auxMaxDistribution;
//	  ArrayList<Interval> satisfyingMaxInterval;
//	  HashMap<String, Ontology> auxReferenceMaxOntology;
	  
	  /**
	   * Distribution with 2 bags with trainInstances.numClasses() classes.
	   * Initializing the distribution by adding all instances in the first bag (with index 0).
	   */
	  m_distribution = new Distribution(2, trainInstances.numClasses());
	  for (Instance instance : trainInstances) 
		  m_distribution.add(0, instance);
	  
	  // Compute minimum number of Instances required in each
	  // subset.
	  minSplit = 0.1 * (m_distribution.total()) / (trainInstances.numClasses());
	  if (Utils.smOrEq(minSplit, m_minNoObj)) {
		  minSplit = m_minNoObj;
	  } 
	  else if (Utils.gr(minSplit, 25)) {
		  minSplit = 25;
	  }
	  
//	  // Compute values of criteria for all possible split
	  // indices.
	  defaultEnt = m_infoGainCrit.oldEnt(m_distribution);
	  
	  System.out.println("defaultEnt=" + defaultEnt);
	  
//	  ArrayList<Interval> intervals = getIntervals(1, numPoints);
	  
	  // For each activated relation of the language
	  // TODO: handle point-based language
	  for (Relation relation : m_language.getActivatedRelations()) {
		  
		  if (relation.isQualitative()) handleQualitativeRelation((QualitativeIntervalRelation) relation, trainInstances, defaultEnt);
		  else if (relation.isMetric()) handleMetricRelation((MetricIntervalRelation) relation, trainInstances, defaultEnt);
		  // For each possible index in the ordered domain (of doubles) of attribute m_attIndex
//		  for (int candidateIndex = 0; candidateIndex < m_attributeOrderedDomain.get(m_attIndex).size(); candidateIndex++) {
//			  
//			  // Creating new distributions with 2 bags and trainInstances.numClasses() classes
//			  auxMinDistribution = new Distribution(2, trainInstances.numClasses());
//			  auxMaxDistribution = new Distribution(2, trainInstances.numClasses());
//			  
//			  // Initializing the auxiliary reference ontology data structures
//			  auxReferenceMinOntology = new HashMap<String, Ontology>(m_referenceOntology.size());
//			  for (Instance instance : m_instanceMap.keySet()) {
//				  auxReferenceMinOntology.put(m_instanceMap.get(instance), m_referenceOntology.get(m_instanceMap.get(instance)));
//			  }
//			  
//			  auxReferenceMaxOntology = new HashMap<String, Ontology>(m_referenceOntology.size());
//			  for (Instance instance : m_instanceMap.keySet()) {
//				  auxReferenceMaxOntology.put(m_instanceMap.get(instance), m_referenceOntology.get(m_instanceMap.get(instance)));
//			  }
//			  	
//			  // For each instance
//			  for (Instance instance : trainInstances) {
//				  
//				  // Sets of satisfying intervals
//				  satisfyingMinInterval = new ArrayList<Interval>();
//				  satisfyingMaxInterval = new ArrayList<Interval>();
//				  
//				  // Necessary if to avoid null pointer exceptions
//				  if (m_referenceOntology.get(m_instanceMap.get(instance)) != null) {
//					  
//					  /**
//					   * TODO
//					   * Handle the case without cast
//					   */
////					  // Intervals
//					  ArrayList<Interval> intervals = ((IntervalRelation) relation).getIntervals((Interval) m_referenceOntology.get(m_instanceMap.get(instance)), numPoints);
//					  
////					  System.out.println(relation.getRelationName() + " ==> on " + m_referenceOntology.get(m_instanceMap.get(instance)) + " there are " + intervals.size() + " intervals");
//					  
//					  for (Interval interval : intervals) {
//							  
//						  // Current reference ontology is in relation with the considered interval?
//						  if (((QualitativeIntervalRelation) relation).checkRelation(m_referenceOntology.get(m_instanceMap.get(instance)), interval)) {
//							  
////									  System.out.println("\t\t\tInstance_" + m_instanceMap.get(instance) + "," + interval + " ?|=? " + relation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " >= " + m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex));
////									  System.out.println("\tvirtualMininum()=" + m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMinimum());
//							  
////								  System.out.println(m_instanceMap.get(instance) + interval);
////								  System.out.println(m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval));
//							  
//							  
//							  // If the candidate is less the virtual minimum
//							  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMinimum() >= m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex)) {
////										  System.out.println("\t\t\tInstance_" + m_instanceMap.get(instance) + "," + interval + " !|=! " + relation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " >= " + m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex) + "<<<<<<");
//								  satisfyingMinInterval.add(interval);
////									  referenceMin = interval;
//								  
//								  // First witness?
////									  break;
//							  }
//							  
//							  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMaximum() <= m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex)) {
////									  System.out.println("\t\t\tInstance_" + m_instanceMap.get(instance) + "," + interval + " !|=! " + relation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " <= " + m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex));
//								  satisfyingMaxInterval.add(interval);
////									  referenceMin = interval;
//								  
//								  // First witness?
////									  break;
//							  }
//						  } // End check relation
//					  } // End intervals
//				  }
//				  
//				  // Is there some interval satisfying the virtual min?
//				  if (satisfyingMinInterval.size() != 0) {
//					  auxMinDistribution.add(0, instance);
//					  // First witness
//					  /**
//					   * TODO
//					   * Add random witness selection or last witness
//					   */
//					  auxReferenceMinOntology.put(m_instanceMap.get(instance), satisfyingMinInterval.get(0));
//				  }
//				  else
//					  auxMinDistribution.add(1, instance);
//				  
//				  // Is there some interval satisfying the virtual max?
//				  if (satisfyingMaxInterval.size() != 0) {
//					  auxMaxDistribution.add(0, instance);
//					  // First witness
//					  /**
//					   * TODO
//					   * Add random witness selection or last witness
//					   */
//					  auxReferenceMaxOntology.put(m_instanceMap.get(instance), satisfyingMaxInterval.get(0));
//				  }
//				  else
//					  auxMaxDistribution.add(1, instance);
//			  } // End instances
//			  		
////			  if (auxMaxDistribution.check(m_minNoObj)) {
////				  m_numSubsets = 2;
////				  
////				  currMaxIG = m_infoGainCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, defaultEnt);
//////				  currMaxGR = m_gainRatioCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, currMaxIG);
////				  
////				  if (candidateIndex == 0 || Utils.gr(currMaxIG, m_infoGain)) {
//////					  m_gainRatio = currMaxGR;
////					  m_infoGain = currMaxIG;
////					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
////					  m_distribution = auxMaxDistribution;
////					  m_splitRelation = relation;
////					  m_bestReferenceOntology = auxReferenceMaxOntology;
////					  m_virtualOrder = true;
////				  }
////			  }
//
//			  // If at least one check passes the test
//			  if (auxMinDistribution.check(m_minNoObj) || auxMaxDistribution.check(m_minNoObj)) {
//				  m_numSubsets = 2;
//				  
//				  currMinIG = m_infoGainCrit.splitCritValue(auxMinDistribution, m_sumOfWeights, defaultEnt);
//				  currMaxIG = m_infoGainCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, defaultEnt);
//				  
////				  System.out.println(currMinIG + "," + currMaxIG);
//				  
//				  if (candidateIndex == 0) {
//					  if (Utils.gr(currMinIG, currMaxIG)) {
//						  m_infoGain = currMinIG;
//						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
//						  m_distribution = auxMinDistribution;
//						  m_splitRelation = relation;
//						  m_bestReferenceOntology = auxReferenceMinOntology;
//						  m_virtualOrder = false;
//					  }
//					  else {
//						  m_infoGain = currMaxIG;
//						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
//						  m_distribution = auxMaxDistribution;
//						  m_splitRelation = relation;
//						  m_bestReferenceOntology = auxReferenceMaxOntology;
//						  m_virtualOrder = true;
//					  }
//				  }
//				  
//				  if (Utils.gr(currMinIG, currMaxIG)) {
//					  if (Utils.gr(currMinIG, m_infoGain)) {
//						  m_infoGain = currMinIG;
//						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
//						  m_distribution = auxMinDistribution;
//						  m_splitRelation = relation;
//						  m_bestReferenceOntology = auxReferenceMinOntology;
//						  m_virtualOrder = false;
//					  }
//				  }
//				  else {
//					  if (Utils.gr(currMaxIG, m_infoGain)) {
//						  m_infoGain = currMaxIG;
//						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
//						  m_distribution = auxMaxDistribution;
//						  m_splitRelation = relation;
//						  m_bestReferenceOntology = auxReferenceMaxOntology;
//						  m_virtualOrder = true;
//					  }
//				  }
//			  }
//			  
////			  if (auxMinDistribution.check(m_minNoObj)) {
////				  m_numSubsets = 2;
////				  
////				  currMinIG = m_infoGainCrit.splitCritValue(auxMinDistribution, m_sumOfWeights, defaultEnt);
//////				  System.out.println("(MIN) old IG=" + m_infoGainCrit.splitCritValue(auxMinDistribution) + "\t newIG: " + currMinIG + "\told GR=" + m_gainRatioCrit);
//////				  currMinGR = m_gainRatioCrit.splitCritValue(auxMinDistribution, m_sumOfWeights, currMinIG);
////				  
//////				  System.out.println(currMinIG);
////				  if (candidateIndex == 0 || Utils.gr(currMinIG, m_infoGain)) {
//////				  if (Utils.gr(currMinGR, m_gainRatio)) {
//////					  m_gainRatio = currMinGR;
////					  m_infoGain = currMinIG;
////					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
////					  m_distribution = auxMinDistribution;
////					  m_splitRelation = relation;
////					  m_bestReferenceOntology = auxReferenceMinOntology;
////					  m_virtualOrder = false;
////				  }
////			  }
////			  
////			  if (auxMaxDistribution.check(m_minNoObj)) {
////				  m_numSubsets = 2;
////				  
////				  currMaxIG = m_infoGainCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, defaultEnt);
//////				  System.out.println("(MAX) old IG=" + m_infoGainCrit.splitCritValue(auxMaxDistribution) + "\t newIG: " + currMaxIG);
//////				  currMaxGR = m_gainRatioCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, currMaxIG);
////				  
////				  System.out.println(currMaxIG); 
////				  if (Utils.gr(currMaxIG, m_infoGain)) {
//////					  m_gainRatio = currMaxGR;
////					  m_infoGain = currMaxIG;
////					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
////					  m_distribution = auxMaxDistribution;
////					  m_splitRelation = relation;
////					  m_bestReferenceOntology = auxReferenceMaxOntology;
////					  m_virtualOrder = true;
////				  }
////			  }
//		  } // End candidates
	  } // End relations
  }
  
  private void handleQualitativeRelation(QualitativeIntervalRelation relation, Instances trainInstances, double defaultEnt) throws Exception {
	  
	  double currMinIG = 0.0;
	  double currMaxIG = 0.0;
	  
	  /**
	   * TODO
	   * HAS TO BE MODIFIED !!!!
	   */
	  int numPoints = trainInstances.instance(0).stringValue(0).split(",").length;
	  
	  /**
	   * Virtual minimum
	   */
	  Distribution auxMinDistribution;
	  ArrayList<Interval> satisfyingMinInterval;
	  HashMap<String, Ontology> auxReferenceMinOntology;
	  
	  /**
	   * Virtual maximum
	   */
	  Distribution auxMaxDistribution;
	  ArrayList<Interval> satisfyingMaxInterval;
	  HashMap<String, Ontology> auxReferenceMaxOntology;
	  
	  // For each possible index in the ordered domain (of doubles) of attribute m_attIndex
	  for (int candidateIndex = 0; candidateIndex < m_attributeOrderedDomain.get(m_attIndex).size(); candidateIndex++) {
		  
		  // Creating new distributions with 2 bags and trainInstances.numClasses() classes
		  auxMinDistribution = new Distribution(2, trainInstances.numClasses());
		  auxMaxDistribution = new Distribution(2, trainInstances.numClasses());
		  
		  // Initializing the auxiliary reference ontology data structures
		  auxReferenceMinOntology = new HashMap<String, Ontology>(m_referenceOntology.size());
		  auxReferenceMaxOntology = new HashMap<String, Ontology>(m_referenceOntology.size());
		  for (Instance instance : m_instanceMap.keySet()) {
			  auxReferenceMinOntology.put(m_instanceMap.get(instance), m_referenceOntology.get(m_instanceMap.get(instance)));
			  auxReferenceMaxOntology.put(m_instanceMap.get(instance), m_referenceOntology.get(m_instanceMap.get(instance)));
		  }
		  	
		  // For each instance
		  for (Instance instance : trainInstances) {
			  
			  // Sets of satisfying intervals
			  satisfyingMinInterval = new ArrayList<Interval>();
			  satisfyingMaxInterval = new ArrayList<Interval>();
			  
			  // Necessary if to avoid null pointer exceptions
			  if (m_referenceOntology.get(m_instanceMap.get(instance)) != null) {
				  
				  /**
				   * TODO
				   * Handle the case without cast
				   */
//				  // Intervals
				  ArrayList<Interval> intervals = relation.getIntervals((Interval) m_referenceOntology.get(m_instanceMap.get(instance)), numPoints);
				  
				  for (Interval interval : intervals) {
						  
					  // Current reference ontology is in relation with the considered interval?
					  if (relation.checkRelation(m_referenceOntology.get(m_instanceMap.get(instance)), interval)) {
						  
						  // If the candidate is less the virtual minimum
						  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMinimum() >= m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex))
							 satisfyingMinInterval.add(interval);
						  
						  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMaximum() <= m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex))
							  satisfyingMaxInterval.add(interval);

					  } // End check relation
				  } // End intervals
			  }
			  
			  // Is there some interval satisfying the virtual min?
			  if (satisfyingMinInterval.size() != 0) {
				  auxMinDistribution.add(0, instance);
				  // First witness
				  /**
				   * TODO
				   * Add random witness selection or last witness
				   */
				  auxReferenceMinOntology.put(m_instanceMap.get(instance), satisfyingMinInterval.get(0));
			  }
			  else
				  auxMinDistribution.add(1, instance);
			  
			  // Is there some interval satisfying the virtual max?
			  if (satisfyingMaxInterval.size() != 0) {
				  auxMaxDistribution.add(0, instance);
				  // First witness
				  /**
				   * TODO
				   * Add random witness selection or last witness
				   */
				  auxReferenceMaxOntology.put(m_instanceMap.get(instance), satisfyingMaxInterval.get(0));
			  }
			  else
				  auxMaxDistribution.add(1, instance);
		  } // End instances

		  // If at least one check passes the test
		  if (auxMinDistribution.check(m_minNoObj) || auxMaxDistribution.check(m_minNoObj)) {
			  m_numSubsets = 2;
			  
			  currMinIG = m_infoGainCrit.splitCritValue(auxMinDistribution, m_sumOfWeights, defaultEnt);
			  currMaxIG = m_infoGainCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, defaultEnt);
			  
			  if (candidateIndex == 0) {
				  if (Utils.gr(currMinIG, currMaxIG)) {
					  m_infoGain = currMinIG;
					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
					  m_distribution = auxMinDistribution;
					  m_splitRelation = relation;
					  m_bestReferenceOntology = auxReferenceMinOntology;
					  m_virtualOrder = false;
				  }
				  else {
					  m_infoGain = currMaxIG;
					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
					  m_distribution = auxMaxDistribution;
					  m_splitRelation = relation;
					  m_bestReferenceOntology = auxReferenceMaxOntology;
					  m_virtualOrder = true;
				  }
			  }
			  
			  if (Utils.gr(currMinIG, currMaxIG)) {
				  if (Utils.gr(currMinIG, m_infoGain)) {
					  m_infoGain = currMinIG;
					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
					  m_distribution = auxMinDistribution;
					  m_splitRelation = relation;
					  m_bestReferenceOntology = auxReferenceMinOntology;
					  m_virtualOrder = false;
				  }
			  }
			  else {
				  if (Utils.gr(currMaxIG, m_infoGain)) {
					  m_infoGain = currMaxIG;
					  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
					  m_distribution = auxMaxDistribution;
					  m_splitRelation = relation;
					  m_bestReferenceOntology = auxReferenceMaxOntology;
					  m_virtualOrder = true;
				  }
			  }
		  }
	  } // End candidates
  }
  
  private void handleMetricRelation(MetricIntervalRelation relation, Instances trainInstances, double defaultEnt) throws Exception {
	  
	  double currMinIG = 0.0;
	  double currMaxIG = 0.0;
	  	  
	  /**
	   * TODO
	   * HAS TO BE MODIFIED !!!!
	   */
	  int numPoints = trainInstances.instance(0).stringValue(0).split(",").length;
	  
	  /**
	   * Virtual minimum
	   */
	  Distribution auxMinDistribution;
	  ArrayList<Interval> satisfyingMinInterval;
	  HashMap<String, Ontology> auxReferenceMinOntology;
	  
	  /**
	   * Virtual maximum
	   */
	  Distribution auxMaxDistribution;
	  ArrayList<Interval> satisfyingMaxInterval;
	  HashMap<String, Ontology> auxReferenceMaxOntology;
	  
	  // For each possible index in the ordered domain (of doubles) of attribute m_attIndex
	  for (int candidateIndex = 0; candidateIndex < m_attributeOrderedDomain.get(m_attIndex).size(); candidateIndex++) {
		  	
		  // For each length
		  for (int length = 1; length <= numPoints; length ++) {
			  
			  relation.setIntervalLength(length);
			  
			  // Creating new distributions with 2 bags and trainInstances.numClasses() classes
			  auxMinDistribution = new Distribution(2, trainInstances.numClasses());
			  auxMaxDistribution = new Distribution(2, trainInstances.numClasses());
			  
			  // Initializing the auxiliary reference ontology data structures
			  auxReferenceMinOntology = new HashMap<String, Ontology>(m_referenceOntology.size());
			  auxReferenceMaxOntology = new HashMap<String, Ontology>(m_referenceOntology.size());
			  for (Instance instance : m_instanceMap.keySet()) {
				  auxReferenceMinOntology.put(m_instanceMap.get(instance), m_referenceOntology.get(m_instanceMap.get(instance)));
				  auxReferenceMaxOntology.put(m_instanceMap.get(instance), m_referenceOntology.get(m_instanceMap.get(instance)));
			  }
			  
			  // For each instance
			  for (Instance instance : trainInstances) {
				  
				  // Sets of satisfying intervals
				  satisfyingMinInterval = new ArrayList<Interval>();
				  satisfyingMaxInterval = new ArrayList<Interval>();
				  
				  // Necessary if to avoid null pointer exceptions
				  if (m_referenceOntology.get(m_instanceMap.get(instance)) != null) {
					  
					  /**
					   * TODO
					   * Handle the case without cast
					   */
	//				  // Intervals
					  ArrayList<Interval> intervals = relation.getIntervals((Interval) m_referenceOntology.get(m_instanceMap.get(instance)), numPoints);
					  
					  for (Interval interval : intervals) {
							  
						  // Current reference ontology is in relation with the considered interval?
						  if (relation.checkRelation(m_referenceOntology.get(m_instanceMap.get(instance)), interval, length)) {
							  
							  // If the candidate is less the virtual minimum
							  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMinimum() >= m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex))
								  satisfyingMinInterval.add(interval);
							  
							  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMaximum() <= m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex))
								  satisfyingMaxInterval.add(interval);

						  } // End check relation
					  } // End intervals
				  }
				  
				  // Is there some interval satisfying the virtual min?
				  if (satisfyingMinInterval.size() != 0) {
					  auxMinDistribution.add(0, instance);
					  // First witness
					  /**
					   * TODO
					   * Add random witness selection or last witness
					   */
					  auxReferenceMinOntology.put(m_instanceMap.get(instance), satisfyingMinInterval.get(0));
				  }
				  else
					  auxMinDistribution.add(1, instance);
				  
				  // Is there some interval satisfying the virtual max?
				  if (satisfyingMaxInterval.size() != 0) {
					  auxMaxDistribution.add(0, instance);
					  // First witness
					  /**
					   * TODO
					   * Add random witness selection or last witness
					   */
					  auxReferenceMaxOntology.put(m_instanceMap.get(instance), satisfyingMaxInterval.get(0));
				  }
				  else
					  auxMaxDistribution.add(1, instance);
			  } // End instances
	
			  // If at least one check passes the test
			  if (auxMinDistribution.check(m_minNoObj) || auxMaxDistribution.check(m_minNoObj)) {
				  m_numSubsets = 2;
				  
				  currMinIG = m_infoGainCrit.splitCritValue(auxMinDistribution, m_sumOfWeights, defaultEnt);
				  currMaxIG = m_infoGainCrit.splitCritValue(auxMaxDistribution, m_sumOfWeights, defaultEnt);
				  				  
				  if (candidateIndex == 0) {
					  if (Utils.gr(currMinIG, currMaxIG)) {
						  m_infoGain = currMinIG;
						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
						  m_distribution = auxMinDistribution;
						  m_splitRelation = (MetricIntervalRelation) relation.clone();
						  m_bestReferenceOntology = auxReferenceMinOntology;
						  m_virtualOrder = false;
					  }
					  else {
						  m_infoGain = currMaxIG;
						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
						  m_distribution = auxMaxDistribution;
						  m_splitRelation = (MetricIntervalRelation) relation.clone();
						  m_bestReferenceOntology = auxReferenceMaxOntology;
						  m_virtualOrder = true;
					  }
				  }
				  
				  if (Utils.gr(currMinIG, currMaxIG)) {
					  if (Utils.gr(currMinIG, m_infoGain)) {
						  m_infoGain = currMinIG;
						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
						  m_distribution = auxMinDistribution;
						  m_splitRelation = (MetricIntervalRelation) relation.clone();
						  m_bestReferenceOntology = auxReferenceMinOntology;
						  m_virtualOrder = false;
					  }
				  }
				  else {
					  if (Utils.gr(currMaxIG, m_infoGain)) {
						  m_infoGain = currMaxIG;
						  m_splitPoint = m_attributeOrderedDomain.get(m_attIndex).get(candidateIndex);
						  m_distribution = auxMaxDistribution;
						  m_splitRelation = (MetricIntervalRelation) relation.clone();
						  m_bestReferenceOntology = auxReferenceMaxOntology;
						  m_virtualOrder = true;
					  }
				  }
			  }
		  } // End lengths
	  } // End candidates
  }
  
  private ArrayList<Interval> getIntervals(int start, int end) {
	  
	  int numPoints = end - start + 1;
	  ArrayList<Interval> intervals = new ArrayList<Interval>(numPoints*(numPoints-1)/2);
	  
	  for (int x = start; x < end; x++)
		  for (int y = x+1; y <= end; y++)
			  intervals.add(new Interval(x,y));
	  
	  return intervals;
  }
  /**
   * Creates split on numeric attribute.
   * 
   * @exception Exception if something goes wrong
   */
  private void handleNumericAttribute(Instances trainInstances)
    throws Exception {

    int firstMiss;
    int next = 1;
    int last = 0;
    int index = 0;
    int splitIndex = -1;
    double currentInfoGain;
    double defaultEnt;
    double minSplit;
    Instance instance;
    int i;

    // Current attribute is a numeric attribute.
    m_distribution = new Distribution(2, trainInstances.numClasses());

    // Only Instances with known values are relevant.
    Enumeration<Instance> enu = trainInstances.enumerateInstances();
    i = 0;
    while (enu.hasMoreElements()) {
      instance = enu.nextElement();
      if (instance.isMissing(m_attIndex)) {
        break;
      }
      m_distribution.add(1, instance);
      i++;
    }
    firstMiss = i;

    // Compute minimum number of Instances required in each
    // subset.
    minSplit = 0.1 * (m_distribution.total()) / (trainInstances.numClasses());
    if (Utils.smOrEq(minSplit, m_minNoObj)) {
      minSplit = m_minNoObj;
    } else if (Utils.gr(minSplit, 25)) {
      minSplit = 25;
    }

    // Enough Instances with known values?
    if (Utils.sm(firstMiss, 2 * minSplit)) {
      return;
    }

    // Compute values of criteria for all possible split
    // indices.
    defaultEnt = m_infoGainCrit.oldEnt(m_distribution);
    while (next < firstMiss) {

      if (trainInstances.instance(next - 1).value(m_attIndex) + 1e-5 < trainInstances
        .instance(next).value(m_attIndex)) {

        // Move class values for all Instances up to next
        // possible split point.
        m_distribution.shiftRange(1, 0, trainInstances, last, next);

        // Check if enough Instances in each subset and compute
        // values for criteria.
        if (Utils.grOrEq(m_distribution.perBag(0), minSplit)
          && Utils.grOrEq(m_distribution.perBag(1), minSplit)) {
          currentInfoGain = m_infoGainCrit.splitCritValue(m_distribution,
            m_sumOfWeights, defaultEnt);
          if (Utils.gr(currentInfoGain, m_infoGain)) {
            m_infoGain = currentInfoGain;
            splitIndex = next - 1;
          }
          index++;
        }
        last = next;
      }
      next++;
    }

    // Was there any useful split?
    if (index == 0) {
      return;
    }

    // Compute modified information gain for best split.
    if (m_useMDLcorrection) {
      m_infoGain = m_infoGain - (Utils.log2(index) / m_sumOfWeights);
    }
    if (Utils.smOrEq(m_infoGain, 0)) {
      return;
    }

    // Set instance variables' values to values for
    // best split.
    m_numSubsets = 2;
    m_splitPoint = (trainInstances.instance(splitIndex + 1).value(m_attIndex) + trainInstances
      .instance(splitIndex).value(m_attIndex)) / 2;

    // In case we have a numerical precision problem we need to choose the
    // smaller value
    if (m_splitPoint == trainInstances.instance(splitIndex + 1).value(
      m_attIndex)) {
      m_splitPoint = trainInstances.instance(splitIndex).value(m_attIndex);
    }

    // Restore distributioN for best split.
    m_distribution = new Distribution(2, trainInstances.numClasses());
    m_distribution.addRange(0, trainInstances, 0, splitIndex + 1);
    m_distribution.addRange(1, trainInstances, splitIndex + 1, firstMiss);

    // Compute modified gain ratio for best split.
    m_gainRatio = m_gainRatioCrit.splitCritValue(m_distribution,
      m_sumOfWeights, m_infoGain);
  }

  /**
   * Returns (C4.5-type) information gain for the generated split.
   */
  public final double infoGain() {

    return m_infoGain;
  }

  /**
   * Prints left side of condition.
   * 
   * @param data the data to get the attribute name from.
   * @return the attribute name
   */
  @Override
  public final String leftSide(Instances data) {

	if (data.attribute(m_attIndex).isString()) 
		return "";
	else
		return data.attribute(m_attIndex).name();
  }

  /**
   * Prints the condition satisfied by instances in a subset.
   * 
   * @param index of subset and training set.
   */
  @Override
  public final String rightSide(int index, Instances data) {

    StringBuffer text;

    text = new StringBuffer();
    if (data.attribute(m_attIndex).isNominal()) {
      if (index == 0) {
        text.append(" = "
          + data.attribute(m_attIndex).value((int) m_splitPoint));
      } else {
        text.append(" != "
          + data.attribute(m_attIndex).value((int) m_splitPoint));
      }
    } 
    else if (data.attribute(m_attIndex).isString()) {
    	if (index == 0) 
    		text.append(((IntervalRelation) m_splitRelation).existentialRelation() + " " + data.attribute(m_attIndex).name() + (m_virtualOrder?" <= ":" >= ") + m_splitPoint);
    	else
    		text.append(((IntervalRelation) m_splitRelation).universalRelation() + " " + data.attribute(m_attIndex).name() + (m_virtualOrder?" > ":" < ") + m_splitPoint);
    }
    else if (index == 0) {
      text.append(" <= " + m_splitPoint);
    } 
    else {
      text.append(" > " + m_splitPoint);
    }

    return text.toString();
  }

  /**
   * Returns a string containing java source code equivalent to the test made at
   * this node. The instance being tested is called "i".
   * 
   * @param index index of the nominal value tested
   * @param data the data containing instance structure info
   * @return a value of type 'String'
   */
  @Override
  public final String sourceExpression(int index, Instances data) {

    StringBuffer expr = null;
    if (index < 0) {
      return "i[" + m_attIndex + "] == null";
    }
    if (data.attribute(m_attIndex).isNominal()) {
      if (index == 0) {
        expr = new StringBuffer("i[");
      } else {
        expr = new StringBuffer("!i[");
      }
      expr.append(m_attIndex).append("]");
      expr.append(".equals(\"")
        .append(data.attribute(m_attIndex).value((int) m_splitPoint))
        .append("\")");
    } else {
      expr = new StringBuffer("((Double) i[");
      expr.append(m_attIndex).append("])");
      if (index == 0) {
        expr.append(".doubleValue() <= ").append(m_splitPoint);
      } else {
        expr.append(".doubleValue() > ").append(m_splitPoint);
      }
    }
    return expr.toString();
  }

  /**
   * Sets split point to greatest value in given data smaller or equal to old
   * split point. (C4.5 does this for some strange reason).
   */
  public final void setSplitPoint(Instances allInstances) {

	System.out.println("BinC45Split.setSplitPoint");
    double newSplitPoint = -Double.MAX_VALUE;

    if ((allInstances.attribute(m_attIndex).isNumeric()) && (m_numSubsets > 1)) {
      for (int i = 0; i < allInstances.numInstances(); i++) {
        Instance instance = allInstances.instance(i);
        double tempValue = instance.value(m_attIndex);
        if (!Utils.isMissingValue(tempValue)) {
          if ((tempValue > newSplitPoint) && (tempValue <= m_splitPoint)) {
            newSplitPoint = tempValue;
          }
        }
      }
      m_splitPoint = newSplitPoint;
    }
  }

  /**
   * Sets distribution associated with model.
   */
  @Override
  public void resetDistribution(Instances data) throws Exception {

	// Check https://stackoverflow.com/questions/11585715/what-is-pruned-and-unpruned-tree-in-weka
//	System.out.println("\n\nBinC45Split.resetDistribution called from C45PruneableClassifierTree.getEstimatedErrorsForBranch -- line 263\n\n");
	
	/**
	 * TODO check this
	 */
//	throw new Exception();
    Instances insts = new Instances(data, data.numInstances());
    for (int i = 0; i < data.numInstances(); i++) {
      if (whichSubset(data.instance(i)) > -1) {
        insts.add(data.instance(i), false);
      }
    }
    Distribution newD = new Distribution(insts, this);
    newD.addInstWithUnknown(data, m_attIndex);
    m_distribution = newD;
  }

  /**
   * Returns weights if instance is assigned to more than one subset. Returns
   * null if instance is only assigned to one subset.
   */
  @Override
  public final double[] weights(Instance instance) {

    double[] weights;
    int i;

    if (instance.isMissing(m_attIndex)) {
      weights = new double[m_numSubsets];
      for (i = 0; i < m_numSubsets; i++) {
        weights[i] = m_distribution.perBag(i) / m_distribution.total();
      }
      return weights;
    } else {
      return null;
    }
  }

  /**
   * Returns index of subset instance is assigned to. Returns -1 if instance is
   * assigned to more than one subset.
   * 
   * @exception Exception if something goes wrong
   */

  @Override
  public final int whichSubset(Instance instance) throws Exception {

    if (instance.isMissing(m_attIndex)) {
      return -1;
    } 
    else {
      if (instance.attribute(m_attIndex).isNominal()) {
        if ((int) m_splitPoint == (int) instance.value(m_attIndex)) {
          return 0;
        } else {
          return 1;
        }
      } 
      else if(instance.attribute(m_attIndex).isString()) {
    	  
//    	  boolean foundSatisfyingInterval = false;
//    	  
//    	  Ontology reference = m_referenceOntology.get(m_instanceMap.get(instance)); // m_referenceOntology or m_bestReferenceOntology?
//    	  
//    	  if (reference == null)
//    		  return 1;
//    	  
////    	  System.out.println("BinC45Split.whichSubset");
//    	  
//    	  String cellValue = instance.stringValue(m_attIndex);
//    	  String[] tsSplittedString = cellValue.split(",");
//    	  double[] tsSplittedDouble = new double[tsSplittedString.length];
//    	  
//    	  for (int i = 0; i < tsSplittedDouble.length; i++)
//    		  tsSplittedDouble[i] = Double.parseDouble(tsSplittedString[i]);
//    	  
//    	  ArrayList<Interval> intervals = getIntervals(1, tsSplittedString.length);
//    	  
//    	  for (Interval interval : intervals) {
//    		  if (m_splitRelation.checkRelation(reference, interval)) {
////    			  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMinimum())
////    				  return 1;
//    			  
//    			  if (m_attributeVirtualMinMaxValues.get(m_instanceMap.get(instance) + interval).get(m_attIndex).getVirtualMinimum() > m_splitPoint) {
//    				  foundSatisfyingInterval = true;
//    		    	  System.out.println("Instance_" + m_instanceMap.get(instance) + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\tmoving on: " + interval);
//    			  }
//    		  }
//    	  }
//    	  
//    	  if (foundSatisfyingInterval)
//    		  return 0;
//    	  else
//    		  return 1;
    	  
    	  
    	  // If the new reference ontology has changed, then the instance goes left (i.e., 0)
    	  if (m_referenceOntology.get(m_instanceMap.get(instance)) != m_bestReferenceOntology.get(m_instanceMap.get(instance))) {
//    		  System.out.println("\t\t\t LEFT\t" + m_referenceOntology.get(m_instanceMap.get(instance)) + "\t" + m_bestReferenceOntology.get(m_instanceMap.get(instance)));
//			  System.out.println("Instance_" + m_instanceMap.get(instance) + "," + m_referenceOntology.get(m_instanceMap.get(instance)) + " |= " + ((IntervalRelation) m_splitRelation).existentialRelation() + " " + instance.attribute(m_attIndex).name() + (m_virtualOrder?" < ":" > ") + m_splitPoint + "\t\t\t moving on: " + m_bestReferenceOntology.get(m_instanceMap.get(instance)));

    		  return 0;
    	  }
    	  // Else, it goes right (i.e., 1)
    	  else {
//    		  System.out.println("\t\t\t RIGHT\t" + m_referenceOntology.get(m_instanceMap.get(instance)) + "\t" + m_bestReferenceOntology.get(m_instanceMap.get(instance)));
//			  System.out.println("Instance_" + m_instanceMap.get(instance) + "," + m_referenceOntology.get(m_instanceMap.get(instance)) + " |= " + ((IntervalRelation) m_splitRelation).universalRelation() + " " + instance.attribute(m_attIndex).name() + (m_virtualOrder?" >= ":" <= ") + m_splitPoint + "\t\t\t staying on: " + m_referenceOntology.get(m_instanceMap.get(instance)));
    		  return 1;
    	  }
//    	  return 0;
      }
      else if (instance.value(m_attIndex) <= m_splitPoint) {
        return 0;
      } 
      else {
        return 1;
      }
    }
  }
  
  /**
   * For classification.
   */
  public final int whichSubset(Instance instance, Ontology reference) throws Exception {

	  if (instance.isMissing(m_attIndex)) {
		  return -1;
	  }
	  else {
		  if (instance.attribute(m_attIndex).isString()) {
			  
	    	  String cellValue = instance.stringValue(m_attIndex);
	    	  String[] tsSplittedString = cellValue.split(",");
	    	  int numPoints = tsSplittedString.length;
	    	  double[] tsSplittedDouble = new double[numPoints];
	    	  
	    	  for (int i = 0; i < numPoints; i++)
	    		  tsSplittedDouble[i] = Double.parseDouble(tsSplittedString[i]);
	    	  
//	    	  ArrayList<Interval> intervals = getIntervals(1, numPoints);
	    	  ArrayList<Interval> intervals = ((IntervalRelation) m_splitRelation).getIntervals((Interval) reference, numPoints);
	    	  
	    	  // Qualitative interval relation
	    	  if (m_splitRelation.isQualitative()) {
				  boolean foundSatisfyingInterval = false;
				  
				  Ontology newReference = null;
				  
		    	  for (Interval interval : intervals) {
		    		  if (((QualitativeIntervalRelation) m_splitRelation).checkRelation(reference, interval)) {
		    			  
		    			  boolean holding = false;
		    			  
		    			  if (!m_virtualOrder) {
			    			  int minHits = 0;
			    			  
			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
			    			  
			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
			    				  if (tsSplittedDouble[tsIndex-1] >= m_splitPoint) {
			    					  minHits++;
			    				  }
			    			  }
			    			  
			    			  if ((double) minHits/intervalLength >= m_fractionValues)
			    				  holding = true;
		    			  }
		    			  else {
			    			  int maxHits = 0;
			    			  
			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
			    			  
			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
			    				  if (tsSplittedDouble[tsIndex-1] <= m_splitPoint) {
			    					  maxHits++;
			    				  }
			    			  }
			    			  
			    			  if ((double) maxHits/intervalLength >= m_fractionValues)
			    				  holding = true;
		    			  }
		    			  
		    			  
		    			  if (holding) {
		    				  foundSatisfyingInterval = true;
		    				  newReference = new Interval(interval.getStart(), interval.getEnd());
	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + (m_virtualOrder?" < ":" > ") + m_splitPoint + "\t\t\t moving on: " + newReference);
	
	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
		    				  // Getting the first witness
		    				  break;
		    			  }
		    			  
	//	    			  if (!holding) {
	////	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.universalRelation() + " " + instance.attribute(m_attIndex).name() + " <= " + m_splitPoint + "\t\t\t staying on: " + reference);
	//	    				  // Getting the first witness
	////	    				  break;
	//	    			  }
	//	    			  else {
	//	    				  foundSatisfyingInterval = true;
	//	    				  newReference = interval;
	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
	//	    				  // Getting the first witness
	////	    				  break;
	//	    			  }
		    		  }
		    	  }
		    	  
		    	  if (foundSatisfyingInterval) {
	//	    		  System.out.print("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + m_splitRelation.existentialRelation() + "  (LEFT) \toldReference=" + reference);
		    		  setSplitOntology(newReference);
	//	    		  System.out.println("\tnewReference=" + getSplitOntology());
		    		  return 0;
		    	  }
		    	  else {
	//	    		  System.out.print("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + m_splitRelation.universalRelation() + " (RIGHT) \toldReference=" + reference);
		    		  setSplitOntology(reference);
	//	    		  System.out.println("\tnewReference=" + getSplitOntology());
		    		  return 1;
		    	  }
	    	  }
	    	  
	    	  else if (m_splitRelation.isMetric()) {
				  boolean foundSatisfyingInterval = false;
				  
				  Ontology newReference = null;
				  
		    	  for (Interval interval : intervals) {
		    		  if (((MetricIntervalRelation) m_splitRelation).checkRelation(reference, interval, ((MetricIntervalRelation) m_splitRelation).getIntervalLength())) {
		    			  
		    			  boolean holding = false;
		    			  
		    			  if (!m_virtualOrder) {
			    			  int minHits = 0;
			    			  
			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
			    			  
			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
			    				  if (tsSplittedDouble[tsIndex-1] >= m_splitPoint) {
			    					  minHits++;
			    				  }
			    			  }
			    			  
			    			  if ((double) minHits/intervalLength >= m_fractionValues)
			    				  holding = true;
		    			  }
		    			  else {
			    			  int maxHits = 0;
			    			  
			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
			    			  
			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
			    				  if (tsSplittedDouble[tsIndex-1] <= m_splitPoint) {
			    					  maxHits++;
			    				  }
			    			  }
			    			  
			    			  if ((double) maxHits/intervalLength >= m_fractionValues)
			    				  holding = true;
		    			  }
		    			  
		    			  
		    			  if (holding) {
		    				  foundSatisfyingInterval = true;
		    				  newReference = new Interval(interval.getStart(), interval.getEnd());
	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + (m_virtualOrder?" < ":" > ") + m_splitPoint + "\t\t\t moving on: " + newReference);
	
	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
		    				  // Getting the first witness
		    				  break;
		    			  }
		    			  
	//	    			  if (!holding) {
	////	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.universalRelation() + " " + instance.attribute(m_attIndex).name() + " <= " + m_splitPoint + "\t\t\t staying on: " + reference);
	//	    				  // Getting the first witness
	////	    				  break;
	//	    			  }
	//	    			  else {
	//	    				  foundSatisfyingInterval = true;
	//	    				  newReference = interval;
	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
	//	    				  // Getting the first witness
	////	    				  break;
	//	    			  }
		    		  }
		    	  }
		    	  
		    	  if (foundSatisfyingInterval) {
//		    		  System.out.println("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + ((IntervalRelation) m_splitRelation).existentialRelation() + "  (LEFT) \toldReference=" + reference);
//		    		  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + ((IntervalRelation) m_splitRelation).existentialRelation() + " " + instance.attribute(m_attIndex).name() + " " + (m_virtualOrder?"<":">") + " " + m_splitPoint);
		    		  setSplitOntology(newReference);
	//	    		  System.out.println("\tnewReference=" + getSplitOntology());
		    		  return 0;
		    	  }
		    	  else {
//		    		  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + ((IntervalRelation) m_splitRelation).universalRelation() + " " + instance.attribute(m_attIndex).name() + " " + (m_virtualOrder?">=":"<=") + " " + m_splitPoint);
		    		  setSplitOntology(reference);
	//	    		  System.out.println("\tnewReference=" + getSplitOntology());
		    		  return 1;
		    	  }
	    	  }
		  }
	  }
	  return -1;
  }
//  public final int whichSubset(Instance instance, Ontology reference) throws Exception {
//
//	  if (instance.isMissing(m_attIndex)) {
//		  return -1;
//	  }
//	  else {
//		  if (instance.attribute(m_attIndex).isString()) {
//			  
//			  boolean foundSatisfyingInterval = false;
//			  
//			  Ontology newReference = null;
//			  
//	    	  String cellValue = instance.stringValue(m_attIndex);
//	    	  String[] tsSplittedString = cellValue.split(",");
//	    	  int numPoints = tsSplittedString.length;
//	    	  double[] tsSplittedDouble = new double[numPoints];
//	    	  
//	    	  for (int i = 0; i < numPoints; i++)
//	    		  tsSplittedDouble[i] = Double.parseDouble(tsSplittedString[i]);
//	    	  
//	    	  /**
//	    	   * TODO
//	    	   * Handle the the case without cast
//	    	   */
////	    	  ArrayList<Interval> intervals = getIntervals(1, numPoints);
//	    	  ArrayList<Interval> intervals = ((IntervalRelation) m_splitRelation).getIntervals((Interval) reference, numPoints);
//	    	  
//	    	  for (Interval interval : intervals) {
//	    		  if (m_splitRelation.isQualitative()) {
//		    		  if (((QualitativeIntervalRelation) m_splitRelation).checkRelation(reference, interval)) {
//		    			  
//		    			  boolean holding = false;
//		    			  
//		    			  if (!m_virtualOrder) {
//			    			  int minHits = 0;
//			    			  
//			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
//			    			  
//			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
//			    				  if (tsSplittedDouble[tsIndex-1] >= m_splitPoint) {
//			    					  minHits++;
//			    				  }
//			    			  }
//			    			  
//			    			  if ((double) minHits/intervalLength >= m_fractionValues)
//			    				  holding = true;
//		    			  }
//		    			  else {
//			    			  int maxHits = 0;
//			    			  
//			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
//			    			  
//			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
//			    				  if (tsSplittedDouble[tsIndex-1] <= m_splitPoint) {
//			    					  maxHits++;
//			    				  }
//			    			  }
//			    			  
//			    			  if ((double) maxHits/intervalLength >= m_fractionValues)
//			    				  holding = true;
//		    			  }
//		    			  
//		    			  
//		    			  if (holding) {
//		    				  foundSatisfyingInterval = true;
//		    				  newReference = new Interval(interval.getStart(), interval.getEnd());
//	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + (m_virtualOrder?" < ":" > ") + m_splitPoint + "\t\t\t moving on: " + newReference);
//	
//	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
//		    				  // Getting the first witness
//		    				  break;
//		    			  }
//		    			  
//	//	    			  if (!holding) {
//	////	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.universalRelation() + " " + instance.attribute(m_attIndex).name() + " <= " + m_splitPoint + "\t\t\t staying on: " + reference);
//	//	    				  // Getting the first witness
//	////	    				  break;
//	//	    			  }
//	//	    			  else {
//	//	    				  foundSatisfyingInterval = true;
//	//	    				  newReference = interval;
//	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
//	//	    				  // Getting the first witness
//	////	    				  break;
//	//	    			  }
//		    		  }
//			    	  if (foundSatisfyingInterval) {
//			    		  System.out.println("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + ((IntervalRelation) m_splitRelation).existentialRelation() + "  (LEFT) \toldReference=" + reference);
//			    		  setSplitOntology(newReference);
////			    		  System.out.println("\tnewReference=" + getSplitOntology());
//			    		  return 0;
//			    	  }
//			    	  else {
//			    		  System.out.println("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + ((IntervalRelation) m_splitRelation).universalRelation() + " (RIGHT) \toldReference=" + reference);
//			    		  setSplitOntology(reference);
////			    		  System.out.println("\tnewReference=" + getSplitOntology());
//			    		  return 1;
//			    	  }
//	    		  }
//	    		  else if (m_splitRelation.isMetric()) {
//		    		  if (((MetricIntervalRelation) m_splitRelation).checkRelation(reference, interval, ((MetricIntervalRelation) m_splitRelation).getIntervalLength())) {
//		    			  
//		    			  boolean holding = false;
//		    			  
//		    			  if (!m_virtualOrder) {
//			    			  int minHits = 0;
//			    			  
//			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
//			    			  
//			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
//			    				  if (tsSplittedDouble[tsIndex-1] >= m_splitPoint) {
//			    					  minHits++;
//			    				  }
//			    			  }
//			    			  
//			    			  if ((double) minHits/intervalLength >= m_fractionValues)
//			    				  holding = true;
//		    			  }
//		    			  else {
//			    			  int maxHits = 0;
//			    			  
//			    			  int intervalLength = interval.getEnd() - interval.getStart() + 1;
//			    			  
//			    			  for (int tsIndex = interval.getStart(); tsIndex <= interval.getEnd(); tsIndex++) {
//			    				  if (tsSplittedDouble[tsIndex-1] <= m_splitPoint) {
//			    					  maxHits++;
//			    				  }
//			    			  }
//			    			  
//			    			  if ((double) maxHits/intervalLength >= m_fractionValues)
//			    				  holding = true;
//		    			  }
//		    			  
//		    			  
//		    			  if (holding) {
//		    				  foundSatisfyingInterval = true;
//		    				  newReference = new Interval(interval.getStart(), interval.getEnd());
//	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + (m_virtualOrder?" < ":" > ") + m_splitPoint + "\t\t\t moving on: " + newReference);
//	
//	//	    				  System.out.println("Instance_" + instance.hashCode() + "," + reference + " |= " + m_splitRelation.existentialRelation() + " " + instance.attribute(m_attIndex).name() + " > " + m_splitPoint + "\t\t\t moving on: " + newReference);
//		    				  // Getting the first witness
//		    				  break;
//		    			  }
//		    		  }
//	    		  }
//		    	  if (foundSatisfyingInterval) {
//		    		  System.out.println("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + ((IntervalRelation) m_splitRelation).existentialRelation() + "  (LEFT) \toldReference=" + reference);
//		    		  setSplitOntology(newReference);
////		    		  System.out.println("\tnewReference=" + getSplitOntology());
//		    		  return 0;
//		    	  }
//		    	  else {
//		    		  System.out.println("\t\t\t\t\tInstance_" + instance.hashCode() + "\tRelation=" + ((IntervalRelation) m_splitRelation).universalRelation() + " (RIGHT) \toldReference=" + reference);
//		    		  setSplitOntology(reference);
////		    		  System.out.println("\tnewReference=" + getSplitOntology());
//		    		  return 1;
//		    	  }
//			  }
//		  }
//	  }
//	  return -1;
//  }
  
  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 14912 $");
  }
}
