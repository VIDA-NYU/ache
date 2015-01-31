package focusedCrawler.util.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import focusedCrawler.util.Page;


public class PageDownloader {


	public static String downloadPage(String url) throws IOException {
		return PageDownloader.downloadPage(new URL(url));
	}
	
	 public static String downloadPage(URL urlCon) throws IOException {
	     URLConnection conn = urlCon.openConnection();
	     conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
	     conn.setConnectTimeout(100000);
	     conn.setReadTimeout(100000);
	     StringBuffer buffer = new StringBuffer();
		 try{
			 BufferedReader inCon = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			 String inputLine;
			 while ((inputLine = inCon.readLine()) != null) {
				 buffer.append(inputLine + " \n");
//		      System.out.println(inputLine);
			 }
			 inCon.close();
		 }catch(java.lang.IllegalArgumentException ex){
//			 System.out.println("ILLEGAL ARGUMENT!!!\n");
			 return null;
		 }
		 catch(java.net.SocketTimeoutException ex){
//			 System.out.println("TIMEOUT EXCEPTION!!!\n");
			 return null;
		 }
		 catch(java.io.FileNotFoundException ex) {
//			 System.out.println("REMOTE FILE NOT FOUND!!!\n");
			 return null;
		 }
		 catch(java.net.UnknownHostException ex) {
//			 System.out.println("UNKNOWN HOST!!!\n");
			 return null;
		 }catch(Exception ex){
//			 ex.printStackTrace();
//			 System.out.println("Generic Exception\n");
			 return null;
		 }
//		 Page pageRes = new Page(urlCon, buffer.toString());
		    //System.out.println("FINISHED TO DOWNLOAD THE PAGE : " + urlCon.toString() + "\n");
		 return buffer.toString();
//		 return pageRes;
	 }

 
	 public static void main(String[] args) {

		try {
			String sitePage = PageDownloader.downloadPage("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/obterTodasPosicoes");	
			System.out.println(sitePage);
			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
	
	
}
