package mstparser;

//import data.format.FileSystem;

import java.io.IOException;
import java.util.*;

public class DependencyBatchEvaluator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){
		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]));
	}
	
	public static void run(String test, String output, int num){
		try{
//			String[] fileSets = {"0", "1", "2", "3", "4"};
			String testFile = "";
			String outputFile = "";
			List<Double> sumValues = new ArrayList<Double>();
//			for (String fileSet : fileSets){
			for (int i = 0; i < num; i++){
				testFile = test + String.valueOf(i);
				outputFile = output + String.valueOf(i);
				List<Double> value = DependencyEvaluator2.evaluate(testFile, outputFile, "CONLL", false);
				sumValues = DependencyBatchEvaluator.sumList(sumValues, value);
			}
			printList(DependencyBatchEvaluator.averList(sumValues, num));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static List<Double> sumList(List<Double> a, List <Double> b){
		
		List<Double> sum = new ArrayList<Double>();
		
		if (!a.isEmpty()){
			for (int i = 0; i < b.size(); i++){
				sum.add(i, a.get(i) + b.get(i));
			}
			return sum;
		}else{
			return b;
		}
	}
	
	public static void printList(List<Double> a){
//		System.out.println("Tokens: " + a.get(0));
//		System.out.println("Correct: " + a.get(1));
		System.out.println("Unlabeled Accuracy: " + a.get(2));
		System.out.println("Unlabeled Complete Correct: " + a.get(3));
		System.out.println("Labeled Accuracy: " + a.get(4));
		System.out.println("Labeled Complete Correct: " + a.get(5));
	}
	
	public static List<Double> averList(List<Double> a, int n){
		
		List<Double> aver = new ArrayList<Double>();
		
		for (int i = 0; i < a.size(); i++){
			aver.add(a.get(i).doubleValue()/n);
		}
		return aver;
	}

}
