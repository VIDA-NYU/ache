package achecrawler.util.vsm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import achecrawler.util.parser.PaginaURL;
import achecrawler.util.string.PorterStemmer;
import achecrawler.util.string.StopList;

public class VSMVector {

  private HashMap<String, VSMElement> elems;

  private PorterStemmer stemmer = new PorterStemmer();
  
  private StopList stoplist;
  
  private String id;
  
  public VSMVector(StopList stoplist) {
	    this.elems = new HashMap<>();
	    this.stoplist = stoplist;
  }

  public VSMVector(String document, StopList stoplist, boolean stem) throws   MalformedURLException {
	  if(!document.contains("<html>")){
		  document = "<html> " + document  + " </html>"; 
	  }
	  this.stoplist = stoplist;
	  this.elems = new HashMap<>();
	  PaginaURL page = new PaginaURL(new URL("http://www"),document, stoplist);
	  
      addTitle(page,stoplist);
	  if(stem){
		  stemPage(page,false);  
	  }else{
	        String[] words = page.words();
	        int[] frequencies = page.ocorrencias();
	        for (int i = 0; i < words.length; i++) {
	            if(frequencies[i] == 0){
	            	 continue;
	             }
	               VSMElement vsmElem = this.getElement(words[i]);
	               if(vsmElem == null){
	                   this.addElement(new VSMElement(words[i],1));
	               }else{
	                   double weight = vsmElem.getWeight();
	                   this.addElement(new VSMElement(words[i],1+weight));
	               }
	        }
	  }
  }
  
    private void addTitle(PaginaURL page, StopList stoplist) throws MalformedURLException {
        this.stoplist = stoplist;
        PaginaURL title = new PaginaURL(new URL("http://www"), page.title(), stoplist);
        String[] titleWords = title.words();
        String[] metaTerms = page.wordsMeta();
        int[] metaOccurrencies = page.occurrencesMeta();
        for (int i = 0; i < metaTerms.length; i++) {
            String word = metaTerms[i].toLowerCase();
            word = stemmer.stem(word);
            if (word.indexOf("No term") != -1) {
                continue;
            }
            if (word.length() > 2) {
                word = "meta" + word;
                VSMElement vsmElem = this.getElement(word);
                if (vsmElem == null) {
                    this.addElement(new VSMElement(word, metaOccurrencies[i]));
                } else {
                    double weight = vsmElem.getWeight();
                    this.addElement(new VSMElement(word, weight + 1));
                }
            }
        }

        for (int i = 0; i < titleWords.length; i++) {
            String word = titleWords[i].toLowerCase();

            word = stemmer.stem(word);
            if (word.indexOf("No term") != -1) {
                continue;
            }
            if (word.length() > 2) {
                word = "title" + word;
                VSMElement vsmElem = this.getElement(word);
                if (vsmElem == null) {
                    this.addElement(new VSMElement(word, 1));
                } else {
                    double weight = vsmElem.getWeight();
                    this.addElement(new VSMElement(word, weight + 1));
                }
            }
        }
    }

  public String getId(){
	  return this.id;
  }
  
  public void addElements(String []words) {
    for (int i = 0; i < words.length; i++) {
    	this.addElement(words[i]);
    }
  }

  public void addElement(String word) {
	  this.addElement(new VSMElement(word, 1));
  }


  public void addElement(VSMElement elem){
		  VSMElement vsmElem = this.getElement(elem.getWord());
		  if(vsmElem == null){
			  elems.put(elem.getWord(), elem);
		  }else{
			  double weight = vsmElem.getWeight();
			  elems.put(elem.getWord(),new VSMElement(elem.getWord(), elem.getWeight()+weight));
		  }
  }

  public VSMElement getElement(String word){
    return elems.get(word);
  }

  public Iterator<VSMElement> getElements(){
      return elems.values().iterator();
  }

