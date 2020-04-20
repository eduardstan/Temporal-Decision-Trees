package demos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Properties;

import tsml.classifiers.multivariate.NN_DTW_D;
import tsml.classifiers.multivariate.NN_DTW_I;
import tsml.classifiers.multivariate.NN_ED_I;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.J48T;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.RemoveObservations;

/**
 * Test suite number 2. <br>
 * This suite is made to cycle on the resampled datasets. In particular, 
 * for each dataset, if the dataset has too many points, then compute the 
 * accuracies incrementally 50 points, 100 points, 150 points, ecc.
 * 
 * @author edu
 *
 */

class Experiment implements Runnable {

	/**
	 * Dataset name.
	 */
	protected String m_dataset;
	
	/**
	 * Number of points for the dataset.
	 */
	protected int m_points;
	
	/**
	 * List of algorithms with parameters.
	 */
	protected String[] m_algorithmsWithParameters;
	
	/**
	 * Thread.
	 */
	protected Thread m_thread;
	
	/**
	 * Thread index.
	 */
	protected int m_threadIndex;
	
	/**
	 * Constructor.
	 * 
	 * @param threadIndex index for the thread
	 * @param dataset dataset name
	 * @param algorithmsWithParameters list of algorithms with parameters
	 */
	Experiment(int threadIndex, String dataset, int points, String[] algorithmsWithParameters) {
		
		m_threadIndex = threadIndex; 
		m_dataset = dataset;
		m_points = points;
		m_algorithmsWithParameters = algorithmsWithParameters;
	}
	
	@Override
	public void run() {
		System.out.println("Running thread #" + m_threadIndex);
		
		try {
			
			String algorithm;
			String parameters;
			String[] listOfParameters;
			
			String path = "/home/estan/weka/TestSuite2/results/";
//			String path = "/home/edu/Workspaces/eclipse/results/";
			String fileName = m_dataset + ".txt";
			File file = new File(path + fileName);
			
			file.createNewFile();
			
			FileWriter fw = new FileWriter(path + fileName, false); // False to begin a new file
			
			// Write header
			fw.write("--------------------------------------------------------------------------------------------------------------------\n");
			fw.write("| ");
			fw.write(String.format("%-30s | ", "DATASET NAME"));
			fw.write(String.format("%-9s | ", "ALGORITHM"));
			fw.write(String.format("%-20s | ", "PARAMETERS"));
			fw.write(String.format("%-8s | ", "#POINTS"));
			fw.write(String.format("%-10s | ", "ACCURACY"));
			fw.write(String.format("%-20s | \n", "TIME"));
			fw.write("--------------------------------------------------------------------------------------------------------------------\n");
			// Close the file
			fw.close();
			
			// For each algorithm
			for (int algIndex = 0; algIndex < m_algorithmsWithParameters.length; algIndex++) {
				
				// Algorithm name
				algorithm = m_algorithmsWithParameters[algIndex].split(":")[0];
				
				// Parameters
				parameters = m_algorithmsWithParameters[algIndex].split(":")[1];
				
				// String[] of parameters
				listOfParameters = parameters.replace("[", "").replace("]", "").split(",");
				
				System.out.println("Thread #" + m_threadIndex + " (" + m_dataset + ")" + " ==> algorithm: " + algorithm + " " + parameters);
				
				// Reopen file
				fw = new FileWriter(path + fileName, true); // True to append
				
				if (algorithm.equals("J48T")) {
					
					// Classifier
					J48T classifier = new J48T();
					// Binary splits
					classifier.setBinarySplits(true);
					// Unpruned tree
					classifier.setUnpruned(true);
					//
					classifier.setFractionValues(Double.parseDouble(listOfParameters[0]));
					
					// Filter
					RemoveObservations filter = new RemoveObservations();
					filter.setKeepType(1);
					filter.setKeepValue(m_points);
					filter.setOutputFormatType(1);	// String-valued dataset
					
					fw.write(compute(m_dataset, filter, classifier, algorithm, parameters, m_points));
				}
				else if (algorithm.equals("J48")) {
					// J48 Classifier
					J48 classifier = new J48();
					// Binary splits
					classifier.setBinarySplits(true);
					// Unpruned tree
					classifier.setUnpruned(true);
					
					// Filter
					RemoveObservations filter = new RemoveObservations();
					filter.setKeepType(1);
					filter.setOutputFormatType(2);	// Numeric-valued dataset
					
					// Parameters
					if (listOfParameters[0].equals("1"))
						filter.setMomentMean(true);		// Mean?
					if (listOfParameters[1].equals("1"))
						filter.setMomentVariance(true);	// Variance?
					if (listOfParameters[2].equals("1"))
						filter.setMomentSkewness(true);	// Skewness?
					if (listOfParameters[3].equals("1"))
						filter.setMomentKurtosis(true); // Kurtosis?
					
					fw.write(compute(m_dataset, filter, classifier, algorithm, parameters, m_points));
				}
				else if (algorithm.equals("NN_ED_I") || algorithm.contentEquals("NN_DTW_I") || algorithm.equals("NN_DTW_D")) {
					
					// Classifier
					Classifier classifier = null;
					// NN_ED_I Classifier
					if (algorithm.equals("NN_ED_I")) {
						classifier = new NN_ED_I();
					}
					// NN_DTW_I Classifier
					else if (algorithm.equals("NN_DTW_I")) {
						classifier = new NN_DTW_I();
					}
					// NN_ED_I Classifier
					else if (algorithm.equals("NN_DTW_D")) {
						classifier = new NN_DTW_D();
					}
					
					// Filter
					RemoveObservations filter = new RemoveObservations();
					filter.setKeepType(1);
					filter.setKeepValue(m_points);
					filter.setOutputFormatType(0);	// Relational-valued dataset
					
					fw.write(compute(m_dataset, filter, classifier, algorithm, parameters, m_points));
				}
				// Write ending
				fw.write("--------------------------------------------------------------------------------------------------------------------\n");
				// Close the file
				fw.close();
			}
			
			// Reopen file
			fw = new FileWriter(path + fileName, true); // True to append
			// Write finished
			fw.write("Finished\n");
			// Close the file
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Thread #" + m_threadIndex + " interrupted");
			e.printStackTrace();
		}
	}
	
