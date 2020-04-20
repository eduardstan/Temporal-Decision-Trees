package demos;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;

import java.text.DecimalFormat;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.RemoveObservations;

public class DemoJ48 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Instances train = null;
		Instances test = null;
		
		try {
			
//			train = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TRAIN.arff");
//			train = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TRAIN.arff");
//			train = DataSource.read("data/mtsc/HandMovementDirection/HandMovementDirection_TRAIN.arff");
//			train = DataSource.read("data/mtsc/Libras/Libras_TRAIN.arff");
//			train = DataSource.read("data/mtsc/RacketSports/RacketSports_TRAIN.arff");
			train = DataSource.read("data/mtsc/StandWalkJump/StandWalkJump_TRAIN.arff");
			train.setClassIndex(train.numAttributes() - 1);
			
//			test = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TEST.arff");
//			test = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TEST.arff");
//			test = DataSource.read("data/mtsc/HandMovementDirection/HandMovementDirection_TEST.arff");
//			test = DataSource.read("data/mtsc/Libras/Libras_TEST.arff");
//			test = DataSource.read("data/mtsc/RacketSports/RacketSports_TEST.arff");
			test = DataSource.read("data/mtsc/StandWalkJump/StandWalkJump_TEST.arff");
			test.setClassIndex(test.numAttributes() - 1);
			
			// Filter
			RemoveObservations ro = new RemoveObservations();
			ro.setKeepType(0);
			ro.setKeepValue(0.12);		// How many points (expressed as fraction)?
			ro.setOutputFormatType(2);	// String-valued dataset
			ro.setMomentMean(true);		// Mean?
			ro.setMomentVariance(true);	// Variance?
			ro.setMomentSkewness(true);	// Skewness?
			ro.setMomentKurtosis(true); // Kurtosis?

			// Classifier
			J48 j48 = new J48();
			j48.setBinarySplits(true);	// Only binary splits have been implemented
			j48.setUnpruned(true);
			
			// Meta classifier
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(ro);
			fc.setClassifier(j48);
			
			long start = System.currentTimeMillis();
			fc.buildClassifier(train);
			long end = System.currentTimeMillis();
			
			System.out.println("=== Classification model (full training set) ===");
			System.out.println(fc);
			float sec = (end - start) / 1000F; 
			System.out.println("Time taken to build model: " + sec + " seconds\n");
			
			// Evaluation train + test
			Evaluation eval = new Evaluation(train);
			
			// Evaluation (full) training set
			eval.evaluateModel(fc, train);
			System.out.println(eval.toSummaryString("=== Error on training data ===\n", false));
			System.out.println(eval.toClassDetailsString("=== Detailed Accuracy by Class (training data) ===\n"));
			System.out.println(eval.toMatrixString("=== Confusion Matrix (training data) ===\n"));
			
			// Evaluation test set
			eval = new Evaluation(train); // Re-initialize evaluation
			eval.evaluateModel(fc, test); // Otherwise, when calling evaluateModel with test set, it will queue the number of instances (i.e., |train|+|test|).
			System.out.println(eval.toSummaryString("=== Error on test data ===\n", false));
			
//			DecimalFormat df = new DecimalFormat("#.##");
//			System.out.println(Double.valueOf(df.format(eval.correct()/test.numInstances()*100)));
			
			System.out.println(eval.toClassDetailsString("=== Detailed Accuracy by Class (test data) ===\n"));
			System.out.println(eval.toMatrixString("=== Confusion Matrix (test data) ===\n"));
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
