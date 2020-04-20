package demos;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48T;

//import java.util.Properties;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.RemoveObservations;

public class DemoJ48T {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Instances train = null;
		Instances test = null;
		
		try {
			
//			Properties properties = new Properties();
			
//			train = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TRAIN.arff");
//			train = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TRAIN.arff");
//			train = DataSource.read("data/mtsc/HandMovementDirection/HandMovementDirection_TRAIN.arff");
//			train = DataSource.read("data/mtsc/Handwriting/Handwriting_TRAIN.arff");
//			train = DataSource.read("data/mtsc/Libras/Libras_TRAIN.arff");
//			train = DataSource.read("data/mtsc/LSST/LSST_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/RacketSports/RacketSports_TRAIN.arff");
//			train = DataSource.read("data/mtsc/StandWalkJump/StandWalkJump_TRAIN.arff");
//			train = DataSource.read("data/mtsc/JapaneseVowels/JapaneseVowels_TRAIN.arff");
//			train = DataSource.read("data/mtsc/PhonemeSpectra/PhonemeSpectra_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/MERGED/AtrialFibrillation/AtrialFibrillation_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/MERGED/CharacterTrajectories/CharacterTrajectories_TRAIN_resampled.arff");
//			train = DataSource.read("data/mtsc/MERGED/HandMovementDirection/HandMovementDirection_TRAIN_resampled.arff");
			train = DataSource.read("data/mtsc/MERGED/RacketSports/RacketSports_TRAIN_resampled.arff");
			train.setClassIndex(train.numAttributes() - 1);
			
//			test = DataSource.read("data/mtsc/BasicMotions/BasicMotions_TEST.arff");
//			test = DataSource.read("data/mtsc/FingerMovements/FingerMovements_TEST.arff");
//			test = DataSource.read("data/mtsc/HandMovementDirection/HandMovementDirection_TEST.arff");
//			test = DataSource.read("data/mtsc/Handwriting/Handwriting_TEST.arff");
//			test = DataSource.read("data/mtsc/Libras/Libras_TEST.arff");
//			test = DataSource.read("data/mtsc/LSST/LSST_TEST.arff");
//			test = DataSource.read("data/mtsc/RacketSports/RacketSports_TEST.arff");
//			test = DataSource.read("data/mtsc/StandWalkJump/StandWalkJump_TEST.arff");
//			test = DataSource.read("data/mtsc/JapaneseVowels/JapaneseVowels_TEST.arff");
//			test = DataSource.read("data/mtsc/PhonemeSpectra/PhonemeSpectra_TEST.arff");
//			test = DataSource.read("data/mtsc/MERGED/AtrialFibrillation/AtrialFibrillation_TEST_resampled.arff");
//			test = DataSource.read("data/mtsc/MERGED/CharacterTrajectories/CharacterTrajectories_TEST_resampled.arff");
//			test = DataSource.read("data/mtsc/MERGED/HandMovementDirection/HandMovementDirection_TEST_resampled.arff");
			test = DataSource.read("data/mtsc/MERGED/RacketSports/RacketSports_TEST_resampled.arff");
			test.setClassIndex(test.numAttributes() - 1);
			
			// Filter
			RemoveObservations ro = new RemoveObservations();
			ro.setKeepType(1);
			ro.setKeepValue(30);		// How many points (expressed as fraction)?
			ro.setOutputFormatType(1);	// String-valued dataset
			ro.setDiscreteDerivative(0);

			// Classifier
			J48T j48t = new J48T();
			j48t.setBinarySplits(true);	// Only binary splits have been implemented
//			j48t.setSubtreeRaising(false);
			j48t.setUnpruned(true);
//			j48t.setCollapseTree(false);
//			j48t.setObjectLanguageRelationMask("1,"		// LaterThan
//											 + "0,"		// AfterThan
//											 + "0,"		// Overlaps
//											 + "0,"		// Ends
//											 + "0,"		// During
//											 + "0,"		// Begins
//											 + "0,"		// InverseLaterThan
//											 + "0,"		// InverseAfterThan
//											 + "0,"		// InverseOverlaps
//											 + "0,"		// InverseEnds
//											 + "0,"		// InverseDuring
//											 + "0,"		// InverseBegins
//											 + "0"		// Equals
//											 );
			j48t.setFractionValues(0.6);
//			j48t.setObjectLanguage(10);
//			j48t.setObjectLanguageRelationMask("1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1");
//			j48t.setObjectLanguageRelationMask("1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0");
			
			// Meta classifier
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(ro);
			fc.setClassifier(j48t);
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
			
//			System.out.println(j48t.toSource("J48T"));
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}