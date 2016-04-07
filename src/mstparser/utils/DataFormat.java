package mstparser.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mstparser.Util;



public class DataFormat {

	public static void main(String[] args) {
//		Util.conll2text("data/sample.data", "data/sample.txt");
//		convertRoot();
//		shuffleData("data/datafull_withforeign.half");
		generateCV("data/datafull_withforeign.half.shuffle" , "data/tmp", 10, 35000);
	}
	
	public static void checkLabel(){
		
		List<String> labelset = new ArrayList<String>(Arrays.asList("WB", "WI", "B", "ROOT", "C"));
		BufferedReader fi = null;
		try {
			fi = new BufferedReader(new FileReader("data/datafull_withchinese.train"));
			for (String line = fi.readLine(); line != null; line = fi.readLine()) {
				line = line.trim();
				if ((line.trim().length() == 0) || (line.trim().charAt(0) == '#')){
					continue;
				}
				String[] toks = line.split("\t");
				if (toks.length < 10){
					System.out.println(line);
				}
				if (!labelset.contains(toks[7])){
					System.out.println(line);
				}
				
				
			}
		} catch (IOException ioe) {
			System.out.println("FileReaderException :" + ioe.toString());
		} finally {
			try {
				fi.close();
			} catch (IOException ioe) {
				System.out.println("CloseReaderException :"+ ioe.toString());
			}
		}
	}
	
	public static void convertRoot(){
		BufferedReader fi = null;
		BufferedWriter fo = null;
		try {
			fi = new BufferedReader(new FileReader("data/datafull_withchinese.half"));
			fo = new BufferedWriter(new FileWriter("data/datafull_withchinese.train"));
			for (String line = fi.readLine(); line != null; line = fi.readLine()) {
				line = line.trim();
				if ((line.trim().length() == 0) || (line.trim().charAt(0) == '#')){
					fo.newLine();
					continue;
				}
				List<String> toks = new ArrayList<String>(Arrays.asList(line.split("\t")));
				if (toks.get(7).equals("R"))
					toks.set(7, "");
				toks.remove(9);
				toks.remove(8);
				fo.write(Util.join(toks, "\t") + '\n');
			}
		} catch (IOException ioe) {
			System.out.println("FileReaderException :" + ioe.toString());
		} finally {
			try {
				fi.close();
				fo.close();
			} catch (IOException ioe) {
				System.out.println("CloseReaderException :"+ ioe.toString());
			}
		}
	}
	
	public static void shuffleData(String filename){
		List<List<String>> phraseList = readData(filename);
		System.out.print(phraseList.size());
		Collections.shuffle(phraseList);
		writeData(phraseList, filename + ".shuffle");
	}
	
	public static void generateCV(String infile, String dir, int piece, int size) {

		List<List<String>> train = null;
		List<List<String>> test = null;
		
		List<List<String>> rawList = readData(infile);
		
		int real_size = (size == 0) ? rawList.size() : size;
		List<List<String>> phraseList = rawList.subList(0, real_size);
		
		int len = phraseList.size();
		int subArrayLength = (int) Math.ceil(new Double(len) / piece);

		System.out.println("dependency tree size: " + phraseList.size());
		for (int i = 0; i < piece; i++) {
			if (i == 0) {
				test = phraseList.subList(0, subArrayLength);
				train = phraseList.subList(subArrayLength,
						phraseList.size());
			} else if (i == piece - 1) {
				test = phraseList.subList((piece - 1) * subArrayLength,
						phraseList.size());
				train = phraseList.subList(0, (piece - 1) * subArrayLength);
			} else {
				test = phraseList.subList(i * subArrayLength, (i + 1) * subArrayLength);
				train = new ArrayList<List<String>>(phraseList.subList(0, i * subArrayLength));
				Collections.copy(train, phraseList.subList(0, i * subArrayLength));
				train.addAll(phraseList.subList((i + 1) * subArrayLength, phraseList.size()));
			}
			System.out.println("CV" + i + ", train : " + train.size() + ", test : " + test.size());
			writeData(train, dir + "/train" + i);
			writeData(test, dir + "/test" + i);
		}

		System.out.println("Formated file generated...");
	}
	
	public static List<List<String>> readData(String infile){
		BufferedReader fi = null;
		List<List<String>> phraseList = null;
		List<String> tempList = null;
		
		try {
			fi = new BufferedReader(new FileReader(infile));
			phraseList = new ArrayList<List<String>>();
			tempList = new ArrayList<>();
			for (String line = fi.readLine(); line != null; line = fi.readLine()) {
				line = line.trim();
				if ((line.trim().length() == 0) || (line.trim().charAt(0) == '#')){
					if (!tempList.isEmpty()){
						phraseList.add(tempList);
						tempList = new ArrayList<String>();
					}
					continue;
				}
				tempList.add(line);
			}
			if (!tempList.isEmpty()){
				phraseList.add(tempList);
				tempList = new ArrayList<String>();
			}
			return phraseList;
		} catch (IOException ioe) {
			System.out.println("FileReaderException :" + ioe.toString());
			return null;
		} finally {
			try {
				fi.close();
			} catch (IOException ioe) {
				System.out.println("CloseReaderException :"+ ioe.toString());
			}
		}
		
	}
	
	public static void writeData(List<List<String>> data, String outfile){
		BufferedWriter fo = null;
		try {
			fo = new BufferedWriter(new FileWriter(outfile));
			for (List<String> phrase : data){
				for (String line : phrase){
					String[] toks = line.split("\t");
					toks[5] = "_";
//					System.out.println(Util.join(toks, '-'));
					fo.write(Util.join(toks, '\t') + '\n');
				}
				fo.newLine();
			}
			
		} catch (IOException ioe) {
			System.out.println("FileReaderException :" + ioe.toString());
		} finally {
			try {
				fo.close();
			} catch (IOException ioe) {
				System.out.println("CloseReaderException :"+ ioe.toString());
			}
		}
		
	}
}
