package demos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

public class MergeDatasets {
	
	public static void main(String[] argv) throws Exception {
		
		Instances train, test, all = null;
		
//		ArffSaver saver = null;
		
		BufferedWriter writer = null;
		
		File rootFolder = new File("data/mtsc/MERGED");
		
		File[] listOfFiles = rootFolder.listFiles();
		
		for (File file : listOfFiles)
			if (file.isDirectory())
				System.out.println(file.getName());
		
		File[] directories = rootFolder.listFiles(File::isDirectory);
		
		System.out.println(Arrays.toString(directories));
		
		for (int i = 0; i < directories.length; i++) {
			
			System.out.println("\t\t" + directories[i].getName());
//			saver = new ArffSaver();
			
			train = DataSource.read(directories[i].getPath() + "/" + directories[i].getName() + "_TRAIN.arff");
			test = DataSource.read(directories[i].getPath() + "/" + directories[i].getName() + "_TEST.arff");
			
			System.out.println("train.numInstances()=" + train.numInstances());
			System.out.println("test.numInstances()=" + test.numInstances());
			
			all = merge(train, test);
			System.out.println("all.numInstances()=" + all.numInstances());
			
			
//			saver.setInstances(all);
//			saver.setFile(new File(directories[i].getPath() + "/" + directories[i].getName() + "_ALL.arff"));
//			saver.writeBatch();
			
//			System.out.println(all.toString());
			
			
//			writer = new BufferedWriter(new FileWriter(directories[i].getPath() + "/" + directories[i].getName() + "_ALL.arff"));
//			writer.write(all.toString());
//			writer.flush();
//			writer.close();
		}
	}
	
	public static final Instances merge(Instances data1, Instances data2) {
		Instances result = new Instances(data1);
		Instances source = new Instances(data2);
		
		result.setRelationName(data1.relationName() + "_mergedWith_" + data2.relationName());
		
		Enumeration<Instance> instances = source.enumerateInstances();
		
		Instance current = null;
				
//		while (instances.hasMoreElements()) {
////			System.out.println("current: " + current);
//			current = instances.nextElement();
//			result.add(current);
//		}
		
//		for (int instIndex = 0; instIndex < source.numInstances(); instIndex++) {
//			Instance instance = source.instance(instIndex);
//			result.add(instance);
//		}
		
		result.addAll(source);
		
		return result;
	}
}
