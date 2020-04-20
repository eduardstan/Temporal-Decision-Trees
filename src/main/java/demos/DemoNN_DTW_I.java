package demos;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.RemoveObservations;

import tsml.classifiers.multivariate.NN_DTW_I;

public class DemoNN_DTW_I {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Instances train = null;
		Instances test = null;
		
		try {
			
//			train = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TRAIN.arff");
//			train = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TRAIN.arff");
//			train = DataSource.read("data/mtsc/HandMovementDirection/HandMovementDirection_TRAIN.arff");
//			train = DataSource.read("data/mtsc/CharacterTrajectories/CharacterTrajectories_TRAIN.arff");
//			train = DataSource.read("data/mtsc/Epilepsy/Epilepsy_TRAIN.arff");
//			train = DataSource.read("data/mtsc/Libras/Libras_TRAIN.arff");
//			train = DataSource.read("data/mtsc/LSST/LSST_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/RacketSports/RacketSports_TRAIN.arff");
//			train = DataSource.read("data/mtsc/StandWalkJump/StandWalkJump_TRAIN.arff");
//			train = DataSource.read("data/mtsc/PhonemeSpectra/PhonemeSpectra_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/MERGED/AtrialFibrillation/AtrialFibrillation_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/MERGED/CharacterTrajectories/CharacterTrajectories_TRAIN_resampled.arff");
			train = DataSource.read("data/mtsc/MERGED/HandMovementDirection/HandMovementDirection_TRAIN_resampled.arff");
			train.setClassIndex(train.numAttributes() - 1);
			
//			test = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TEST.arff");
//			test = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TEST.arff");
//			test = DataSource.read("data/mtsc/HandMovementDirection/HandMovementDirection_TEST.arff");
//			test = DataSource.read("data/mtsc/CharacterTrajectories/CharacterTrajectories_TEST.arff");
//			test = DataSource.read("data/mtsc/Epilepsy/Epilepsy_TEST.arff");
//			test = DataSource.read("data/mtsc/Libras/Libras_TEST.arff");
//			test = DataSource.read("data/mtsc/LSST/LSST_TEST.arff");
//			test = DataSource.read("data/mtsc/RacketSports/RacketSports_TEST.arff");
//			test = DataSource.read("data/mtsc/StandWalkJump/StandWalkJump_TEST.arff");
//			test = DataSource.read("data/mtsc/PhonemeSpectra/PhonemeSpectra_TEST.arff");
//			test = DataSource.read("data/mtsc/MERGED/AtrialFibrillation/AtrialFibrillation_TEST_resampled.arff");
//			test = DataSource.read("data/mtsc/MERGED/CharacterTrajectories/CharacterTrajectories_TEST_resampled.arff");
			test = DataSource.read("data/mtsc/MERGED/HandMovementDirection/HandMovementDirection_TEST_resampled.arff");
			test.setClassIndex(test.numAttributes() - 1);
			
			// Filter
			RemoveObservations ro = new RemoveObservations();
			ro.setKeepType(1);
			ro.setKeepValue(25);		// How many points (expressed as fraction)?
			ro.setOutputFormatType(0);	// Relational-valued dataset

			// Classifier
			NN_DTW_I nn_dtw_i = new NN_DTW_I();
			
			// Meta classifier
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(ro);
			fc.setClassifier(nn_dtw_i);
			
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
			System.out.println(eval.toClassDetailsString("=== Detailed Accuracy by Class (test data) ===\n"));
			System.out.println(eval.toMatrixString("=== Confusion Matrix (test data) ===\n"));
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
