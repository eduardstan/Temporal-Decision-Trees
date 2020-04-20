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
 *    BinC45ModelSelection.java
 *    Copyright (C) 1999-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.trees.j48t;

import java.io.Serializable;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * Imports added to J48T
 */
import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import timeseries.languages.*;
import timeseries.languages.interval.*;

/**
 * Class for selecting a C4.5-like binary (!) split for a given dataset.
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 10531 $
 */
public class BinC45ModelSelection extends ModelSelection {

  /** for serialization */
  private static final long serialVersionUID = 179170923545122001L;

  /** Minimum number of instances in interval. */
  protected final int m_minNoObj;

  /** Use MDL correction? */
  protected final boolean m_useMDLcorrection;

  /** The FULL training dataset. */
  protected Instances m_allData;

  /** Do not relocate split point to actual data value */
  protected final boolean m_doNotMakeSplitPointActualValue;
  
  /**
   * J48T extra options
   */
  /**
   * Witness selection method
   */
  protected boolean m_randomWitnessSelection = false;
  
  /**
   * HS language.
   */
  protected static final int LANG_HS = 0;
  
  /**
   * HS3 language.
   */
  protected static final int LANG_HS3 = 1;
  
  /**
   * HS7 language.
   */
  protected static final int LANG_HS7 = 2;
  
  /**
   * LTL language.
   */
  protected static final int LANG_LTL = 3;
  
  /**
   * HSMetric language.
   */
  protected static final int LANG_HS_METRIC = 10;
  
  /**
   * Languages.
   */
  public static final Tag[] LANGUAGES = {
		  new Tag(LANG_HS, "HS", "HS (interval-based)"),
		  new Tag(LANG_HS3, "HS3", "HS3 (interval-based)"),
		  new Tag(LANG_HS7, "HS7", "HS7 (interval-based)"),
		  new Tag(LANG_LTL, "LTL", "LTL (point-based)"),
		  new Tag(LANG_HS_METRIC, "HSM", "HS metric (interval-based)")
  };
  
  /**
   * Object language for the learned theory.
   */ 
  protected int m_objectLanguage = LANG_HS;
  protected Language m_language;
  
  /**
   * Given the object language, which relations may we consider?
   * 
   * Example, if the language is HS, there are 13 relations:
   *  1) LaterThan
   *  2) AfterThan
   *  3) Overlaps
   *  4) Ends
   *  5) During
   *  6) Begins
   *  7) InverseLaterThan
   *  8) InverseAfterThan
   *  9) InverseOverlaps
   * 10) InverseEnds
   * 11) InverseDuring
   * 12) InverseBegins
   * 13) Equals
   * A mask like "1,0,0,...,0" means that only the LaterThan relation 
   * is activated. By default, we activate all the relations.
   */
  protected String m_objectLanguageRelationMask =
		  "1,"+		// <L>
		  "1,"+		// <A>
		  "1,"+		// <O>
		  "1,"+		// <E>
		  "1,"+		// <D>
		  "1,"+		// <B>
		  "1,"+		// <EQ>
		  "1,"+		// <InvL>
		  "1,"+		// <InvA>
		  "1,"+		// <InvO>
		  "1,"+		// <InvE>
		  "1,"+		// <InvD>
		  "1"		// <InvB>
  ;
  
  /**
   * Boolean algebra.
   */
  protected static final int BOOLEAN = 0;
  
  /**
   * Heyting algebra.
   */
  protected static final int HEYTING = 1;
  
  /**
   * Algebras
   */
  public static final Tag[] ALGEBRAS = {
		  new Tag(BOOLEAN, "Boolean algebra"),
		  new Tag(HEYTING, "Heyting algebra")
  };
  
  /**
   * Propositional algebra.
   */
  protected int m_propositionalAlgebra = BOOLEAN;
  
  /**
   * Relational algebra.
   */
  protected int m_relationalAlgebra = BOOLEAN;
  
  /**
   * Fraction of values that meet the requirement for a given interval.
   */
  protected double m_fractionValues = 0.7;
  
  /**
   * Data structures for J48T
   */
  
  /**
   * Instance map.
   */
  protected HashMap<Instance, String> m_instanceMap;
  
  /**
   * For each attribute, identified by an integer number,
   * it stores the ordered domain for that attribute
   */
  protected HashMap<Integer, ArrayList<Double>> m_attributeOrderedDomain;
  
