package mstparser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DependencyPipe2O extends DependencyPipe {

	public DependencyPipe2O(ParserOptions options) throws IOException {
		super(options);
	}

	@Override
	protected void addExtendedFeatures(DependencyInstance instance, FeatureVector fv) {

		final int instanceLength = instance.length();
		int[] heads = instance.heads;

		// find all trip features
		for (int i = 0; i < instanceLength; i++) {
			if (heads[i] == -1 && i != 0)
				continue;
			// right children
			int prev = i;
			for (int j = i + 1; j < instanceLength; j++) {
				if (heads[j] == i) {
					addTripFeatures(instance, i, prev, j, fv);
					addSiblingFeatures(instance, prev, j, prev == i, fv);
					prev = j;
				}
			}
			// left children
			prev = i;
			for (int j = i - 1; j >= 0; j--) {
				if (heads[j] == i) {
					addTripFeatures(instance, i, prev, j, fv);
					addSiblingFeatures(instance, prev, j, prev == i, fv);
					prev = j;
				}
			}
		}
	}

	public void fillFeatureVectors(DependencyInstance instance, FeatureVector[][][] fvs,
			double[][][] probs, FeatureVector[][][] fvs_trips, double[][][] probs_trips,
			FeatureVector[][][] fvs_sibs, double[][][] probs_sibs, FeatureVector[][][][] nt_fvs,
			double[][][][] nt_probs, Parameters params) {

		fillFeatureVectors(instance, fvs, probs, nt_fvs, nt_probs, params);

		final int instanceLength = instance.length();

		for (int w1 = 0; w1 < instanceLength; w1++) {
			for (int w2 = w1; w2 < instanceLength; w2++) {
				for (int w3 = w2 + 1; w3 < instanceLength; w3++) {
					FeatureVector prodFV = new FeatureVector();
					addTripFeatures(instance, w1, w2, w3, prodFV);
					double prodProb = params.getScore(prodFV);
					fvs_trips[w1][w2][w3] = prodFV;
					probs_trips[w1][w2][w3] = prodProb;
				}
			}
			for (int w2 = w1; w2 >= 0; w2--) {
				for (int w3 = w2 - 1; w3 >= 0; w3--) {
					FeatureVector prodFV = new FeatureVector();
					addTripFeatures(instance, w1, w2, w3, prodFV);
					double prodProb = params.getScore(prodFV);
					fvs_trips[w1][w2][w3] = prodFV;
					probs_trips[w1][w2][w3] = prodProb;
				}
			}
		}

		for (int w1 = 0; w1 < instanceLength; w1++) {
			for (int w2 = 0; w2 < instanceLength; w2++) {
				for (int wh = 0; wh < 2; wh++) {
					if (w1 != w2) {
						FeatureVector prodFV = new FeatureVector();
						addSiblingFeatures(instance, w1, w2, wh == 0, prodFV);
						double prodProb = params.getScore(prodFV);
						fvs_sibs[w1][w2][wh] = prodFV;
						probs_sibs[w1][w2][wh] = prodProb;
					}
				}
			}
		}
	}

	private final void addSiblingFeatures(DependencyInstance instance, int ch1, int ch2,
			boolean isST, FeatureVector fv) {

		String[] forms = instance.forms;
		String[] pos = instance.postags;

		// ch1 is always the closes to par
		String dir = ch1 > ch2 ? "RA" : "LA";

		String ch1_pos = isST ? "STPOS" : pos[ch1];
		String ch2_pos = pos[ch2];
		String ch1_word = isST ? "STWRD" : forms[ch1];
		String ch2_word = forms[ch2];

//		add("CH_PAIR=" + ch1_pos + "_" + ch2_pos + "_" + dir, 1.0, fv);
//		add("CH_WPAIR=" + ch1_word + "_" + ch2_word + "_" + dir, 1.0, fv);
//		add("CH_WPAIRA=" + ch1_word + "_" + ch2_pos + "_" + dir, 1.0, fv);
//		add("CH_WPAIRB=" + ch1_pos + "_" + ch2_word + "_" + dir, 1.0, fv);
//		add("ACH_PAIR=" + ch1_pos + "_" + ch2_pos, 1.0, fv);
//		add("ACH_WPAIR=" + ch1_word + "_" + ch2_word, 1.0, fv);
//		add("ACH_WPAIRA=" + ch1_word + "_" + ch2_pos, 1.0, fv);
//		add("ACH_WPAIRB=" + ch1_pos + "_" + ch2_word, 1.0, fv);
		
		// tag
		String startTag = "<P>";
		String mid1Tag = "<M1>";
		String mid2Tag = "<M2>";
		// child 1
		String c1 = forms[ch1];
		String c1m1 = ch1 > ch2 + 1 ? forms[ch1 - 1] : mid2Tag;
		String c1m2 = ch1 > ch2 + 2 ? forms[ch1 - 2] : mid2Tag;
		String c1m3 = ch1 > ch2 + 3 ? forms[ch1 - 3] : mid2Tag;
		String c1p1 = ch1 < forms.length - 1 ? forms[ch1 + 1] : mid1Tag;
		String c1p2 = ch1 < forms.length - 2 ? forms[ch1 + 2] : mid1Tag;
		String c1p3 = ch1 < forms.length - 3 ? forms[ch1 + 3] : mid1Tag;
		// child 2
		String c2 = forms[ch2];
		String c2m1 = ch2 > 0 ? forms[ch2 - 1] : startTag;
		String c2m2 = ch2 > 1 ? forms[ch2 - 2] : startTag;
		String c2m3 = ch2 > 2 ? forms[ch2 - 3] : startTag;
		String c2p1 = ch2 < ch1 - 1 ? forms[ch2 + 1] : mid2Tag;
		String c2p2 = ch2 < ch1 - 2 ? forms[ch2 + 2] : mid2Tag;
		String c2p3 = ch2 < ch1 - 3 ? forms[ch2 + 3] : mid2Tag;
		
		List<String> featList = new ArrayList<String>();
		// ch1 and ch2
		featList.add(c2 + " " + c1);
		featList.add(c2m1 + " " + c2 + " " + c1);
		featList.add(c2 + " " + c1m1 + " " + c1);
		featList.add(c2m2 + " " + c2m1 + " " + c2 + " " + c1);
		featList.add(c2 + " " + c1m2 + " " + c1m1 + " " + c1);
		featList.add(c2m1 + " " + c2 + " " + c1m1 + " " + c1);
		featList.add(c2m2 + " " + c2m1 + " " + c2 + " " + c1m1 + " " + c1);
		featList.add(c2m1 + " " + c2 + " " + c1m2 + " " + c1m1 + " " + c1);
		featList.add(c2m2 + " " + c2m1 + " " + c2 + " " + c1m2 + " " + c1m1 + " " + c1);
		
		for (int i = 0; i < featList.size(); i++){
			add("FORM_SIP:" + i + ":" + featList.get(i) + "_" + dir, 1.0, fv);
			add("AFORM_SIP:" + i + ":" + featList.get(i), 1.0, fv);
			if (isAVFeat == true) {
				int temp = getAvCommon(featList.get(i));
				if (temp >= 0){
					if (isAVClass == true){
						// class way
						add("AV_SIP:" + i + ':' + temp + "_" + dir, fv);
						add("AAV_SIP:" + i + ':' + temp, fv);
					}else{
						// value way
						add("AV_SIP:" + i + "_" + dir, 1 + (float)temp / 10, fv);
						add("AAV_SIP:" + i, 1 + (float)temp / 10, fv);
					}
				}
			}
			if (isDictFeat == true) {
				add("DIC_SIP:" + i + ":" + getDicFeat(splitSpace(featList.get(i))) + "_" + dir, 1.0, fv);
				add("ADIC_SIP:" + i + ":" + getDicFeat(splitSpace(featList.get(i))), 1.0, fv);
			}
			if (isPosFeat == true) {
				List<String> results = getPosFeat(splitSpace(featList.get(i)));
				if (results != null){
					for (String re : results){
						add("POS_SIP:" + i + ":" + re  + "_" + dir, 1.0, fv);
						add("APOS_SIP:" + i + ":" + re, 1.0, fv);
					}
				}
			}
		}
				
		int dist = Math.max(ch1, ch2) - Math.min(ch1, ch2);
		String distBool = "0";
		if (dist > 1)
			distBool = "1";
		if (dist > 2)
			distBool = "2";
		if (dist > 3)
			distBool = "3";
		if (dist > 4)
			distBool = "4";
		if (dist > 5)
			distBool = "5";
		if (dist > 10)
			distBool = "10";
		add("SIB_PAIR_DIST=" + distBool + "_" + dir, 1.0, fv);
		add("ASIB_PAIR_DIST=" + distBool, 1.0, fv);
//		add("CH_PAIR_DIST=" + ch1_pos + "_" + ch2_pos + "_" + distBool + "_" + dir, 1.0, fv);
//		add("ACH_PAIR_DIST=" + ch1_pos + "_" + ch2_pos + "_" + distBool, 1.0, fv);

	}

	private final void addTripFeatures(DependencyInstance instance, int par, int ch1, int ch2,
			FeatureVector fv) {

		String[] pos = instance.postags;
		String[] forms = instance.forms;

		// ch1 is always the closest to par
		String dir = par > ch2 ? "RA" : "LA";
		
		String startTag = "<P>";
		String endTag = "</P>";
		String mid1Tag = "<M1>";
		String mid2Tag = "<M2>";
		
		String par_pos = pos[par];
		String ch1_pos = ch1 == par ? "STPOS" : pos[ch1];
		String ch2_pos = pos[ch2];

		String p = forms[par];
		String pm1 = par > 0 ? forms[par - 1] : startTag;
		String pm2 = par > 1 ? forms[par - 2] : startTag;
		String pp1 = par < forms.length - 1 ? forms[par + 1] : endTag;
		String pp2 = par < forms.length - 2 ? forms[par + 2] : endTag;

		String c1 = forms[ch1];
		String c1m1 = ch1 > 0 ? forms[ch1 - 1] : startTag;
		String c1m2 = ch1 > 1 ? forms[ch1 - 2] : startTag;
		String c1p1 = ch1 < forms.length - 1 ? forms[ch1 + 1] : endTag;
		String c1p2 = ch1 < forms.length - 2 ? forms[ch1 + 2] : endTag;

		String c2 = forms[ch2];
		String c2m1 = ch2 > 0 ? forms[ch2 - 1] : startTag;
		String c2m2 = ch2 > 1 ? forms[ch2 - 2] : startTag;
		String c2p1 = ch2 < forms.length - 1 ? forms[ch2 + 1] : endTag;
		String c2p2 = ch2 < forms.length - 2 ? forms[ch2 + 2] : endTag;
		
//		String p = forms[par];
//		String pm1 = par > ch1 + 1 ? forms[par - 1] : mid1Tag;
//		String pm2 = par > ch1 + 2 ? forms[par - 2] : mid1Tag;
//		String pp1 = par < forms.length - 1 ? forms[par + 1] : endTag;
//		String pp2 = par < forms.length - 2 ? forms[par + 2] : endTag;
//
//		String c1 = forms[ch1];
//		String c1m1 = ch1 > ch2 + 1 ? forms[ch1 - 1] : mid2Tag;
//		String c1m2 = ch1 > ch2 + 2 ? forms[ch1 - 2] : mid2Tag;
//		String c1p1 = ch1 < par - 1 ? forms[ch1 + 1] : mid1Tag;
//		String c1p2 = ch1 < par - 2 ? forms[ch1 + 2] : mid1Tag;
//
//		String c2 = forms[ch2];
//		String c2m1 = ch2 > 0 ? forms[ch2 - 1] : startTag;
//		String c2m2 = ch2 > 1 ? forms[ch2 - 2] : startTag;
//		String c2p1 = ch2 < ch1 - 1 ? forms[ch2 + 1] : mid2Tag;
//		String c2p2 = ch2 < ch1 - 2 ? forms[ch2 + 2] : mid2Tag;

//		String pTrip = par_pos + "_" + ch1_pos + "_" + ch2_pos;
//		add("POS_TRIP=" + pTrip + "_" + dir, 1.0, fv);
//		add("APOS_TRIP=" + pTrip, 1.0, fv);
		
		if (isAffixFeat == true){
			add("p:" + this.getSuffixFeature(p), fv);
			add("p:" + this.getPrefixFeature(p), fv);
			add("c1:" + this.getSuffixFeature(c1), fv);
			add("c1:" + this.getPrefixFeature(c1), fv);
			add("c2:" + this.getSuffixFeature(c2), fv);
			add("c2:" + this.getPrefixFeature(c2), fv);
		}
		
		StringBuilder feat = null;
		
//		if (isClusterFeat == true) {
//			feat = new StringBuilder("clu:c2c1:" + this.getCharClusterFeat(c2) + ":" + this.getCharClusterFeat(c1));
//			add(feat.toString(), 1.0, fv);
//			feat.append("_" + dir);
//			add(feat.toString(), 1.0, fv);
//			
//			feat = new StringBuilder("clu:c2p:" + this.getCharClusterFeat(c2) + ":" + this.getCharClusterFeat(p));
//			add(feat.toString(), 1.0, fv);
//			feat.append("_" + dir);
//			add(feat.toString(), 1.0, fv);
//			
//			feat = new StringBuilder("clu:c1p:" + this.getCharClusterFeat(c1) + ":" + this.getCharClusterFeat(p));
//			add(feat.toString(), 1.0, fv);
//			feat.append("_" + dir);
//			add(feat.toString(), 1.0, fv);
//			
//			feat = new StringBuilder("clu:c2c1p:" + this.getCharClusterFeat(c2) + ":" + this.getCharClusterFeat(c1) + ":" + this.getCharClusterFeat(p));
//			add(feat.toString(), 1.0, fv);
//			feat.append("_" + dir);
//			add(feat.toString(), 1.0, fv);
//		}
		
		List<String> featList = new ArrayList<String>();
		
		//ch1 and par
//		featList.add(c1 + " " + p);
//		featList.add(c1m1 + " " + c1 + " " + p);
//		featList.add(c1 + " " + pm1 + " " + p);
//		featList.add(c1m1 + " " + c1 + " " + pm1 + " " + p);
		
		//ch2 and par
//		featList.add(c2 + " " + p);
//		featList.add(c1m2 + " " + c2 + " " + p);
//		featList.add(c2 + " " + pm1 + " " + p);
//		featList.add(c1m2 + " " + c2 + " " + pm1 + " " + p);
		
		// ch1, ch2 and par
		featList.add(c2 + " " + c1 + " " + p);
		featList.add(c2m1 + " " + c2 + " " + c1 + " " + p);
		featList.add(c2 + " " + c1m1 + " " + c1 + " " + p);
		featList.add(c2 + " " + c1 + " " + pm1 + " " + p);
		featList.add(c2m1 + " " + c2 + " " + c1m1 + " " + c1 + " " + p);
		featList.add(c2m1 + " " + c2 + " " + c1m1 + " " + c1 + " " + pm1 + " " + p);
		
		for (int i = 0; i < featList.size(); i++){
			add("FORM_TRIP:" + i + ":" + featList.get(i) + "_" + dir, 1.0, fv);
			add("AFORM_TRIP:" + i + ":" + featList.get(i), 1.0, fv);
			if (isAVFeat == true) {
				int temp = getAvCommon(featList.get(i));
				if (temp >= 0){
					if (isAVClass == true){
						// class way
						add("AV_TRIP:" + i + ':' + temp + "_" + dir, fv);
						add("AAV_TRIP:" + i + ':' + temp, fv);
					}else{
						// value way
						add("AV_TRIP:" + i + "_" + dir, 1 + (float)temp / 10, fv);
						add("AAV_TRIP:" + i, 1 + (float)temp / 10, fv);
					}
				}
			}
			if (isDictFeat == true) {
				add("DIC_TRIP:" + i + ":" + getDicFeat(splitSpace(featList.get(i))) + "_" + dir, 1.0, fv);
				add("ADIC_TRIP:" + i + ":" + getDicFeat(splitSpace(featList.get(i))), 1.0, fv);
			}
			if (isPosFeat == true) {
				List<String> results = getPosFeat(splitSpace(featList.get(i)));
				if (results != null){
					for (String re : results){
						add("POS_TRIP:" + i + ":" + re  + "_" + dir, 1.0, fv);
						add("APOS_TRIP:" + i + ":" + re, 1.0, fv);
					}
				}
			}
		}
		
//		featList = new ArrayList<String>();
//		featList.add(c1 + "\t" + p);
//		featList.add(c2 + "\t" + p);
//		featList.add(c2 + "\t" + c1);
//		featList.add(c1m1 + " " + c1 + "\t" + p);
//		featList.add(c1m2 + " " + c2 + "\t" + p);
//		featList.add(c1 + "\t" + pm1 + " " + p);
//		featList.add(c2 + "\t" + pm1 + " " + p);
//		featList.add(c1m1 + " " + c1 + "\t" + pm1 + " " + p);
//		featList.add(c1m2 + " " + c2 + "\t" + pm1 + " " + p);
//		featList.add(c2m1 + " " + c2 + "\t" + c1);
//		featList.add(c2 + "\t" + c1m1 + " " + c1);
//		featList.add(c2m1 + " " + c2 + "\t" + c1m1 + " " + c1);
//		featList.add(c2 + " " + c1 + "\t" + p);
//		featList.add(c2m1 + " " + c2 + "\t" + c1 + "\t" + p);
//		featList.add(c2 + "\t" + c1m1 + " " + c1 + "\t" + p);
//		featList.add(c2 + "\t" + c1 + "\t" + pm1 + " " + p);
//		featList.add(c2m1 + " " + c2 + "\t" + c1m1 + " " + c1 + "\t" + p);
//		featList.add(c2m1 + " " + c2 + "\t" + c1m1 + " " + c1 + "\t" + pm1 + " " + p);
		
//		StringBuilder feat = null;
//		for (int i = 0; i < featList.size(); i++){
//			String[] feats = featList.get(i).split("\t");
//			if (isAVFeat == true) {
//				feat = new StringBuilder("AVDL_TRIP" + ":" + i + ":" + getAvClass(feats[0]) + "-" + getAvClass(feats[1]));
//				add(feat.toString(), 1.0, fv);
//				feat.append("_" + dir);
//				add(feat.toString(), 1.0, fv);
//			}
//			if (isDictFeat == true) {
//				feat = new StringBuilder("DICDL_TRIP" + ":" + i + ":" + getDicFeat(splitSpace(feats[0])) + "-" + getDicFeat(splitSpace(feats[1])));
//				add(feat.toString(), 1.0, fv);
//				feat.append("_" + dir);
//				add(feat.toString(), 1.0, fv);
//			}
//			if (isPosFeat == true) {
//				List<String> results1 = getPosFeat(splitSpace(feats[0]));
//				List<String> results2 = getPosFeat(splitSpace(feats[1]));
//				if (results1 != null){
//					for (String re1 : results1){
//						if (results2 != null){
//							for (String re2 : results2){
//								feat = new StringBuilder("POSDL_TRIP" + ":" + i + ":" + re1  + "-" + re2);
//								add(feat.toString(), 1.0, fv);
//								feat.append("_" + dir);
//								add(feat.toString(), 1.0, fv);
//							}
//						}else{
//							feat = new StringBuilder("POSDL_TRIP" + ":" + i + ":" + re1  + "-" + "null");
//							add(feat.toString(), 1.0, fv);
//							feat.append("_" + dir);
//							add(feat.toString(), 1.0, fv);
//						}
//					}
//				}else{
//					if (results2 != null){
//						for (String re2 : results2){
//							feat = new StringBuilder("POSDL_TRIP" + ":" + i + ":" + "null" + "-" + re2);
//							add(feat.toString(), 1.0, fv);
//							feat.append("_" + dir);
//							add(feat.toString(), 1.0, fv);
//						}
//					}else{
//						feat = new StringBuilder("POSDL_TRIP" + ":" + i + ":" + "null" + "-" + "null");
//						add(feat.toString(), 1.0, fv);
//						feat.append("_" + dir);
//						add(feat.toString(), 1.0, fv);
//					}
//				}
//			}
//		}

	}

	/**
	 * Write out the second order features.
	 * 
	 **/
	@Override
	protected void writeExtendedFeatures(DependencyInstance instance, ObjectOutputStream out)
			throws IOException {

		final int instanceLength = instance.length();

		for (int w1 = 0; w1 < instanceLength; w1++) {
			for (int w2 = w1; w2 < instanceLength; w2++) {
				for (int w3 = w2 + 1; w3 < instanceLength; w3++) {
					FeatureVector prodFV = new FeatureVector();
					addTripFeatures(instance, w1, w2, w3, prodFV);
					out.writeObject(prodFV.keys());
				}
			}
			for (int w2 = w1; w2 >= 0; w2--) {
				for (int w3 = w2 - 1; w3 >= 0; w3--) {
					FeatureVector prodFV = new FeatureVector();
					addTripFeatures(instance, w1, w2, w3, prodFV);
					out.writeObject(prodFV.keys());
				}
			}
		}

		out.writeInt(-3);

		for (int w1 = 0; w1 < instanceLength; w1++) {
			for (int w2 = 0; w2 < instanceLength; w2++) {
				for (int wh = 0; wh < 2; wh++) {
					if (w1 != w2) {
						FeatureVector prodFV = new FeatureVector();
						addSiblingFeatures(instance, w1, w2, wh == 0, prodFV);
						out.writeObject(prodFV.keys());
					}
				}
			}
		}

		out.writeInt(-3);
	}

	public DependencyInstance readInstance(ObjectInputStream in, int length, FeatureVector[][][] fvs,
			double[][][] probs, FeatureVector[][][] fvs_trips, double[][][] probs_trips,
			FeatureVector[][][] fvs_sibs, double[][][] probs_sibs, FeatureVector[][][][] nt_fvs,
			double[][][][] nt_probs, Parameters params) throws IOException {

		try {
			// Get production crap.
			for (int w1 = 0; w1 < length; w1++) {
				for (int w2 = w1 + 1; w2 < length; w2++) {
					for (int ph = 0; ph < 2; ph++) {
						FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
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
								FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
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

			for (int w1 = 0; w1 < length; w1++) {
				for (int w2 = w1; w2 < length; w2++) {
					for (int w3 = w2 + 1; w3 < length; w3++) {
						FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
						double prodProb = params.getScore(prodFV);
						fvs_trips[w1][w2][w3] = prodFV;
						probs_trips[w1][w2][w3] = prodProb;
					}
				}
				for (int w2 = w1; w2 >= 0; w2--) {
					for (int w3 = w2 - 1; w3 >= 0; w3--) {
						FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
						double prodProb = params.getScore(prodFV);
						fvs_trips[w1][w2][w3] = prodFV;
						probs_trips[w1][w2][w3] = prodProb;
					}
				}
			}
			last = in.readInt();
			if (last != -3) {
				System.out.println("Error reading file.");
				System.exit(0);
			}

			for (int w1 = 0; w1 < length; w1++) {
				for (int w2 = 0; w2 < length; w2++) {
					for (int wh = 0; wh < 2; wh++) {
						if (w1 != w2) {
							FeatureVector prodFV = new FeatureVector((int[]) in.readObject());
							double prodProb = params.getScore(prodFV);
							fvs_sibs[w1][w2][wh] = prodFV;
							probs_sibs[w1][w2][wh] = prodProb;
						}
					}
				}
			}
			last = in.readInt();
			if (last != -3) {
				System.out.println("Error reading file.");
				System.exit(0);
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

}