  public VSMElement[] getArrayElements(){
      VSMElement[] elementsTemp = new VSMElement[elems.size()];
      Iterator<VSMElement> iterator = elems.values().iterator();
      int count = 0;
      while (iterator.hasNext()) {
          elementsTemp[count] = iterator.next();
          count++;
      }
    return elementsTemp;
  }

  public int size(){
    return elems.size();
  }

  public void addDFs(HashMap<String, Integer> idfs){
      Iterator<String> iter = elems.keySet().iterator();
      while(iter.hasNext()){
    	  VSMElement elem = elems.get(iter.next());
    	  if(elem != null){
        	  String term = elem.getWord();
        	  double freq = elem.getWeight();
        	  Integer df = idfs.get(term);
        	  if(df == null){
        		  elem.setWeight(0);
        	  }else{
        		  double weight = freq  / df.doubleValue();
        		  elem.setWeight(weight);
        	  }
    	  }
      }
	  
  }
  
  public double vectorSpaceSimilarityIDF(VSMVector vectorB, HashMap<String, Integer> idfs){

    VSMVector vectorA = this;
    double denominatorA = 0;
    double denominatorB = 0;
    VSMElement elem = null;

    Iterator<VSMElement> iterA = vectorA.getElements();
    while(iterA.hasNext()){
      elem = iterA.next();
      if((Integer)idfs.get(elem.getWord()) != null){
        int idf = idfs.get(elem.getWord()).intValue();
        double weight = elem.getWeight()*Math.log((double)idfs.size()/(double)idf);
        denominatorA = denominatorA + (weight*weight);
      }
    }

      Iterator<VSMElement> iterB = vectorB.getElements();
      while(iterB.hasNext()){
        elem = iterB.next();
        if((Integer)idfs.get(elem.getWord()) != null){
          int idf = ( (Integer) idfs.get(elem.getWord())).intValue();
          double weight = elem.getWeight() *
              Math.log( (double) idfs.size() / (double) idf);
          denominatorB = denominatorB + (weight * weight);
        }
      }

      double numerator = 0;
      iterA = vectorA.getElements();
      while(iterA.hasNext()){
        VSMElement elemA = iterA.next();
        VSMElement elemB = vectorB.getElement(elemA.getWord());
        if( elemB != null){

          if(idfs.get(elemA.getWord()) != null){
              int idf = ( (Integer) idfs.get(elemA.getWord())).intValue();
              double weightA = elemA.getWeight() * Math.log( (double) idfs.size() / (double) idf);
              double weightB = elemB.getWeight() * Math.log( (double) idfs.size() / (double) idf);
              numerator = numerator + weightA*weightB;
          }

        }
      }
      double weight = numerator/(Math.sqrt(denominatorA)*Math.sqrt(denominatorB));
      return weight;
    }

  public double jaccardSimilarity(VSMVector vectorB){

	  VSMVector vectorA = this;
      

      double numerator = 0;
      Iterator<VSMElement> iterA = vectorA.getElements();
      while(iterA.hasNext()){
    	  VSMElement elemA = iterA.next();
    	  VSMElement elemB = vectorB.getElement(elemA.getWord());
          if( elemB != null){ //overlap
        	  numerator = numerator + 1;
          }
      }
      double denominator = vectorA.size() + vectorB.size() - numerator;
      return numerator/denominator;
  }
  
  public double intersection(VSMVector vectorB){
	  VSMVector vectorA = this;
      double numerator = 0;
      Iterator<VSMElement> iterA = vectorA.getElements();
      while(iterA.hasNext()){
    	  VSMElement elemA = iterA.next();
    	  VSMElement elemB = vectorB.getElement(elemA.getWord());
          if( elemB != null){ //overlap
        	  numerator = numerator + 1;
          }
      }
      return numerator;
  }


    public VSMVector clone(){
    	VSMVector res = new VSMVector(stoplist);
        Iterator<VSMElement> iter = this.getElements();
        while(iter.hasNext()){
          VSMElement tempElem = iter.next();
          res.addElement(tempElem);
        }
    	return res;
    }
    
