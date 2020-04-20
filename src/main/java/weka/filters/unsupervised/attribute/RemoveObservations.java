package weka.filters.unsupervised.attribute;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;


/**
 * <!-- globalinfo-start -->
 * An instance filter for a relational type dataset that removes a fraction of observations.
 * The new dataset will contain only the specified fraction of observations of the original dataset.
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> 
 * Valid options are:
 * <p/>
 * 
 * <pre>
 * -O &lt;format&gt; 
 * The format of the output. (Default: "Relational-valued").
 * </pre>
 * 
 * <pre>
 * -K &lt;type&gt;
 * How to compute the remaining points. (Default: "fraction")
 * </pre>
 * 
 * <pre>
 * -V &lt;value&gt;
 * Keep value. (Default: 1.0)
 * </pre>
 * 
 * <pre>
 * -D &lt;derivativen&gt;
 * The maximum number of discrete derivative to be computed. (Default: 0)
 * </pre>
 * 
 * <pre>
 * -M1
 * Compute mean (i.e., 1st moment).
 * </pre>
 * 
 * <pre>
 * -M2
 * Compute variance (i.e., 2nd moment).
 * </pre>
 * 
 * <pre>
 * -M3
 * Compute skewness (i.e., 3rd moment).
 * </pre>
 * 
 * <pre>
 * -M4
 * Compute kurtosis (i.e., 4th moment).
 * </pre>
 * 
 * <!-- options-end -->
 * @author edu
 *
 */
public class RemoveObservations 
			extends Filter 
			implements UnsupervisedFilter, 
//					   StreamableFilter, 
					   OptionHandler, 
					   WeightedInstancesHandler, 
					   WeightedAttributesHandler {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 5966166909675014053L;
	
	/**
	 * The output format type.
	 */
	public static final int REL_OUTPUT = 0;
	public static final int STR_OUTPUT = 1;
	public static final int NUM_OUTPUT = 2;
	
	public static final Tag[] OUTPUT_FORMAT_TYPE = {
			new Tag(REL_OUTPUT, "REL", "Relational-valued"),
			new Tag(STR_OUTPUT, "STR", "String-valued"),
			new Tag(NUM_OUTPUT, "NUM", "Numeric-valued")
	};
	
	/** 
	 * Record the type of the output format.
	 */
	protected int m_outputFormatType = REL_OUTPUT;
	 
	/**
	 * The keep value type.
	 */
	public static final int FRACTION_KEEP = 0;
	public static final int EFFECTIVE_KEEP = 1;
	
	public static final Tag[] KEEP_TYPE = {
			new Tag(FRACTION_KEEP, "FRA", "Fraction"),
			new Tag(EFFECTIVE_KEEP, "EFF", "Effective")
	};
	
	/** 
	 * Record the type of the keep value.
	 */
	protected int m_keepType = FRACTION_KEEP;
	
	/**
	 * Keep value.
	 */
	protected double m_keepValue = 1.0;
	
	/**
	 * The maximum number of discrete derivative to be computed. If it is
	 * 0, then we keep the entire time series. If, for example,
	 * it is 1, then we drop the first point in the time series to 
	 * keep the entire time series without such point for the 0th
	 * derivative, and we compute the 1th derivative on the entire
	 * time series with all the points. Similarly, we can do for the
	 * nth derivative.
	 */
	protected int m_discreteDerivative = 0;
	
	/**
	 * Class index for determining the input format.
	 * 
	 * TODO
	 * Add this parameter to options
	 */
	protected int m_classIndex = 1;
	
	/**
	 * Compute mean (i.e., 1st moment)?
	 */
	protected boolean m_momentMean = false;
	
	/**
	 * Compute variance (i.e., 2nd moment)?
	 */
	protected boolean m_momentVariance = false;
	
	/**
	 * Compute skewness (i.e., 3rd moment)?
	 */
	protected boolean m_momentSkewness = false;
	
	/**
	 * Compute kurtosis (i.e., 4th moment)?
	 */
	protected boolean m_momentKurtosis = false;
	

	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "An instance filter for a relational type dataset that removes observations from the dataset. "
				+ "The new dataset will contain only the specified fraction of observations or the specified effective number of points of the original dataset.";
	}
	
	/**
	 * Returns the Capabilities of this filter.
	 * 
	 * @return the capabilities of this object
	 * @see Capabilities
	 */
	@Override
	public Capabilities getCapabilities() {
		
		Capabilities result = super.getCapabilities();
		result.disableAll();
		
//		// Attributes
//		result.disableAllAttributes();
//		result.enable(Capability.NOMINAL_ATTRIBUTES);
//		result.enable(Capability.RELATIONAL_ATTRIBUTES);
		
	    // attributes
	    result.disableAllAttributes();
//	    result.enable(Capability.NOMINAL_ATTRIBUTES);
//	    result.enable(Capability.NUMERIC_ATTRIBUTES);
	    result.enable(Capability.RELATIONAL_ATTRIBUTES);
	    result.enable(Capability.MISSING_VALUES);
	    
	    // class
	    result.enableAllClasses();
	    result.enable(Capability.MISSING_CLASS_VALUES);
	    
	    // other
//	    result.enable(Capability.ONLY_MULTIINSTANCE);
		
////		// Class
////	    result.enableAllClasses();
////	    result.enable(Capability.MISSING_CLASS_VALUES);
////	    result.enable(Capability.NO_CLASS);
//		
//	    // Class
//	    result.enableAllClasses();
//	    result.enable(Capability.MISSING_CLASS_VALUES);
//	    
//	    // Other
//	    result.enable(Capability.ONLY_MULTIINSTANCE);
	    
		return result;
	}
	
