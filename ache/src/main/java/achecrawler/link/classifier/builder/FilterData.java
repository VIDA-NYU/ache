/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
package achecrawler.link.classifier.builder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class FilterData {

	private int maxElements;
	private int maxWordSize;

	public FilterData(int maxElements, int maxWordSize) {
		this.maxElements = maxElements;
		this.maxWordSize = maxWordSize;
	}

	public List<WordFrequency> filter(List<WordFrequency> sortList, List<WordFrequency> aroundWords){

		boolean stem = aroundWords != null;
		List<WordFrequency> filteredWords = new ArrayList<>();
		int minFreq = 5;
		int count = 0;
		int i = 0;
		int maxFreq = sortList.get(0).getFrequency();
		if(maxFreq <= 10){
			minFreq = 1;
		}
		while (i < sortList.size() && (count < maxElements || stem)) {
			WordFrequency wordFrequency = sortList.get(i);
			String word = wordFrequency.getWord();
			int frequency = wordFrequency.getFrequency();
			if(word.length() > maxWordSize){
				if(frequency > minFreq){
					filteredWords.add(wordFrequency);
					count++;
				}
			}
			i++;
		}
		if(stem){
		    List<WordFrequency> stemmedWords = stemming(filteredWords, aroundWords, sortList);
		    List<WordFrequency> result = new ArrayList<>();
			for (int j = 0; j < maxElements && j < stemmedWords.size(); j++) {
				result.add(stemmedWords.get(j));
			}
			return result;
		}else{
			return filteredWords;
		}
	}
	
	public List<WordFrequency> filter(List<WordFrequency> sortList){
	    List<WordFrequency> filteredWords = new ArrayList<>();
		int count = 0;
		int i = 0;
		
		while (i < sortList.size() && count < maxElements) {
			WordFrequency wordFrequency = sortList.get(i);
			String word = wordFrequency.getWord();
			
			if(word.length() > maxWordSize){
				filteredWords.add(wordFrequency);
				count++;
			}
			i++;
		}
		return stemming(filteredWords, null, sortList);
	}

	private List<WordFrequency> stemming(List<WordFrequency> wordFreqList, List<WordFrequency> aroundWords, List<WordFrequency> initialList){
	    List<WordFrequency> finalWords = new ArrayList<>();
		HashSet<String> usedWords = new HashSet<>();
		if(aroundWords != null){
			for (int i = 0; i < aroundWords.size(); i++) {
				boolean exist = false;
				WordFrequency firstWordFreq = aroundWords.get(i);
				String word = firstWordFreq.getWord();
				firstWordFreq = new WordFrequency(word,0);
				for (int j = 0; j < wordFreqList.size(); j++) {
					WordFrequency wordFreqTemp = wordFreqList.get(j);
					if(word.equals(wordFreqTemp.getWord())){
						exist = true;
						break;
					}
					if (!usedWords.contains(word) && wordFreqTemp.getWord() != null &&
							wordFreqTemp.getWord().indexOf(word) != -1) {
						usedWords.add(wordFreqTemp.getWord());
						firstWordFreq.incrementFrequncy(wordFreqTemp.getFrequency());
					}
				}
				if(!exist){
					wordFreqList.add(firstWordFreq);
				}
			}
		}
		Collections.sort(wordFreqList, WordFrequency.WORD_SIZE_ASC_COMPARATOR);
		for (int i = 0; i < wordFreqList.size(); i++) {
			WordFrequency firstWordFreq = wordFreqList.get(i);
			String word = firstWordFreq.getWord();
            if (word != null &&
                    (usedWords.contains(word) ||
                            (word.equals("net") ||
                                    word.equals("http") ||
                                    word.equals("www") ||
                                    word.equals("cfm") ||
                                    word.equals("cgi") ||
                                    word.equals("asp") ||
                                    word.equals("php") ||
                                    word.equals("jsp"))
			        ) ) {
				continue;
			}
			if(word != null){
				for (int j = 0; j < initialList.size(); j++) {
					WordFrequency wordFreqTemp = initialList.get(j);
					if (wordFreqTemp.getWord() != null &&
							wordFreqTemp.getWord().indexOf(word) != -1) {
						usedWords.add(wordFreqTemp.getWord());
						firstWordFreq.incrementFrequncy(wordFreqTemp.getFrequency());
					}
				}
				if(firstWordFreq.getFrequency() > 0){
					finalWords.add(firstWordFreq);
				}
			}
		}
		Collections.sort(finalWords, WordFrequency.WORD_FREQUENCY_DESC_COMPARATOR);
		return finalWords;
	}

}
