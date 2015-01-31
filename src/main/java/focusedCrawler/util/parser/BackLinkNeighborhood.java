package focusedCrawler.util.parser;

public class BackLinkNeighborhood {

	private String link;
	
	private String title;

	public BackLinkNeighborhood(String link, String title){
		this.link = link;
		this.title = title;
	}
	
	public BackLinkNeighborhood(){

	}
	
	public String getLink(){
		return this.link;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public void setLink(String link){
		this.link = link;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
}