	public void start() {
		System.out.println("Starting thread #" + m_threadIndex);
		
		if (m_thread == null) {
			m_thread = new Thread(this);
			m_thread.start();
		}
	}
	
	protected static String compute(String dataset, RemoveObservations filter, Classifier classifier, String algorithm, String parameters, int points) throws Exception {
		
		Instances train, test;
		Evaluation eval;
		
		String output = "";
		
		String path = "/home/estan/weka/TestSuite2/";
//		String path = "/home/edu/Workspaces/eclipse/";
		
		// Training set
		train = DataSource.read(path + "data/mtsc/MERGED/" + dataset + "/" + dataset + "_TRAIN_resampled.arff");
		train.setClassIndex(train.numAttributes() - 1);
		
		// Test set
		test = DataSource.read("data/mtsc/MERGED/" + dataset + "/" + dataset + "_TEST_resampled.arff");
		test.setClassIndex(test.numAttributes() - 1);

		// Set number of points to keep
		filter.setKeepValue(points);	
		
		FilteredClassifier fc = new FilteredClassifier();
		// Set filter
		fc.setFilter(filter);
		// Set classifier
		fc.setClassifier(classifier);
		// Start time
		long startTime = System.currentTimeMillis();
		// Build classifier
		fc.buildClassifier(train);
		// New evaluation
		eval = new Evaluation(train);
		// Evaluate on test set
		eval.evaluateModel(fc, test);							
		// End time
		long endTime = System.currentTimeMillis();
		// Elapsed time in seconds
		float seconds = (endTime - startTime) / 1000F; 
		
		// Output		
		output += "| ";
		output += String.format("%-30s | ", dataset);
		output += String.format("%-9s | ", algorithm);
		output += String.format("%-20s | ", parameters);
		output += String.format("%8s | ", points);
		output += String.format("%10s | ", String.format("%.4f", eval.correct()/test.numInstances()*100) + " %");
		output += String.format("%20s | \n", String.format("%.2f", seconds) + " secs");
		
		return output;
	}
}

public class TestSuite2 {
	
	public static void main(String[] argv) {
	
		InputStream inputStream;
		
		String[] datasetsWithPoints;
		String[] datasets;
		int[] numPoints;
		
		String[] algorithmsWithParameters;
		
		try {
			
			Properties properties = new Properties();
			
			String path = "/home/estan/weka/TestSuite2/";
//			String path = "/home/edu/Workspaces/eclipse/";
			
			inputStream = new FileInputStream(path + "testsuite.properties");
			
			properties.load(inputStream);
			
			// Datasets with points
			datasetsWithPoints = properties.getProperty("datasets").toString().split(";");
			
			datasets = new String[datasetsWithPoints.length];
			numPoints = new int[datasetsWithPoints.length];
			
			for (int i = 0; i < datasetsWithPoints.length; i++) {
				datasets[i] = datasetsWithPoints[i].split(":")[0];
				numPoints[i] = Integer.parseInt(datasetsWithPoints[i].split(":")[1]);
			}
			
			
			// Algorithms with parameters
			algorithmsWithParameters = properties.getProperty("algorithms").toString().split(";");
			
			// For each dataset
			for (int dsIndex = 0; dsIndex < datasetsWithPoints.length; dsIndex++) {
				// Create new experiment; i.e., a Thread for each dataset
				new Experiment(dsIndex, datasets[dsIndex], numPoints[dsIndex], algorithmsWithParameters).start();
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