//	/**
//	 * Returns the capabilities of this multi-instance filter for the
//	 * relational data (i.e., the bags).
//	 *
//	 * @return            the capabilities of this object
//     * @see               Capabilities
//	 */
//	@Override
//	public Capabilities getMultiInstanceCapabilities() {
//		
//		Capabilities result = super.getCapabilities();
//		
//		// Attributes
//	    result.enableAllAttributes();
//	    result.disable(Capability.RELATIONAL_ATTRIBUTES);
//	    result.enable(Capability.MISSING_VALUES);
//		
//		// Class
//	    result.enableAllClasses();
//	    result.enable(Capability.MISSING_CLASS_VALUES);
//	    result.enable(Capability.NO_CLASS);
//	    
//	    // Other
//	    result.setMinimumNumberInstances(0);
//	    
//		return result;
//	}
	
	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		  
		Vector<Option> newVector = new Vector<Option>();
		String desc;
		SelectedTag tag;
		  
		desc = "";
		  
		for (int i = 0; i < OUTPUT_FORMAT_TYPE.length; i++) {
			tag = new SelectedTag(OUTPUT_FORMAT_TYPE[i].getID(), OUTPUT_FORMAT_TYPE);
			desc += "\t" + tag.getSelectedTag().getIDStr() + " = "
					+ tag.getSelectedTag().getReadable() + "\n";
		}
		  
		newVector.addElement(new Option("\tThe type of the output format:\n" + desc + "\t(default: " + new SelectedTag(REL_OUTPUT, OUTPUT_FORMAT_TYPE) + ")", 
				"O", 
				1, 
				"-O " + Tag.toOptionList(OUTPUT_FORMAT_TYPE)
		));
		 
		desc = "";
		  
		for (int i = 0; i < KEEP_TYPE.length; i++) {
			tag = new SelectedTag(KEEP_TYPE[i].getID(), KEEP_TYPE);
			desc += "\t" + tag.getSelectedTag().getIDStr() + " = "
					+ tag.getSelectedTag().getReadable() + "\n";
		}
		
		newVector.addElement(new Option("\tThe type of the keep value:\n" + desc + "\t(default: " + new SelectedTag(FRACTION_KEEP, KEEP_TYPE) + ")", 
				"K", 
				1, 
				"-K " + Tag.toOptionList(KEEP_TYPE)
		));
		newVector.addElement(new Option(
				"\tKeep value.",
				"V",
				1,
				"-V"
		));
		
		newVector.addElement(new Option(
				"\tThe maximum discrete derivative to be computed.",
				"D",
				1,
				"-D"
		));
		  
		newVector.addElement(new Option(
				"\tCompute mean (i.e., 1st moment).",
				"M1",
				0,
				"-M1"
		));
		
		newVector.addElement(new Option(
				"\tCompute variance (i.e., 2nd moment).",
				"M2",
				0,
				"-M2"
		));
		
		newVector.addElement(new Option(
				"\tCompute skewness (i.e., 3rd moment).",
				"M3",
				0,
				"-M3"
		));
		
		newVector.addElement(new Option(
				"\tCompute kurtosis (i.e., 4th moment).",
				"M4",
				0,
				"-M4"
		));
		
		newVector.addAll(Collections.list(super.listOptions()));
		  
		return newVector.elements();
	}
	
	public void setOptions(String[] options) throws Exception {
		
		String tmpStr;

		/**
		 * Output format type.
		 */
	    tmpStr = Utils.getOption('O', options);
	    
	    // If the type is different from the default one
	    if (tmpStr.length() != 0) {
	    	setOutputFormatType(new SelectedTag(tmpStr, OUTPUT_FORMAT_TYPE));
	    } 
	    // Default case
	    else {
	    	setOutputFormatType(new SelectedTag(REL_OUTPUT, OUTPUT_FORMAT_TYPE));
	    }
	    
		/**
		 * Keep value type.
		 */
	    tmpStr = Utils.getOption('K', options);
	    
	    // If the type is different from the default one
	    if (tmpStr.length() != 0) {
	    	setKeepType(new SelectedTag(tmpStr, KEEP_TYPE));
	    } 
	    // Default case
	    else {
	    	setKeepType(new SelectedTag(FRACTION_KEEP, KEEP_TYPE));
	    }
	    
	    /**
	     * Keep value.
	     */
	    m_keepValue = Double.parseDouble(Utils.getOption("V", options));
	    
	    /**
	     * The maximum discrete derivative to be computed.
	     */
	    m_discreteDerivative = Integer.parseInt(Utils.getOption("D", options));
	    
	    /**
	     * Moments.
	     */
	    
//	    if (m_outputFormatType == 2) {
		    m_momentMean = Utils.getFlag("M1", options);
		    m_momentVariance = Utils.getFlag("M2", options);
		    m_momentSkewness = Utils.getFlag("M3", options);
		    m_momentKurtosis = Utils.getFlag("M4", options);
//	    }
//	    else {
//	    	m_momentMean = false;
//	    	m_momentVariance = false;
//	    	m_momentSkewness = false;
//	    	m_momentKurtosis = false;
//	    }
	    
	    super.setOptions(options);
	    
	    Utils.checkForRemainingOptions(options);
	}
	
	/**
	 * Gets the current settings of the filter.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {
		
		Vector<String> result = new Vector<String>();
		
		if (m_outputFormatType != REL_OUTPUT) {
			result.add("-O");
			result.add("" + getOutputFormatType());
		}
		
		if (m_keepType != FRACTION_KEEP) {
			result.add("-K");
			result.add("" + getKeepType());
		}
		
		result.add("-V");
	    result.add("" + m_keepValue);
	    
	    result.add("-D");
	    result.add("" + m_discreteDerivative);
	    
	    if (m_outputFormatType == NUM_OUTPUT) {
	    	
	    	if (m_momentMean)
	    		result.add("-M1");
	    	
	    	if (m_momentVariance)
	    		result.add("-M2");
	    	
	    	if (m_momentSkewness)
	    		result.add("-M3");
	    	
	    	if (m_momentKurtosis)
	    		result.add("-M4");
	    }
		
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String outputFormatTypeTipText() {
	    return "Defines the type of the output format.";
	}
	
	/**
	 * Sets the type of the output format.
	 * 
	 * @param value the output format type
	 */
	public void setOutputFormatType(SelectedTag value) {
		if (value.getTags() == OUTPUT_FORMAT_TYPE) {
			m_outputFormatType = value.getSelectedTag().getID();
			
//			if (value.getSelectedTag().getID() != 2) {
//				setMomentMean(false);
//				setMomentVariance(false);
//				setMomentSkewness(false);
//				setMomentKurtosis(false);
//			}
//			else {
//				setMomentMean(getMomentVariance());
//				setMomentVariance(getMomentVariance());
//				setMomentSkewness(getMomentSkewness());
//				setMomentKurtosis(getMomentKurtosis());
//			}
		}
	}
	
	/**
	 * Sets the type of the output format.
	 * 
	 * @param value the output format type
	 */
	public void setOutputFormatType(int value) {
		SelectedTag tag = new SelectedTag(value, OUTPUT_FORMAT_TYPE);
		if (tag.getTags() == OUTPUT_FORMAT_TYPE) {
			m_outputFormatType = tag.getSelectedTag().getID();
			
//			if (tag.getSelectedTag().getID() != 2) {
//				setMomentMean(false);
//				setMomentVariance(false);
//				setMomentSkewness(false);
//				setMomentKurtosis(false);
//			}
		}
	}
	
	/**
	 * Gets the type of the output format.
	 * 
	 * @return the current output format type.
	 */
	public SelectedTag getOutputFormatType() {
		return new SelectedTag(m_outputFormatType, OUTPUT_FORMAT_TYPE);
	}
	 
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String keepTypeTipText() {
	    return "Defines the type of the keep value.";
	}
	
	/**
	 * Sets the type of the keep value.
	 * 
	 * @param value the keep value type
	 */
	public void setKeepType(SelectedTag value) {
		if (value.getTags() == KEEP_TYPE) {
			m_keepType = value.getSelectedTag().getID();
		}
	}
	
	/**
	 * Sets the type of the keep value.
	 * 
	 * @param value the keep value type
	 */
	public void setKeepType(int value) {
		SelectedTag tag = new SelectedTag(value, KEEP_TYPE);
		if (tag.getTags() == KEEP_TYPE) {
			m_keepType = tag.getSelectedTag().getID();
		}
	}
	
	/**
	 * Gets the type of the keep value.
	 * 
	 * @return the current keep value type.
	 */
	public SelectedTag getKeepType() {
		return new SelectedTag(m_keepType, KEEP_TYPE);
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String keepValueTipText() {
		return "Keep value.";
	}
	
	/**
	 * Get keep value.
	 * 
	 * @return the current keep value
	 */
	public double getKeepValue() {
		return m_keepValue;
	}
	
	/**
	 * Sets the keep value.
	 * 
	 * @param value the keep value
	 */
	public void setKeepValue(double value) {
		this.m_keepValue = value;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String discreteDerivativeTipText() {
		return "The maximum discrete derivative to be computed.";
	}
	
	/**
	 * Gets the maximum discrete derivative to be computed.
	 * 
	 * @return the maximum discrete derivative to be computed
	 */
	public int getDiscreteDerivative() {
		return m_discreteDerivative;
	}
	
	/**
	 * Sets the maximum discrete derivative to be computed.
	 * 
	 * @param derivative the maximum discrete derivative
	 */
	public void setDiscreteDerivative(int derivative) {
		this.m_discreteDerivative = derivative;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String momentMeanTipText() {
		return "Compute mean (ie, 1st moment)?";
	}
	
	/**
	 * Compute mean (i.e., 1st moment)?
	 * 
	 * @return value of momentMean
	 */
	public boolean getMomentMean() {
		return m_momentMean;
	}
	
	/**
	 * Set the computation of the mean (i.e., 1st moment)
	 * 
	 * @param flag value to assign to momentMean
	 */
	public void setMomentMean(boolean flag) {
		this.m_momentMean = flag;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String momentVarianceTipText() {
		return "Compute variance (ie, 2nd moment)?";
	}
	
	/**
	 * Compute variance (i.e., 2nd moment)?
	 * 
	 * @return value of momentVariance
	 */
	public boolean getMomentVariance() {
		return m_momentVariance;
	}
	
	/**
	 * Set the computation of the variance (i.e., 2nd moment)
	 * 
	 * @param flag value to assign to momentVariance
	 */
	public void setMomentVariance(boolean flag) {
		this.m_momentVariance = flag;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String momentSkewnessTipText() {
		return "Compute skewness (ie, 3rd moment)?";
	}
	
	/**
	 * Compute skewness (i.e., 3rd moment)?
	 * 
	 * @return value of momentSkewness
	 */
	public boolean getMomentSkewness() {
		return m_momentSkewness;
	}
	
	/**
	 * Set the computation of the kurtosis (i.e., 3rd moment)
	 * 
	 * @param flag value to assign to momentSkewness
	 */
	public void setMomentSkewness(boolean flag) {
		this.m_momentSkewness = flag;
	}
	
	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String momentKurtosisTipText() {
		return "Compute kurtosis (ie, 4th moment)?";
	}
	
	/**
	 * Compute kurtosis (i.e., 4th moment)?
	 * 
	 * @return value of momentKurtosis
	 */
	public boolean getMomentKurtosis() {
		return m_momentKurtosis;
	}
	
	/**
	 * Set the computation of the kurtosis (i.e., 4th moment)
	 * 
	 * @param flag value to assign to momentKurtosis
	 */
	public void setMomentKurtosis(boolean flag) {
		this.m_momentKurtosis = flag;
	}
	
	/**
	 * Sets the format of the input instances.
	 * 
	 * @param instanceInfo an Instances object containing the input instance
	 *          structure (any instances contained in the object are ignored -
	 *          only the structure is required).
	 * @return true if the outputFormat may be collected immediately
	 * @throws Exception if the format couldn't be set successfully
	 */
	@Override
	public boolean setInputFormat(Instances instanceInfo) throws Exception {
		
		// Set class index
		instanceInfo.setClassIndex(m_classIndex);
		
		// Set input format
		super.setInputFormat(instanceInfo);

		// outputFormat cannot be collected immediately
	    return false;
	}
	
	/**
	 * Input an instance for filtering. Ordinarily the instance is processed and
	 * made available for output immediately. Some filters require all instances
	 * be read before producing output.
	 * 
	 * @param instance the input instance
	 * @return true if the filtered instance may now be collected with output().
	 * @throws IllegalStateException if no input format has been defined.
	 */
	@Override
	public boolean input(Instance instance) throws Exception {
		
		if (getInputFormat() == null)
			throw new IllegalStateException("No input instance format defined");
			
		if (m_NewBatch) {
			resetQueue();
			m_NewBatch = false;
		}
		
		if (isFirstBatchDone()) {
			convertInstance(instance);
			return true; // Can be immediately collected via output()
		}
		
		bufferInput(instance);
		return false;
	}

	/**
	 * Signifies that this batch of input to the filter is finished. If the filter
	 * requires all instances prior to filtering, output() may now be called to
	 * retrieve the filtered instances. Note that, it must call setOutputFormat()
	 * here because there has not been such setting in setInputFormat().
	 * 
	 * @return true if there are instances pending output
	 * @throws IllegalStateException if no input structure has been defined
	 */
	public boolean batchFinished() {
	
		if (getInputFormat() == null)
			throw new IllegalStateException("No input instance format defined");
			
		if (!isFirstBatchDone()) {
				
			// Relational-valued output
			if (getOutputFormatType().getSelectedTag().getID() == REL_OUTPUT) {
				
				int numAttrs = getInputFormat().instance(0).relationalValue(0).instance(0).numAttributes();
				
				/** 
				 * IMPORTANT
				 * Read https://waikato.github.io/weka-wiki/formats_and_processing/creating_arff_file/
				 * In particular https://svn.cms.waikato.ac.nz/svn/weka/branches/stable-3-8/wekaexamples/src/main/java/wekaexamples/core/CreateInstances.java
				 */
				
				// Setting the attributes
				ArrayList<Attribute> attributes = new ArrayList<Attribute>(getInputFormat().numAttributes());
				
				// Number of attributes in the relational attribute
				if (getKeepType().getSelectedTag().getID() == FRACTION_KEEP)
					// Fraction
					numAttrs = (int) (numAttrs * m_keepValue);
				else if (getKeepType().getSelectedTag().getID() == EFFECTIVE_KEEP) {
					// Effective
					if (numAttrs > getKeepValue())
						numAttrs = (int) getKeepValue();
				}

				// Relational attributes
				ArrayList<Attribute> attributesRelational = new ArrayList<Attribute>(numAttrs);
				
				// Add each single attribute to the relational attributes
				for (int attrRelIndex = 0; attrRelIndex < numAttrs; attrRelIndex++)
					attributesRelational.add(getInputFormat().instance(0).attribute(0).relation().attribute(attrRelIndex));
								
				// Instances object containing the relational attributes
				Instances dataRelational = new Instances(getInputFormat().instance(0).attribute(0).name(), attributesRelational, 0);
				
				// Add relational attributes to attributes
				attributes.add(new Attribute(getInputFormat().instance(0).attribute(0).name(), dataRelational, 0));
				
				// Add class attribute to attributes
				attributes.add(getInputFormat().instance(0).attribute(1));
				
				// Build output format based on the computed attributes
				Instances outputFormat = new Instances(getInputFormat().relationName(), attributes, getInputFormat().numInstances());
				
				// Set class index
				outputFormat.setClassIndex(getInputFormat().classIndex());
				
				// Same output format as input format
				
				setOutputFormat(outputFormat);
				
			}
			// String-valued output
			else if (getOutputFormatType().getSelectedTag().getID() == STR_OUTPUT) {
				
				// Input
				Instances input = getInputFormat();

				/**
				 * TODO
				 * An instance can have, in general, more relational-valued attributes, not just only 1 (i.e., index 0)
				 */
				// Number of channels for each instance
				int numChannels = getInputFormat().instance(0).relationalValue(0).size();
				
				/**
				 * Attributes for the output format. There are numChannels+1 attributes representing the
				 * channels and the class. This variable is needed only for 
				 */
				ArrayList<Attribute> attributes = new ArrayList<Attribute>(numChannels+1);
				
				// For each channel
				for (int i = 0; i < numChannels; i++) {
					// For each discrete derivative 
					for (int j = 0; j <= m_discreteDerivative; j++) {
						
						if (j == 0)
							// Add a string attribute, represented by the flag true, to the attributes
							attributes.add(new Attribute("var" + Integer.toString(i), true));
						else {
							
							/**
							 * Auxiliary string for computing a string format of the derivatives
							 * If the discrete derivative is 1, then it will be ’
							 * If the discrete derivative is 2, then it will be ’’
							 * And so on..
							 */
							StringBuilder strBuilder = new StringBuilder("var" + Integer.toString(i));
							for (int k = 1; k <= j; k++)
								strBuilder.append('’');
							
							
							// Add a string attribute, represented by the flag true, to the attributes
							attributes.add(new Attribute(strBuilder.toString(), true));
						}
					}
				}
				
				// Add the class attribute with its values, if nominal
				attributes.add(input.classAttribute());
				
				// Output format
				Instances outputFormat = new Instances(input.relationName(), attributes, input.numInstances());
				
				// Set output format class index
				outputFormat.setClassIndex(numChannels*(m_discreteDerivative+1));
				
				System.out.println(outputFormat);
				
				// Set output format as desired with numChannels+1 attributes
				setOutputFormat(outputFormat);
			}
			// Numeric-valued output
			else if (getOutputFormatType().getSelectedTag().getID() == NUM_OUTPUT) {
				
				// Input
				Instances input = getInputFormat();

				/**
				 * TODO
				 * An instance can have, in general, more relational-valued attributes, not just only 1 (i.e., index 0)
				 */
				// Number of channels for each instance
				int numChannels = getInputFormat().instance(0).relationalValue(0).size();
				
				// How many moments to be computed?
				int numMoments = 0;
				
				if (m_momentMean)
					numMoments++;
				if (m_momentVariance)
					numMoments++;
				if (m_momentSkewness)
					numMoments++;
				if (m_momentKurtosis)
					numMoments++;
				
				// Number of attributes (+1 for the class)
				int numAttributes = numChannels * numMoments + 1;
				
				// Attributes object
				ArrayList<Attribute> attributes = new ArrayList<Attribute>(numAttributes);
				
				// For each channel
				for (int i = 0; i < numChannels; i++) {
					
					// Add moments' attributes, if true
					if (m_momentMean)
						attributes.add(new Attribute("var" + Integer.toString(i) + "_mean"));
					if (m_momentVariance)
						attributes.add(new Attribute("var" + Integer.toString(i) + "_variance"));
					if (m_momentSkewness)
						attributes.add(new Attribute("var" + Integer.toString(i) + "_skewness"));
					if (m_momentKurtosis)
						attributes.add(new Attribute("var" + Integer.toString(i) + "_kurtosis"));
				}
				
				// Add the class attribute with its values, if nominal
				attributes.add(input.classAttribute());
				
				// Output format
				Instances outputFormat = new Instances(input.relationName(), attributes, input.numInstances());
				
				// Set output format class index
				outputFormat.setClassIndex(numAttributes-1);
				
				// Set output format as desired with numChannels+1 attributes
				setOutputFormat(outputFormat);
				
			}
			
			
			// Convert each instance
			for (int instIndex = 0; instIndex < getInputFormat().numInstances(); instIndex++)
				convertInstance(getInputFormat().instance(instIndex));

		}

		// Remove all buffered Instance objects from the input queue
		flushInput();
		
		// At the new batch
		m_NewBatch = true;
		// First batch completed
		m_FirstBatchDone = true;
		
		return (numPendingOutput() != 0);
	}
	
	/**
	 * Helper function to convert an instance.
	 * 
	 * @param instance instance to be converted.
	 */
	protected void convertInstance(Instance instance) {
		
		int numPoints = 0;
		/**
		 * TODO
		 * An instance can have, in general, more relational-valued attributes, not just only 1 (i.e., index 0)
		 */
		// Instances where each instance is a single time series (i.e., a channel)
		Instances allTimeSeries = instance.relationalValue(0);
		
		// How many points after clipping
		if (getKeepType().getSelectedTag().getID() == FRACTION_KEEP)
			// Fraction
			numPoints = (int) (allTimeSeries.instance(0).numValues() * m_keepValue);	
		else if (getKeepType().getSelectedTag().getID() == EFFECTIVE_KEEP) {
			// Effective; if m_keepValue > numPoints, then the we keep the numPoints
			numPoints = allTimeSeries.instance(0).numValues();
			if (numPoints > getKeepValue())
				numPoints = (int) getKeepValue();
		}
		
		// Empty clipped instance having outputFormat.numAttributes() attributes
		Instance clippedInstance = new DenseInstance(getOutputFormat().numAttributes());
		
		// Set the dataset to have access to
		clippedInstance.setDataset(getOutputFormat());
		
		// Relational-valued output
		if (getOutputFormatType().getSelectedTag().getID() == REL_OUTPUT) {
			
			// Relational data that will contain the clipped time series
			Instances dataRelational = new Instances(getOutputFormat().attribute(0).relation(), 0);
			
			// Array of doubles that will contain the clipped values for each time series
			double[] valuesRelational;
			
			// For each time series (i.e., channel)
			for (int tsIndex = 0; tsIndex < allTimeSeries.size(); tsIndex++) {
				
				// The values for the time series of length numPoints
				valuesRelational = new double[numPoints];
				
				// For each value in the time serie
				for (int attrRelIndex = 0; attrRelIndex < numPoints; attrRelIndex++) {
					valuesRelational[attrRelIndex] = allTimeSeries.instance(tsIndex).value(attrRelIndex);
				}
				
				// Add values
				dataRelational.add(new DenseInstance(1.0, valuesRelational));
			}
			
			
			// Set the relational attribute at index 0
			clippedInstance.setValue(0, clippedInstance.attribute(0).addRelation(dataRelational));
			
			// Set the class value
			clippedInstance.setValue(getOutputFormat().classIndex(), instance.classValue());
			
			// Set weight
			clippedInstance.setWeight(instance.weight());

			// Push on output queue; no need to copy
			push(clippedInstance, false); 

		}
		// String-valued output
		else if (getOutputFormatType().getSelectedTag().getID() == STR_OUTPUT) {
			
			// Variable needed to put the values at the correct position
			int counter = 0;
			
			// For each time series
			for (int tsIndex = 0; tsIndex < allTimeSeries.size(); tsIndex++) {
				
				// Double values of the time series
				double[] tsDoubleValues = new double[numPoints];
				
				for (int i = 0; i < numPoints; i++)
					tsDoubleValues[i] = Double.parseDouble(allTimeSeries.instance(tsIndex).toString(i));
				
				// If the maximum discrete derivative is 0, no need for further computation
				if (m_discreteDerivative == 0)
					// Join String[] Arrays.toString(tsDoubleValues) to a String separated by commas
					clippedInstance.setValue(tsIndex, String.join(",", Arrays.toString(tsDoubleValues).replace("[", "").replace("]", "")));
				else {
					double[][] derivatives = derivatives(tsDoubleValues);
					
					// Index counter that is reset for each time series; needed to correctly compute the index for the derivative values
					for (int derivativeCounter = 0; derivativeCounter <= m_discreteDerivative; derivativeCounter++) {
						
						// Copy values from derivatives
						double[] derivativeValues = Arrays.copyOfRange(derivatives[derivativeCounter], m_discreteDerivative-1, tsDoubleValues.length);
						
						// Join String[] Arrays.toString(tsDoubleValues) to a String separated by commas
						clippedInstance.setValue(counter, String.join(",", Arrays.toString(derivativeValues).replace("[", "").replace("]", "")));
						
						// Increase counter
						counter++;
					}
				}
			}
			
			// Set the class value
			clippedInstance.setValue(getOutputFormat().classIndex(), instance.classValue());
			
			// Set weight
			clippedInstance.setWeight(instance.weight());
			
			// Push on output queue; no need to copy
			push(clippedInstance, false); 

		}
		// Numeric-valued output
		else if (getOutputFormatType().getSelectedTag().getID() == NUM_OUTPUT) {
			
			// How many moments to be computed?
			int numMoments = 0;
			
			if (m_momentMean)
				numMoments++;
			if (m_momentVariance)
				numMoments++;
			if (m_momentSkewness)
				numMoments++;
			if (m_momentKurtosis)
				numMoments++;
			
			// For each time series
			for (int tsIndex = 0; tsIndex < allTimeSeries.size(); tsIndex++) {
				
				// Values of the time series
				double[] tsDoubleValues = new double[numPoints];
				
				for (int i = 0; i < numPoints; i++)
					tsDoubleValues[i] = Double.parseDouble(allTimeSeries.instance(tsIndex).toString(i));
				
				// Moments' values
				double[] moments = moments(tsDoubleValues);
				
				// Index counter that is reset for each time series; needed to correctly compute the index for the moments' values
				int indexCounter = 0;
				
				// Add mean
				if (m_momentMean) {
					clippedInstance.setValue(tsIndex*numMoments+indexCounter, moments[0]);
					indexCounter++;
				}
				// Add variance
				if (m_momentVariance) {
					clippedInstance.setValue(tsIndex*numMoments+indexCounter, moments[1]);
					indexCounter++;
				}
				// Add skewness
				if (m_momentSkewness) {
					clippedInstance.setValue(tsIndex*numMoments+indexCounter, moments[2]);
					indexCounter++;
				}
				// Add kurtosis
				if (m_momentKurtosis) {
					clippedInstance.setValue(tsIndex*numMoments+indexCounter, moments[3]);
					indexCounter++;
				}
			}
			
			// Set the class value
			clippedInstance.setValue(getOutputFormat().classIndex(), instance.classValue());
			
			// Set weight
			clippedInstance.setWeight(instance.weight());
			
			// Push on output queue; no need to copy
			push(clippedInstance, false); 
			
		}
	}
	
	protected double[][] derivatives(double[] values) {
		
		double result[][] = new double[m_discreteDerivative+1][values.length];

		for (int i = 0; i < values.length; i++)
			result[0][i] = values[i];
		
		/**
		 * index        |    0      1      2      3
		 * derivative 0 |   v0     v1     v2     v3
		 * derivative 1 |          v0'    v1'   v2'
		 * derivative 2 |                v0''  v1''
		 * ...
		 */
		// dth derivative
		for (int d = 1; d <= m_discreteDerivative; d++)
			for (int i = d; i < values.length; i++)
				result[d][i] = result[d-1][i] - result[d-1][i-1];
		
		return result;
	}
	
	/**
	 * Helper function to compute the moments. Note that we compute
	 * the corrected variance.
	 * @param values values to compute the moments
	 * @return an array of doubles containing the mean (0), variance (1), 
	 *         skewness (2), and kurtosis (3)
	 */
	protected double[] moments(double[] values) {
		
		/**
		 * TODO
		 * Can be optimized.
		 */
		double result[] = new double[4];
		double mean = 0.0;
		double variance = 0.0;
		double skewness = 0.0;
		double kurtosis = 0.0;
		double standardDeviation = 0.0;
		double accumulator = 0.0;
		
		// Mean
		mean = Utils.mean(values);
		
		// Variance
		variance = Utils.variance(values);
		
		// Standard deviation
		standardDeviation = Math.sqrt(variance);
		
		// Skewness
		accumulator = 0.0;
		for (int i = 0; i < values.length; i++)
			accumulator += Math.pow((values[i] - mean)/standardDeviation, 3);
		skewness = accumulator / values.length;
		
		// Kurtosis
		accumulator = 0.0;
		for (int i = 0; i < values.length; i++)
			accumulator += Math.pow((values[i] - mean)/standardDeviation, 4);
		kurtosis = accumulator / values.length;
		
		// Set result values
		result[0] = mean;
		result[1] = variance;
		result[2] = skewness;
		result[3] = kurtosis;
		
		return result;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] argv) {

		runFilter(new RemoveObservations(), argv);
	}
	
}
