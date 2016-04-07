package mstparser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mstparser.io.*;

public class DependencyEvaluator2 {

	public static List<Double> evaluate(String act_file, String pred_file, String format, boolean hasConfidence,
			String oper, int len) throws IOException {

		DependencyReader goldReader = DependencyReader.createDependencyReader(format);
		boolean labeled = goldReader.startReading(act_file);

		DependencyReader predictedReader;
		if (hasConfidence) {
			predictedReader = DependencyReader.createDependencyReaderWithConfidenceScores(format);
		} else {
			predictedReader = DependencyReader.createDependencyReader(format);
		}
		boolean predLabeled = predictedReader.startReading(pred_file);

		if (labeled != predLabeled)
			System.out.println(
					"Gold file and predicted file appear to differ on whether or not they are labeled. Expect problems!!!");

		int total = 0;
		int corr = 0;
		int corrL = 0;
		int numsent = 0;
		int corrsent = 0;
		int corrsentL = 0;
		int root_act = 0;
		int root_guess = 0;
		int root_corr = 0;

		DependencyInstance goldInstance = goldReader.getNext();
		DependencyInstance predInstance = predictedReader.getNext();

		while (goldInstance != null) {

			int instanceLength = goldInstance.length();

			if (instanceLength != predInstance.length())
				System.out.println("Lengths do not match on sentence " + numsent);

			int[] goldHeads = goldInstance.heads;
			String[] goldLabels = goldInstance.deprels;
			int[] predHeads = predInstance.heads;
			String[] predLabels = predInstance.deprels;

			boolean whole = true;
			boolean wholeL = true;

			// NOTE: the first item is the root info added during
			// nextInstance(), so we skip it.

			if (goldInstance.length() > 3) {

				for (int i = 1; i < instanceLength; i++) {
					if (predHeads[i] == goldHeads[i]) {
						corr++;
						if (labeled) {
							if (goldLabels[i].equals(predLabels[i]))
								corrL++;
							else
								wholeL = false;
						}
					} else {
						whole = false;
						wholeL = false;
					}
				}
				total += instanceLength - 1; // Subtract one to not score fake
												// root
												// token

				if (whole)
					corrsent++;
				if (wholeL)
					corrsentL++;
				numsent++;

				boolean islog = true;
				if (islog) {
					if (!whole) {
						// System.out.println("Characters: " +
						// Arrays.toString(goldInstance.forms));
						// System.out.println("gold Head: " +
						// Arrays.toString(goldInstance.heads));
						// System.out.println("gold Deprel: " +
						// Arrays.toString(goldInstance.deprels));
						// System.out.println("pred Head: " +
						// Arrays.toString(predInstance.heads));
						// System.out.println("pred Deprel: " +
						// Arrays.toString(predInstance.deprels));
						// System.out.println();
					}

					if (!whole && !wholeL) {
						// System.out.println("Characters: " +
						// Arrays.toString(goldInstance.forms));
						// System.out.println("gold Head: " +
						// Arrays.toString(goldInstance.heads));
						// System.out.println("gold Deprel: " +
						// Arrays.toString(goldInstance.deprels));
						// System.out.println("pred Head: " +
						// Arrays.toString(predInstance.heads));
						// System.out.println("pred Deprel: " +
						// Arrays.toString(predInstance.deprels));
						// System.out.println();
					}
				}

			}
			goldInstance = goldReader.getNext();
			predInstance = predictedReader.getNext();
		}

		List<Double> results = new ArrayList<Double>();

		results.add(new Double(total));
		results.add(new Double(corr));
		results.add(Double.valueOf((double) corr / total));
		results.add(Double.valueOf((double) corrsent / numsent));
		// System.out.println("Tokens: " + total);
		// System.out.println("Correct: " + corr);
		// System.out.println("Unlabeled Accuracy: " + ((double) corr / total));
		// System.out.println("Unlabeled Complete Correct: " + ((double)
		// corrsent / numsent));
		if (labeled) {
			results.add(Double.valueOf((double) corrL / total));
			results.add(Double.valueOf((double) corrsentL / numsent));
			// System.out.println("Labeled Accuracy: " + ((double) corrL /
			// total));
			// System.out.println("Labeled Complete Correct: " + ((double)
			// corrsentL / numsent));
		}

		return results;
	}

	public static void main(String[] args) throws IOException {
		String format = "CONLL";
		if (args.length > 2)
			format = args[2];

		evaluate(args[0], args[1], format, false, "equal", 3);
	}

}
