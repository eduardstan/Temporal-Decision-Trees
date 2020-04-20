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
 *    J48.java
 *    Copyright (C) 1999-2012 University of Waikato, Hamilton, New Zealand
 *
 */
/**
 * TODO
 * Add comment to J48T parameters tips, get, set
 */
package weka.classifiers.trees;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Sourcable;
import weka.classifiers.trees.j48t.BinC45ModelSelection;
import weka.classifiers.trees.j48t.C45ModelSelection;
import weka.classifiers.trees.j48t.C45PruneableClassifierTree;
import weka.classifiers.trees.j48t.ClassifierTree;
import weka.classifiers.trees.j48t.ModelSelection;
import weka.classifiers.trees.j48t.PruneableClassifierTree;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Matchable;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.PartitionGenerator;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Summarizable;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
//import weka.core.Capabilities;
import weka.core.Capabilities.Capability;

/**
 * <!-- globalinfo-start --> Class for generating a pruned or unpruned C4.5
 * decision tree. For more information, see<br/>
 * <br/>
 * Ross Quinlan (1993). C4.5: Programs for Machine Learning. Morgan Kaufmann
 * Publishers, San Mateo, CA.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start --> BibTeX:
 * 
 * <pre>
 * &#64;book{Quinlan1993,
 *    address = {San Mateo, CA},
 *    author = {Ross Quinlan},
 *    publisher = {Morgan Kaufmann Publishers},
 *    title = {C4.5: Programs for Machine Learning},
 *    year = {1993}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -U
 *  Use unpruned tree.
 * </pre>
 * 
 * <pre>
 * -O
 *  Do not collapse tree.
 * </pre>
 * 
 * <pre>
 * -C &lt;pruning confidence&gt;
 *  Set confidence threshold for pruning.
 *  (default 0.25)
 * </pre>
 * 
 * <pre>
 * -M &lt;minimum number of instances&gt;
 *  Set minimum number of instances per leaf.
 *  (default 2)
 * </pre>
 * 
 * <pre>
 * -R
 *  Use reduced error pruning.
 * </pre>
 * 
 * <pre>
 * -N &lt;number of folds&gt;
 *  Set number of folds for reduced error
 *  pruning. One fold is used as pruning set.
 *  (default 3)
 * </pre>
 * 
 * <pre>
 * -B
 *  Use binary splits only.
 * </pre>
 * 
 * <pre>
 * -S
 *  Don't perform subtree raising.
 * </pre>
 * 
 * <pre>
 * -L
 *  Do not clean up after the tree has been built.
 * </pre>
 * 
 * <pre>
 * -A
 *  Laplace smoothing for predicted probabilities.
 * </pre>
 * 
 * <pre>
 * -J
 *  Do not use MDL correction for info gain on numeric attributes.
 * </pre>
 * 
 * <pre>
 * -Q &lt;seed&gt;
 *  Seed for random data shuffling (default 1).
 * </pre>
 * 
 * <pre>
 * -doNotMakeSplitPointActualValue
 *  Do not make split point actual value.
 * </pre>
 * 
 * <pre>
 * -fractionValues &lt;proportion&gt;
 * Fraction of values that meet the requirement for a given interval. (Default: 0.7)
 * </pre>
 * 
 * <pre>
 * -randomWitnessSelection
 *  Random witness selection method. (Default: false)
 *  </pre>
 *  
 * <pre>
 * -objectLanguage &lt;language&gt;
 *  Object language. (Default: HS)
 * </pre>
 *  
 * <pre>
 * -objectLanguageRelationMask &lt;mask&gt;
 *  The mask of included relations in the object language. (Default: 1,1,1,...,1)
 * </pre>
 *  
 * <pre>
 * -propositionalAlgebra &lt;algebra&gt;
 *  Propositional algebra. (Default: Boolean)
 * </pre>
 *  
 * <pre>
 * -relationalAlgebra &lt;algebra&gt;
 *  Relational algebra. (Default: Boolean)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 15233 $
 */
