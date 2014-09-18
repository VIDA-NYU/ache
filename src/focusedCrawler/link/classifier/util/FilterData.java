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
package focusedCrawler.link.classifier.util;


import java.util.Collections;
import java.util.Vector;
import java.util.HashSet;


public class FilterData {

	private int maxElements;

	private int maxWordSize;


	public FilterData(int maxElements, int maxWordSize) {
		this.maxElements = maxElements;
		this.maxWordSize = maxWordSize;
	}

	public Vector filter(Vector sortList, Vector aroundWords){

		boolean stem = aroundWords != null;
		Vector filteredWords = new Vector();
		int minFreq = 5;
		int count = 0;
		int i = 0;
		int maxFreq = ((WordFrequency)sortList.firstElement()).getFrequency();
		if(maxFreq <= 10){
			minFreq = 1;
		}
		while (i < sortList.size() && (count < maxElements || stem)) {
			WordFrequency wordFrequency = (WordFrequency)sortList.elementAt(i);
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
			Vector stemmedWords =  stemming(filteredWords, aroundWords, sortList);
			Vector result = new Vector();
			for (int j = 0; j < maxElements && j < stemmedWords.size(); j++) {
				result.add(stemmedWords.elementAt(j));
			}
			return result;
		}else{
			return filteredWords;
		}
	}
	
	public Vector filter(Vector sortList){
		Vector filteredWords = new Vector();
		int count = 0;
		int i = 0;
		int irrelevantWords = 0;
		while (i < sortList.size() && count < maxElements) {
			WordFrequency wordFrequency = (WordFrequency)sortList.elementAt(i);
			String word = wordFrequency.getWord();
			int frequency = wordFrequency.getFrequency();
			if(word.length() > maxWordSize){
				filteredWords.add(wordFrequency);
				count++;
			}else
				irrelevantWords++;
			i++;
		}
		return stemming(filteredWords, null, sortList);
	}

	private Vector stemming(Vector wordFreqList, Vector aroundWords, Vector initialList){
		Vector finalWords = new Vector();
		HashSet usedWords = new HashSet();
		if(aroundWords != null){
			for (int i = 0; i < aroundWords.size(); i++) {
				boolean exist = false;
				WordFrequency firstWordFreq = (WordFrequency)aroundWords.elementAt(i);
				String word = firstWordFreq.getWord();
				firstWordFreq = new WordFrequency(word,0);
				for (int j = 0; j < wordFreqList.size(); j++) {
					WordFrequency wordFreqTemp = (WordFrequency) wordFreqList.elementAt(j);
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
		Collections.sort(wordFreqList, new WordSizeComparator());
		for (int i = 0; i < wordFreqList.size(); i++) {
			WordFrequency firstWordFreq = (WordFrequency)wordFreqList.elementAt(i);
			String word = firstWordFreq.getWord();
			if(word != null &&
					(usedWords.contains(word) ||
							(word.equals("net") || word.equals("http") || word.equals("www") || word.equals("cfm") || word.equals("cgi") ||
									word.equals("asp") || word.equals("php") || word.equals("jsp")))){//test
				continue;
			}
			if(word != null){
				for (int j = 0; j < initialList.size(); j++) {
					WordFrequency wordFreqTemp = (WordFrequency) initialList.elementAt(j);
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
		Collections.sort(finalWords, new WordFrequencyComparator());
		return finalWords;
	}
}