  /**
   * For each instance, it stores the reference ontology for that instance.
   */
  protected HashMap<String, Ontology> m_referenceOntology;
  
  /**
   * For each instance and for each ontology, it stores the 
   * the minimum and maximum for each attribute
   * 
   * In particular, the key is the juxtaposing of strings:
   * 	1) instance index ranging from 0 to m_allData.numInstances()-1
   * 	2) ontology representing the world (to go on)
   * 
   * The mapped value is a set of triples (attributeIndex, minimum, maximum).
   * If the ontology is a point, the the minimum is the same as maximum.
   * If the ontology is an interval, the minimum and the maximum are not
   * necessarily the same.
   * 
   *    * UPDATE: 
   * Se la domanda è p >= k, guardo nella struttura dei minimi.
   * In particolare, se il massimo dei minimi < k, allora no.
   * Altrimenti, se il massimo dei minimi >= k, allora si.
   * 
   * Assumendo che i minimi sono ordinati in maniera crescente, 
   * allora i candidati si troveranno nel suffisso della struttura.
   * 
   * Se la domanda è p <= k, guardo nella struttura dei massimi.
   * In particolare, se il minimo dei massimi > k, allora no.
   * Altrimenti, se il minimo dei massimi <= k, allora si.
   * 
   * Segue, come prima, che i candidati si troveranno nel prefisso della struttura.
   * 
   * IMPORTANT!
   * https://forums.pentaho.com/threads/62182-suggestion-how-about-adding-a-ID-field-in-WEKA-s-Instance-class/
   * https://weka.8497.n7.nabble.com/How-to-map-original-dataset-with-ID-attribute-and-trained-dataset-without-ID-attribute-td27360.html
   */
  protected HashMap<String, ArrayList<TripleAttributeVirtualMinMax>> m_attributeVirtualMinMaxValues;

  /**
   * Initializes the split selection method with the given parameters.
   * 
   * @param minNoObj minimum number of instances that have to occur in at least
   *          two subsets induced by split
   * @param allData FULL training dataset (necessary for selection of split
   *          points).
   * @param useMDLcorrection whether to use MDL adjustement when finding splits
   *          on numeric attributes
   * @param randomWitnessSelection random witness selection method
   * @param objectLanguage object language
   * @param objectLanguageRelationMask mask of included relations in the 
   *          object language
   * @param propositionalAlgebra propositional algebra
   * @param relationalAlgebra relational algebra
   */
  public BinC45ModelSelection(int minNoObj, 
    Instances allData,
    boolean useMDLcorrection, boolean doNotMakeSplitPointActualValue,
    boolean randomWitnessSelection,
    int objectLanguage,
    String objectLanguageRelationMask,
    int propositionalAlgebra,
    int relationalAlgebra,
    double fractionValues) {
    m_minNoObj = minNoObj;
    
//    for (Instance instance : allData) 
//    	instance.setReference(new Interval(0,1));
    m_allData = allData;
    
    m_useMDLcorrection = useMDLcorrection;
    m_doNotMakeSplitPointActualValue = doNotMakeSplitPointActualValue;
    
    /**
     * J48T extra options
     */
    m_randomWitnessSelection = randomWitnessSelection;
    m_objectLanguage = objectLanguage;
    m_objectLanguageRelationMask = objectLanguageRelationMask;
    m_propositionalAlgebra = propositionalAlgebra;
    m_relationalAlgebra = relationalAlgebra;
    m_fractionValues = fractionValues;
    
    try {
    	SelectedTag languageTag = new SelectedTag(objectLanguage, LANGUAGES);
    	/**
    	 * Assumption: getIDStr() returns the ID String associated with the tag integer;
    	 * thus, the String must match an implemented language class.
    	 */
    	String objLangName = languageTag.getSelectedTag().getIDStr();
    	
    	System.out.println("objLangName=" + objLangName);
    	/**
    	 * TODO 
    	 * Handle equals
    	 */
    	m_language = (Language) Class.
    			forName("timeseries.languages.interval." + objLangName).
    			getConstructor(String.class).
    			newInstance(objectLanguageRelationMask);
//    	m_language = (Language) Class.
//    			forName("timeseries.languages.interval." + objLangName).
//    			getConstructor(String.class).
//    			newInstance("1,1,1,1,1,1,1,1,1,1,1,1,0");
    }
    catch (Exception e) {
    	System.out.println(e.getMessage());
    }
    
    /**
     * J48 data structures
     */
    m_instanceMap = createInstanceMap(allData);
    
    m_attributeOrderedDomain = createAttributeOrderedDomain(allData);
    
//    System.out.println("\nDebug \nm_attributeOrderedDomain.get(5).size() = " + m_attributeOrderedDomain.get(5).size() + "\n");
    
//    System.out.println("Domain fever:");
//    for (double d : m_attributeOrderedDomain.get(0))
//    	System.out.print(d + ",");
//    System.out.println("\n");
    
    if (m_language.isIntervalType()) {
      Ontology referenceInterval = new Interval(0,1);
      m_referenceOntology = assignReferenceOntology(allData, referenceInterval);
    }
    
//    System.out.println("Debug BinC45ModelSelection constructor");
//    for (Instance instance : m_allData) 
//    	System.out.println(instance.getReference());
    
    m_attributeVirtualMinMaxValues = createAttributeVirtualMinMaxValues(allData);
    
//    System.out.println("BinC45ModelSelection.BinC45ModelSelection");
//	for (Instance instance : m_allData) 
//		System.out.println(instance.hashCode());
  }

