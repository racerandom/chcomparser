package mstparser;

import gnu.trove.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import mstparser.io.DependencyReader;
import mstparser.io.DependencyWriter;

public class DependencyPipe {

	public Alphabet dataAlphabet;

	public Alphabet typeAlphabet;

	private DependencyReader depReader;

	private DependencyWriter depWriter;

	public String[] types;

	public int[] typesInt;

	public boolean labeled = false;

	private boolean isCONLL = true;

	private final ParserOptions options;

	// @kevin
	private THashMap avReMap = null;

	private THashMap posMap = null;

	private THashMap prefixMap = null;

	private THashMap suffixMap = null;
	
	private THashMap charClusters = null;
	
	private THashMap vecClusters = null;
	
	public boolean isAVFeat = true;
	
	public boolean isAVClass = true;

	public boolean isDictFeat = false;
	
	public boolean isPosFeat = false;
	
	public boolean isAffixFeat = false;
	
	public boolean isClusterFeat = false;
	
	public boolean isRootFeat = false;

	public DependencyPipe(ParserOptions options) throws IOException {
		this.options = options;

		if (!options.format.equals("CONLL")) {
			isCONLL = false;
		}

		dataAlphabet = new Alphabet();
		typeAlphabet = new Alphabet();

		depReader = DependencyReader.createDependencyReader(options.format,
				options.discourseMode);

		// @kevin
		String avFeatFile = "/home/fei-c/resource/chgigaword/xincmn_avfeats.out";
		String clusterFeatFile = "/home/fei-c/resource/chgigaword/gigaword_class.txt";
		String dictFile = "dicts/mecab-dic.csv";
		String vecclusterFile = "/home/fei-c/resource/chgigaword/xincmn_k100.sorted";

		if (isAVFeat == true) {
			setAvReMap(avFeatFile);
		}
		if (isDictFeat == true || isPosFeat == true) {
			setPOSMap(dictFile);
		}
		
		if (isAffixFeat == true){
			setPrefixMap("/home/fei-c/resource/feats/prefix_list");
			setSuffixMap("/home/fei-c/resource/feats/suffix_list");
		}
		
		if (isClusterFeat == true) {
//			setCharClusters(clusterFeatFile);
			setVecClusters(vecclusterFile);
		}
	}
	
