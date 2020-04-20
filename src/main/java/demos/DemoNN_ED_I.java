package demos;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.RemoveObservations;

import tsml.classifiers.multivariate.NN_ED_I;

public class DemoNN_ED_I {
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Instances train = null;
		Instances test = null;
		
		try {
			
//			train = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TRAIN.arff");
//			train = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TRAIN.arff");
			train = DataSource.read("data/mtsc/Libras/Libras_TRAIN.arff");
			train.setClassIndex(train.numAttributes() - 1);
			
//			test = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TEST.arff");
//			test = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TEST.arff");
			test = DataSource.read("data/mtsc/Libras/Libras_TEST.arff");
			test.setClassIndex(test.numAttributes() - 1);
			
			// Filter
			RemoveObservations ro = new RemoveObservations();
			ro.setKeepType(0);
			ro.setKeepValue(0.25);		// How many points (expressed as fraction)?
			ro.setOutputFormatType(0);	// Relational-valued dataset

			// Classifier
			NN_ED_I nn_ed_i = new NN_ED_I();
			
			// Meta classifier
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(ro);
			fc.setClassifier(nn_ed_i);
			
			fc.buildClassifier(train);
			
			System.out.println("=== Classification model (full training set) ===");
			System.out.println(fc);
			
			// Evaluation train + test
			Evaluation eval = new Evaluation(train);
			
			// Evaluation (full) training set
			eval.evaluateModel(fc, train);
			System.out.println(eval.toClassDetailsString("=== Detailed Accuracy by Class (training data) ===\n"));
			System.out.println(eval.toMatrixString("=== Confusion Matrix (training data) ===\n"));
			System.out.println(eval.toSummaryString("=== Error on training data ===\n", false));
			
			// Evaluation test set
			eval = new Evaluation(train); // Re-initialize evaluation
			eval.evaluateModel(fc, test); // Otherwise, when calling evaluateModel with test set, it will queue the number of instances (i.e., |train|+|test|).
			System.out.println(eval.toClassDetailsString("=== Detailed Accuracy by Class (test data) ===\n"));
			System.out.println(eval.toMatrixString("=== Confusion Matrix (test data) ===\n"));
			System.out.println(eval.toSummaryString("=== Error on test data ===\n", false));
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
