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
import java.util.Arrays;
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

	public boolean isAVFeat = false;
	
	public boolean isAVClass = true;
	
	public boolean isAVMin = true;

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
		String avFeatFile = "feats/xincmn_avfeats.out";
//		String clusterFeatFile = "/home/fei-c/resource/chgigaword/gigaword_class.txt";
		String dictFile = "dicts/mecab-dic.csv";
		String vecclusterFile = "feats/xincmn_k100.sorted";

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
		if (vecClusters.containsKey(retoken)) {
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
//			return null;
			return Arrays.asList("<N>");
	}
	
	public int getAvLeft(String token) {
		 String tok_space = joinString(token, " ");
		if (this.avReMap.containsKey(tok_space)){
			int[] value = (int[])this.avReMap.get(tok_space);
			return value[0];
		}else
			return -1;
	}
	
	public int getAvRight(String token) {
		 String tok_space = joinString(token, " ");
		if (this.avReMap.containsKey(tok_space)){
			int[] value = (int[])this.avReMap.get(tok_space);
			return value[1];
		}else
			return -1;
	}
	
	public int getAvCommon(String tok_space) {
//		String tok_space = joinString(token, " ");
		if (this.avReMap.containsKey(tok_space)){
			int[] value = (int[])this.avReMap.get(tok_space);
			return Math.min(value[0], value[1]);
//			System.out.print(result);
		}else
			return -1;
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
		
		boolean isLabeled = false;
		String symbol = "U";
		
		List<String> featList = new ArrayList<String>();
		
		featList = contextFeatTemp(forms, headIndex, childIndex);
		
		addContextFeat(featList, suffix, isLabeled, symbol, fv);
		
		if (isAVFeat == true || isDictFeat == true || isPosFeat == true){
			featList = extraFeatTemp(forms, headIndex, childIndex);
			addExtraFeat(featList, suffix, isLabeled, symbol, fv);
		}
		
		if (isAVFeat == true || isDictFeat == true || isPosFeat == true){
			featList = twoFeatTemp(forms, headIndex, childIndex);
			addDoubleFeat(featList, suffix, isLabeled, symbol, fv);
		}
		
	}
	
	public void addCustomlabelFeats(String[] forms, int[] heads, int word, String suffix, FeatureVector fv) {

		boolean isLabeled = true;
		String symbol = "L";
		
		int head = heads[word];
		if (head >= 0 && head < forms.length) {

			List<String> featList = contextFeatTemp(forms, head, word);
			
			addContextFeat(featList, suffix, isLabeled, symbol, fv);
			
			if (isAVFeat == true || isDictFeat == true || isPosFeat == true){
				featList = extraFeatTemp(forms, head, word);
				addExtraFeat(featList, suffix, isLabeled, symbol, fv);
			}
			
			// extra feats by combining local child and head feats
			if (isAVFeat == true || isDictFeat == true || isPosFeat == true){
				featList = twoFeatTemp(forms, head, word);
				addDoubleFeat(featList, suffix, isLabeled, symbol,fv);
			}
		}

	}
	
	public List<String> contextFeatTemp(String[] forms, int head, int word){
		String startTag = "<P>";
		String endTag = "</P>";
		String midTag = "<M>";
		
		
		List<String> featList = new ArrayList<String>();

		String w = forms[word];
		String wm1 = word > 0 ? forms[word - 1] : startTag;
		String wm2 = word > 1 ? forms[word - 2] : startTag;
		String wm3 = word > 2 ? forms[word - 3] : startTag;
		String wp1 = word < head - 1 ? forms[word + 1] : midTag;
		String wp2 = word < head - 2 ? forms[word + 2] : midTag;
		String wp3 = word < head - 3 ? forms[word + 3] : midTag;

		String h = forms[head];
		String hm1 = head > word + 1 ? forms[head - 1] : midTag;
		String hm2 = head > word + 2 ? forms[head - 2] : midTag;
		String hm3 = head > word + 3 ? forms[head - 3] : midTag;
		String hp1 = head < forms.length - 1 ? forms[head + 1] : endTag;
		String hp2 = head < forms.length - 2 ? forms[head + 2] : endTag;
		String hp3 = head < forms.length - 3 ? forms[head + 3] : endTag;

		// word local feats
		featList.add(w);
		featList.add(w + " " + wp1);
		featList.add(wm1 + " " + w);
		featList.add(wm2 + " " + wm1);
		featList.add(wp1 + " " + wp2);
		featList.add(wm1 + " " + w + " " + wp1);
		featList.add(wm2 + " " + wm1 + " " + w);
		featList.add(w + " " + wp1 + " " + wp2);
		featList.add(wm3 + " " + wm2 + " " + wm1);
		featList.add(wp1 + " " + wp2 + " " + wp3);

		// head local feats
		featList.add(h);
		featList.add(h + " " + hp1);
		featList.add(hm1 + " " + h);
		featList.add(hm2 + " " + hm1);
		featList.add(hp1 + " " + hp2);
		featList.add(hm1 + " " + h + " " + hp1);
		featList.add(hm2 + " " + hm1 + " " + h);
		featList.add(h + " " + hp1 + " " + hp2);
		featList.add(hm3 + " " + hm2 + " " + hm1);
		featList.add(hp1 + " " + hp2 + " " + hp3);

		// word combine head
		featList.add(w + "\t" + h);
		featList.add(wm1 + " " + w + "\t" + h);
		featList.add(w + "\t" + hm1 + " " + h);
		featList.add(wm2 + " " + wm1 + " " + w + "\t" + h);
		featList.add(w + "\t" + hm2 + " " + hm1 + " " + h);
		featList.add(wm1 + " " + w + "\t" + hm1 + " " + h);
		featList.add(wm2 + " " + wm1 + " " + w + "\t" + hm1 + " " + h);
		featList.add(wm1 + " " + w + "\t" + hm2 + " " + hm1 + " " + h);
		featList.add(wm2 + " " + wm1 + " " + w + "\t" + hm2 + " " + hm1 + " " + h);
			
		
		return featList;
	}
	
	public List<String> extraFeatTemp(String[] forms, int head, int word){
		String startTag = "<P>";
		String endTag = "</P>";
		String midTag = "<M>";
		

		List<String> featList = new ArrayList<String>();

		String w = forms[word];
		String wm1 = word > 0 ? forms[word - 1] : startTag;
		String wm2 = word > 1 ? forms[word - 2] : startTag;
		String wm3 = word > 2 ? forms[word - 3] : startTag;
		String wm4 = word > 3 ? forms[word - 4] : startTag;
		String wp1 = word < head - 1 ? forms[word + 1] : midTag;
		String wp2 = word < head - 2 ? forms[word + 2] : midTag;
		String wp3 = word < head - 3 ? forms[word + 3] : midTag;
		String wp4 = word < head - 4 ? forms[word + 4] : midTag;

		String h = forms[head];
		String hm1 = head > word + 1 ? forms[head - 1] : midTag;
		String hm2 = head > word + 2 ? forms[head - 2] : midTag;
		String hm3 = head > word + 3 ? forms[head - 3] : midTag;
		String hm4 = head > word + 4 ? forms[head - 4] : midTag;
		String hp1 = head < forms.length - 1 ? forms[head + 1] : endTag;
		String hp2 = head < forms.length - 2 ? forms[head + 2] : endTag;
		String hp3 = head < forms.length - 3 ? forms[head + 3] : endTag;
		String hp4 = head < forms.length - 4 ? forms[head + 4] : endTag;

		// word local feats
		featList.add(w);
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
		featList.add(h);
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
		
		return featList;
	}
	
	
	public List<String> twoFeatTemp(String[] forms, int head, int word){
		String startTag = "<P>";
		String midTag = "<M>";

		List<String> featList = new ArrayList<String>();

		String w = forms[word];
		String wm1 = word > 0 ? forms[word - 1] : startTag;
		String wm2 = word > 1 ? forms[word - 2] : startTag;

		String h = forms[head];
		String hm1 = head > word + 1 ? forms[head - 1] : midTag;
		String hm2 = head > word + 2 ? forms[head - 2] : midTag;
		
		featList.add(w + "\t" + h);
		featList.add(wm1 + " " + w + "\t" + h);
		featList.add(w + "\t" + hm1 + " " + h);
		featList.add(wm2 + " " + wm1 + " " + w + "\t" + h);
		featList.add(w + "\t" + hm2 + " " + hm1 + " " + h);
		featList.add(wm1 + " " + w + "\t" + hm1 + " " + h);
		featList.add(wm2 + " " + wm1 + " " + w + "\t" + hm1 + " " + h);
		featList.add(wm1 + " " + w + "\t" + hm2 + " " + hm1 + " " + h);
		featList.add(wm2 + " " + wm1 + " " + w + "\t" + hm2 + " " + hm1 + " " + h);
		return featList;
	}

	
	// add character context features
	public void addContextFeat(List<String> featList, String suffix, boolean isLabeled, String symbol, FeatureVector fv){
		StringBuilder feat = null;
		if (isLabeled){
			for (int i = 0; i < featList.size(); i++) {
				feat = new StringBuilder("F" + symbol + ":" + i + ":" + featList.get(i) + ":" + suffix);
				add(feat.toString(), fv);
			}
		}else{
			for (int i = 0; i < featList.size(); i++) {
				feat = new StringBuilder("F" + symbol + ":" + i + ":" + featList.get(i));
				add(feat.toString(), fv);
				feat.append(":").append(suffix);
				add(feat.toString(), fv);
			}
		}
	}
	
	// add cluster, access variable, pos tag features
	public void addExtraFeat(List<String> featList, String suffix, boolean isLabeled, String symbol, FeatureVector fv){
		
		StringBuilder feat = null;
		if (isLabeled){
			for (int i = 0; i < featList.size(); i++) {
				if (isClusterFeat == true) {
					feat = new StringBuilder("C" + symbol + ":" + i + ":" + getVecClusterFeat(featList.get(i)) + ":" + suffix);
					add(feat.toString(), fv);
				}
				if (isAVFeat == true) {
					int temp = getAvCommon(featList.get(i));
					if (temp >= 0){
						if (isAVClass == true){
							// class way
							feat = new StringBuilder("A" + symbol + ":" + i + ':' + temp + ":" + suffix);
							add(feat.toString(), fv);
						}else{
							// value way
							feat = new StringBuilder("A" + symbol + ":" + i + ":" + suffix);
							add(feat.toString(), 1 + (float)temp / 10, fv);
						}
					}
				}
				if (isDictFeat == true) {
					feat = new StringBuilder("D" + symbol + ":" + i + ":" + getDicFeat(splitSpace(featList.get(i))) + ":" + suffix);
					add(feat.toString(), fv);
				}
				if (isPosFeat == true) {
					List<String> results = getPosFeat(splitSpace(featList.get(i)));
					if (results != null){
						for (String re : results){
							feat = new StringBuilder("P" + symbol + ":" + i + ":" + re + ":" + suffix);
							add(feat.toString(), fv);
//							System.out.println(feat.toString());
						}
					}
				}
			}
		}else{
			for (int i = 0; i < featList.size(); i++) {
				if (isClusterFeat == true) {
					feat = new StringBuilder("C" + symbol + ":" + i + ":" + getVecClusterFeat(featList.get(i)));
					add(feat.toString(), fv);
					feat.append(":").append(suffix);
					add(feat.toString(), fv);
				}
				if (isAVFeat == true) {
					int temp = getAvCommon(featList.get(i));
					if (temp >= 0){
						// class way
						if (isAVClass == true){
							feat = new StringBuilder("A" + symbol + ":" + i + ':' + temp);
							add(feat.toString(), fv);
							feat.append(':').append(suffix);
							add(feat.toString(), fv);
						}else{
							// value way
							feat = new StringBuilder("A" + symbol + ":" + i );
							add(feat.toString(), 1 + (float)temp / 10, fv);
							feat.append(":").append(suffix);
							add(feat.toString(), 1 + (float)temp / 10, fv);
						}
					}
				}
				if (isDictFeat == true) {
					feat = new StringBuilder("D" + symbol + ":" + i + ":" + getDicFeat(splitSpace(featList.get(i))));
					add(feat.toString(), fv);
					feat.append(":").append(suffix);
					add(feat.toString(), fv);
				}
				if (isPosFeat == true) {
					List<String> results = getPosFeat(splitSpace(featList.get(i)));
					for (String re : results){
						feat = new StringBuilder("P" + symbol + ":" + i + ":" + re);
						add(feat.toString(), fv);
						feat.append(":").append(suffix);
						add(feat.toString(), fv);
					}
						//					System.out.println();
					
				}
			}
		}
	}
	
	// add extra feature with split context inputs
	public void addDoubleFeat(List<String> featList, String suffix, boolean isLabeled, String symbol, FeatureVector fv){
		StringBuilder feat = null;
		if(isLabeled){
			for (int i = 0; i < featList.size(); i++){
				String[] feats = featList.get(i).split("\t");
				String refeat = feats[0] + " " + feats[1];
				if (isAVFeat) {
					int temp = getAvCommon(feats[0]);
					int temp1 = getAvCommon(feats[1]);
					int reav = getAvCommon(refeat);
					if (temp >= 0 || temp1 >= 0){
						if (isAVClass){
							// class way
							if (isAVMin){
								feat = new StringBuilder("AF" + symbol + ":" + i + ":" + reav + ":" + suffix);
								add(feat.toString(), fv);
								
								feat = new StringBuilder("AD" + symbol + ":" + i + ":" + Math.min(temp, temp1) + ":" + suffix);
								add(feat.toString(), fv);
							}else{
								feat = new StringBuilder("AF" + symbol + ":" + i + ":" + reav + ":" + suffix);
								add(feat.toString(), fv);
								
								feat = new StringBuilder("AD" + symbol + ":" + i + ":" + temp + " " + temp1 + ":" + suffix);
								add(feat.toString(), fv);
							}
//							System.out.println(temp + " " + temp1);
						}else{
							// value way
							feat = new StringBuilder("AD" + symbol + ":" + i + ":" + suffix);
							add(feat.toString(), Math.min(1 + (float)temp / 10, 1 + (float)temp1 / 10), fv);
						}
					}
				}
				if (isDictFeat == true) {
					feat = new StringBuilder("DF" + symbol + ":" + i + ":" + getDicFeat(splitSpace(refeat)) + ":" + suffix);
					add(feat.toString(), fv);
					
					feat = new StringBuilder("DD" + symbol + ":" + i + ":" + getDicFeat(splitSpace(feats[0])) + "-" + getDicFeat(splitSpace(feats[1])) + ":" + suffix);
					add(feat.toString(), fv);
				}
				if (isPosFeat == true) {
					List<String> results1 = getPosFeat(splitSpace(feats[0]));
					List<String> results2 = getPosFeat(splitSpace(feats[1]));
					
					feat = new StringBuilder("PF" + symbol + ":" + i + ":" + getPosFeat(splitSpace(refeat)) + ":" + suffix);
					add(feat.toString(), fv);
					
					for (String re1 : results1){
						for (String re2 : results2){
							feat = new StringBuilder("PD" + symbol + ":" + i + ":" + re1  + "-" + re2 + ":" + suffix);
							add(feat.toString(), fv);
						}
					}
					
				}
			}
		}else{
			for (int i = 0; i < featList.size(); i++){
				//			System.out.println(featList.get(i));
				String[] feats = featList.get(i).split("\t");
				String refeat = feats[0] + " " + feats[1];
				if (isAVFeat == true) {
					int temp = getAvCommon(feats[0]);
					int temp1 = getAvCommon(feats[1]);
					int reav = getAvCommon(refeat);
					if (temp >= 0 || temp1 >= 0){
						if (isAVClass == true){
							// class way
							if (isAVMin){
								feat = new StringBuilder("AF" + symbol + ":" + i + ":" + reav);
								add(feat.toString(), fv);
								feat.append(":").append(suffix);
								add(feat.toString(), fv);
								
								feat = new StringBuilder("AD" + symbol + ":" + i + ":" + Math.min(temp, temp1));
								add(feat.toString(), fv);
								feat.append(":").append(suffix);
								add(feat.toString(), fv);
							}else{
								feat = new StringBuilder("AF" + symbol + ":" + i + ":" + reav + ":" + suffix);
								add(feat.toString(), fv);
								feat.append(":").append(suffix);
								add(feat.toString(), fv);
								
								feat = new StringBuilder("AD" + symbol + ":" + i + ":" + temp + " " + temp1);
								add(feat.toString(), fv);
								feat.append(":").append(suffix);
								add(feat.toString(), fv);
							}
						}else{
							// value way
							feat = new StringBuilder("AD" + symbol + ":" + i);
							add(feat.toString(), Math.min(1 + (float)temp / 10, 1 + (float)temp1 / 10), fv);
							feat.append(":").append(suffix);
							add(feat.toString(), Math.min(1 + (float)temp / 10, 1 + (float)temp1 / 10), fv);
						}
					}
				}
				if (isDictFeat == true) {
					feat = new StringBuilder("DF" + symbol + ":" + i + ":" + getDicFeat(splitSpace(refeat)));
					add(feat.toString(), fv);
					feat.append(":").append(suffix);
					add(feat.toString(), fv);
					
					feat = new StringBuilder("DD" + symbol + ":" + i + ":" + getDicFeat(splitSpace(feats[0])) + "-" + getDicFeat(splitSpace(feats[1])));
					add(feat.toString(), fv);
					feat.append(":").append(suffix);
					add(feat.toString(), fv);
				}
				if (isPosFeat == true) {
					List<String> results1 = getPosFeat(splitSpace(feats[0]));
					List<String> results2 = getPosFeat(splitSpace(feats[1]));
					
					feat = new StringBuilder("PF" + symbol + ":" + i + ":" + getPosFeat(splitSpace(refeat)));
					add(feat.toString(), fv);
					feat.append(":").append(suffix);
					add(feat.toString(), fv);
					
					for (String re1 : results1){
						for (String re2 : results2){
							feat = new StringBuilder("PD" + symbol + ":" + i + ":" + re1  + "-" + re2);
							add(feat.toString(), fv);
							feat.append(":").append(suffix);
							add(feat.toString(), fv);
						}
					}
				}
			}
		}
	}
	
	public List<String> triFeatTemp(String[] forms, int par, int ch1, int ch2){
		
		List<String> featList = new ArrayList<String>();

		String startTag = "<P>";
		String mid1Tag = "<M1>";
		String mid2Tag = "<M2>";
		
		String p = forms[par];
		String pm1 = par > ch1 + 1 ? forms[par - 1] : mid1Tag;

		String c1 = forms[ch1];
		String c1m1 = ch1 > ch2 + 1 ? forms[ch1 - 1] : mid2Tag;

		String c2 = forms[ch2];
		String c2m1 = ch2 > 0 ? forms[ch2 - 1] : startTag;
		
//		System.out.println(ch2 + " " + ch1 + " " + par);
//		System.out.println(c2 + " " + c1 + " " + p);
		
		featList.add(c2 + "\t" + c1 + "\t" + p);
		featList.add(c2m1 + " " + c2 + "\t" + c1 + "\t" + p);
		featList.add(c2 + "\t" + c1m1 + " " + c1 + "\t" + p);
		featList.add(c2 + "\t" + c1 + "\t" + pm1 + " " + p);
		featList.add(c2m1 + " " + c2 + "\t" + c1m1 + " " + c1 + "\t" + p);
		featList.add(c2m1 + " " + c2 + "\t" + c1 + "\t" + pm1 + " " + p);
		featList.add(c2 + "\t" + c1m1 + " " + c1 + "\t" + pm1 + " " + p);
		featList.add(c2m1 + " " + c2 + "\t" + c1m1 + " " + c1 + "\t" + pm1 + " " + p);
		
		return featList;
	}
	
	public void addTriFeat(List<String> featList, String dir, String symbol, FeatureVector fv){
		for (int i = 0; i < featList.size(); i++){
			String[] feats = featList.get(i).split("\t");
			String ch2 = feats[0];
			String ch1 = feats[1];
			String par = feats[2];
			String refeat = ch2 + " " + ch1 + " " + par;
//			System.out.println(refeat);
			add("F" + symbol + ":" + i + ":" + refeat + "_" + dir, 1.0, fv);
			add("F" + symbol + ":" + i + ":" + refeat, 1.0, fv);
			if (isAVFeat == true) {
				int temp = getAvCommon(refeat);
//				if (temp >= 0)
//					System.out.println(temp);
				int avch2 = getAvCommon(ch2);
				int avch1 = getAvCommon(ch1);
				int avpar = getAvCommon(par);
				int minav = Math.min(avch2, Math.min(avch1, avpar));
				if (temp >= 0){
					if (isAVClass == true){
						// class way
						add("A" + symbol + ":" + i + ':' + temp + "_" + dir, fv);
						add("A" + symbol + ":" + i + ':' + temp, fv);
						if (isAVMin){
							add("AS" + symbol + ":" + i + ':' + minav + "_" + dir, fv);
							add("AS" + symbol + ":" + i + ':' + minav, fv);
						}else{
							add("AS" + symbol + ":" + i + ':' + avch2 + " " + avch1 + " " + avpar + "_" + dir, fv);
							add("AS" + symbol + ":" + i + ':' + avch2 + " " + avch1 + " " + avpar, fv);
						}
					}else{
						// value way
						add("A" + symbol + ":" + i + "_" + dir, 1 + (float)temp / 10, fv);
						add("A" + symbol + ":" + i, 1 + (float)temp / 10, fv);
					}
				}
			}
			if (isDictFeat == true) {
				String ch2d = getDicFeat(splitSpace(ch2));
				String ch1d = getDicFeat(splitSpace(ch1));
				String pard = getDicFeat(splitSpace(par));
				String outd = ch2d + " " + ch1d + " " + pard;
				add("D" + symbol + ":" + i + ":" + getDicFeat(splitSpace(refeat)) + "_" + dir, 1.0, fv);
				add("D" + symbol + ":" + i + ":" + getDicFeat(splitSpace(refeat)), 1.0, fv);
				add("DS" + symbol + ":" + i + ":" + outd + "_" + dir, 1.0, fv);
				add("DS" + symbol + ":" + i + ":" + outd, 1.0, fv);
			}
			if (isPosFeat == true) {
				List<String> results = getPosFeat(splitSpace(refeat));
				List<String> ch2p = getPosFeat(splitSpace(ch2));
				List<String> ch1p = getPosFeat(splitSpace(ch1));
				List<String> parp = getPosFeat(splitSpace(par));
				for (String re : results){
					add("P" + symbol + ":" + i + ":" + re  + "_" + dir, 1.0, fv);
					add("P" + symbol + ":" + i + ":" + re, 1.0, fv);
				}
				for (String re1 : ch2p){
					for (String re2: ch1p){
						for (String re3: parp){
							String re = re1 + " " + re2 + " " + re3;
							add("PS" + symbol + ":" + i + ":" + re + "_" + dir, 1.0, fv);
							add("PS" + symbol + ":" + i + ":" + re, 1.0, fv);

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

		if (isCONLL) {


			if (options.discourseMode) {
				// Note: The features invoked here are designed for
				// discourse parsing (as opposed to sentential
				// parsing). It is conceivable that they could help for
				// sentential parsing, but current testing indicates that


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

		add("NTS1=" + type + "&" + att, fv);
		add("ANTS1=" + type, fv);
		for (int i = 0; i < 2; i++) {
			String suff = i < 1 ? "&" + att : "";
			suff = "&" + type + suff;

			addCustomlabelFeats(forms, heads, word, suff, fv);

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
