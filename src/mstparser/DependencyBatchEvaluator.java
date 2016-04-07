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
		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]), "equal", 3);
//		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]), "equal", 4);
//		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]), "equal", 5);
//		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]), "equal", 6);
//		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]), "equal", 7);
//		DependencyBatchEvaluator.run(args[0], args[1], Integer.parseInt(args[2]), "equal", 8);
	}
	
	public static void run(String test, String output, int num, String oper, int len){
		try{
            System.out.println("word length equal: " + len);
//			String[] fileSets = {"0", "1", "2", "3", "4"};
			String testFile = "";
			String outputFile = "";
			List<Double> sumValues = new ArrayList<Double>();
//			for (String fileSet : fileSets){
			for (int i = 0; i < num; i++){
				testFile = test + String.valueOf(i);
				outputFile = output + String.valueOf(i);
				List<Double> value = DependencyEvaluator2.evaluate(testFile, outputFile, "CONLL", false, oper, len);
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
		System.out.format("Unlabeled Accuracy: %.4f%n", a.get(2));
		System.out.format("Unlabeled Complete Correct: %.4f%n", a.get(3));
		System.out.format("Labeled Accuracy: %.4f%n", a.get(4));
		System.out.format("Labeled Complete Correct: %.4f%n", a.get(5));
	}
	
	public static List<Double> averList(List<Double> a, int n){
		
		List<Double> aver = new ArrayList<Double>();
		
		for (int i = 0; i < a.size(); i++){
			aver.add(a.get(i).doubleValue()/n);
		}
		return aver;
	}

}
