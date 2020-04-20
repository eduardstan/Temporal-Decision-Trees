package demos;

import java.io.FileInputStream;
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
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveObservations;

/**
 * Test suite number 1. <br>
 * This suite is made to cycle on the resampled datasets. In particular, 
 * for each dataset, if the dataset has too many points, then compute the 
 * accuracies incrementally 50 points, 100 points, 150 points, ecc.
 * 
 * @author edu
 *
 */
public class TestSuite1 {
	
	public static void main(String[] argv) {
		
		InputStream inputStream;
		
		String[] datasetsWithPoints = null;
		String[] datasets = null;
		int[] numPoints = null;
		
		String[] algorithmsWithParameters = null;
		String algorithm;
		String[] listOfParameters;
		String parameters;
		
		try {
			
			Properties properties = new Properties();
			
			inputStream = new FileInputStream("resources/testsuite1.properties");
			
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
			
			// https://dzone.com/articles/java-string-format-examples
			// Header
			System.out.println("--------------------------------------------------------------------------------------------------------------------");
			System.out.print("| ");
			System.out.print(String.format("%-30s | ", "DATASET NAME"));
			System.out.print(String.format("%-9s | ", "ALGORITHM"));
			System.out.print(String.format("%-20s | ", "PARAMETERS"));
			System.out.print(String.format("%-8s | ", "#POINTS"));
			System.out.print(String.format("%-10s | ", "ACCURACY"));
			System.out.println(String.format("%-20s | ", "TIME TO BUILD MODEL"));
			System.out.println("--------------------------------------------------------------------------------------------------------------------");
			
			// For each dataset
			for (int dsIndex = 0; dsIndex < datasetsWithPoints.length; dsIndex++) {
				
				// For each algorithm
				for (int algIndex = 0; algIndex < algorithmsWithParameters.length; algIndex++) {
					
					// Algorithm name
					algorithm = algorithmsWithParameters[algIndex].split(":")[0];
					
					// Parameters
					parameters = algorithmsWithParameters[algIndex].split(":")[1];
					
					// String[] of parameters
					listOfParameters = parameters.replace("[", "").replace("]", "").split(",");
					
					if (algorithm.equals("J48")) {
						
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
						
						// If there are less than or equal 50 points, compute the output for those points
						if (numPoints[dsIndex] <= 50) {
							
							compute(datasets[dsIndex], filter, classifier, algorithm, parameters, numPoints[dsIndex]);
							
							System.out.println("--------------------------------------------------------------------------------------------------------------------");
						}
						// Else
						else {
							// Go incrementally from 50 to 100 to 150, ecc.. up to less than the exact number of points
							for (int points = 50; points < numPoints[dsIndex]; points=points+50) {
								compute(datasets[dsIndex], filter, classifier, algorithm, parameters, points);
							}
							
							// Since we have left out the exact points, compute the output for those points
							compute(datasets[dsIndex], filter, classifier, algorithm, parameters, numPoints[dsIndex]);
							
							System.out.println("--------------------------------------------------------------------------------------------------------------------");
						}
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
						filter.setOutputFormatType(0);	// Relational-valued dataset
						
						// If there are less than or equal 50 points, compute the output for those points
						if (numPoints[dsIndex] <= 50) {
							
							compute(datasets[dsIndex], filter, classifier, algorithm, parameters, numPoints[dsIndex]);
							
							System.out.println("--------------------------------------------------------------------------------------------------------------------");
						}
						// Else
						else {
							// Go incrementally from 50 to 100 to 150, ecc.. up to less than the exact number of points
							for (int points = 50; points < numPoints[dsIndex]; points=points+50) {
								compute(datasets[dsIndex], filter, classifier, algorithm, parameters, points);
							}
							
							// Since we have left out the exact points, compute the output for those points
							compute(datasets[dsIndex], filter, classifier, algorithm, parameters, numPoints[dsIndex]);
							
							System.out.println("--------------------------------------------------------------------------------------------------------------------");
						}
					}
				}
				// Print to break the output from dataset_i to dataset_i+1
				System.out.println("--------------------------------------------------------------------------------------------------------------------");
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected static void compute(String dataset, RemoveObservations filter, Classifier classifier, String algorithm, String parameters, int points) throws Exception {
		
		Instances train, test;
		Evaluation eval;
		DecimalFormat df = new DecimalFormat("#.####");
		
		// Training set
		train = DataSource.read("data/mtsc/MERGED/" + dataset + "/" + dataset + "_TRAIN_resampled.arff");
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
		System.out.print("| ");
		System.out.print(String.format("%-30s | ", dataset));
		System.out.print(String.format("%-9s | ", algorithm));
		System.out.print(String.format("%-20s | ", parameters));
		System.out.print(String.format("%8s | ", points));
//		System.out.print(String.format("%10s | ", Double.valueOf(df.format(eval.correct()/test.numInstances()*100))));
//		System.out.println(String.format("%20s | ", Double.valueOf(df.format(seconds))));
		System.out.print(String.format("%10s | ", String.format("%.4f", eval.correct()/test.numInstances()*100) + " %"));
		System.out.println(String.format("%20s | ", String.format("%.2f", seconds) + " secs"));
	}
}