  /**
   * Sets reference to training data to null.
   */
  public void cleanup() {

    m_allData = null;
  }

  /**
   * Selects C4.5-type split for the given dataset.
   */
  @Override
  public final ClassifierSplitModel selectModel(Instances data) {

    double minResult;
    BinC45Split[] currentModel;
    BinC45Split bestModel = null;
    NoSplit noSplitModel = null;
    double averageInfoGain = 0;
    int validModels = 0;
//    boolean multiVal = true;
    Distribution checkDistribution;
    double sumOfWeights;
    int i;
    
    System.out.println("BinC45ModelSelection # inside selectModel(Instances) -- m_allData.numInstances() " + m_allData.numInstances() + " data.numInstances() " + data.numInstances());
//    System.out.println("m_referecenOntology.size() = " + m_referenceOntology.size());
    
    
    try {

      // Check if all Instances belong to one class or if not
      // enough Instances to split.
      checkDistribution = new Distribution(data);
      noSplitModel = new NoSplit(checkDistribution);
      if (Utils.sm(checkDistribution.total(), 2 * m_minNoObj)
        || Utils.eq(checkDistribution.total(),
          checkDistribution.perClass(checkDistribution.maxClass()))) {
        return noSplitModel;
      }

      /**
       * Old J48
       */
//      // Check if all attributes are nominal and have a
//      // lot of values.
//      Enumeration<Attribute> enu = data.enumerateAttributes();
//      while (enu.hasMoreElements()) {
//        Attribute attribute = enu.nextElement();
//        if ((attribute.isNumeric())
//          || (Utils.sm(attribute.numValues(), (0.3 * m_allData.numInstances())))) {
//          multiVal = false;
//          break;
//        }
//      }
      
      /**
       * TODO: 
       * maybe data.numAttributes() - 1 ?
       */
      currentModel = new BinC45Split[data.numAttributes()];
      sumOfWeights = data.sumOfWeights();

      /**
       * J48T handling only string attributes
       */
      for (i = 0; i < data.numAttributes(); i++) {
    	  if (i != data.classIndex()) {
    		  if (data.attribute(i).isString()) {
    			  currentModel[i] = new BinC45Split(i, 
    					  m_minNoObj, 
    					  sumOfWeights, 
    					  m_useMDLcorrection,
    					  m_language,
    					  m_instanceMap,
    					  m_attributeOrderedDomain,
    					  m_referenceOntology,
    					  m_attributeVirtualMinMaxValues,
    					  m_fractionValues);
    			  currentModel[i].buildClassifier(data);
    			  
    			  if (currentModel[i].checkModel()) {
    				  averageInfoGain = averageInfoGain + currentModel[i].infoGain();
    				  validModels++;
    			  }
    		  }
    	  }
    	  /**
    	   * TODO
    	   * It follows from the fact that there are data.numAttributes() current models, adjust?
    	   */
    	  else 
    		  currentModel[i] = null;
      }
      
      
      /**
       * Old J48 handling numeric attributes
       */
//      // For each attribute.
//      for (i = 0; i < data.numAttributes(); i++) {
//
//        // Apart from class attribute.
//        if (i != (data).classIndex()) {
//
//          // Get models for current attribute.
//          currentModel[i] = new BinC45Split(i, m_minNoObj, sumOfWeights,
//            m_useMDLcorrection);
//          currentModel[i].buildClassifier(data);
//
//          // Check if useful split for current attribute
//          // exists and check for enumerated attributes with
//          // a lot of values.
//          if (currentModel[i].checkModel()) {
//            if ((data.attribute(i).isNumeric())
//              || (multiVal || Utils.sm(data.attribute(i).numValues(),
//                (0.3 * m_allData.numInstances())))) {
//              averageInfoGain = averageInfoGain + currentModel[i].infoGain();
//              validModels++;
//            }
//          }
//        } else {
//          currentModel[i] = null;
//        }
//      } 
      

      // Check if any useful split was found.
      if (validModels == 0) {
        return noSplitModel;
      }
      averageInfoGain = averageInfoGain / validModels;

      
      /**
       * J48T
       */
      
      // Find "best" attribute to split on.
      minResult = 0;
      
      for (i = 0; i < data.numAttributes(); i++) {
    	  if (i != data.classIndex() && currentModel[i].checkModel()) {
    		  if ((currentModel[i].infoGain() >= (averageInfoGain - 1E-3)) &&
    			  Utils.gr(currentModel[i].infoGain(), minResult)) {
    			  bestModel = currentModel[i];
    			  minResult = currentModel[i].infoGain();
    		  }
    	  }
      }
      
//      System.out.println("minResult=" + minResult + " order: " + (bestModel.getVirtualOrder()?"max":"min"));
//      
//      System.exit(0);
      
//      System.out.println("virtual order: " + (bestModel.getVirtualOrder()?"<":">"));
      /**
       * Old J48
       */
//      // Find "best" attribute to split on.
//      minResult = 0;
//      for (i = 0; i < data.numAttributes(); i++) {
//        if ((i != (data).classIndex()) && (currentModel[i].checkModel())) {
//          // Use 1E-3 here to get a closer approximation to the original
//          // implementation.
//          if ((currentModel[i].infoGain() >= (averageInfoGain - 1E-3))
//            && Utils.gr(currentModel[i].gainRatio(), minResult)) {
//            bestModel = currentModel[i];
//            minResult = currentModel[i].gainRatio();
//          }
//        }
//      }

      // Check if useful split was found.
      if (Utils.eq(minResult, 0)) {
        return noSplitModel;
      }

      // Add all Instances with unknown values for the corresponding
      // attribute to the distribution for the model, so that
      // the complete distribution is stored with the model.
      bestModel.distribution().addInstWithUnknown(data, bestModel.attIndex());
      
//      System.out.println("BinC45ModelSelection.selectModel \t\tBefore resetting");
//      for (String key : m_referenceOntology.keySet())
//    	  System.out.println(key + " ==> " + m_referenceOntology.get(key));
      
      // Resetting the reference ontologies
      m_referenceOntology = bestModel.getBestReferenceOntology();
//      System.out.println("\t\t <<< ending BinC45ModelSelection.selectModel >>>");
      
//      System.out.println("BinC45ModelSelection.selectModel \t\tAfter resetting");
//      for (String key : m_referenceOntology.keySet())
//    	  System.out.println(key + " ==> " + m_referenceOntology.get(key));

      /**
       * TODO
       * check this
       */
      // Set the split point analogue to C45 if attribute numeric.
//      if (!m_doNotMakeSplitPointActualValue) {
//        bestModel.setSplitPoint(m_allData);
//      }
      return bestModel;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Selects C4.5-type split for the given dataset.
   */
  @Override
  public final ClassifierSplitModel selectModel(Instances train, Instances test) {

    return selectModel(train);
  }

  /**
   * J48T functions for creating data structures
   */
  
  private HashMap<Instance, String> createInstanceMap(Instances allData) {
	  
	  int instIndex = 0;
	  HashMap<Instance, String> instanceMap = new HashMap<Instance, String>(allData.numInstances());
	  
	  for (Instance instance : allData) {
		  instanceMap.put(instance, Integer.toString(instIndex));
		  instIndex++;
	  }
	  
	  return instanceMap;
  }
  
  private HashMap<String, ArrayList<TripleAttributeVirtualMinMax>> createAttributeVirtualMinMaxValues(Instances data) {
	  
	  HashMap<String, ArrayList<TripleAttributeVirtualMinMax>> attributeValues = new HashMap<String, ArrayList<TripleAttributeVirtualMinMax>>();
	  
	  // Instance key
//	  String instanceKey;
	  
	  // Minimum and maximum
	  double[] minAndMax;
	  
	  // Minimum value
	  double min;
	  
	  // Maximum value
	  double max;
	  
	  // The time series in the cell that is a string
	  String cellValue = data.instance(0).stringValue(0);
	  
	  // Split the values of the time series into String[]
	  String[] tsSplittedString = cellValue.split(",");
	  
	  // Number of points
	  /**
	   * TODO is it ok?
	   */
	  // ASSUMPTION: each time series has the same number of points
	  int numPoints = tsSplittedString.length;
	  
	  // Time series values as doubles
	  double[] tsSplittedDouble = new double[numPoints];
	  
	  // Ontologies
	  ArrayList<Interval> ontologies;
	  
	  /**
	   * TODO: add Point ontology
	   */
	  // Get intervals from 1 to numPoints (included)
	  ontologies = getIntervals(1, numPoints);
	  
	  // For each instance
	  for (Instance instance : data) {
//		  instanceKey = "";
//		  for (int attrIndex = 0; attrIndex < data.numAttributes(); attrIndex++)
//			  instanceKey += instance.stringValue(attrIndex);
//	  for (int instIndex = 0; instIndex < data.numInstances(); instIndex++) {
		  // For each interval
		  for (Interval interval : ontologies) {
			  // For each attribute
			  for (int attrIndex = 0; attrIndex < data.numAttributes(); attrIndex++) {
				  // That is not the class attribute
				  if (attrIndex != data.classIndex()) {
					  
					  cellValue = instance.stringValue(attrIndex);
					  tsSplittedString = cellValue.split(",");
					  
					  /**
					   * TODO: I can add this for loop inside getMinimumAndMaximum defined over a String[] instead of double[]
					   */
					  // Convert each string value to a double value
					  for (int stringValueIndex = 0; stringValueIndex < numPoints; stringValueIndex++)
						  tsSplittedDouble[stringValueIndex] = Double.parseDouble(tsSplittedString[stringValueIndex]);
					  
					  minAndMax = getVirtualMinimumAndMaximum(m_attributeOrderedDomain.get(attrIndex), tsSplittedDouble, interval.getStart()-1, interval.getEnd()-1);
					  
					  min = minAndMax[0];
					  
					  max = minAndMax[1];	
					  
//					  System.out.println(attrIndex + ",[" + (interval.getStart()-1) + "," + (interval.getEnd()-1) + "] ==> " + min + " -- " + max);
					  
					  TripleAttributeVirtualMinMax triple = new TripleAttributeVirtualMinMax(attrIndex, min, max);
					  
					  if (attributeValues.get(m_instanceMap.get(instance) + interval) == null) {
						  
//							  System.out.println(Integer.toString(instIndex) + interval + " is null ==> adding attribute " + attrIndex);
						  
						  // The mapped list
						  ArrayList<TripleAttributeVirtualMinMax> triplesList = new ArrayList<TripleAttributeVirtualMinMax>();
						  triplesList.add(triple);
						  attributeValues.put(m_instanceMap.get(instance) + interval, triplesList);
					  }
					  else {
//							  System.out.println(Integer.toString(instIndex) + interval + " is NOT null ==> adding attribute " + attrIndex + "; before adding there are " + attributeValues.get(Integer.toString(instIndex) + interval).size() + " triples");
						  attributeValues.get(m_instanceMap.get(instance) + interval).add(triple);
					  }
				  }
			  }
		  }
	  }
	  
//	  System.out.println("\n\nattributeValues.size() = " + attributeValues.size() + "\n\n");
	  
//	  System.out.println("\n\n\nSize of instance 1 on interval [1,2]: " + attributeValues.get(Integer.toString(1) + "[1,2]").size() + "\n\n");
	  return attributeValues;
  }
  
  private double[] getVirtualMinimumAndMaximum(ArrayList<Double> orderedValues, double[] values, int start, int end) {
	  
	  double[] virtualMinAndMax;
	  
	  int left;
	  int right;
	  int k;
	  
	  double virtualMin = Double.NaN;
	  int bestVirtualMinIndex = -1;
	  int minHits = 0;
	  
	  double virtualMax = Double.NaN;
	  int bestVirtualMaxIndex = -1;
	  int maxHits = 0;
	  
	  left = 0;
	  right = orderedValues.size();
	  k = (right - left)/2;
	  
//	  System.out.println("\t\tMINIMUM:");
//	  System.out.println("left \tk \tcorr \tright \tnew_k \tfraction");
//	  System.out.println("---------------------------------------------------------");
	  while (left < right) {
		  minHits = 0;
//		  System.out.print(left + "\t" + k + "\t" + bestVirtualMinIndex + " \t" + right + "\t");
		  for (int i=start; i<= end; i++)
			  if (values[i] >= orderedValues.get(k))
				  minHits++;
		  		  
		  // If the values that respect k are >= m_fractionValues, increase k
		  if ((double) minHits/(end-start+1) >= m_fractionValues) {
			  left = k+1;
			  bestVirtualMinIndex = k;
		  }
		  // Else, decrease k
		  else {
			  right = k;
//			  bestVirtualMinIndex = k;
		  }
		  k = left + (right - left)/2;
//		  System.out.println(k + "\t" + (double) minHits/(end-start+1));
//		  System.out.println(left + "\t" + k + "\t" + virtualMinIndex + " \t" + right + "\t" + (double) minHits/(end-start+1));
	  }
	  
//	  k = bestVirtualMinIndex;
//	  System.out.println("\n\tk=" + k + "\n\n");
	  virtualMin = orderedValues.get(bestVirtualMinIndex);
	  
//	  minHits = 0;
//	  for (int i=start; i<= end; i++)
//		  if (values[i] >= virtualMin)
//			  minHits++;
//	  		  
//	  // If the values that respect k are >= m_fractionValues, increase k
//	  if ((double) minHits/(end-start+1) >= m_fractionValues) {
//		  System.out.println("IT HOLDS! " + (double) minHits/(end-start+1));
//	  }
//	  // Else, decrease k
//	  else {
//		  System.out.println("IT DOES NOT HOLD! " + (double) minHits/(end-start+1));
//	  }
	  
//	  minHits = 0;
//	  for (int i=start; i<= end; i++)
//		  if (values[i] >= orderedValues.get(bestVirtualMinIndex))
//			  minHits++;
//	  		  
//	  // If the values that respect k are >= m_fractionValues, increase k
//	  if ((double) minHits/(end-start+1) >= m_fractionValues) {
//		  System.out.println("IT HOLDS! " + (double) minHits/(end-start+1));
//	  }
//	  // Else, decrease k
//	  else {
//		  System.out.println("IT DOES NOT HOLD! " + (double) minHits/(end-start+1));
//	  }
	  

	  left = 0;
	  right = orderedValues.size();
	  k = (right - left)/2;
	  
//	  System.out.println("\t\tMAXIMUM:");
//	  System.out.println("left \tk \tcorr \tright \tnew_k \tfraction");
//	  System.out.println("---------------------------------------------------------");
	  while (left < right) {
		  maxHits = 0;
//		  System.out.print(left + "\t" + k + "\t" + bestVirtualMaxIndex + " \t" + right + "\t");
		  for (int i=start; i<= end; i++)
			  if (values[i] <= orderedValues.get(k))
				  maxHits++;
		  		  
		  // If the values that respect k are >= m_fractionValues, increase k
		  if ((double) maxHits/(end-start+1) >= m_fractionValues) {
			  right = k;
			  bestVirtualMaxIndex = k;
		  }
		  // Else, decrease k
		  else {
			  left = k+1;
//			  bestVirtualMinIndex = k;
		  }
		  k = left + (right - left)/2;
//		  System.out.println(k + "\t" + (double) maxHits/(end-start+1));
//		  System.out.println(left + "\t" + k + "\t" + virtualMinIndex + " \t" + right + "\t" + (double) minHits/(end-start+1));
	  }
	  
//	  k = bestVirtualMaxIndex;
//	  System.out.println("\n\tk=" + k + "\n\n");
	  virtualMax = orderedValues.get(bestVirtualMaxIndex);
	  
//	  maxHits = 0;
//	  for (int i=start; i<= end; i++)
//		  if (values[i] <= virtualMax)
//			  maxHits++;
//	  		  
//	  // If the values that respect k are >= m_fractionValues, increase k
//	  if ((double) maxHits/(end-start+1) >= m_fractionValues) {
//		  System.out.println("IT HOLDS! " + (double) maxHits/(end-start+1));
//	  }
//	  // Else, decrease k
//	  else {
//		  System.out.println("IT DOES NOT HOLD! " + (double) maxHits/(end-start+1));
//	  }
//	  
//	  System.out.println("\n\n\n");
	  virtualMinAndMax = new double[] {virtualMin, virtualMax};
	  
//	  System.out.println("[" + start + "," + end + "] ==> " + virtualMin + " -- " + virtualMax);
	  
	  return virtualMinAndMax;
  }
  
  public double[] getMinimumAndMaximum(double[] values, int start, int end) {
	  
	  double min = values[start];
	  
	  double max = values[start];
	  
	  double[] minAndMax;
	  
	  for (int i = start+1; i <= end; i++) {
		  if (values[i] < min)
			  min = values[i];
		  if (values[i] > max)
			  max = values[i];
	  }
	  
	  minAndMax = new double[] {min, max};
	  
	  return minAndMax;
  }
  
  /**
   * TODO
   * private or public?
   */
  private ArrayList<Interval> getIntervals(int start, int end) {
	  
	  int numPoints = end - start + 1;
	  ArrayList<Interval> intervals = new ArrayList<Interval>(numPoints*(numPoints-1)/2);
	  
	  for (int x = start; x < end; x++)
		  for (int y = x+1; y <= end; y++)
			  intervals.add(new Interval(x,y));
	  
	  return intervals;
  }
  
  /**
   * Function that computes the ordered domain of all attributes
   * @param data instances to compute the ordered domain
   * @return a map that maps integers (attributes) to ordered domains (of doubles)
   */
  private HashMap<Integer,ArrayList<Double>> createAttributeOrderedDomain(Instances data) {
	  
	  HashMap<Integer,ArrayList<Double>> attributeDomain = new HashMap<Integer,ArrayList<Double>>(data.numAttributes());

	  // For each attribute
	  for (int attrIndex = 0; attrIndex < data.numAttributes(); attrIndex++) {
		  // That is not the class attribute
		  if (attrIndex != data.classIndex()) {
			  
			  // Create an empty set of double called values to store unique values 
			  // for the attribute attrIndex
			  Set<Double> values = new HashSet<Double>();
			  
			  // For each instance
			  for (int instIndex = 0; instIndex < data.numInstances(); instIndex++) {
				  
				  // Get the cellValue in position (instIndex, attrIndex) (i.e., a time series)
				  String cellValue = data.instance(instIndex).stringValue(attrIndex);
				  // Split cellValue into String[]
				  String[] tsSplittedString = cellValue.split(",");
				  
				  int numPoints = tsSplittedString.length;
				  
				  // For each element in tsSplittedString convert it to a double,
				  // and store it in the values set
				  for (int i = 0; i < numPoints; i++)
					  values.add(Double.parseDouble(tsSplittedString[i]));
			  }
			  
			  // Sort the values using https://stackoverflow.com/questions/740299/how-do-i-sort-a-set-to-a-list-in-java
			  // then put at attrIndex the ordered (double) values
			  attributeDomain.put(attrIndex, new ArrayList<Double>(new TreeSet<Double>(values)));
		  }
	  }
	  
	  return attributeDomain;
  }

  /**
   * Function that assigns the reference ontology for each instance represented as an integer
   * @param data set of instances to assign the reference ontology
   * @param ontology reference ontology
   * @return a map that maps integers (instances) to (reference) ontologies
   */
  public HashMap<String, Ontology> assignReferenceOntology(Instances data, Ontology ontology) {
	  
	  HashMap<String, Ontology> reference = new HashMap<String, Ontology>(data.numInstances());
	  String key = "";
	  
	  // For each instance
	  for (Instance instance : data) {
		  key = "";
		  for (int attrIndex = 0; attrIndex < data.numAttributes(); attrIndex++)
			  key += instance.stringValue(attrIndex);
		  
		  try {
			  // Store the reference ontology
			  reference.put(m_instanceMap.get(instance), ontology);
		  }
		  catch (Exception e) {
			  System.out.println(e.getMessage());
		  }
	  }
	  
	  return reference;
  }
  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 10531 $");
  }
}
