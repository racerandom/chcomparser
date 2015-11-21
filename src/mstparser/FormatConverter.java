package mstparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FormatConverter {
	
	protected BufferedReader inputReader;
	protected BufferedWriter writer;
	
	private static char[] punctCharList = {'℃', '·', '／', '。', '，', '…', '！', '？', '、', '；', '：', '“', '”', '‘', '’', '〈', '〉', '《', '》', '〔', '〕', 
		'【', '】', '[', ']', '」', '「', '﹁', '﹂', '『', '』', '﹃', '﹄', '（', '）', '—', '(', ')', ',', '・', ';', '!', '.', '―', '-', '{', '}', '━', '/', '%', '－'};
	
	private static char[] connCharList = {'·', '×', '—', '至'};
	
	private static char[] numCharList = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '．', '∶', '点', '-', '数', '／', '几', '上',
		'○', '一', '二', '三', '四', '五', '六', '七', '八', '九', 
		'０', '１', '２', '３', '４', '５', '６', '７', '８', '９', '〇', '单',
		'十', '百', '千', '万', '亿', '兆', '京', '两', '零', '壹', '貳', '叁', '肆', '伍', '陸', '柒', '捌', '玖', '拾', '廿', '卅', '卌', '佰', '仟', '萬', '億' };
	
	private static char[] dateCharList = {'歲', '岁', '年', '季', '月', '週', '周', '日', '分', '秒', '時', '时', '刻', '点', '夜', '路'};
	
	public String getCharType(char token){
		for (char ch : this.punctCharList){
			if (ch == token){
				return "P";
			}
		}
		for (char ch : this.connCharList){
			if (ch == token){
				return "C";
			}
		}
		for (char ch : this.numCharList){
			if (ch == token){
				return "N";
			}
		}
		for (char ch : this.dateCharList){
			if (ch == token){
				return "D";
			}
		}
		return "O";
	}
	
	public static void main(String[] args) throws IOException {
		FormatConverter converter = new FormatConverter();
		converter.file2conll(args[0], args[1]);
	}
	
	public void file2conll(String raw_file, String conll_file) throws IOException{
		
		inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(raw_file), "UTF8"));
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(conll_file), "UTF8"));
		
		String line = inputReader.readLine();
	    while (line != null && !line.trim().isEmpty() && !line.startsWith("*")) {
	    	char[] chars = line.trim().toCharArray();
	    	for (int i = 1; i <= chars.length; i++ ){
	    		writer.write(String.valueOf(i));
	    		writer.write('\t');
	    		writer.write(chars[i - 1]);
	    		writer.write('\t');
	    		writer.write('_');
	    		writer.write('\t');
	    		writer.write(getCharType(chars[i - 1]));
	    		writer.write('\t');
	    		writer.write('_');
	    		writer.write('\t');
	    		writer.write('_');
	    		writer.write('\t');
	    		writer.write('0');
	    		writer.write('\t');
	    		writer.write("LAB");
	    		writer.newLine();
	    	}
	    	writer.newLine();
	    	line = inputReader.readLine();
	    	// System.out.println("## "+line);
	    }
	}
	
}