	public void setCharClusters(String clusterFile) throws IOException {
		System.out.print("[pre]Start reading char clusters ...");
		charClusters = new THashMap();

		BufferedReader in = new BufferedReader(new FileReader(clusterFile));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0) {
				continue;
			}
			String[] daum = line.split(" ");
			if (!charClusters.containsKey(daum[0]))
				charClusters.put(daum[0], daum[1]);
		}
		in.close();
		System.out.println("... done. size : " + charClusters.size());
	}
	
	
	public void setVecClusters(String vecclusterFile) throws IOException {
		System.out.print("[pre]Start reading word2vec clusters ...");
		vecClusters = new THashMap();

		BufferedReader in = new BufferedReader(new FileReader(vecclusterFile));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0) {
				continue;
			}
			String[] daum = line.split(" ");
			if (!vecClusters.containsKey(daum[0]))
				vecClusters.put(daum[0], daum[1]);
		}
		in.close();
		System.out.println("... done. size : " + vecClusters.size());
	}
	
	public void setPrefixMap(String prefixDict) throws IOException {
		System.out.print("[pre]Start reading prefix Map ...");
		this.prefixMap = new THashMap();

		BufferedReader in = new BufferedReader(new FileReader(prefixDict));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0) {
				continue;
			}
			String[] daum = line.split("\t");
			if (!prefixMap.containsKey(daum[0]))
				prefixMap.put(daum[0], daum[1]);
		}
		in.close();

		System.out.println("... done. size : " + prefixMap.size());
	}
	
	public void setSuffixMap(String prefixDict) throws IOException {
		System.out.print("[pre]Start reading suffix Map ...");
		this.suffixMap = new THashMap();

		BufferedReader in = new BufferedReader(new FileReader(prefixDict));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0) {
				continue;
			}
			String[] daum = line.split("\t");
			if (!suffixMap.containsKey(daum[0]))
				suffixMap.put(daum[0], daum[1]);
		}
		in.close();

		System.out.println("... done. size : " + suffixMap.size());
	}
	
	public void setPOSMap(String dictFile) throws IOException {

		System.out.print("[pre]Start reading dictionary ...");

		this.posMap = new THashMap();

		BufferedReader in = new BufferedReader(new FileReader(dictFile));

		// line = 爱,NN,*,*,爱

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0) {
				continue;
			}
			// System.out.println(line);
			String[] daum = line.split(",");
			String key = daum[0].trim();
			if (key.length() <= 4) {
				String pos = daum[1].trim() + "-" + daum[2].trim() + "-"
						+ daum[3].trim();
				if (posMap.containsKey(key)) {
					List<String> l = (List<String>)posMap.get(key);
					if (!l.contains(pos)) {
						l.add(pos);
					}
				} else {
					posMap.put(key, new ArrayList<String>());
					List<String> l = (List<String>)posMap.get(key);
					l.add(pos);
				}
			}
		}
		in.close();

		System.out.println("... done. size : " + posMap.size());
	}

	public void setAvReMap(String avFeatFile) throws IOException {
		System.out.print("[pre]Start reading avReMap ...");
		BufferedReader in;

		this.avReMap = new THashMap();

		in = new BufferedReader(new FileReader(avFeatFile));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0) {
				continue;
			}
			String[] daum = line.trim().split("\t");
			String token = daum[0].trim();
			if (!token.equals("") || daum.length == 3) {
				int[] value = new int[2];
				value[0] = Integer.valueOf(daum[1]);
				value[1] = Integer.valueOf(daum[2]);
				this.avReMap.put(token, value);
			}
		}
		in.close();
		System.out.println("... done. size : " + avReMap.size());
	}
	
	public String getCharClusterFeat(String token) {
		if (charClusters.containsKey(token)) {
			return (String)charClusters.get(token);
		} else
			return "N";
	}
	
	public String getVecClusterFeat(String token) {
		String[] toks = token.split(" ");
		String retoken = StringUtils.join(toks);
//		System.out.println(retoken);
		if (vecClusters.containsKey(retoken)) {
//			System.out.println(retoken);
			return (String)vecClusters.get(token);
		} else
			return "N";
	}
	
	public String getClusterFeat(String token, String sep) {
		StringBuffer sb = new StringBuffer();
		String[] toks = token.split(sep);
	    sb.append(toks[0]);
		for (int i = 1; i < toks.length; i++){
			sb.append('-').append(getCharClusterFeat(toks[i]));
		}
		return sb.toString();
	}
	
	public String getDicFeat(String s) {

		if (posMap.containsKey(s)) {
			return "I";
		} else
			return "N";
	}
	
	public String getPrefixFeature(String token) {
		if (this.prefixMap.containsKey(token)) {
			return (String)this.prefixMap.get(token);
		} else
			return "_";
	}
	
	public String getSuffixFeature(String token) {
		if (this.suffixMap.containsKey(token)) {
			return (String)this.suffixMap.get(token);
		} else
			return "_";
	}
	
	public List<String> getPosFeat(String s) {

		if (posMap.containsKey(s)) {
			return (List<String>)posMap.get(s);
		} else
			return null;
	}
	
	public int getAvLeft(String token) {
		 String tok_space = joinString(token, " ");
		if (this.avReMap.containsKey(tok_space)){
			int[] value = (int[])this.avReMap.get(tok_space);
			return value[0];
		}else
			return 0;
	}
	
	public int getAvRight(String token) {
		 String tok_space = joinString(token, " ");
		if (this.avReMap.containsKey(tok_space)){
			int[] value = (int[])this.avReMap.get(tok_space);
			return value[1];
		}else
			return 0;
	}
	
	public int getAvCommon(String tok_space) {
//		String tok_space = joinString(token, " ");
		if (this.avReMap.containsKey(tok_space)){
			int[] value = (int[])this.avReMap.get(tok_space);
			return Math.min(value[0], value[1]);
//			System.out.print(result);
		}else
			return 0;
	}

	public static String joinString(String word, String delimiter) {
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char c : word.toCharArray()) {
			chars.add(c);
		}
		String joined = StringUtils.join(chars, delimiter);
		return joined;
	}

	public void initInputFile(String file) throws IOException {
		labeled = depReader.startReading(file);
	}

	public void initOutputFile(String file) throws IOException {
		depWriter = DependencyWriter.createDependencyWriter(options.format,
				labeled);
		depWriter.startWriting(file);
	}

	public void outputInstance(DependencyInstance instance) throws IOException {
		depWriter.write(instance);
	}

	public void close() throws IOException {
		if (null != depWriter) {
			depWriter.finishWriting();
		}
	}

	public String getType(int typeIndex) {
		return types[typeIndex];
	}

	public final DependencyInstance nextInstance() throws IOException {
		DependencyInstance instance = depReader.getNext();
		if (instance == null || instance.forms == null) {
			return null;
		}

		instance.setFeatureVector(createFeatureVector(instance));

		String[] labs = instance.deprels;
		int[] heads = instance.heads;

		StringBuffer spans = new StringBuffer(heads.length * 5);
		for (int i = 1; i < heads.length; i++) {
			spans.append(heads[i]).append("|").append(i).append(":")
					.append(typeAlphabet.lookupIndex(labs[i])).append(" ");
		}
		instance.actParseTree = spans.substring(0, spans.length() - 1);

		return instance;
	}

	public int[] createInstances(String file, File featFileName)
			throws IOException {

		createAlphabet(file);

		System.out.println("Num Features: " + dataAlphabet.size());

		labeled = depReader.startReading(file);

		TIntArrayList lengths = new TIntArrayList();

		ObjectOutputStream out = options.createForest ? new ObjectOutputStream(
				new FileOutputStream(featFileName)) : null;

		DependencyInstance instance = depReader.getNext();
		int num1 = 0;

		System.out.println("Creating Feature Vector Instances: ");
		while (instance != null) {

			if (num1 % 1000 == 0)
				System.out.print(num1 + " ");
			

			instance.setFeatureVector(createFeatureVector(instance));

			String[] labs = instance.deprels;
			int[] heads = instance.heads;

			StringBuffer spans = new StringBuffer(heads.length * 5);
			for (int i = 1; i < heads.length; i++) {
				spans.append(heads[i]).append("|").append(i).append(":")
						.append(typeAlphabet.lookupIndex(labs[i])).append(" ");
			}
			instance.actParseTree = spans.substring(0, spans.length() - 1);

			lengths.add(instance.length());

			if (options.createForest) {
				writeInstance(instance, out);
			}
			instance = null;

			instance = depReader.getNext();

			num1++;
		}

		System.out.println(num1 + " ");

		closeAlphabets();

		if (options.createForest) {
			out.close();
		}

		return lengths.toNativeArray();

	}

	private final void createAlphabet(String file) throws IOException {

		System.out.print("Creating Alphabet ... ");

		labeled = depReader.startReading(file);

		DependencyInstance instance = depReader.getNext();

		while (instance != null) {

			String[] labs = instance.deprels;
			for (String lab : labs) {
				typeAlphabet.lookupIndex(lab);
			}

			createFeatureVector(instance);

			instance = depReader.getNext();
		}

		closeAlphabets();

		System.out.println("Done.");
	}

	public void closeAlphabets() {
		dataAlphabet.stopGrowth();
		typeAlphabet.stopGrowth();

		types = new String[typeAlphabet.size()];
		Object[] keys = typeAlphabet.toArray();
		for (Object key : keys) {
			int indx = typeAlphabet.lookupIndex(key);
			types[indx] = (String) key;
		}

		KBestParseForest.rootType = typeAlphabet.lookupIndex("<root-type>");
	}

	// add with default 1.0
	public final void add(String feat, FeatureVector fv) {
		int num = dataAlphabet.lookupIndex(feat);
		if (num >= 0) {
			fv.add(num, 1.0);
		}
	}

	public final void add(String feat, double val, FeatureVector fv) {
		int num = dataAlphabet.lookupIndex(feat);
		if (num >= 0) {
			fv.add(num, val);
		}
	}

	public FeatureVector createFeatureVector(DependencyInstance instance) {

		final int instanceLength = instance.length();

		String[] labs = instance.deprels;
		int[] heads = instance.heads;

		FeatureVector fv = new FeatureVector();
		for (int i = 0; i < instanceLength; i++) {
			if (heads[i] == -1) {
				continue;
			}
			int small = i < heads[i] ? i : heads[i];
			int large = i > heads[i] ? i : heads[i];
			boolean attR = i < heads[i] ? false : true;
			addCoreFeatures(instance, small, large, attR, fv);
			if (labeled) {
				addLabeledFeatures(instance, i, labs[i], attR, true, fv);
				addLabeledFeatures(instance, heads[i], labs[i], attR, false, fv);
			}
		}

		addExtendedFeatures(instance, fv);

		return fv;
	}

	protected void addExtendedFeatures(DependencyInstance instance,
			FeatureVector fv) {
	}
	
	public List<String> generateFeatsTemplate(){
		List<String> featList = new ArrayList<String>();
		
		return featList;
	}
	
	public void addCustomUnlabelFeats(String[] forms, int childIndex,
			int headIndex, String suffix, FeatureVector fv) {
		String startTag = "<P>";
		String endTag = "</P>";
		String midTag = "<M>";

		String c = forms[childIndex];
		String h = forms[headIndex];
		String cm1 = childIndex > 0 ? forms[childIndex - 1] : startTag;
		String cm2 = childIndex > 1 ? forms[childIndex - 2] : startTag;
		String cm3 = childIndex > 2 ? forms[childIndex - 3] : startTag;
		String cm4 = childIndex > 3 ? forms[childIndex - 4] : startTag;
		String hp1 = headIndex < forms.length - 1 ? forms[headIndex + 1] : endTag;
		String hp2 = headIndex < forms.length - 2 ? forms[headIndex + 2] : endTag;
		String hp3 = headIndex < forms.length - 3 ? forms[headIndex + 3] : endTag;
		String hp4 = headIndex < forms.length - 4 ? forms[headIndex + 4] : endTag;
		// String cp1 = childIndex < forms.length - 1 ? forms[childIndex + 1] :
		// endTag;
		// String cp2 = childIndex < forms.length - 2 ? forms[childIndex + 2] :
		// endTag;
		// String cp3 = childIndex < forms.length - 3 ? forms[childIndex + 3] :
		// endTag;
		String cp1 = childIndex < headIndex - 1 ? forms[childIndex + 1] : midTag;
		String cp2 = childIndex < headIndex - 2 ? forms[childIndex + 2] : midTag;
		String cp3 = childIndex < headIndex - 3 ? forms[childIndex + 3] : midTag;
		String cp4 = childIndex < headIndex - 4 ? forms[childIndex + 4] : midTag;
		// String hm1 = headIndex > 0 ? forms[headIndex - 1] : startTag;
		// String hm2 = headIndex > 1 ? forms[headIndex - 2] : startTag;
		// String hm3 = headIndex > 2 ? forms[headIndex - 3] : startTag;
		String hm1 = headIndex > childIndex + 1 ? forms[headIndex - 1] : midTag;
		String hm2 = headIndex > childIndex + 2 ? forms[headIndex - 2] : midTag;
		String hm3 = headIndex > childIndex + 3 ? forms[headIndex - 3] : midTag;
		String hm4 = headIndex > childIndex + 4 ? forms[headIndex - 4] : midTag;

		List<String> featList = new ArrayList<String>();
		
		// child local feats
		featList.add(cm1 + " " + c);
		featList.add(c + " " + cp1);
		featList.add(cm2 + " " + cm1);
		featList.add(cp1 + " " + cp2);
		featList.add(cm2 + " " + cm1 + " " + c);
		featList.add(cm1 + " " + c + " " + cp1);
		featList.add(c + " " + cp1 + " " + cp2);
		featList.add(cm3 + " " + cm2 + " " + cm1);
		featList.add(cp1 + " " + cp2 + " " + cp3);
		featList.add(cm3 + " " + cm2 + " " + cm1 + " " + c);
		featList.add(cm2 + " " + cm1 + " " + c + " " + cp1);
		featList.add(cm1 + " " + c + " " + cp1 + " " + cp2);
		featList.add(c + " " + cp1 + " " + cp2 + " " + cp3);
		featList.add(cm4 + " " + cm3 + " " + cm2 + " " + cm1);
		featList.add(cp1 + " " + cp2 + " " + cp3 + " " + cp4);
		
		
		// head local feats
		featList.add(hm1 + " " + h);
		featList.add(h + " " + hp1);
		featList.add(hm2 + " " + hm1);
		featList.add(hp1 + " " + hp2);
		featList.add(hm2 + " " + hm1 + " " + h);
		featList.add(hm1 + " " + h + " " + hp1);
		featList.add(h + " " + hp1 + " " + hp2);
		featList.add(hm3 + " " + hm2 + " " + hm1);
		featList.add(hp1 + " " + hp2 + " " + hp3);
		featList.add(h + " " + hp1 + " " + hp2 + " " + hp3);
		featList.add(hm3 + " " + hm2 + " " + hm1 + " " + h);
		featList.add(hm1 + " " + h + " " + hp1 + " " + hp2);
		featList.add(hm2 + " " + hm1 + " " + h + " " + hp1);
		featList.add(hm4 + " " + hm3 + " " + hm2 + " " + hm1);
		featList.add(hp1 + " " + hp2 + " " + hp3 + " " + hp4);
		
		//head and child
		featList.add(c + " " + h);
		featList.add(c + " " + hm1 + " " + h);
		featList.add(cm1 + " " + c + " " + h);
		featList.add(cm2 + " " + cm1 + " " + c + " " + h);
		featList.add(cm1 + " " + c + " " + hm1 + " " + h);
		featList.add(c + " " + hm2 + " " + hm1 + " " + h);
		featList.add(cm2 + " " + cm1 + " " + c + " " + hm1 + " " + h);
		featList.add(cm1 + " " + c + " " + hm2 + " " + hm1 + " " + h);
		featList.add(cm2 + " " + cm1 + " " + c + " " + hm2 + " " + hm1 + " " + h);
		
//		featList.add(c + " " + h + " " + hp1);
//		featList.add(c + " " + cp1 + " " + h);
//		featList.add(c + " " + hm1 + " " + h + " " + hp1);
//		featList.add(cm1 + " " + c + " " + h + " " + hp1);
//		featList.add(c + " " + h + " " + hp1 + " " + hp2);
//		featList.add(cm1 + " " + c + " " + cp1 + " " + h + " " + hp1);
//		featList.add(cm1 + " " + c + " " + cp1 + " " + hm1 + " " + h);
//		featList.add(cm1 + " " + c + " " + hm1 + " " + h + " " + hp1);
//		featList.add(c + " " + cp1 + " " + hm1 + " " + h + " " + hp1);
//		featList.add(cm1 + " " + c + " " + cp1 + " " + hm1 + " " + h + " " + hp1);
		
		StringBuilder feat = null;
		
		if (isAffixFeat == true){
			feat = new StringBuilder("uhs:" + this.getSuffixFeature(h));
			add(feat.toString(), fv);
			feat.append(":").append(suffix);
			add(feat.toString(), fv);
			
			feat = new StringBuilder("uhp:" + this.getPrefixFeature(h));
			add(feat.toString(), fv);
			feat.append(":").append(suffix);
			add(feat.toString(), fv);
			
			feat = new StringBuilder("ucs:" + this.getSuffixFeature(c));
			add(feat.toString(), fv);
			feat.append(":").append(suffix);
			add(feat.toString(), fv);
			
			feat = new StringBuilder("ucp:" + this.getPrefixFeature(c));
			add(feat.toString(), fv);
			feat.append(":").append(suffix);
			add(feat.toString(), fv);
		}
		

		for (int i = 0; i < featList.size(); i++) {
			feat = new StringBuilder("FORM" + ":" + i + ":" + featList.get(i));
			add(feat.toString(), fv);
			feat.append(":").append(suffix);
			add(feat.toString(), fv);
			if (isClusterFeat == true) {
				feat = new StringBuilder("CLU:" + i + ":" + getVecClusterFeat(featList.get(i)));
				add(feat.toString(), fv);
				feat.append(":").append(suffix);
				add(feat.toString(), fv);
			}
			if (isAVFeat == true) {
				int temp = getAvCommon(featList.get(i));
				if (temp >= 0){
					// class way
					if (isAVClass == true){
						feat = new StringBuilder("AV:" + i + ':' + temp);
						add(feat.toString(), fv);
						feat.append(':').append(suffix);
						add(feat.toString(), fv);
					}else{
						// value way
						feat = new StringBuilder("AV:" + i );
						add(feat.toString(), 1 + (float)temp / 10, fv);
						feat.append(":").append(suffix);
						add(feat.toString(), 1 + (float)temp / 10, fv);
					}
					
					

				}
			}
			if (isDictFeat == true) {
				feat = new StringBuilder("DIC" + ":" + i + ":" + getDicFeat(splitSpace(featList.get(i))));
				add(feat.toString(), fv);
				feat.append(":").append(suffix);
				add(feat.toString(), fv);
			}
			if (isPosFeat == true) {
				List<String> results = getPosFeat(splitSpace(featList.get(i)));
				if (results != null){
					for (String re : results){
//						System.out.print(re + " : ");
						feat = new StringBuilder("POS" + ":" + i + ":" + re);
//						System.out.println(feat);
						add(feat.toString(), fv);
						feat.append(":").append(suffix);
						add(feat.toString(), fv);
					}
//					System.out.println();
				}
			}
		}
		
		if (isAVFeat == true || isDictFeat == true || isPosFeat == true){
			featList = new ArrayList<String>();
			featList.add(cm1 + " " + c + "\t" + hm1 + " " + h);
			featList.add(cm2 + " " + cm1 + " " + c + "\t" + hm1 + " " + h);
			featList.add(cm1 + " " + c + "\t" + hm2 + " " + hm1 + " " + h);
			featList.add(cm2 + " " + cm1 + " " + c + "\t" + hm2 + " " + hm1 + " " + h);
			
			for (int i = 0; i < featList.size(); i++){
	//			System.out.println(featList.get(i));
				String[] feats = featList.get(i).split("\t");
				if (isAVFeat == true) {
					int temp = getAvCommon(feats[0]);
					int temp1 = getAvCommon(feats[1]);
					if (temp >= 0 || temp1 >= 0){
						if (isAVClass == true){
							// class way
							feat = new StringBuilder("AVD:" + i + ":" + Math.min(temp, temp1));
							add(feat.toString(), fv);
							feat.append(":").append(suffix);
							add(feat.toString(), fv);
						}else{
							// value way
							feat = new StringBuilder("AVD" + ":" + i);
							add(feat.toString(), Math.min(1 + (float)temp / 10, 1 + (float)temp1 / 10), fv);
							feat.append(":").append(suffix);
							add(feat.toString(), Math.min(1 + (float)temp / 10, 1 + (float)temp1 / 10), fv);
						}
						
						
						
					}
				}
				if (isDictFeat == true) {
					feat = new StringBuilder("DICD" + ":" + i + ":" + getDicFeat(splitSpace(feats[0])) + "-" + getDicFeat(splitSpace(feats[1])));
					add(feat.toString(), fv);
					feat.append(":").append(suffix);
					add(feat.toString(), fv);
				}
				if (isPosFeat == true) {
					List<String> results1 = getPosFeat(splitSpace(feats[0]));
					List<String> results2 = getPosFeat(splitSpace(feats[1]));
					if (results1 != null){
						for (String re1 : results1){
							if (results2 != null){
								for (String re2 : results2){
									feat = new StringBuilder("POSD" + ":" + i + ":" + re1  + "-" + re2);
									add(feat.toString(), fv);
									feat.append(":").append(suffix);
									add(feat.toString(), fv);
								}
							}else{
								feat = new StringBuilder("POSD" + ":" + i + ":" + re1  + "-" + "null");
								add(feat.toString(), fv);
								feat.append(":").append(suffix);
								add(feat.toString(), fv);
							}
						}
					}else{
						if (results2 != null){
							for (String re2 : results2){
								feat = new StringBuilder("POSD" + ":" + i + ":" + "null" + "-" + re2);
								add(feat.toString(), fv);
								feat.append(":").append(suffix);
								add(feat.toString(), fv);
							}
						}else{
							feat = new StringBuilder("POSD" + ":" + i + ":" + "null" + "-" + "null");
							add(feat.toString(), fv);
							feat.append(":").append(suffix);
							add(feat.toString(), fv);
						}
					}
					
					
	//				feat = new StringBuilder("POSD" + ":" + i + ":" + getDicFeat(splitSpace(feats[0])) + "-" + getDicFeat(splitSpace(feats[1])));
	//				add(feat.toString(), fv);
	//				feat.append(":").append(suffix);
	//				add(feat.toString(), fv);
				}
			}
		}
		
	}

	public void addCustomlabelFeats(String[] forms, int[] heads, int word,
			String suffix, FeatureVector fv) {

		String startTag = "<P>";
		String endTag = "</P>";
		String midTag = "<M>";
		
//		String rootFeat = ( word == forms.length - 1) ? "isRoot" : "notRoot";
//		add(rootFeat, fv);
		
//		if ( word == forms.length - 1 )
//			System.out.println(forms.length + " : " + word + " : " + forms[word]);
//		
//		for (int i = 0; i < forms.length; i++){
//			System.out.print(forms[i] + " ");
//		}
//		System.out.println("");
//		System.out.println(word + " : " + forms[word]);
		
		int head = heads[word];
		if (head >= 0 && head < forms.length) {
			String w = forms[word];
			String wm1 = word > 0 ? forms[word - 1] : startTag;
			String wm2 = word > 1 ? forms[word - 2] : startTag;
			String wm3 = word > 2 ? forms[word - 3] : startTag;
			String wm4 = word > 3 ? forms[word - 4] : startTag;
			// String wp1 = word < forms.length - 1 ? forms[word + 1] : "</s>";
			// String wp2 = word < forms.length - 2 ? forms[word + 2] : "</s>";
			// String wp3 = word < forms.length - 3 ? forms[word + 3] : "</s>";
			String wp1 = word < head - 1 ? forms[word + 1] : midTag;
			String wp2 = word < head - 2 ? forms[word + 2] : midTag;
			String wp3 = word < head - 3 ? forms[word + 3] : midTag;
			String wp4 = word < head - 4 ? forms[word + 4] : midTag;

			String h = forms[head];
			// String hm1 = head > 0 ? forms[head - 1] : "<s>";
			// String hm2 = head > 1 ? forms[head - 2] : "<s>";
			// String hm3 = head > 2 ? forms[head - 3] : "<s>";
			String hm1 = head > word + 1 ? forms[head - 1] : midTag;
			String hm2 = head > word + 2 ? forms[head - 2] : midTag;
			String hm3 = head > word + 3 ? forms[head - 3] : midTag;
			String hm4 = head > word + 4 ? forms[head - 4] : midTag;
			String hp1 = head < forms.length - 1 ? forms[head + 1] : endTag;
			String hp2 = head < forms.length - 2 ? forms[head + 2] : endTag;
			String hp3 = head < forms.length - 3 ? forms[head + 3] : endTag;
			String hp4 = head < forms.length - 4 ? forms[head + 4] : endTag;

			// System.out.println(head + ":" + h);

			List<String> featList = new ArrayList<String>();
			
			// word local feats
			featList.add(w + " " + wp1);
			featList.add(wm1 + " " + w);
			featList.add(wm2 + " " + wm1);
			featList.add(wp1 + " " + wp2);
			featList.add(wm1 + " " + w + " " + wp1);
			featList.add(wm2 + " " + wm1 + " " + w);
			featList.add(w + " " + wp1 + " " + wp2);
			featList.add(wm3 + " " + wm2 + " " + wm1);
			featList.add(wp1 + " " + wp2 + " " + wp3);
			featList.add(wm3 + " " + wm2 + " " + wm1 + " " + w);
			featList.add(wm2 + " " + wm1 + " " + w + " " + wp1);
			featList.add(wm1 + " " + w + " " + wp1 + " " + wp2);
			featList.add(w + " " + wp1 + " " + wp2 + " " + wp3);
			featList.add(wm4 + " " + wm3 + " " + wm2 + " " + wm1);
			featList.add(wp1 + " " + wp2 + " " + wp3 + " " + wp4);
			
			// head local feats
			featList.add(h + " " + hp1);
			featList.add(hm1 + " " + h);
			featList.add(hm2 + " " + hm1);
			featList.add(hp1 + " " + hp2);
			featList.add(hm1 + " " + h + " " + hp1);
			featList.add(hm2 + " " + hm1 + " " + h);
			featList.add(h + " " + hp1 + " " + hp2);
			featList.add(hm3 + " " + hm2 + " " + hm1);
			featList.add(hp1 + " " + hp2 + " " + hp3);
			featList.add(hm3 + " " + hm2 + " " + hm1 + " " + h);
			featList.add(hm2 + " " + hm1 + " " + h + " " + hp1);
			featList.add(hm1 + " " + h + " " + hp1 + " " + hp2);
			featList.add(h + " " + hp1 + " " + hp2 + " " + hp3);
			featList.add(hm4 + " " + hm3 + " " + hm2 + " " + hm1);
			featList.add(hp1 + " " + hp2 + " " + hp3 + " " + hp4);
			
			// word combine head
			featList.add(w + " " + h);
			featList.add(w + " " + hm1 + " " + h);
			featList.add(wm1 + " " + w + " " + h);
			featList.add(wm2 + " " + wm1 + " " + w + " " + h);
			featList.add(wm1 + " " + w + " " + hm1 + " " + h);
			featList.add(w + " " + hm2 + " " + hm1 + " " + h);
			featList.add(wm2 + " " + wm1 + " " + w + " " + hm1 + " " + h);
			featList.add(wm1 + " " + w + " " + hm2 + " " + hm1 + " " + h);
			featList.add(wm2 + " " + wm1 + " " + w + " " + hm2 + " " + hm1 + " " + h);
			
//			featList.add(w + " " + wp1 + " " + h);
//			featList.add(w + " " + hm1 + " " + h + " " + hp1);
//			featList.add(w + " " + wp1 + " " + h + " " + hp1);
//			featList.add(wm1 + " " + w + " " + wp1 + " " + h + " " + hp1);
//			featList.add(wm1 + " " + w + " " + wp1 + " " + hm1 + " " + h);
//			featList.add(wm1 + " " + w + " " + hm1 + " " + h + " " + hp1);
//			featList.add(w + " " + wp1 + " " + hm1 + " " + h + " " + hp1);
//			featList.add(wm1 + " " + w + " " + wp1 + " " + hm1 + " " + h + " " + hp1);
			
			StringBuilder feat = null;
			
			if (isAffixFeat == true){
				feat = new StringBuilder("uhs:" + this.getSuffixFeature(h) + ":" + suffix);
				add(feat.toString(), fv);
				
				feat = new StringBuilder("uhp:" + this.getPrefixFeature(h) + ":" + suffix);
				add(feat.toString(), fv);
				
				feat = new StringBuilder("ucs:" + this.getSuffixFeature(w) + ":" + suffix);
				add(feat.toString(), fv);
				
				feat = new StringBuilder("ucp:" + this.getPrefixFeature(w) + ":" + suffix);
				add(feat.toString(), fv);
			}
			
//			if (isClusterFeat == true) {
//				feat = new StringBuilder("clul:ch" + this.getCharClusterFeat(w) + ":" + this.getCharClusterFeat(h) + ":" + suffix);
//				add(feat.toString(), fv);
//				
//			}
			
			for (int i = 0; i < featList.size(); i++) {
				feat = new StringBuilder("FORML" + ":" + i + ":" + featList.get(i) + ":" + suffix);
				add(feat.toString(), fv);
				if (isClusterFeat == true) {
					
					feat = new StringBuilder("CLUL:" + i + ":" + getVecClusterFeat(featList.get(i)) + ":" + suffix);
					add(feat.toString(), fv);
				}
				if (isAVFeat == true) {
					int temp = getAvCommon(featList.get(i));
					if (temp >= 0){
						if (isAVClass == true){
							// class way
							feat = new StringBuilder("AVL:" + i + ':' + temp + ":" + suffix);
							add(feat.toString(), fv);
						}else{
							// value way
							feat = new StringBuilder("AVL" + ":" + i + ":" + suffix);
							add(feat.toString(), 1 + (float)temp / 10, fv);
						}
					}
				}
				if (isDictFeat == true) {
					feat = new StringBuilder("DICL" + ":" + i + ":" + getDicFeat(splitSpace(featList.get(i))) + ":" + suffix);
					add(feat.toString(), fv);
				}
				if (isPosFeat == true) {
					List<String> results = getPosFeat(splitSpace(featList.get(i)));
					if (results != null){
						for (String re : results){
							feat = new StringBuilder("POSL" + ":" + i + ":" + re + ":" + suffix);
							add(feat.toString(), fv);
						}
					}
				}
			}
			
			featList = new ArrayList<String>();
			
			// extra feats by combining local child and head feats
			if (isAVFeat == true || isDictFeat == true || isPosFeat == true){
				
				featList.add(w + "\t" + h);
				featList.add(wm1 + " " + w + "\t" + h);
				featList.add(w + "\t" + hm1 + " " + h);
				featList.add(wm2 + " " + wm1 + " " + w + "\t" + h);
				featList.add(w + "\t" + hm2 + " " + hm1 + " " + h);
				
				featList.add(wm1 + " " + w + "\t" + hm1 + " " + h);
				featList.add(wm2 + " " + wm1 + " " + w + "\t" + hm1 + " " + h);
				featList.add(wm1 + " " + w + "\t" + hm2 + " " + hm1 + " " + h);
				featList.add(wm2 + " " + wm1 + " " + w + "\t" + hm2 + " " + hm1 + " " + h);
			
				for (int i = 0; i < featList.size(); i++){
					String[] feats = featList.get(i).split("\t");
					if (isAVFeat == true) {
						int temp = getAvCommon(feats[0]);
						int temp1 = getAvCommon(feats[1]);
						if (temp >= 0 || temp1 >= 0){
							if (isAVClass == true){
								// class way
								feat = new StringBuilder("AVDL" + ":" + i + ":" + Math.min(temp, temp1) + ":" + suffix);
								add(feat.toString(), fv);
							}else{
								// value way
								feat = new StringBuilder("AVDL" + ":" + i + ":" + suffix);
								add(feat.toString(), Math.min(1 + (float)temp / 10, 1 + (float)temp1 / 10), fv);
							}
						}
					}
					if (isDictFeat == true) {
						feat = new StringBuilder("DICDL" + ":" + i + ":" + getDicFeat(splitSpace(feats[0])) + "-" + getDicFeat(splitSpace(feats[1])) + ":" + suffix);
						add(feat.toString(), fv);
					}
					if (isPosFeat == true) {
						List<String> results1 = getPosFeat(splitSpace(feats[0]));
						List<String> results2 = getPosFeat(splitSpace(feats[1]));
						if (results1 != null){
							for (String re1 : results1){
								if (results2 != null){
									for (String re2 : results2){
										feat = new StringBuilder("POSDL" + ":" + i + ":" + re1  + "-" + re2 + ":" + suffix);
										add(feat.toString(), fv);
									}
								}else{
									feat = new StringBuilder("POSDL" + ":" + i + ":" + re1  + "-" + "null" + ":" + suffix);
									add(feat.toString(), fv);
								}
							}
						}else{
							if (results2 != null){
								for (String re2 : results2){
									feat = new StringBuilder("POSDL" + ":" + i + ":" + "null" + "-" + re2 + ":" + suffix);
									add(feat.toString(), fv);
								}
							}else{
								feat = new StringBuilder("POSDL" + ":" + i + ":" + "null" + "-" + "null" + ":" + suffix);
								add(feat.toString(), fv);
							}
						}
					}
				}
			}
		}

	}

	public String splitSpace(String seq) {
		String[] toks = seq.split(" ");
		String result = "";
		for (String tok : toks) {
			result += tok;
		}
		return result;
	}

	public void addCoreFeatures(DependencyInstance instance, int small,
			int large, boolean attR, FeatureVector fv) {

		String[] forms = instance.forms;
		String[] pos = instance.postags;
		String[] posA = instance.cpostags;

		String att = attR ? "RA" : "LA";

		int dist = Math.abs(large - small);
		String distBool = "0";
		if (dist > 10) {
			distBool = "10";
		} else if (dist > 5) {
			distBool = "5";
		} else {
			distBool = Integer.toString(dist - 1);
		}

		String attDist = "&" + att + "&" + distBool;

		// addLinearFeatures("FORM", forms, small, large, attDist, fv);
		// addLinearFeatures("POS", pos, small, large, attDist, fv);
		// addLinearFeatures("CPOS", posA, small, large, attDist, fv);

		// ////////////////////////////////////////////////////////////////////

		int headIndex = small;
		int childIndex = large;
		if (!attR) {
			headIndex = large;
			childIndex = small;
		}

		addCustomUnlabelFeats(forms, childIndex, headIndex, attDist, fv);

//		addTwoObsFeatures("HC", forms[headIndex], pos[headIndex],
//				forms[childIndex], pos[childIndex], attDist, fv);

		if (isCONLL) {

//			addTwoObsFeatures("HCA", forms[headIndex], posA[headIndex],
//					forms[childIndex], posA[childIndex], attDist, fv);
//
//			addTwoObsFeatures("HCC", instance.lemmas[headIndex],
//					pos[headIndex], instance.lemmas[childIndex],
//					pos[childIndex], attDist, fv);
//
//			addTwoObsFeatures("HCD", instance.lemmas[headIndex],
//					posA[headIndex], instance.lemmas[childIndex],
//					posA[childIndex], attDist, fv);

			if (options.discourseMode) {
				// Note: The features invoked here are designed for
				// discourse parsing (as opposed to sentential
				// parsing). It is conceivable that they could help for
				// sentential parsing, but current testing indicates that
				// they hurt sentential parsing performance.

				addDiscourseFeatures(instance, small, large, headIndex,
						childIndex, attDist, fv);

			} else {
				// Add in features from the feature lists. It assumes
				// the feature lists can have different lengths for
				// each item. For example, nouns might have a
				// different number of morphological features than
				// verbs.

				for (int i = 0; i < instance.feats[headIndex].length; i++) {
					for (int j = 0; j < instance.feats[childIndex].length; j++) {
						addTwoObsFeatures("FF" + i + "*" + j,
								instance.forms[headIndex],
								instance.feats[headIndex][i],
								instance.forms[childIndex],
								instance.feats[childIndex][j], attDist, fv);

//						addTwoObsFeatures("LF" + i + "*" + j,
//								instance.lemmas[headIndex],
//								instance.feats[headIndex][i],
//								instance.lemmas[childIndex],
//								instance.feats[childIndex][j], attDist, fv);
					}
				}
			}

		} else {
			// We are using the old MST format. Pick up stem features
			// the way they used to be done. This is kept for
			// replicability of results for old versions.
			int hL = forms[headIndex].length();
			int cL = forms[childIndex].length();
			if (hL > 5 || cL > 5) {
				addOldMSTStemFeatures(instance.lemmas[headIndex],
						pos[headIndex], instance.lemmas[childIndex],
						pos[childIndex], attDist, hL, cL, fv);
			}
		}

	}

	private final void addLinearFeatures(String type, String[] obsVals,
			int first, int second, String attachDistance, FeatureVector fv) {

		String pLeft = first > 0 ? obsVals[first - 1] : "STR";
		String pRight = second < obsVals.length - 1 ? obsVals[second + 1]
				: "END";
		String pLeftRight = first < second - 1 ? obsVals[first + 1] : "MID";
		String pRightLeft = second > first + 1 ? obsVals[second - 1] : "MID";

		// feature posR posMid posL
		StringBuilder featPos = new StringBuilder(type + "PC=" + obsVals[first]
				+ " " + obsVals[second]);

		for (int i = first + 1; i < second; i++) {
			String allPos = featPos.toString() + ' ' + obsVals[i];
			add(allPos, fv);
			add(allPos + attachDistance, fv);

		}

		// addCorePosFeatures(type + "PT", pLeft, obsVals[first], pLeftRight,
		// pRightLeft, obsVals[second],
		// pRight, attachDistance, fv);
		addCoreFormFeatures(type + "PT", pLeft, obsVals[first], pLeftRight,
				pRightLeft, obsVals[second], pRight, attachDistance, fv);

	}

	private final void addCorePosFeatures(String prefix, String leftOf1,
			String one, String rightOf1, String leftOf2, String two,
			String rightOf2, String attachDistance, FeatureVector fv) {

		// feature posL-1 posL posR posR+1

		add(prefix + "=" + leftOf1 + " " + one + " " + two + "*"
				+ attachDistance, fv);

		StringBuilder feat = new StringBuilder(prefix + "1=" + leftOf1 + " "
				+ one + " " + two);
		add(feat.toString(), fv);
		feat.append(' ').append(rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2=" + leftOf1 + " " + two + " "
				+ rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "3=" + leftOf1 + " " + one + " "
				+ rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "4=" + one + " " + two + " "
				+ rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		// ///////////////////////////////////////////////////////////
		prefix = "A" + prefix;

		// feature posL posL+1 posR-1 posR
		add(prefix + "1=" + one + " " + rightOf1 + " " + leftOf2 + "*"
				+ attachDistance, fv);

		feat = new StringBuilder(prefix + "1=" + one + " " + rightOf1 + " "
				+ leftOf2);
		add(feat.toString(), fv);
		feat.append(' ').append(two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " "
				+ two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "3=" + one + " " + leftOf2 + " "
				+ two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "4=" + rightOf1 + " " + leftOf2 + " "
				+ two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		// /////////////////////////////////////////////////////////////
		prefix = "B" + prefix;

		// // feature posL-1 posL posR-1 posR
		feat = new StringBuilder(prefix + "1=" + leftOf1 + " " + one + " "
				+ leftOf2 + " " + two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		// // feature posL posL+1 posR posR+1
		feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " "
				+ two + " " + rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

	}

	/**
	 * Add features for two items, each with two observations, e.g. head, head
	 * pos, child, and child pos.
	 * 
	 * The use of StringBuilders is not yet as efficient as it could be, but
	 * this is a start. (And it abstracts the logic so we can add other features
	 * more easily based on other items and observations.)
	 **/
	private final void addTwoObsFeatures(String prefix, String item1F1,
			String item1F2, String item2F1, String item2F2,
			String attachDistance, FeatureVector fv) {

		StringBuilder feat = new StringBuilder(prefix + "2FF1=" + item1F1);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2
				+ " " + item2F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2
				+ " " + item2F2 + " " + item2F1);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF2=" + item1F1 + " " + item2F1);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF3=" + item1F1 + " " + item2F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF4=" + item1F2 + " " + item2F1);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF4=" + item1F2 + " " + item2F1
				+ " " + item2F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF5=" + item1F2 + " " + item2F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF6=" + item2F1 + " " + item2F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF7=" + item1F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF8=" + item2F1);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2FF9=" + item2F2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

	}

	private final void addCoreFormFeatures(String prefix, String leftOf1,
			String one, String rightOf1, String leftOf2, String two,
			String rightOf2, String attachDistance, FeatureVector fv) {

		// feature formL-1 formL formR formR+1

		add(prefix + "=" + leftOf1 + " " + one + " " + two + "*"
				+ attachDistance, fv);

		StringBuilder feat = new StringBuilder(prefix + "1=" + leftOf1 + " "
				+ one + " " + two);
		add(feat.toString(), fv);
		feat.append(' ').append(rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2=" + leftOf1 + " " + two + " "
				+ rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "3=" + leftOf1 + " " + one + " "
				+ rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "4=" + one + " " + two + " "
				+ rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		// ///////////////////////////////////////////////////////////
		prefix = "A" + prefix;

		// feature formL formL+1 formR-1 formR
		add(prefix + "1=" + one + " " + rightOf1 + " " + leftOf2 + "*"
				+ attachDistance, fv);

		feat = new StringBuilder(prefix + "1=" + one + " " + rightOf1 + " "
				+ leftOf2);
		add(feat.toString(), fv);
		feat.append(' ').append(two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " "
				+ two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "3=" + one + " " + leftOf2 + " "
				+ two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		feat = new StringBuilder(prefix + "4=" + rightOf1 + " " + leftOf2 + " "
				+ two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		// /////////////////////////////////////////////////////////////
		prefix = "B" + prefix;

		// // feature formL-1 formL formR-1 formR
		feat = new StringBuilder(prefix + "1=" + leftOf1 + " " + one + " "
				+ leftOf2 + " " + two);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

		// // feature formL formL+1 formR formR+1
		feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " "
				+ two + " " + rightOf2);
		add(feat.toString(), fv);
		feat.append('*').append(attachDistance);
		add(feat.toString(), fv);

	}

	public void addLabeledFeatures(DependencyInstance instance, int word,
			String type, boolean attR, boolean childFeatures, FeatureVector fv) {

		if (!labeled) {
			return;
		}

		String[] forms = instance.forms;
		String[] pos = instance.postags;
		int[] heads = instance.heads;

		String att = "";
		if (attR) {
			att = "RA";
		} else {
			att = "LA";
		}

		att += "&" + childFeatures;

		String w = forms[word];
		String wP = pos[word];

		String wPm1 = word > 0 ? pos[word - 1] : "STR";
		String wPp1 = word < pos.length - 1 ? pos[word + 1] : "END";

		add("NTS1=" + type + "&" + att, fv);
		add("ANTS1=" + type, fv);
		for (int i = 0; i < 2; i++) {
			String suff = i < 1 ? "&" + att : "";
			suff = "&" + type + suff;

//			add("NTH=" + w + " " + wP + suff, fv);
//			add("NTI=" + wP + suff, fv);
//			add("NTIA=" + wPm1 + " " + wP + suff, fv);
//			add("NTIB=" + wP + " " + wPp1 + suff, fv);
//			add("NTIC=" + wPm1 + " " + wP + " " + wPp1 + suff, fv);
//			add("NTJ=" + w + suff, fv); // this

			addCustomlabelFeats(forms, heads, word, suff, fv);

		}
	}

	private void addDiscourseFeatures(DependencyInstance instance, int small,
			int large, int headIndex, int childIndex, String attDist,
			FeatureVector fv) {

		addLinearFeatures("FORM", instance.forms, small, large, attDist, fv);
		addLinearFeatures("LEMMA", instance.lemmas, small, large, attDist, fv);

		addTwoObsFeatures("HCB1", instance.forms[headIndex],
				instance.lemmas[headIndex], instance.forms[childIndex],
				instance.lemmas[childIndex], attDist, fv);

		addTwoObsFeatures("HCB2", instance.forms[headIndex],
				instance.lemmas[headIndex], instance.forms[childIndex],
				instance.postags[childIndex], attDist, fv);

		addTwoObsFeatures("HCB3", instance.forms[headIndex],
				instance.lemmas[headIndex], instance.forms[childIndex],
				instance.cpostags[childIndex], attDist, fv);

		addTwoObsFeatures("HC2", instance.forms[headIndex],
				instance.postags[headIndex], instance.forms[childIndex],
				instance.cpostags[childIndex], attDist, fv);

		addTwoObsFeatures("HCC2", instance.lemmas[headIndex],
				instance.postags[headIndex], instance.lemmas[childIndex],
				instance.cpostags[childIndex], attDist, fv);

		// // Use this if your extra feature lists all have the same length.
		for (int i = 0; i < instance.feats.length; i++) {

			addLinearFeatures("F" + i, instance.feats[i], small, large,
					attDist, fv);

			addTwoObsFeatures("FF" + i, instance.forms[headIndex],
					instance.feats[i][headIndex], instance.forms[childIndex],
					instance.feats[i][childIndex], attDist, fv);

			addTwoObsFeatures("LF" + i, instance.lemmas[headIndex],
					instance.feats[i][headIndex], instance.lemmas[childIndex],
					instance.feats[i][childIndex], attDist, fv);

			addTwoObsFeatures("PF" + i, instance.postags[headIndex],
					instance.feats[i][headIndex], instance.postags[childIndex],
					instance.feats[i][childIndex], attDist, fv);

			addTwoObsFeatures("CPF" + i, instance.cpostags[headIndex],
					instance.feats[i][headIndex],
					instance.cpostags[childIndex],
					instance.feats[i][childIndex], attDist, fv);

			for (int j = i + 1; j < instance.feats.length; j++) {

				addTwoObsFeatures("CPF" + i + "_" + j,
						instance.feats[i][headIndex],
						instance.feats[j][headIndex],
						instance.feats[i][childIndex],
						instance.feats[j][childIndex], attDist, fv);

			}

			for (int j = 0; j < instance.feats.length; j++) {

				addTwoObsFeatures("XFF" + i + "_" + j,
						instance.forms[headIndex],
						instance.feats[i][headIndex],
						instance.forms[childIndex],
						instance.feats[j][childIndex], attDist, fv);

				addTwoObsFeatures("XLF" + i + "_" + j,
						instance.lemmas[headIndex],
						instance.feats[i][headIndex],
						instance.lemmas[childIndex],
						instance.feats[j][childIndex], attDist, fv);

				addTwoObsFeatures("XPF" + i + "_" + j,
						instance.postags[headIndex],
						instance.feats[i][headIndex],
						instance.postags[childIndex],
						instance.feats[j][childIndex], attDist, fv);

				addTwoObsFeatures("XCF" + i + "_" + j,
						instance.cpostags[headIndex],
						instance.feats[i][headIndex],
						instance.cpostags[childIndex],
						instance.feats[j][childIndex], attDist, fv);

			}

		}

		// Test out relational features
		if (options.useRelationalFeatures) {

			// for (int rf_index=0; rf_index<2; rf_index++) {
			for (int rf_index = 0; rf_index < instance.relFeats.length; rf_index++) {

				String headToChild = "H2C"
						+ rf_index
						+ instance.relFeats[rf_index].getFeature(headIndex,
								childIndex);

				addTwoObsFeatures("RFA1", instance.forms[headIndex],
						instance.lemmas[headIndex],
						instance.postags[childIndex], headToChild, attDist, fv);

				addTwoObsFeatures("RFA2", instance.postags[headIndex],
						instance.cpostags[headIndex],
						instance.forms[childIndex], headToChild, attDist, fv);

				addTwoObsFeatures("RFA3", instance.lemmas[headIndex],
						instance.postags[headIndex],
						instance.forms[childIndex], headToChild, attDist, fv);

				addTwoObsFeatures("RFB1", headToChild,
						instance.postags[headIndex],
						instance.forms[childIndex],
						instance.lemmas[childIndex], attDist, fv);

				addTwoObsFeatures("RFB2", headToChild,
						instance.forms[headIndex],
						instance.postags[childIndex],
						instance.cpostags[childIndex], attDist, fv);

				addTwoObsFeatures("RFB3", headToChild,
						instance.forms[headIndex], instance.lemmas[childIndex],
						instance.postags[childIndex], attDist, fv);

			}
		}
	}

	public void fillFeatureVectors(DependencyInstance instance,
			FeatureVector[][][] fvs, double[][][] probs,
			FeatureVector[][][][] nt_fvs, double[][][][] nt_probs,
			Parameters params) {

		final int instanceLength = instance.length();

		// Get production crap.
		for (int w1 = 0; w1 < instanceLength; w1++) {
			for (int w2 = w1 + 1; w2 < instanceLength; w2++) {
				for (int ph = 0; ph < 2; ph++) {
					boolean attR = ph == 0 ? true : false;

					int childInt = attR ? w2 : w1;
					int parInt = attR ? w1 : w2;

					FeatureVector prodFV = new FeatureVector();
					addCoreFeatures(instance, w1, w2, attR, prodFV);
					double prodProb = params.getScore(prodFV);
					fvs[w1][w2][ph] = prodFV;
					probs[w1][w2][ph] = prodProb;
				}
			}
		}

		if (labeled) {
			for (int w1 = 0; w1 < instanceLength; w1++) {
				for (int t = 0; t < types.length; t++) {
					String type = types[t];
					for (int ph = 0; ph < 2; ph++) {

						boolean attR = ph == 0 ? true : false;
						for (int ch = 0; ch < 2; ch++) {

							boolean child = ch == 0 ? true : false;

							FeatureVector prodFV = new FeatureVector();
							addLabeledFeatures(instance, w1, type, attR, child,
									prodFV);

							double nt_prob = params.getScore(prodFV);
							nt_fvs[w1][t][ph][ch] = prodFV;
							nt_probs[w1][t][ph][ch] = nt_prob;

						}
					}
				}
			}
		}
	}

	/**
	 * Write an instance to an output stream for later reading.
	 * 
	 **/
	protected void writeInstance(DependencyInstance instance,
			ObjectOutputStream out) {

		int instanceLength = instance.length();

		try {

			for (int w1 = 0; w1 < instanceLength; w1++) {
				for (int w2 = w1 + 1; w2 < instanceLength; w2++) {
					for (int ph = 0; ph < 2; ph++) {
						boolean attR = ph == 0 ? true : false;
						FeatureVector prodFV = new FeatureVector();
						addCoreFeatures(instance, w1, w2, attR, prodFV);
						out.writeObject(prodFV.keys());
					}
				}
			}
			out.writeInt(-3);

			if (labeled) {
				for (int w1 = 0; w1 < instanceLength; w1++) {
					for (String type : types) {
						for (int ph = 0; ph < 2; ph++) {
							boolean attR = ph == 0 ? true : false;
							for (int ch = 0; ch < 2; ch++) {
								boolean child = ch == 0 ? true : false;
								FeatureVector prodFV = new FeatureVector();
								addLabeledFeatures(instance, w1, type, attR,
										child, prodFV);
								out.writeObject(prodFV.keys());
							}
						}
					}
				}
				out.writeInt(-3);
			}

			writeExtendedFeatures(instance, out);

			out.writeObject(instance.fv.keys());
			out.writeInt(-4);

			out.writeObject(instance);
			out.writeInt(-1);

			out.reset();

		} catch (IOException e) {
		}

	}

	/**
	 * Override this method if you have extra features that need to be written
	 * to disk. For the basic DependencyPipe, nothing happens.
	 * 
	 */
	protected void writeExtendedFeatures(DependencyInstance instance,
			ObjectOutputStream out) throws IOException {
	}

	/**
	 * Read an instance from an input stream.
	 * 
	 **/
	public DependencyInstance readInstance(ObjectInputStream in, int length,
			FeatureVector[][][] fvs, double[][][] probs,
			FeatureVector[][][][] nt_fvs, double[][][][] nt_probs,
			Parameters params) throws IOException {

		try {

			// Get production crap.
			for (int w1 = 0; w1 < length; w1++) {
				for (int w2 = w1 + 1; w2 < length; w2++) {
					for (int ph = 0; ph < 2; ph++) {
						FeatureVector prodFV = new FeatureVector(
								(int[]) in.readObject());
						double prodProb = params.getScore(prodFV);
						fvs[w1][w2][ph] = prodFV;
						probs[w1][w2][ph] = prodProb;
					}
				}
			}
			int last = in.readInt();
			if (last != -3) {
				System.out.println("Error reading file.");
				System.exit(0);
			}

			if (labeled) {
				for (int w1 = 0; w1 < length; w1++) {
					for (int t = 0; t < types.length; t++) {
						String type = types[t];

						for (int ph = 0; ph < 2; ph++) {
							for (int ch = 0; ch < 2; ch++) {
								FeatureVector prodFV = new FeatureVector(
										(int[]) in.readObject());
								double nt_prob = params.getScore(prodFV);
								nt_fvs[w1][t][ph][ch] = prodFV;
								nt_probs[w1][t][ph][ch] = nt_prob;
							}
						}
					}
				}
				last = in.readInt();
				if (last != -3) {
					System.out.println("Error reading file.");
					System.exit(0);
				}
			}

			FeatureVector nfv = new FeatureVector((int[]) in.readObject());
			last = in.readInt();
			if (last != -4) {
				System.out.println("Error reading file.");
				System.exit(0);
			}

			DependencyInstance marshalledDI;
			marshalledDI = (DependencyInstance) in.readObject();
			marshalledDI.setFeatureVector(nfv);

			last = in.readInt();
			if (last != -1) {
				System.out.println("Error reading file.");
				System.exit(0);
			}

			return marshalledDI;

		} catch (ClassNotFoundException e) {
			System.out.println("Error reading file.");
			System.exit(0);
		}

		// this won't happen, but it takes care of compilation complaints
		return null;
	}

	/**
	 * Get features for stems the old way. The only way this differs from
	 * calling addTwoObsFeatures() is that it checks the lengths of the full
	 * lexical items are greater than 5 before adding features.
	 * 
	 */
	private final void addOldMSTStemFeatures(String hLemma, String headP,
			String cLemma, String childP, String attDist, int hL, int cL,
			FeatureVector fv) {

		String all = hLemma + " " + headP + " " + cLemma + " " + childP;
		String hPos = headP + " " + cLemma + " " + childP;
		String cPos = hLemma + " " + headP + " " + childP;
		String hP = headP + " " + cLemma;
		String cP = hLemma + " " + childP;
		String oPos = headP + " " + childP;
		String oLex = hLemma + " " + cLemma;

		add("SA=" + all + attDist, fv); // this
		add("SF=" + oLex + attDist, fv); // this
		add("SAA=" + all, fv); // this
		add("SFF=" + oLex, fv); // this

		if (cL > 5) {
			add("SB=" + hPos + attDist, fv);
			add("SD=" + hP + attDist, fv);
			add("SK=" + cLemma + " " + childP + attDist, fv);
			add("SM=" + cLemma + attDist, fv); // this
			add("SBB=" + hPos, fv);
			add("SDD=" + hP, fv);
			add("SKK=" + cLemma + " " + childP, fv);
			add("SMM=" + cLemma, fv); // this
		}
		if (hL > 5) {
			add("SC=" + cPos + attDist, fv);
			add("SE=" + cP + attDist, fv);
			add("SH=" + hLemma + " " + headP + attDist, fv);
			add("SJ=" + hLemma + attDist, fv); // this

			add("SCC=" + cPos, fv);
			add("SEE=" + cP, fv);
			add("SHH=" + hLemma + " " + headP, fv);
			add("SJJ=" + hLemma, fv); // this
		}

	}

}
