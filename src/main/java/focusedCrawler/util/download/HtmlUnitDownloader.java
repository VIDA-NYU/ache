package focusedCrawler.util.download;

import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitDownloader {

	
	 public static String[] googleBacklinks(String urlStr){
//		 System.out.println("INI:" + ini);
//		 System.out.println("END:" + end);
		 Vector<String> tempUrls = new Vector<String>();
		 try{
			 URL url = new URL(urlStr);
			 String host = url.getHost();
			 System.out.println("HOST:" + host);
			 int count = 0;
			 for (int start = 0; start < 50; start=start+10){
				 if(count < start){
					 break;
				 }
				 try {
					 Thread.sleep(3000);
				 } catch (InterruptedException e) {
							// TODO Auto-generated catch block
					 e.printStackTrace();
				 }  
				 final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_7);	           
				 webClient.setIncorrectnessListener(new IncorrectnessListenerEmpty());
				 webClient.setCssErrorHandler(new QuietCssErrorHandler());
				 CookieManager cm = webClient.getCookieManager();
				 cm.setCookiesEnabled(true);
				 webClient.setCookieManager(cm);
//				 final HtmlPage page = webClient.getPage("http://www.google.com/search?q=link%3Awww.unicef.org&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a&start="+start);
				 final HtmlPage page = webClient.getPage("https://www.google.com/search?hl=en&gl=us&tbm=nws&q=gun+control&oq=gun+control&gs_l=news-cc.3..43j0l9j43i53.21784.24502.0.24693.11.7.0.4.4.0.175.670.3j4.7.0...0.0...1ac.1.vGgyX86R96Q");
				 DomNodeList<HtmlElement> list = page.getElementsByTagName("a");
				 Iterator<HtmlElement> iter = list.iterator();
				 while(iter.hasNext()){
					 HtmlElement elem = iter.next();
					 String href = elem.toString();
//					 System.out.println(href);
					 if(!href.contains("google") && !href.contains("onclick") 
							 && href.contains("http") && !href.contains("search?q=link")
							 && !href.contains("/url?url=http")){
						 count++;
						 int iniIndex = href.indexOf("http");
						 int endIndex = href.indexOf("\" class");
						 String urlTemp = href.substring(iniIndex,endIndex);
						 urlTemp = urlTemp.replace("amp;", "");
						 URL temp = new URL(urlTemp);
						 if(!temp.getHost().equals(host)){
							 System.out.println(">>>" + urlTemp);
							 tempUrls.add(urlTemp);							 
						 }
					 }
				 }
			 }
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		 String[] result = new String[tempUrls.size()];
		 tempUrls.toArray(result);
		 return result;
	 }
	 
	 public static void main(String[] args) {
		try {
			String[] links = HtmlUnitDownloader.googleBacklinks("http://www.uefa.com/");
			for (int i = 0; i < links.length; i++) {
				System.out.println(links[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	
}
