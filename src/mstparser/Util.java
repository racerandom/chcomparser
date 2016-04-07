package mstparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods that may be generally useful.
 * 
 * @author Jason Baldridge
 * @created August 27, 2006
 */
public class Util {

  // Assumes input is a String[] containing integers as strings.
  public static int[] stringsToInts(String[] stringreps) {
    int[] nums = new int[stringreps.length];
    for (int i = 0; i < stringreps.length; i++)
      nums[i] = Integer.parseInt(stringreps[i]);
    return nums;
  }

  // Assumes input is a String[] containing doubles as strings.
  public static double[] stringsToDoubles(String[] stringreps) {
    double[] nums = new double[stringreps.length];
    for (int i = 0; i < stringreps.length; i++)
      nums[i] = Double.parseDouble(stringreps[i]);
    return nums;
  }

  public static String join(String[] a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a[0]);
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(a[i]);
    return sb.toString();
  }
  
  public static String join(String[] a, String sep) {
	    StringBuffer sb = new StringBuffer();
	    sb.append(a[0]);
	    for (int i = 1; i < a.length; i++)
	      sb.append(sep).append(a[i]);
	    return sb.toString();
	  }
  
  public static String join(List<String> a, String sep) {
	    StringBuffer sb = new StringBuffer();
	    sb.append(a.get(0));
	    for (int i = 1; i < a.size(); i++)
	      sb.append(sep).append(a.get(i));
	    return sb.toString();
	  }

  public static String join(int[] a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a[0]);
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(a[i]);
    return sb.toString();
  }

  public static String join(double[] a, char sep, int fractionDigits) {
    StringBuffer sb = new StringBuffer();
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(fractionDigits);
    sb.append(df.format(a[0]));
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(df.format(a[i]));
    return sb.toString();
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }
  
  public static void conll2text(String inputfile, String outputfile) {

	  BufferedReader fi = null;
	  BufferedWriter fo = null;
	  try {
		  fi = new BufferedReader(new FileReader(inputfile));
		  fo = new BufferedWriter(new FileWriter(outputfile));
		  List<String> words = new ArrayList<String>(); 
		  StringBuilder cache = new StringBuilder();
		  for (String line = fi.readLine(); line != null; line = fi.readLine()) {
				line = line.trim();
				if ((line.length() == 0) || (line.charAt(0) == '#')){
					if (!cache.toString().isEmpty()){
						words.add(cache.toString());
						cache = new StringBuilder();
					}
					if (words.size() != 0){
						fo.write(join(words, "  ") + '\n');
						words = new ArrayList<String>();
					}
					continue;
				}
				List<String> toks = new ArrayList<String>(Arrays.asList(line.split("\t")));
				if (toks.get(7).equals("WB") || toks.get(7).equals("WI")) {
					cache.append(toks.get(1));
				}else{
					cache.append(toks.get(1));
					words.add(cache.toString());
					cache = new StringBuilder();
				}
		  }
		  if (!cache.toString().isEmpty()){
			  words.add(cache.toString());
			  cache = new StringBuilder();
		  }
		  if (words.size() != 0){
			  fo.write(join(words, "  ") + '\n');
			  words = new ArrayList<String>();
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
}