    public double vectorSpaceSimilarity(VSMVector vectorB){

      VSMVector vectorA = this;
      double denominatorA = 0;
      double denominatorB = 0;
      VSMElement elem = null;

      Iterator<VSMElement> iterA = vectorA.getElements();
      while(iterA.hasNext()){
          elem = iterA.next();
          double weight = elem.getWeight();
          denominatorA = denominatorA + (weight*weight);
      }
      if(denominatorA == 0){
          return 0;
      }
        Iterator<VSMElement> iterB = vectorB.getElements();
        while(iterB.hasNext()){
          elem = iterB.next();
          double weight = elem.getWeight();
          denominatorB = denominatorB + (weight * weight);
        }
        if(denominatorB == 0){
            return 0;
        }
        double numerator = 0;
        iterA = vectorA.getElements();
        while(iterA.hasNext()){
          VSMElement elemA = iterA.next();
          VSMElement elemB = vectorB.getElement(elemA.getWord());
          if( elemB != null){
              double weightA = elemA.getWeight();
              double weightB = elemB.getWeight();
              numerator = numerator + weightA*weightB;
          }
        }
        
        double den = (Math.sqrt(denominatorA)*Math.sqrt(denominatorB));
        double weight = numerator/den;
        return weight;
      }

    public void addVector(VSMVector pageVector){

      VSMVector centroidVector = this;

      Iterator<VSMElement> iter = pageVector.getElements();

      while(iter.hasNext()){
        VSMElement tempElem = iter.next();
        centroidVector.addElement(tempElem);
      }
    }

    public void multiplyWeights(double factor){
    	Iterator<String>  iter = elems.keySet().iterator();
    	while(iter.hasNext()){
    		String word = (String)iter.next();
    		VSMElement elem = elems.get(word);
    		elems.put(word,new VSMElement(word,elem.getWeight()*factor));
    	}
    }

    
    public void negativeVector(){
    	Iterator<String>  iter = elems.keySet().iterator();
    	while(iter.hasNext()){
    		String word = (String)iter.next();
    		VSMElement elem = elems.get(word);
    		elems.put(word,new VSMElement(word,-elem.getWeight()));
    	}
    }

    public static HashMap<String, Integer> calculateIDFs(VSMVector[] vectors) {

      HashMap<String, Integer> idfs = new HashMap<>();

      for (int i = 0; i < vectors.length; i++) {

        VSMVector pageVector = vectors[i];
        Iterator<VSMElement> iter = pageVector.getElements();

        while(iter.hasNext()){

          String word = iter.next().getWord();

          Integer ocur = (Integer)idfs.get(word);
          if( ocur == null){
            idfs.put(word,new Integer(1));
          }else{
            idfs.put(word,new Integer(ocur.intValue()+1));
          }
        }
      }

      return idfs;
    }

    public HashMap<String, Integer> calculateWordOccurence(VSMVector[] vectors) {

        HashMap<String, Integer> idfs = new HashMap<>();

      for (int i = 0; i < vectors.length; i++) {

        VSMVector pageVector = vectors[i];
        Iterator<VSMElement> iter = pageVector.getElements();

        while(iter.hasNext()){
            VSMElement elem = iter.next();
            String word = elem.getWord();
            Integer ocur = (Integer)idfs.get(word);
            if( ocur == null){
                idfs.put(word,new Integer((int)elem.getWeight()));
            }else{
                idfs.put(word,new Integer(ocur.intValue()+(int)elem.getWeight()));
            }
        }
      }

      return idfs;
    }

    private void stemPage(PaginaURL page, boolean isForm){
    	
       String[] words = page.words();
       int[] frequencies = page.ocorrencias();
       for (int i = 0; i < words.length; i++) {
         String word = null;

         try{

        	 frequencies[i] = 1;
        	 word = stemWord(words[i]);
        	 if(word == null){
        		 continue;
        	 }
         }catch(Exception e){
        	 continue;
         }
         if(frequencies[i] == 0){
        	 continue;
         }
         if(word.length() > 2 ){
        	 VSMElement vsmElem = this.getElement(word);
             if(vsmElem == null){
            	 this.addElement(new VSMElement(word,frequencies[i]));
             }else{
            	 double weight = vsmElem.getWeight();
            	 this.addElement(new VSMElement(word,frequencies[i]+weight));
             }
         }
     }
  }

