package focusedCrawler.link.linkanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import focusedCrawler.link.BipartiteGraphRep;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;

public class SALSA {

	private HashMap<String,Vector<VSMElement>> incidenceHubMatrix;
	
	private HashMap<String,Vector<VSMElement>> incidenceAuthMatrix;
	
	private BipartiteGraphRep graphRep;
	
	private HashMap<String,VSMElement> initialValues;
	
	private HashMap<String,VSMElement> nodeRelevance;
	
	private boolean pageRank = false;
	
	private HashMap<String,VSMElement> hubValues = new HashMap<String, VSMElement>();
	
	private HashMap<String,VSMElement> authValues = new HashMap<String, VSMElement>();

	public SALSA(BipartiteGraphRep graphRep){
		this.graphRep = graphRep;
		this.incidenceHubMatrix = new HashMap<String, Vector<VSMElement>>();
		this.incidenceAuthMatrix = new HashMap<String, Vector<VSMElement>>();
		this.initialValues = new HashMap<String, VSMElement>();
		this.nodeRelevance = new HashMap<String, VSMElement>();
	}
	
	public VSMElement[] getHubValues() throws IOException{
		Iterator<VSMElement> elems = hubValues.values().iterator();
		VSMElement[] result = new VSMElement[hubValues.values().size()];
		int i = 0;
		while(elems.hasNext()){
			VSMElement elem = elems.next();
			String id = elem.getWord();
			String url = graphRep.getHubURL(id);
			elem.setWord(url);
			result[i] = elem;
			i++;
		}
		return result;
	}
	
	public VSMElement[] getAuthValues(){
		Iterator<VSMElement> elems = authValues.values().iterator();
		VSMElement[] result = new VSMElement[authValues.values().size()];
		int i = 0;
		while(elems.hasNext()){
			VSMElement elem = elems.next();
			String id = elem.getWord();
			String url = graphRep.getAuthURL(id);
			elem.setWord(url);
			result[i] = elem;
			i++;
		}
		return result;
	}

	public void setPageRank(boolean pageRank){
		this.pageRank = pageRank;
	}
	
	public void setNodeRelevance(HashMap<String,VSMElement> nr){
		this.nodeRelevance = nr;
	}
	
	public void execute() throws Exception{
		createInitialMatrices();
		hubValues = initialValues;
		authValues = initialValues;
		for (int i = 0; i < 10; i++) {
			System.out.println("Iteration:" + i);
			hubValues = updateVector(incidenceHubMatrix,hubValues,"hub");	
			authValues = updateVector(incidenceAuthMatrix,authValues,"auth");
		}
		Vector<VSMElement> sortedNodes = new Vector<VSMElement>(hubValues.values());
		Collections.sort(sortedNodes, new VSMElementComparator());
		for (int i = 0; i < sortedNodes.size() && i < 100; i++) {
			VSMElement elem = sortedNodes.elementAt(i);
			String hubURL = graphRep.getHubURL(elem.getWord()).toString();
			System.out.println(i + ":HUB:" + elem.getWord() + ":" + hubURL + "=" + elem.getWeight());
		}
		sortedNodes = new Vector<VSMElement>(authValues.values());
		Collections.sort(sortedNodes, new VSMElementComparator());
		for (int i = 0; i < sortedNodes.size() && i < 100; i++) {
			VSMElement elem = sortedNodes.elementAt(i);
			if(graphRep.getAuthURL(elem.getWord()) != null){
				String authURL = graphRep.getAuthURL(elem.getWord()).toString();
				System.out.println(i + ":AUTH:" + elem.getWord() + ":" + authURL + "=" + elem.getWeight());
			}
		}

	}
	
