package focusedCrawler.link;

import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;

public class GraphStorage extends StorageDefault{
	
	private BipartiteGraphRep graphRep;
	
	
	@Override
	public synchronized Enumeration selectEnumeration(Object obj) throws StorageException  {

		try {
			Tuple[] tuples = graphRep.getAuthGraph();
			for (int i = 0; i < tuples.length; i++) {
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
	    	throw new StorageException(ex.getMessage());
		}
		
		return null;

	}
	

}