public class J48T extends AbstractClassifier implements OptionHandler, Drawable,
  Matchable, Sourcable, WeightedInstancesHandler, Summarizable,
  AdditionalMeasureProducer, TechnicalInformationHandler, PartitionGenerator {

  /** for serialization */
  static final long serialVersionUID = -217733168393644444L;

  /** The decision tree */
  protected ClassifierTree m_root;

  /** Unpruned tree? */
  protected boolean m_unpruned = false;

  /** Collapse tree? */
  /**
   * Note that, even when pruning is turned off, and regardless of the value 
   * of the confidence parameter, C4.5/J48 will still perform a tree 
   * "collapsing" operation where subtrees are pruned if this doesn't 
   * increase raw classification error on the training data (e.g. if there is 
   * a subtree with two leaf nodes that have the same classification, this 
   * subtree would be replaced by a single leaf). In WEKA 3.7, you can turn 
   * collapsing off (e.g. if you want to use J48 for probability estimation 
   * rather than classification). 
   */
  protected boolean m_collapseTree = true;

  /** Confidence level (used by Error Based Pruning) */
  protected float m_CF = 0.25f;

  /** Minimum number of instances */
  protected int m_minNumObj = 2;

  /** Use MDL correction? */
  protected boolean m_useMDLcorrection = true;

  /**
   * Determines whether probabilities are smoothed using Laplace correction when
   * predictions are generated
   */
  protected boolean m_useLaplace = false;

  /** Use reduced error pruning? */
  protected boolean m_reducedErrorPruning = false;

  /** Number of folds for reduced error pruning. */
  protected int m_numFolds = 3;

  /** Binary splits on nominal attributes? */
  protected boolean m_binarySplits = false; // TODO: modify it?

  /** 
   * Subtree raising to be performed? <br>
   * https://stackoverflow.com/questions/11585715/what-is-pruned-and-unpruned-tree-in-weka
   */
  protected boolean m_subtreeRaising = true;

  /** Cleanup after the tree has been built. */
  protected boolean m_noCleanup = false;

  /** Random number seed for reduced-error pruning. */
  protected int m_Seed = 1;

  /** Do not relocate split point to actual data value */
  protected boolean m_doNotMakeSplitPointActualValue;
  
  /**
   * J48T extra options
   */
  /**
   * Random witness selection method.
   * If true, then the witness is randomly selected from a set of witnesses;
   * otherwise, the witness is selected deterministically
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
  
//  /**
//   * Interval languages for warnings.
//   */
//  public static final Tag[] INTERVAL_LANGUAGES = {
//		  new Tag(LANG_HS, "HS", "HS language"),
//		  new Tag(LANG_HS3, "HS3", "HS3 language"),
//		  new Tag(LANG_HS7, "HS7", "HS7 language")
//  };
  
  /**
   * Object language for the learned theory.
   */ 
  protected int m_objectLanguage = LANG_HS;
  
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
  //protected String m_propositionalAlgebra = "Boolean";
  
  /**
   * Relational algebra.
   */
  protected int m_relationalAlgebra = BOOLEAN;
//  protected String m_relationalAlgebra = "Boolean";
  /**
   * Fraction of values that meet the requirement for a given interval.
   */
  protected double m_fractionValues = 0.7;
  
  /**
   * Returns a string describing classifier
   * 
   * @return a description suitable for displaying in the explorer/experimenter
   *         gui
   */
  public String globalInfo() {

//    return "Class for generating a pruned or unpruned C4.5 decision tree. For more "
//      + "information, see\n\n" + getTechnicalInformation().toString();
	  return "Class for generating a pruned or unpruned decision tree using temporal data."
        + "The algorithm is based on J48 code: \n\n"
	    + getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing detailed
   * information about the technical background of this class, e.g., paper
   * reference or book this class is based on.
   * 
   * @return the technical information about this class
   */
  @Override
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result;

    result = new TechnicalInformation(Type.INPROCEEDINGS);
    result.setValue(Field.AUTHOR, "A. Brunello, G. Sciavicco, I. E. Stan");
    result.setValue(Field.YEAR, "2019");
    result.setValue(Field.TITLE, "Interval Temporal Logic Decision Tree Learning");
    result.setValue(Field.BOOKTITLE, "Proceedings of the 16th European Conference on Logics in Artificial Intelligence (JELIA)");
    result.setValue(Field.SERIES, "Lecture Notes on Computer Science (subseries in Artificial Intelligence)");
    result.setValue(Field.NUMBER, "11468");
    result.setValue(Field.PAGES, "778-793");
    result.setValue(Field.ISBN, "978-3-030-19570-0");
    result.setValue(Field.URL, "https://link.springer.com/chapter/10.1007/978-3-030-19570-0_50");

    return result;
  }

  /**
   * Returns default capabilities of the classifier.
   * 
   * @return the capabilities of this classifier
   */
  @Override
  public Capabilities getCapabilities() {
    Capabilities result;

    result = new Capabilities(this);
    result.disableAll();
    // attributes
    /**
     * Up to now, J48T handles only String values
     */
    result.enable(Capability.STRING_ATTRIBUTES);
    
    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    
    // instances
    result.setMinimumNumberInstances(0);

    return result;
  }

  /**
   * Generates the classifier.
   * 
   * @param instances the data to train the classifier with
   * @throws Exception if classifier can't be built successfully
   */
  @Override
  public void buildClassifier(Instances instances) throws Exception {
	
//	System.out.println("\n\n\nJ48T # inside buildClassifier(Instances) -- instances.numInstances() " + instances.numInstances());

    if ((m_unpruned) && (!m_subtreeRaising)) {
      throw new Exception("Subtree raising does not need to be unset for unpruned trees!");
    }
    if ((m_unpruned) && (m_reducedErrorPruning)) {
      throw new Exception("Unpruned tree and reduced error pruning cannot be selected simultaneously!");
    }
    if ((m_unpruned) && (m_CF != 0.25f)) {
      throw new Exception("It does not make sense to change the confidence for an unpruned tree!");
    }
    if ((m_reducedErrorPruning) && (m_CF != 0.25f)) {
      throw new Exception("Changing the confidence does not make sense for reduced error pruning.");
    }
    if ((!m_reducedErrorPruning) && (m_numFolds != 3)) {
      throw new Exception("Changing the number of folds does not make sense if"
              + " reduced error pruning is not selected.");
    }
    if ((!m_reducedErrorPruning) && (m_Seed != 1)) {
      throw new Exception("Changing the seed does not make sense if"
              + " reduced error pruning is not selected.");
    }
    if ((m_CF <= 0) || (m_CF >= 1)) {
      throw new Exception("Confidence has to be greater than zero and smaller than one!");
    }
    /**
     * TODO
     * Add conditions for exceptions
     */
    getCapabilities().testWithFail(instances);

    ModelSelection modSelection;

    if (m_binarySplits) {
      modSelection = new BinC45ModelSelection(m_minNumObj, instances,
        m_useMDLcorrection, m_doNotMakeSplitPointActualValue,
        m_randomWitnessSelection,
        m_objectLanguage,
        m_objectLanguageRelationMask,
        m_propositionalAlgebra,
        m_relationalAlgebra,
        m_fractionValues);
    } else {
      modSelection = new C45ModelSelection(m_minNumObj, instances,
        m_useMDLcorrection, m_doNotMakeSplitPointActualValue);
    }
    /**
     * TODO: 
     * Look inside C45PruneableClassifierTree
     */
    if (!m_reducedErrorPruning) {
      m_root = new C45PruneableClassifierTree(modSelection, !m_unpruned, m_CF,
        m_subtreeRaising, !m_noCleanup, m_collapseTree);
    } else {
      m_root = new PruneableClassifierTree(modSelection, !m_unpruned,
        m_numFolds, !m_noCleanup, m_Seed);
    }
    m_root.buildClassifier(instances);
    if (m_binarySplits) {
      ((BinC45ModelSelection) modSelection).cleanup();
    } else {
      ((C45ModelSelection) modSelection).cleanup();
    }
  }

  /**
   * Classifies an instance.
   * 
   * @param instance the instance to classify
   * @return the classification for the instance
   * @throws Exception if instance can't be classified successfully
   */
  @Override
  public double classifyInstance(Instance instance) throws Exception {
	
    return m_root.classifyInstance(instance);
  }

  /**
   * Returns class probabilities for an instance.
   * 
   * @param instance the instance to calculate the class probabilities for
   * @return the class probabilities
   * @throws Exception if distribution can't be computed successfully
   */
  @Override
  public final double[] distributionForInstance(Instance instance)
    throws Exception {
	    
//	System.out.println("\t\t\tJ48T.distributionForInstance(instance)");
//	System.out.println("\n");
//	System.out.println("\t\tinstance: " + instance);
    return m_root.distributionForInstance(instance, m_useLaplace);
  }

  /**
   * Returns the type of graph this classifier represents.
   * 
   * @return Drawable.TREE
   */
  @Override
  public int graphType() {
    return Drawable.TREE;
  }

  /**
   * Returns graph describing the tree.
   * 
   * @return the graph describing the tree
   * @throws Exception if graph can't be computed
   */
  @Override
  public String graph() throws Exception {

    return m_root.graph();
  }

  /**
   * Returns tree in prefix order.
   * 
   * @return the tree in prefix order
   * @throws Exception if something goes wrong
   */
  @Override
  public String prefix() throws Exception {

    return m_root.prefix();
  }

  /**
   * Returns tree as an if-then statement.
   * 
   * @param className the name of the Java class
   * @return the tree as a Java if-then type statement
   * @throws Exception if something goes wrong
   */
  @Override
  public String toSource(String className) throws Exception {

    StringBuffer[] source = m_root.toSource(className);
    return "class " + className + " {\n\n"
      + "  public static double classify(Object[] i)\n"
      + "    throws Exception {\n\n" + "    double p = Double.NaN;\n"
      + source[0] // Assignment code
      + "    return p;\n" + "  }\n" + source[1] // Support code
      + "}\n";
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * Valid options are:
   * <p>
   * 
   * -U <br>
   * Use unpruned tree.
   * <p>
   * 
   * -C confidence <br>
   * Set confidence threshold for pruning. (Default: 0.25)
   * <p>
   * 
   * -M number <br>
   * Set minimum number of instances per leaf. (Default: 2)
   * <p>
   * 
   * -R <br>
   * Use reduced error pruning. No subtree raising is performed.
   * <p>
   * 
   * -N number <br>
   * Set number of folds for reduced error pruning. One fold is used as the
   * pruning set. (Default: 3)
   * <p>
   * 
   * -B <br>
   * Use binary splits for nominal attributes.
   * <p>
   * 
   * -S <br>
   * Don't perform subtree raising.
   * <p>
   * 
   * -L <br>
   * Do not clean up after the tree has been built.
   * 
   * -A <br>
   * If set, Laplace smoothing is used for predicted probabilites.
   * <p>
   * 
   * -Q <br>
   * The seed for reduced-error pruning.
   * <p>
   * 
   * -fractionValues proportion <br>
   * Fraction of values that meet the requirement for a given interval. (Default: 0.7)
   * <p>
   * 
   * -randomWitnessSelection <br>
   * Random witness selection method. (Default: false)
   * <p>
   * 
   * -objectLanguage language <br>
   * Object language. (Default: HS)
   * <p>
   * 
   * -objectLanguageRelationMask mask <br>
   * The mask of included relations in the object language. (Default: "1,1,1,...,1") 
   * 
   * -propositionalAlgebra algebra <br>
   * Propositional algebra. (Default: Boolean)
   * <p>
   * 
   * -relationalAlgebra algebra <br>
   * Relational algebra. (Default: Boolean)
   * <p>
   * 
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {

    Vector<Option> newVector = new Vector<Option>(19);

    newVector.addElement(new Option("\tUse unpruned tree.", "U", 0, "-U"));
    newVector.addElement(new Option("\tDo not collapse tree.", "O", 0, "-O"));
    newVector.addElement(new Option("\tSet confidence threshold for pruning.\n"
      + "\t(default 0.25)", "C", 1, "-C <pruning confidence>"));
    newVector.addElement(new Option(
      "\tSet minimum number of instances per leaf.\n" + "\t(default 2)", "M",
      1, "-M <minimum number of instances>"));
    newVector.addElement(new Option("\tUse reduced error pruning.", "R", 0,
      "-R"));
    newVector.addElement(new Option("\tSet number of folds for reduced error\n"
      + "\tpruning. One fold is used as pruning set.\n" + "\t(default 3)", "N",
      1, "-N <number of folds>"));
    newVector.addElement(new Option("\tUse binary splits only.", "B", 0, "-B"));
    newVector.addElement(new Option("\tDo not perform subtree raising.", "S", 0,
      "-S"));
    newVector.addElement(new Option(
      "\tDo not clean up after the tree has been built.", "L", 0, "-L"));
    newVector.addElement(new Option(
      "\tLaplace smoothing for predicted probabilities.", "A", 0, "-A"));
    newVector.addElement(new Option(
      "\tDo not use MDL correction for info gain on numeric attributes.", "J",
      0, "-J"));
    newVector.addElement(new Option(
      "\tSeed for random data shuffling (default 1).", "Q", 1, "-Q <seed>"));
    newVector.addElement(new Option("\tDo not make split point actual value.",
      "-doNotMakeSplitPointActualValue", 0, "-doNotMakeSplitPointActualValue"));
    
    /**
     * J48T extra options.
     */
    
    /**
     * Fraction of values that meet the requirement for a given interval.
     */
    newVector.addElement(new Option(
    		"\tFraction of values that meet the requirement for a given interval.",
    		"fractionValues",
    		1,
    		"-fractionValues"
    ));
    
    /**
     * Random witness selection method.
     */
    newVector.addElement(new Option(
    		"\tRandom witness selection method.",
    		"randomWitnessSelection",
    		1,
    		"-randomWitnessSelection"
    ));
    /**
     * Object language.
     */
    newVector.addElement(new Option(
    		"\tObject language.",
    		"objectLanguage",
    		1,
    		"-objectLanguage <0=HS | 1=HS3 | 2=HS7 | 3=LTL | 10=HSMetric>"
    ));
    /**
     * The mask of included relations in the object language.
     */
    newVector.addElement(new Option(
    		"\tThe mask of included relations in the object language.",
    		"objectLanguageRelationMask",
    		1,
    		"-objectLanguageRelationMask"
    ));
    /**
     * Propositional algebra.
     */
//    newVector.addElement(new Option(
//    		"\tPropositional algebra.",
//    		"propositionalAlgebra",
//    		1,
//    		"-propositionalAlgebra"
//    ));
    newVector.add(new Option(
    		"\tPropositional algebra.", 
    		"propositionalAlgebra", 
    		1, 
    		"-propositionalAlgebra <0=Boolean | 1=Heyting>"
    ));
    /**
     * Relational algebra.
     */
//    newVector.addElement(new Option(
//    		"\tRelational algebra.",
//    		"relationalAlgebra",
//    		1,
//    		"-relationalAlgebra"
//    ));
    newVector.add(new Option(
    		"\tRelationa algebra.", 
    		"relationalAlgebra", 
    		1, 
    		"-relationalAlgebra <0=Boolean | 1=Heyting>"
    ));

    newVector.addAll(Collections.list(super.listOptions()));

    return newVector.elements();
  }

  /**
   * Parses a given list of options.
   * 
   * <!-- options-start --> Valid options are:
   * <p/>
   * 
   * <pre>
   * -U
   *  Use unpruned tree.
   * </pre>
   * 
   * <pre>
   * -O
   *  Do not collapse tree.
   * </pre>
   * 
   * <pre>
   * -C &lt;pruning confidence&gt;
   *  Set confidence threshold for pruning.
   *  (default 0.25)
   * </pre>
   * 
   * <pre>
   * -M &lt;minimum number of instances&gt;
   *  Set minimum number of instances per leaf.
   *  (default 2)
   * </pre>
   * 
   * <pre>
   * -R
   *  Use reduced error pruning.
   * </pre>
   * 
   * <pre>
   * -N &lt;number of folds&gt;
   *  Set number of folds for reduced error
   *  pruning. One fold is used as pruning set.
   *  (default 3)
   * </pre>
   * 
   * <pre>
   * -B
   *  Use binary splits only.
   * </pre>
   * 
   * <pre>
   * -S
   *  Don't perform subtree raising.
   * </pre>
   * 
   * <pre>
   * -L
   *  Do not clean up after the tree has been built.
   * </pre>
   * 
   * <pre>
   * -A
   *  Laplace smoothing for predicted probabilities.
   * </pre>
   * 
   * <pre>
   * -J
   *  Do not use MDL correction for info gain on numeric attributes.
   * </pre>
   * 
   * <pre>
   * -Q &lt;seed&gt;
   *  Seed for random data shuffling (default 1).
   * </pre>
   * 
   * <pre>
   * -doNotMakeSplitPointActualValue
   *  Do not make split point actual value.
   * </pre>
   * 
   * <pre>
   * -fractionValues &lt;proportion&gt;
   * Fraction of values that meet the requirement for a given interval. (default 0.7)
   * </pre>
   * 
   * <pre>
   * -randomWitnessSelection
   * Random witness selection method (default false).
   * </pre>
   * 
   * <pre>
   * -objectLanguage &lt;language&gt;
   * Object language. (default "HS")
   * </pre>
   * 
   * <pre>
   * -objectLanguageRelationMask &lt;mask&gt;
   * The mask of included relations in the object language. (default "1,1,1,...,1")
   * </pre>
   * 
   * <pre>
   * -propositionalAlgebra &lt;0=Boolean, 1=Heyting&gt;
   * Propositional algebra. (default Boolean)
   * </pre>
   * 
   * <pre>
   * -relationalAlgebra &lt;0=Boolean, 1=Heyting&gt;
   * Relational algebra. (default Boolean)
   * </pre>
   * 
   * <!-- options-end -->
   * 
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {

    // Other options
    String minNumString = Utils.getOption('M', options);
    if (minNumString.length() != 0) {
      m_minNumObj = Integer.parseInt(minNumString);
    } else {
      m_minNumObj = 2;
    }
    m_binarySplits = Utils.getFlag('B', options);
    m_useLaplace = Utils.getFlag('A', options);
    m_useMDLcorrection = !Utils.getFlag('J', options);

    // Pruning options
    m_unpruned = Utils.getFlag('U', options);
    m_collapseTree = !Utils.getFlag('O', options);
    m_subtreeRaising = !Utils.getFlag('S', options);
    m_noCleanup = Utils.getFlag('L', options);
    m_doNotMakeSplitPointActualValue = Utils.getFlag("doNotMakeSplitPointActualValue", options);
    m_reducedErrorPruning = Utils.getFlag('R', options);
    String confidenceString = Utils.getOption('C', options);
    if (confidenceString.length() != 0) {
      setConfidenceFactor((new Float(confidenceString)).floatValue());
    } else {
      m_CF = 0.25f;
    }
    String numFoldsString = Utils.getOption('N', options);
    if (numFoldsString.length() != 0) {
      m_numFolds = Integer.parseInt(numFoldsString);
    } else {
      m_numFolds = 3;
    }
    String seedString = Utils.getOption('Q', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1;
    }

    /**
     * J48T extra options.
     */
    
    /**
     * Fraction of values that meet the requirement for a given interval.
     */
    m_fractionValues = Double.parseDouble(Utils.getOption("fractionValues", options));
    
    /**
     * Witness selection method.
     */
    m_randomWitnessSelection = Utils.getFlag("randomWitnessSelection", options);
    
    /**
     * Object language.
     */
    String objLang = Utils.getOption("objectLanguage", options);
    if (objLang.length() != 0) {
    	setObjectLanguage(new SelectedTag(Integer.parseInt(objLang), LANGUAGES));
    }

    /**
     * Object language relation mask.
     */
    m_objectLanguageRelationMask = Utils.getOption("objectLanguageRelationMask", options);
    
    /**
     * Propositional algebra.
     */
    String propAlgebraString = Utils.getOption("propositionalAlgebra", options);
    if (propAlgebraString.length() != 0) {
        setPropositionalAlgebra(new SelectedTag(Integer.parseInt(propAlgebraString),
          ALGEBRAS));
    }
    
    /**
     * Relational algebra.
     */
    String relAlgebraString = Utils.getOption("relationalAlgebra", options);
    if (relAlgebraString.length() != 0) {
        setRelationalAlgebra(new SelectedTag(Integer.parseInt(relAlgebraString),
          ALGEBRAS));
    }
    
    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Gets the current settings of the Classifier.
   * 
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public String[] getOptions() {

    Vector<String> options = new Vector<String>();

    // Issue some warnings for the current configuration if necessary
    if (m_unpruned) {
      if (!m_subtreeRaising) {
        System.err.println("WARNING: Subtree raising does not need to be unset for an unpruned tree!");
      }
      if (m_reducedErrorPruning) {
        System.err.println("WARNING: Unpruned tree and reduced error pruning cannot be selected simultaneously!");
      }
    }
    if (m_unpruned || m_reducedErrorPruning) {
      if (m_CF != 0.25f) {
        System.err.println("WARNING: Changing the confidence will only affect error-based pruning!");
      }
    }
    if (m_unpruned || !m_reducedErrorPruning) {
      if (m_Seed != 1) {
        System.err.println("WARNING: Changing the seed only makes sense when using reduced error pruning");
      }
      if (m_numFolds != 3) {
        System.err.println("WARNING: Changing the number of folds does not make sense if " +
                "reduced error pruning is not selected.");
      }
    }
    /**
     * TODO: 
     * Add warnings if the language is not interval, etc.
     */
    if (m_fractionValues <= 0.0 || m_fractionValues > 1.0) {
    	System.err.println("WARNING: The fraction of values that meet " + 
    			"the requirement for a given interval should be a proportion; " + 
    			"i.e., an element of the open-closed (real numbers) interval (0.0,1.0].");
    }
    
    if (m_objectLanguage == LANG_HS3)
    	System.err.println("WARNING: No implementation for HS3 language");
    else if (m_objectLanguage == LANG_HS7)
    	System.err.println("WARNING: No implementation for HS7 language");
    else if (m_objectLanguage == LANG_LTL)
    	System.err.println("WARNING: No implementation for LTL language");
    /** */
    
    if (m_noCleanup) {
      options.add("-L");
    }
    if (!m_collapseTree) {
      options.add("-O");
    }
    if (m_unpruned) {
      options.add("-U");
    }
    if (!m_subtreeRaising) {
      options.add("-S");
    }
    if (m_reducedErrorPruning) {
      options.add("-R");
    }
    if (m_binarySplits) {
      options.add("-B");
    }
    if (m_useLaplace) {
      options.add("-A");
    }
    if (!m_useMDLcorrection) {
      options.add("-J");
    }
    if (m_doNotMakeSplitPointActualValue) {
      options.add("-doNotMakeSplitPointActualValue");
    }
    if (m_reducedErrorPruning) {
      options.add("-N");
      options.add("" + m_numFolds);
      options.add("-Q");
      options.add("" + m_Seed);
    } else if (!m_unpruned) {
      options.add("-C");
      options.add("" + m_CF);
    }
    options.add("-M");
    options.add("" + m_minNumObj);
    
    /**
     * J48T extra options.
     */
    /**
     * Fraction of values that meet the requirement for a given interval.
     * 
     * TODO:
     * Add -fractionValues only if m_objectLanguage is an interval-based language
     */
    options.add("-fractionValues");
    options.add("" + m_fractionValues);
    /**
     * Random witness selection method.
     */
    if (m_randomWitnessSelection) {
    	options.add("-randomWitnessSelection");
    }
    /**
     * Object language.
     */
    options.add("-objectLanguage");
    options.add("" + m_objectLanguage);
    /**
     * The mask of included relations in the object language.
     */
    options.add("-objectLanguageRelationMask");
    options.add("" + m_objectLanguageRelationMask);
    /**
     * Propositional algebra.
     */
//    options.add("-propositionalAlgebra");
//    options.add("" + m_propositionalAlgebra);
    
    options.add("-propositionalAlgebra");
    options.add("" + m_propositionalAlgebra);
    /**
     * Relational algebra.
     */
//    options.add("-relationalAlgebra");
//    options.add("" + m_relationalAlgebra);
    options.add("-relationalAlgebra");
    options.add("" + m_relationalAlgebra);

    Collections.addAll(options, super.getOptions());

    return options.toArray(new String[0]);
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String seedTipText() {
    return "The seed used for randomizing the data "
      + "when reduced-error pruning is used.";
  }

  /**
   * Get the value of Seed.
   * 
   * @return Value of Seed.
   */
  public int getSeed() {

    return m_Seed;
  }

  /**
   * Set the value of Seed.
   * 
   * @param newSeed Value to assign to Seed.
   */
  public void setSeed(int newSeed) {

    m_Seed = newSeed;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String useLaplaceTipText() {
    return "Whether counts at leaves are smoothed based on Laplace.";
  }

  /**
   * Get the value of useLaplace.
   * 
   * @return Value of useLaplace.
   */
  public boolean getUseLaplace() {

    return m_useLaplace;
  }

  /**
   * Set the value of useLaplace.
   * 
   * @param newuseLaplace Value to assign to useLaplace.
   */
  public void setUseLaplace(boolean newuseLaplace) {

    m_useLaplace = newuseLaplace;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String useMDLcorrectionTipText() {
    return "Whether MDL correction is used when finding splits on numeric attributes.";
  }

  /**
   * Get the value of useMDLcorrection.
   * 
   * @return Value of useMDLcorrection.
   */
  public boolean getUseMDLcorrection() {

    return m_useMDLcorrection;
  }

  /**
   * Set the value of useMDLcorrection.
   * 
   * @param newuseMDLcorrection Value to assign to useMDLcorrection.
   */
  public void setUseMDLcorrection(boolean newuseMDLcorrection) {

    m_useMDLcorrection = newuseMDLcorrection;
  }

  /**
   * Returns a description of the classifier.
   * 
   * @return a description of the classifier
   */
  @Override
  public String toString() {

    if (m_root == null) {
      return "No classifier built";
    }
    if (m_unpruned) {
      return "J48T unpruned tree\n------------------\n" + m_root.toString();
    } else {
      return "J48T pruned tree\n------------------\n" + m_root.toString();
    }
  }

  /**
   * Returns a superconcise version of the model
   * 
   * @return a summary of the model
   */
  @Override
  public String toSummaryString() {

    return "Number of leaves: " + m_root.numLeaves() + "\n"
      + "Size of the tree: " + m_root.numNodes() + "\n";
  }

  /**
   * Returns the size of the tree
   * 
   * @return the size of the tree
   */
  public double measureTreeSize() {
    return m_root.numNodes();
  }

  /**
   * Returns the number of leaves
   * 
   * @return the number of leaves
   */
  public double measureNumLeaves() {
    return m_root.numLeaves();
  }

  /**
   * Returns the number of rules (same as number of leaves)
   * 
   * @return the number of rules
   */
  public double measureNumRules() {
    return m_root.numLeaves();
  }

  /**
   * Returns an enumeration of the additional measure names
   * 
   * @return an enumeration of the measure names
   */
  @Override
  public Enumeration<String> enumerateMeasures() {
    Vector<String> newVector = new Vector<String>(3);
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }

  /**
   * Returns the value of the named measure
   * 
   * @param additionalMeasureName the name of the measure to query for its value
   * @return the value of the named measure
   * @throws IllegalArgumentException if the named measure is not supported
   */
  @Override
  public double getMeasure(String additionalMeasureName) {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0) {
      return measureNumRules();
    } else if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0) {
      return measureTreeSize();
    } else if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0) {
      return measureNumLeaves();
    } else {
      throw new IllegalArgumentException(additionalMeasureName
        + " not supported (j48)");
    }
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String unprunedTipText() {
    return "Whether pruning is performed.";
  }

  /**
   * Get the value of unpruned.
   * 
   * @return Value of unpruned.
   */
  public boolean getUnpruned() {

    return m_unpruned;
  }

  /**
   * Set the value of unpruned. Turns reduced-error pruning off if set.
   * 
   * @param v Value to assign to unpruned.
   */
  public void setUnpruned(boolean v) {

    m_unpruned = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String collapseTreeTipText() {
    return "Whether parts are removed that do not reduce training error.";
  }

  /**
   * Get the value of collapseTree.
   * 
   * @return Value of collapseTree.
   */
  public boolean getCollapseTree() {

    return m_collapseTree;
  }

  /**
   * Set the value of collapseTree.
   * 
   * @param v Value to assign to collapseTree.
   */
  public void setCollapseTree(boolean v) {

    m_collapseTree = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String confidenceFactorTipText() {
    return "The confidence factor used for pruning (smaller values incur "
      + "more pruning).";
  }

  /**
   * Get the value of CF.
   * 
   * @return Value of CF.
   */
  public float getConfidenceFactor() {

    return m_CF;
  }

  /**
   * Set the value of CF.
   * 
   * @param v Value to assign to CF.
   */
  public void setConfidenceFactor(float v) {

    m_CF = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String minNumObjTipText() {
    return "The minimum number of instances per leaf.";
  }

  /**
   * Get the value of minNumObj.
   * 
   * @return Value of minNumObj.
   */
  public int getMinNumObj() {

    return m_minNumObj;
  }

  /**
   * Set the value of minNumObj.
   * 
   * @param v Value to assign to minNumObj.
   */
  public void setMinNumObj(int v) {

    m_minNumObj = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String reducedErrorPruningTipText() {
    return "Whether reduced-error pruning is used instead of C.4.5 pruning.";
  }

  /**
   * Get the value of reducedErrorPruning.
   * 
   * @return Value of reducedErrorPruning.
   */
  public boolean getReducedErrorPruning() {

    return m_reducedErrorPruning;
  }

  /**
   * Set the value of reducedErrorPruning. Turns unpruned trees off if set.
   * 
   * @param v Value to assign to reducedErrorPruning.
   */
  public void setReducedErrorPruning(boolean v) {

    m_reducedErrorPruning = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numFoldsTipText() {
    return "Determines the amount of data used for reduced-error pruning. "
      + " One fold is used for pruning, the rest for growing the tree.";
  }

  /**
   * Get the value of numFolds.
   * 
   * @return Value of numFolds.
   */
  public int getNumFolds() {

    return m_numFolds;
  }

  /**
   * Set the value of numFolds.
   * 
   * @param v Value to assign to numFolds.
   */
  public void setNumFolds(int v) {

    m_numFolds = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String binarySplitsTipText() {
    return "Whether to use binary splits on nominal attributes when "
      + "building the trees.";
  }

  /**
   * Get the value of binarySplits.
   * 
   * @return Value of binarySplits.
   */
  public boolean getBinarySplits() {

    return m_binarySplits;
  }

  /**
   * Set the value of binarySplits.
   * 
   * @param v Value to assign to binarySplits.
   */
  public void setBinarySplits(boolean v) {

    m_binarySplits = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String subtreeRaisingTipText() {
    return "Whether to consider the subtree raising operation when pruning.";
  }

  /**
   * Get the value of subtreeRaising.
   * 
   * @return Value of subtreeRaising.
   */
  public boolean getSubtreeRaising() {

    return m_subtreeRaising;
  }

  /**
   * Set the value of subtreeRaising.
   * 
   * @param v Value to assign to subtreeRaising.
   */
  public void setSubtreeRaising(boolean v) {

    m_subtreeRaising = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String saveInstanceDataTipText() {
    return "Whether to save the training data for visualization.";
  }

  /**
   * Check whether instance data is to be saved.
   * 
   * @return true if instance data is saved
   */
  public boolean getSaveInstanceData() {

    return m_noCleanup;
  }

  /**
   * Set whether instance data is to be saved.
   * 
   * @param v true if instance data is to be saved
   */
  public void setSaveInstanceData(boolean v) {

    m_noCleanup = v;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String doNotMakeSplitPointActualValueTipText() {
    return "If true, the split point is not relocated to an actual data value."
      + " This can yield substantial speed-ups for large datasets with numeric attributes.";
  }

  /**
   * Gets the value of doNotMakeSplitPointActualValue.
   * 
   * @return the value
   */
  public boolean getDoNotMakeSplitPointActualValue() {
    return m_doNotMakeSplitPointActualValue;
  }

  /**
   * Sets the value of doNotMakeSplitPointActualValue.
   * 
   * @param m_doNotMakeSplitPointActualValue the value to set
   */
  public void setDoNotMakeSplitPointActualValue(
    boolean m_doNotMakeSplitPointActualValue) {
    this.m_doNotMakeSplitPointActualValue = m_doNotMakeSplitPointActualValue;
  }

  /**
   * J48T extra options tip text, get and set
   */
  /**
   * Fraction values that meet the requirement for a given interval.
   */
  public String fractionValuesTipText() {
	  return "Fraction values that meet the requirement for a given interval.";
  }
  public double getFractionValues() {
	  return m_fractionValues;
  }
  public void setFractionValues(double fractionValues) {
	  this.m_fractionValues = fractionValues;
  }
  /**
   * Random witness selection method.
   */
  public String randomWitnessSelectionTipText() {
	  return "Random witness selection method.";
  }
  public boolean getRandomWitnessSelection() {
	  return m_randomWitnessSelection;
  }
  public void setRandomWitnessSelection(boolean b) {
	  this.m_randomWitnessSelection = b;
  }
  /**
   * Object language.
   */
  public String objectLanguageTipText() {
	  return "Object language.";
  }
  public SelectedTag getObjectLanguage() {
	  return new SelectedTag(m_objectLanguage, LANGUAGES);
  }
  public void setObjectLanguage(SelectedTag tag) {
	  if (tag.getTags() == LANGUAGES)
		  m_objectLanguage = tag.getSelectedTag().getID();
  }
  public void setObjectLanguage(int value) {
	SelectedTag tag = new SelectedTag(value, LANGUAGES);
	if (tag.getTags() == LANGUAGES) {
		m_objectLanguage = tag.getSelectedTag().getID();
	}
  }
	
  /**
   * The mask of included relations in the object language.
   */
  public String objectLanguageRelationMaskTipText() {
	  return "The mask of included relations in the object language.";
  }
  public String getObjectLanguageRelationMask() {
	  return m_objectLanguageRelationMask;
  }
  public void setObjectLanguageRelationMask(String s) {
	  this.m_objectLanguageRelationMask = s;
  }
  /**
   * Propositional algebra.
   */
  public String propositionalAlgebraTipText() {
	  return "Propositional algebra.";
  }
  public SelectedTag getPropositionalAlgebra() {
	  return new SelectedTag(m_propositionalAlgebra, ALGEBRAS);
  }
  public void setPropositionalAlgebra(SelectedTag tag) {
	  if (tag.getTags() == ALGEBRAS)
		  m_propositionalAlgebra = tag.getSelectedTag().getID();
  }
  
//  public String propositionalAlgebraTipText() {
//	  return "Propositional algebra.";
//  }
//  public String getPropositionalAlgebra() {
//	  return m_propositionalAlgebra;
//  }
//  public void setPropositionalAlgebra(String s) {
//	  this.m_propositionalAlgebra = s;
//  }
  /**
   * Relational algebra.
   */
  public String relationalAlgebraTipText() {
	  return "Relational algebra.";
  }
  public SelectedTag getRelationalAlgebra() {
	  return new SelectedTag(m_relationalAlgebra, ALGEBRAS);
  }
  public void setRelationalAlgebra(SelectedTag tag) {
	  if (tag.getTags() == ALGEBRAS)
		  m_relationalAlgebra = tag.getSelectedTag().getID();
  }
//  public String relationalAlgebraTipText() {
//	  return "Relational algebra.";
//  }
//  public String getRelationalAlgebra() {
//	  return m_relationalAlgebra;
//  }
//  public void setRelationalAlgebra(String s) {
//	  this.m_relationalAlgebra = s;
//  }
  
  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 15233 $");
  }

  /**
   * Builds the classifier to generate a partition.
   */
  @Override
  public void generatePartition(Instances data) throws Exception {

    buildClassifier(data);
  }

  /**
   * Computes an array that indicates node membership.
   */
  @Override
  public double[] getMembershipValues(Instance inst) throws Exception {

    return m_root.getMembershipValues(inst);
  }

  /**
   * Returns the number of elements in the partition.
   */
  @Override
  public int numElements() throws Exception {

    return m_root.numNodes();
  }

  /**
   * Main method for testing this class
   * 
   * @param argv the commandline options
   */
  public static void main(String[] argv) {
    runClassifier(new J48T(), argv);
  }
}