	private void createInitialMatrices() throws Exception{
		HashMap<String,Vector<VSMElement>> lr = new  HashMap<String,Vector<VSMElement>>();
		HashMap<String,Vector<VSMElement>> lcTranspose = new  HashMap<String,Vector<VSMElement>>();
//		Tuple[] authTuples = new Tuple[8];
//		Tuple[] hubTuples = new Tuple[3];
//		authTuples[0] = new Tuple("1","2###");
//		authTuples[1] = new Tuple("0","2###");
//		authTuples[2] = new Tuple("3","2###5###");
//		authTuples[3] = new Tuple("4","5###");
//		authTuples[4] = new Tuple("6","5###7###");
//		authTuples[5] = new Tuple("8","7###");
//		authTuples[6] = new Tuple("9","7###");
//		authTuples[7] = new Tuple("10","7###");
//		hubTuples[0] = new Tuple("2","0###1###3###");
//		hubTuples[1] = new Tuple("5","3###4###6###");
//		hubTuples[2] = new Tuple("7","6###8###9###10###");
		Tuple[] hubTuples = graphRep.getHubGraph();
		for (int i = 0; i < hubTuples.length; i++) {//prob for hubs
			String key = hubTuples[i].getKey();
			if(initialValues.get(key) == null){
				initialValues.put(key, new VSMElement(key, 1));	
			}
			Vector<VSMElement> row = lr.get(key);
			if(row == null){
				row = new Vector<VSMElement>();
			}
			String values = hubTuples[i].getValue();
			String[] outlinks = parseRecord(values);
			for (int j = 0; j < outlinks.length; j++) {
				if(initialValues.get(outlinks[j]) == null){
					initialValues.put(outlinks[j], new VSMElement(outlinks[j], 1));	
				}
				row.add(new VSMElement(outlinks[j], 1/(double)outlinks.length));				
			}
			lr.put(key,row);
		}
		System.out.println("LR:" + lr.size());
		Tuple[] authTuples = graphRep.getAuthGraph();
		for (int i = 0; i < authTuples.length; i++) {
			String key = authTuples[i].getKey();
			if(initialValues.get(key) == null){
				initialValues.put(key, new VSMElement(key, 1));	
			}
			Vector<VSMElement> row = lcTranspose.get(key);
			if(row == null){
				row = new Vector<VSMElement>();
			}
			String values = authTuples[i].getValue();
			String[] backlinks = parseRecord(values);
			for (int j = 0; j < backlinks.length; j++) {
				if(initialValues.get(backlinks[j]) == null){
					initialValues.put(backlinks[j], new VSMElement(backlinks[j], 1));	
				}
				row.add(new VSMElement(backlinks[j], 1/(double)backlinks.length));
			}
			lcTranspose.put(key,row);
		}
		System.out.println("LC:" + lcTranspose.size());
		incidenceHubMatrix = multiply(lr,lcTranspose);
		System.out.println("incidenceHubMatrix:" + incidenceHubMatrix.size());
		incidenceAuthMatrix = multiply(lcTranspose,lr);
		System.out.println(incidenceAuthMatrix.size());
	}

	
	private HashMap<String,Vector<VSMElement>> transpose(HashMap<String,Vector<VSMElement>> matrix){
		HashMap<String,Vector<VSMElement>> result = new HashMap<String, Vector<VSMElement>>();
		Iterator<String> keys = matrix.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Vector<VSMElement> elems = matrix.get(key);
			for (int i = 0; i < elems.size(); i++) {
				String newKey = elems.elementAt(i).getWord();
				Vector<VSMElement> newValues = result.get(newKey);
				if(newValues == null){
					newValues = new Vector<VSMElement>();
				}
				newValues.add(new VSMElement(key, elems.elementAt(i).getWeight()));
				result.put(newKey,newValues);
			}
		}
		return result;
	}
	
	private HashMap<String,Vector<VSMElement>> multiply(HashMap<String,Vector<VSMElement>> matrix1, HashMap<String,Vector<VSMElement>> matrix2){
		HashMap<String,Vector<VSMElement>> result = new HashMap<String, Vector<VSMElement>>();
		HashMap<String,Vector<VSMElement>> matrix2Trans = transpose(matrix2);
		Iterator<String> keys = matrix1.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Vector<VSMElement> elemsMatrix1 = matrix1.get(key);
			Iterator<String> keys2 = matrix2Trans.keySet().iterator();
			while(keys2.hasNext()){
				String key2 = keys2.next();
				Vector<VSMElement> elemsMatrix2 = matrix2Trans.get(key2);
				double sum = sumVectors(elemsMatrix1,elemsMatrix2);
				if(sum != 0){
					Vector<VSMElement> resultElems = result.get(key);
					if(resultElems == null){
						resultElems = new Vector<VSMElement>();
					}
					resultElems.add(new VSMElement(key2, sum));
					result.put(key, resultElems);
				}
			}
		}
		return result;
	}
	
	private double sumVectors(Vector<VSMElement> v1, Vector<VSMElement> v2){
		double result = 0;
		for (int i = 0; i < v1.size(); i++) {
			VSMElement elem1 = v1.elementAt(i);
			for (int j = 0; j < v2.size(); j++) {
				VSMElement elem2 = v2.elementAt(j);
				if(elem1.getWord().equals(elem2.getWord())){
					result = result + elem1.getWeight()*elem2.getWeight();
				}
			}
		}
		return result;
	}
	
	private HashMap<String,VSMElement> updateVector(HashMap<String,Vector<VSMElement>> incidenceMatrix, HashMap<String,VSMElement> values, String prefix){
		HashMap<String,VSMElement> newValues = new HashMap<String, VSMElement>();
		for (Iterator<String> iterator = incidenceMatrix.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Vector<VSMElement> neighbours = incidenceMatrix.get(key);
			for (int j = 0; j < neighbours.size(); j++) {
				VSMElement value = values.get(neighbours.elementAt(j).getWord());
				if(value != null){
					double newValue = (value.getWeight()*neighbours.elementAt(j).getWeight());
					VSMElement elem = newValues.get(neighbours.elementAt(j).getWord());
					if(elem != null){
						newValue = newValue + elem.getWeight();
					}
					newValues.put(neighbours.elementAt(j).getWord(), new VSMElement(neighbours.elementAt(j).getWord(),newValue));	
				}
			}
		}
		if(pageRank){
			Iterator<String> iter = newValues.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				double rel = 0;
				VSMElement nodeRel = nodeRelevance.get(key + "_" + prefix);
				if(nodeRel != null){
					rel = nodeRel.getWeight();
				}
				VSMElement elem = newValues.get(key);
				elem.setWeight(0.15 * elem.getWeight() + 0.85 * rel);
			}
		}
		normalize(newValues);
		return newValues;
	}
	
	private void normalize(HashMap<String,VSMElement> values){
		//normalize
		double total = 0;
		Iterator<String> iter = values.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			VSMElement elem = values.get(key);
			total = total + elem.getWeight();
		}		
		iter = values.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			VSMElement elem = values.get(key);
			elem.setWeight(elem.getWeight()/total);
		}		
	}
	
	private String[] parseRecord(String strLinks){
		if(strLinks != null){
			return strLinks.split("###");
		}else{
			return null;
		}
	}

	
	private double[][] transpose(double[][] matrix){
		double[][] result = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				result[j][i] = matrix[i][j];
			}
		}
		return result;
	}
	
	public double[][] multiply(double[][] matrixA, double[][] matrixB){
		double[][] result = new double[matrixA.length][matrixB.length];
		double[][] matrixC = transpose(matrixB);
		for (int i = 0; i < result.length; i++) {
			double[] matrixArow = matrixA[i];
			for (int j = 0; j < matrixArow.length; j++) {
				double[] matrixCrow = matrixC[j];
				for (int k = 0; k < matrixCrow.length; k++) {
					result[i][j] = result[i][j] + matrixArow[k]*matrixCrow[k];
				}
			}
		}
		return result;
	}
	
	public void pageRank(){
		double[] seeds = new double[12];
		seeds[1] = 0.33;
		seeds[3] = 0.33;
		seeds[4] = 0.33;
		double[] weights = new double[12];
		weights[0] = 0;
		weights[1] = 0;
		weights[2] = 1;
		weights[3] = 0;
		weights[4] = 0;
		weights[5] = 0;
		weights[6] = 0;
		weights[7] = 0;
		weights[8] = 0;
		weights[9] = 0;
		weights[10] = 0;
		weights[11] = 0;
		double[][] transMatrix = new double[12][12];
		transMatrix[0][2] = 1;
		transMatrix[1][2] = 1;
		transMatrix[2][0] = 1;
		transMatrix[2][1] = 1;
		transMatrix[2][3] = 1;
		transMatrix[3][2] = 1;
		transMatrix[3][5] = 1;
		transMatrix[4][5] = 1;
		transMatrix[5][3] = 1;
		transMatrix[5][4] = 1;
		transMatrix[5][6] = 1;
		transMatrix[6][5] = 1;
		transMatrix[6][7] = 1;
		transMatrix[7][6] = 1;
		transMatrix[7][8] = 1;
		transMatrix[7][9] = 1;
		transMatrix[7][10] = 1;
		transMatrix[7][11] = 1;
		transMatrix[8][7] = 1;
		transMatrix[9][7] = 1;
		transMatrix[10][7] = 1;
		transMatrix[11][7] = 1;
		double[][] lr = new double[12][12];
		for (int i = 0; i < lr.length; i++) {
			double[] row = transMatrix[i];
			double total = 0;
			for (int j = 0; j < row.length; j++) {
				total = total + row[j];
			}
			for (int j = 0; j < row.length; j++) {
				lr[i][j] = row[j]/total;
			}
		}
		double[][] lc = new double[12][12];
		for (int i = 0; i < lc.length; i++) {
			double total = 0;
			for (int j = 0; j < lc.length; j++) {
				total = total + transMatrix[j][i];
			}
			for (int j = 0; j < lc.length; j++) {
				lc[j][i] = transMatrix[j][i]/total;
			} 
		}
		double[][] hubMatrix = new double[12][12];
		hubMatrix = multiply(lr,transpose(lc));
		double[][] authMatrix = new double[12][12];
		authMatrix = multiply(transpose(lc),lr);
		for (int l = 0; l < 100; l++) {
			double total = 0;
			for (int i = 0; i < weights.length; i++) {
				total = total + weights[i];
			}
			for (int i = 0; i < weights.length; i++) {
				weights[i] = weights[i]/total;
			}
			double[] newWeights = new double[12];
			for (int i = 0; i < hubMatrix.length; i++) {
				double[] values = hubMatrix[i];
				double newValue = 0;
				for (int j = 0; j < values.length; j++) {
					newValue= newValue + weights[j]*values[j];
				}
//				newWeights[i] = 0.85*newValue + 0.15*seeds[i];
				newWeights[i] = newValue;
				System.out.print(i + ":" + newWeights[i] + " ");
			}
			System.out.println("\n");
			weights = newWeights;
		}
	}
	
	
	public static void main(String[] args) {
		ParameterFile config = new ParameterFile(args[0]);
		try {
			PersistentHashtable url2id = new PersistentHashtable(config.getParam("URL_ID_DIRECTORY"),100000);
			PersistentHashtable authID = new PersistentHashtable(config.getParam("AUTH_ID_DIRECTORY"),100000);
			PersistentHashtable authGraph = new PersistentHashtable(config.getParam("AUTH_GRAPH_DIRECTORY"),100000);
			PersistentHashtable hubID = new PersistentHashtable(config.getParam("HUB_ID_DIRECTORY"),100000);
			PersistentHashtable hubGraph = new PersistentHashtable(config.getParam("HUB_GRAPH_DIRECTORY"),100000);
			BipartiteGraphRep rep = new BipartiteGraphRep(authGraph,url2id,authID,hubID,hubGraph);
			SALSA salsa = new SALSA(rep);
			salsa.setPageRank(true);
			File file = new File(args[1]);
			HashMap<String,Double> authRelevance = new HashMap<String, Double>();
			HashMap<String,Double> hubRelevance = new HashMap<String, Double>();
			BufferedReader input = new BufferedReader(new FileReader(file));
			for (String line = input.readLine(); line != null; line = input.readLine()) {
				if(line.startsWith("------")){
					String host = line.replace("-", "");
					String url = "http://" + host + "/";
					String id = rep.getID(url);
					if(id == null){
						continue;
					}
					authRelevance.put(id,new Double(1));
					String[] backlinks = rep.getBacklinks(id);
					if(backlinks == null){
						continue;
					}
					for (int i = 0; i < backlinks.length; i++) {
						id = rep.getID(backlinks[i]);
						Double counter = hubRelevance.get(backlinks[i]);
						if(counter == null){
							counter = new Double(0);
						}
						hubRelevance.put(id, new Double(counter.doubleValue()+1));
					}
				}
			}
			
			HashMap<String,VSMElement> nodeRelevance = new HashMap<String, VSMElement>();
			Iterator<String> iter = authRelevance.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				nodeRelevance.put(key + "_auth", new VSMElement(key + "_auth", 1/(double)authRelevance.size()));
			}

			Iterator<Double> iter1 = hubRelevance.values().iterator();
			double total = 0;
			while(iter1.hasNext()){
				Double value = iter1.next();
				total = total + value.doubleValue();
			}
			 iter = hubRelevance.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				Double value = hubRelevance.get(key);
				nodeRelevance.put(key + "_hub", new VSMElement(key + "_hub", value.doubleValue()/total));
			}
			salsa.setNodeRelevance(nodeRelevance);
			salsa.execute();
//			HashSet<String> relSites = new HashSet<String>();
//			BufferedReader input = new BufferedReader(new FileReader(new File(args[0])));
//			for (String line = input.readLine(); line != null; line = input.readLine()) {
//				relSites.add(line.trim());
//			}
//			salsa.simplifiedSALSA();
//			salsa.seedSALSA(relSites);
//			salsa.originalSALSA();
//			salsa.pageRank();
//			SALSA salsa = new SALSA(null);
//			salsa.setPageRank(true);
//			salsa.execute();

			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}
	
}