    private String stemWord(String word){
        if(word.indexOf("font-") != -1 || word.indexOf("padding") != -1
                || word.indexOf("border") != -1 || word.indexOf("margin") != -1
                || word.indexOf("background") != -1 || word.indexOf("color") != -1
                || word.indexOf("width") != -1 || word.indexOf("field") != -1
                || word.indexOf("verdana") != -1 || word.indexOf("helvetica") != -1
                || word.indexOf("sans") != -1 || word.indexOf("arial") != -1){ //parser bug
        	return null;
        }
        word = stemmer.stem(word);
        if(word.indexOf("No term") != -1){
        	return null;
        }
        return word;
    }

    public void normalizebyMax(){
    	VSMElement[] topElems = topElements(1);
    	double max = topElems[0].getWeight();
    	double total = 0;
    	for(VSMElement elem : elems.values()) {
    		total = total + elem.getWeight();
    	}
    	if(total != 0){
    	    for(Map.Entry<String, VSMElement> entry : elems.entrySet()) {
    	        VSMElement elem = entry.getValue();
    	        elems.put(entry.getKey(),new VSMElement(entry.getKey(),elem.getWeight()/max));
    	    }
    	}
    }

    public void normalize(){
    	double total = 0;
    	for(VSMElement elem : elems.values()) {
    		total = total + elem.getWeight();
    	}
    	if(total != 0){
    	    for(Map.Entry<String, VSMElement> entry : elems.entrySet()) {
    			VSMElement elem = entry.getValue();
    			elems.put(entry.getKey(),new VSMElement(entry.getKey(),elem.getWeight()/total));
    		}
    	}
    }
  
  
  public void squaredNormalization(){
	  double total = 0;
	  for(VSMElement elem : elems.values()) {
		  total = total +  Math.sqrt(elem.getWeight());
	  }
	  if(total != 0){
	      for(Map.Entry<String, VSMElement> entry : elems.entrySet()) {
			  VSMElement elem = entry.getValue();
			  elems.put(entry.getKey(),new VSMElement(entry.getKey(),Math.sqrt(elem.getWeight())/total));
		  }
	  }
  }

  public void normalizeOverElements() {
      double total = elems.size();
      for(Map.Entry<String, VSMElement> entry : elems.entrySet()) {
          VSMElement elem = entry.getValue();
          elems.put(entry.getKey(), new VSMElement(entry.getKey(), elem.getWeight() / total));
      }
  }

  public VSMElement[] topElements(int n){
	  Vector<VSMElement> temp = new Vector<>();
	  for(VSMElement elem : elems.values()) {
          temp.add(elem);
      }
      Collections.sort(temp, VSMElement.DESC_ORDER_COMPARATOR);
      VSMElement[] res = new VSMElement[n];
      for (int i = 0; i < temp.size() && i < n; i++) {
          res[i] = temp.elementAt(i);
      }
	  return res;
  }
  
  public String toString(){
      StringBuffer buf = new StringBuffer();
      Vector<VSMElement> temp = new Vector<>();
      Iterator<VSMElement> iter = elems.values().iterator();
      while(iter.hasNext()){
          VSMElement elem = iter.next();
          temp.add(elem);
      }
      Collections.sort(temp, VSMElement.DESC_ORDER_COMPARATOR);
      buf.append("[");
      for (int i = 0; i < temp.size(); i++) {
          VSMElement elem = temp.elementAt(i);
          buf.append(elem.toString());
          buf.append(",");
      }
      buf.append("]");
      return buf.toString();
  }

  public void remove(String word){
	  elems.remove(word);
  }
  
}
