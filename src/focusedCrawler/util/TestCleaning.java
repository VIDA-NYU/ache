package focusedCrawler.util;

import java.io.*;

public class TestCleaning {

	public static void main(String[] args) {
        StringBuffer tempBuffer = new StringBuffer();
        String[] sentences;
		try {
			StringBuffer content = new StringBuffer();
		     BufferedReader input = new BufferedReader(new FileReader(new File(args[0])));

	          for (String line = input.readLine(); line != null;
	                  line = input.readLine()) {

	              content.append(line);
	              content.append("\n");

	          }
	          input.close();
	          String src = content.toString();

			Html2Txt html2txt = new Html2Txt();
			sentences = html2txt.convert(src);
			for (int i = 0; i < sentences.length; i++) {
				boolean addedWord = false;
				if(sentences[i].trim().equals("")){
					continue;
				}
				String[] words = sentences[i].split(" ");
				for (int j = 0; j < words.length && words.length < 500; j++) {
//					System.out.println(">>"+sentences[i]);
					if (!words[j].contains("http://")){
						char[] charSentence = words[j].toCharArray();
						int cons = 0;
						boolean isCons = false;
						boolean previousCons = false;
						StringBuffer tempSentence = new StringBuffer();
						for (int l = 0; l < charSentence.length && charSentence.length < 30; l++) {
						   if(((int)charSentence[l] == 45 && charSentence.length > 1 && l != 0 && l != charSentence.length-1)  || ((int)charSentence[l] >= 48 && (int)charSentence[l] <= 57) ||
							  ((int)charSentence[l] >= 65 && (int)charSentence[l] <= 90) ||
							  ((int)charSentence[l] >= 97 && (int)charSentence[l] <= 122)){
							  tempSentence.append(charSentence[l]);
							  if(!((int)charSentence[l] == 65 || (int)charSentence[l] == 97
								 || (int)charSentence[l] == 69 || (int)charSentence[l] == 101
								 || (int)charSentence[l] == 73 || (int)charSentence[l] == 105
								 || (int)charSentence[l] == 79 || (int)charSentence[l] == 111
								 || (int)charSentence[l] == 85 || (int)charSentence[l] == 117)){
								isCons = true;
							  }else{
					            isCons = false;
					          }
						   if(previousCons && isCons){
							  cons++;
					          if(cons > 4){
					            break;
					          }
							}else{
							  cons = 0;
					        }
					      }
					      previousCons = isCons;
					    }
//						tempBuffer.append(" ");
					    if(cons < 5){
					        tempBuffer.append(tempSentence);
					    	tempBuffer.append(" ");
					    	addedWord = true;
//					    	System.out.println("##"+tempBuffer.toString());
						}
					}
				}
				if(addedWord){
					tempBuffer.append("\n");	
				}

			}
			System.out.println(tempBuffer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
